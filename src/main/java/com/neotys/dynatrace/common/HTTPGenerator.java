package com.neotys.dynatrace.common;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import static com.neotys.dynatrace.common.HTTPGeneratorUtils.*;

public class HTTPGenerator {
	public final static String HTTP_GET_METHOD = "GET";
	public final static String HTTP_POST_METHOD = "POST";
	public final static String HTTP_OPTION_METHOD = "OPTION";
	public final static String HTTP_PUT_METHOD = "PUT";

	private final DefaultHttpClient httpClient;
	private final HttpRequestBase request;

	public HTTPGenerator(final String httpMethod,
						 final String url,
						 final Map<String, String> headers,
						 final Map<String, String> params,
						 final Optional<Proxy> proxy)
			throws MalformedURLException, URISyntaxException {
		this.request = generateHttpRequest(httpMethod, url);
		final boolean isHttps = url.contains("https");
		this.httpClient = newHttpClient(isHttps);

		if (proxy.isPresent()) {
			initProxy(proxy.get(), url);
		}
		addHeaders(request, headers);
		if (params != null && !params.isEmpty()) {
			if (!Objects.equals(httpMethod, HTTP_GET_METHOD))
				request.setParams(generateParams(params));
			else {
				setRequestUrl(request, url, params);
			}
		}
	}

	public static HTTPGenerator newJsonHttpGenerator(final String httpMethod,
													 final String url,
													 final Map<String, String> headers,
													 final Map<String, String> params,
													 final Optional<Proxy> proxy,
													 final String jsonString)
			throws MalformedURLException, URISyntaxException {
		final HTTPGenerator httpGenerator = new HTTPGenerator(httpMethod, url, headers, params, proxy);
		final StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
		addJsonParameters(httpGenerator.request, requestEntity, httpMethod);
		return httpGenerator;
	}

	private void initProxy(final Proxy proxy, final String url) {
		final HttpHost proxyHttpHost;
		if (url.startsWith("https")) {
			proxyHttpHost = new HttpHost(proxy.getHost(), proxy.getPort(), "https");
		} else {
			proxyHttpHost = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
		}
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);
		if (proxy.getLogin() != null) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxy.getHost(), proxy.getPort()),
					new UsernamePasswordCredentials(proxy.getLogin(), proxy.getPassword()));
		}
	}

	public void closeHttpClient() {
		httpClient.getConnectionManager().shutdown();
	}

	public void setAllowHostnameSSL() throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, null, null);
		final SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		final Scheme sch = new Scheme("https", 443, sf);
		httpClient.getConnectionManager().getSchemeRegistry().register(sch);
	}

	public JSONArray executeAndGetJsonArrayResponse() throws IOException {
		final HttpResponse response = httpClient.execute(request);
		if (isJsonContent(response)) {
			final String stringResponse = getStringResponse(response);
			if (stringResponse != null) {
				return new JSONArray(stringResponse);
			}
		}
		return null;
	}

	public JSONObject executeAnGetJsonResponse() throws IOException {
		final HttpResponse response = httpClient.execute(request);
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200 && isJsonContent(response)) {
			final String stringResponse = getStringResponse(response);
			if (stringResponse != null) {
				return new JSONObject(stringResponse);
			}
		}
		return null;
	}

	public int executeAndGetResponseCode() throws IOException {
		final HttpResponse response = httpClient.execute(request);
		return response.getStatusLine().getStatusCode();
	}
}
