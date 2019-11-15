package com.neotys.dynatrace.common;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import static com.neotys.dynatrace.common.HTTPGeneratorUtils.*;

// TODO : check HttpClient deprecation warnings
public class HTTPGenerator {
	public static final String HTTP_GET_METHOD = "GET";
	public static final String HTTP_POST_METHOD = "POST";
	public static final String HTTP_OPTION_METHOD = "OPTION";
	public static final String HTTP_PUT_METHOD = "PUT";
	public static final String HTTP_DELETE_METHOD = "DELETE";

	private final DefaultHttpClient httpClient;

	private final HttpRequestBase request;

	public HTTPGenerator(final String httpMethod,
						 final String url,
						 final Map<String, String> headers,
						 final MultivaluedMap<String, String> params,
						 final Optional<Proxy> proxy)
			throws Exception {
		this.request = generateHttpRequest(httpMethod, url);
		final boolean isHttps = url.contains("https");
		this.httpClient = newHttpClient(isHttps);

		if (proxy.isPresent()) {
			initProxy(proxy.get());
		}
		addHeaders(request, headers);
		if (params != null && !params.isEmpty()) {
			setRequestUrl(request, url, params);
		}
	}
	public static HTTPGenerator deleteHttpGenerator(final String url,
													final Map<String, String> headers,
													final MultivaluedMap<String, String> params,
													final Optional<Proxy> proxy) throws Exception {
		final HTTPGenerator httpGenerator = new HTTPGenerator(HTTP_DELETE_METHOD, url, headers, params, proxy);
		return httpGenerator;

	}
		
	public HTTPGenerator(final String httpMethod, 
						 final String url, 
						 final Map<String, String> headers,
						 final MultivaluedMap<String, String> params, 
						 final Optional<Proxy> proxy, 
						 final String bodyJson)
			throws Exception {
		this(httpMethod, url, headers, params, proxy);
		final StringEntity requestEntity = new StringEntity(bodyJson, "application/json", "utf8");
		addJsonParameters(request, requestEntity, httpMethod);
	}
	
	private void initProxy(final Proxy proxy) {
		final HttpHost proxyHttpHost = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);
		if (!Strings.isNullOrEmpty(proxy.getLogin())) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxy.getHost(), proxy.getPort()),
					new UsernamePasswordCredentials(proxy.getLogin(), proxy.getPassword()));
		}
	}

	public void closeHttpClient() {
		httpClient.getConnectionManager().shutdown();
	}

	public HttpResponse execute() throws IOException {
		return httpClient.execute(request);
	}

	public HttpRequestBase getRequest() {
		return request;
	}
}
