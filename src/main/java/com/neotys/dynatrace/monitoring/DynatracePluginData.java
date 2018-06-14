package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.swagger.client.ApiClient;
import io.swagger.client.api.ResultsApi;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Timer;

public class DynatracePluginData {
    private static DynatracePluginData instance;
    private static final String NLWEB_VERSION = "v1";

    private static final int TIMER_FREQUENCY = 10000;
    private static final int TIMER_DELAY = 0;
    private final String virtualUserId;

    private Context neoLoadContext;
    private ApiClient neoLoadWebApiClient;
    private ResultsApi nlWebResult;

    private NeoLoadStatAggregator neoLoadAggregator = null;

    private Timer timerDynatrace = null;
    private final String dataExchangeApiUrl;

    private String dynataceApiKey;

    private String dynatraceAccountId = null;
    private Optional<String> dynatraceManagedHostname = null;
    private final Optional<String> proxyName;
    private DynatracePluginData(final String dynataceApiKey,
                                final Optional<String> proxyName, final Context context, final String dynatraceId,
                                final String dataExchangeApiUrl, final Optional<String> dynatraceManagedHostname, final boolean traceMode) throws Exception {

        this.dynataceApiKey = dynataceApiKey;
        dynatraceAccountId = dynatraceId;
        virtualUserId = context.getCurrentVirtualUser().getId();

        //----define  the NLWEB API-----
        neoLoadWebApiClient = new ApiClient();
        neoLoadWebApiClient.setApiKey(context.getAccountToken());
        final String basePath = getBasePath(context);
        neoLoadWebApiClient.setBasePath(basePath);
        final Optional<Proxy> proxyOptional = DynatraceUtils.getProxy(context, proxyName, basePath);
        if(proxyOptional.isPresent()) {
            initProxyForNeoloadWebApiClient(proxyOptional.get());
        }
        this.dynatraceManagedHostname = dynatraceManagedHostname;
        initNeoLoadApi();
        //-------------------------
        neoLoadContext = context;
        this.proxyName = proxyName;
        this.dataExchangeApiUrl = dataExchangeApiUrl;
        neoLoadAggregator = new NeoLoadStatAggregator(dynataceApiKey, dynatraceAccountId, nlWebResult,
                context, dataExchangeApiUrl, dynatraceManagedHostname, proxyName, traceMode);

//        startTimer();
    }

    public synchronized static DynatracePluginData getInstance(final Context context,
                                                               final String dynatraceId,
                                                               final String dynatraceApiKey,
                                                               final Optional<String> dynatraceManagedHostname,
                                                               final String dataExchangeApiUrl,
                                                               final Optional<String> proxyName,
                                                               final boolean traceMode) throws Exception {
        if (instance == null) {
            instance = new DynatracePluginData(
                    dynatraceApiKey,
                    proxyName,
                    context,
                    dynatraceId,
                    dataExchangeApiUrl,
                    dynatraceManagedHostname,
                    traceMode);
        }else{
            instance.neoLoadAggregator.setContext(context);
        }
        return instance;
    }

    public static DynatracePluginData getInstance() {
        return instance;
    }

    private void initProxyForNeoloadWebApiClient(final Proxy proxy) throws KeyManagementException, NoSuchAlgorithmException {
        neoLoadWebApiClient.getHttpClient().setProxy(toOkHttpProxy(proxy));
		if (!Strings.isNullOrEmpty(proxy.getLogin())) {
			Authenticator proxyAuthenticator = new Authenticator() {
				@Override
				public Request authenticate(java.net.Proxy p, Response response) throws IOException {
					final String credential = Credentials.basic(proxy.getLogin(), proxy.getPassword());
					return response.request().newBuilder()
							.header("Proxy-Authorization", credential)
							.build();
				}

				@Override
				public Request authenticateProxy(java.net.Proxy p, Response response) throws IOException {
					final String credential = Credentials.basic(proxy.getLogin(), proxy.getPassword());
					return response.request().newBuilder()
							.header("Proxy-Authorization", credential)
							.build();
				}
			};
			neoLoadWebApiClient.getHttpClient().setAuthenticator(proxyAuthenticator);
		}
		// Create a trust manager that does not validate certificate chains
		final TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[0];
					}
				}
		};

		// Install the all-trusting trust manager
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, null);
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

		neoLoadWebApiClient.getHttpClient().setSslSocketFactory(sslSocketFactory);
		neoLoadWebApiClient.getHttpClient().setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

    private static java.net.Proxy toOkHttpProxy(final Proxy proxy) {
        return new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
    }

    private String getBasePath(final Context context) {
        final String webPlatformApiUrl = context.getWebPlatformApiUrl();
        final StringBuilder basePathBuilder = new StringBuilder(webPlatformApiUrl);
        if(!webPlatformApiUrl.endsWith("/")) {
            basePathBuilder.append("/");
        }
        basePathBuilder.append(NLWEB_VERSION + "/");
        return basePathBuilder.toString();
    }

    public String getVirtualUserId() {
        return virtualUserId;
    }

    private void initNeoLoadApi() {
        nlWebResult = new ResultsApi(neoLoadWebApiClient);
    }

    public NeoLoadStatAggregator getNeoLoadAggregator() {
        return neoLoadAggregator;
    }
}
