package com.neotys.dynatrace.common;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.neotys.dynatrace.common.HTTPGenerator.*;

/**
 * Created by anouvel on 18/12/2017.
 */
class HTTPGeneratorUtils {

	private HTTPGeneratorUtils() {

	}

	static void setRequestUrl(final HttpRequestBase request, final String url, final Map<String, String> params)
			throws URISyntaxException, MalformedURLException {
		final String urlWithParameters = addGetParametersToUrl(url, params);
		request.setURI(new URL(urlWithParameters).toURI());
	}

	static DefaultHttpClient newHttpClient(final boolean isHttps) throws Exception {
		if (isHttps) {
			return newHttpsClient();
		} else {
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getConnectionManager();
			return httpClient;
		}
	}

	@SuppressWarnings("deprecation")
	private static DefaultHttpClient newHttpsClient() throws Exception {
		final DefaultHttpClient client = new DefaultHttpClient();
		final HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		final TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] cert, String authType) throws CertificateException {
				return true;
			}
		};

		final SchemeRegistry registry = new SchemeRegistry();
		final SSLSocketFactory socketFactory;
		socketFactory = new SSLSocketFactory(acceptingTrustStrategy, (X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		final SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		// Set verifier
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return new DefaultHttpClient(mgr, client.getParams());
	}

	static void addJsonParameters(final HttpRequestBase request, final StringEntity jsonContent, final String httpMethod) {
		switch (httpMethod) {
			case HTTP_POST_METHOD:
				((HttpPost) request).setEntity(jsonContent);
				break;
			case HTTP_PUT_METHOD:
				((HttpPut) request).setEntity(jsonContent);
				break;
			default:
				throw new UnsupportedOperationException("Invalid http method");
		}
	}

	@SuppressWarnings("deprecation")
	static HttpParams generateParams(final Map<String, String> params) {
		if (params != null) {
			HttpParams result = new BasicHttpParams();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.setParameter(entry.getKey(), entry.getValue());
			}
			return result;
		} else return null;
	}

	static void addHeaders(final HttpRequestBase request, final Map<String, String> headersMap) {
		if (headersMap != null) {
			for (final Map.Entry<String, String> entry : headersMap.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}

	static HttpRequestBase generateHttpRequest(final String method, final String url) {
		switch (method) {
			case HTTP_GET_METHOD:
				return new HttpGet(url);
			case HTTP_POST_METHOD:
				return new HttpPost(url);
			case HTTP_PUT_METHOD:
				return new HttpPut(url);
			case HTTP_OPTION_METHOD:
			default:
				throw new IllegalStateException("Unsupported method");
		}
	}

	private static String addGetParametersToUrl(final String url, final Map<String, String> params) {
		final StringBuilder urlBuilder = new StringBuilder(url);
		if (!url.endsWith("?")) {
			urlBuilder.append("?");
		}
		final List<NameValuePair> parameters = new LinkedList<>();
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		final String paramString = URLEncodedUtils.format(parameters, HTTP.UTF_8);
		urlBuilder.append(paramString);
		return urlBuilder.toString();
	}

	private static String convertStreamToString(final InputStream is) throws IOException {
		final StringBuilder sb = new StringBuilder();
		try (final InputStreamReader in = new InputStreamReader(is); final BufferedReader reader = new BufferedReader(in)) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		}
		return sb.toString();
	}

	static boolean isJsonContent(final HttpResponse resp) {
		final Header contentTypeHeader = resp.getFirstHeader("Content-Type");
		return contentTypeHeader.getValue().contains("application/json");
	}

	static String getStringResponse(final HttpResponse resp) throws IOException {
		final HttpEntity entity = resp.getEntity();
		if (entity != null) {
			// A Simple JSON Response Read
			try (final InputStream inputStream = entity.getContent()) {
				final String result = convertStreamToString(inputStream);
				if (resp.getStatusLine().getStatusCode() != 200) {
					return null;
				} else {
					return result;
				}
			}
		}
		return null;
	}
}
