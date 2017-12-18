package com.neotys.dynatrace.common;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
						 final Map<String, String> params)
			throws MalformedURLException, URISyntaxException {
		this.request = generateHttpRequest(httpMethod, url);
		final boolean isHttps = url.contains("https");
		this.httpClient = newHttpClient(isHttps);

		addHeaders(request, headers);
		if (params != null && !params.isEmpty()) {
			if (!Objects.equals(httpMethod, HTTP_GET_METHOD))
				request.setParams(generateParams(params));
			else {
				setRequestUrl(request, url, params);
			}
		}
	}

	public HTTPGenerator(final String httpMethod, final String url, final String proxyHost, final String proxyPort,
						 final String proxyUser, final String proxyPass, final Map<String, String> headers,
						 final Map<String, String> Params) {
		this.httpClient = new DefaultHttpClient();
		HttpHost proxy = null;

		if (url.contains("http")) {
			proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
		} else if (url.contains("https")) {
			proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "https");
		}


		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		if (proxyUser != null) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxyHost, Integer.parseInt(proxyPort)),
					new UsernamePasswordCredentials(proxyUser, proxyPass));
		}

		request = generateHttpRequest(httpMethod, url);
		addHeaders(request, headers);
		if (Params != null) {
			request.setParams(generateParams(Params));
		}
	}

	public static HTTPGenerator newJsonHttpGenerator(final String httpMethod,
													 final String url,
													 final Map<String, String> headers,
													 final Map<String, String> params,
													 final String jsonString)
			throws MalformedURLException, URISyntaxException {
		final HTTPGenerator httpGenerator = new HTTPGenerator(httpMethod, url, headers, params);
		final StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
		addJsonParameters(httpGenerator.request, requestEntity, httpMethod);
		return httpGenerator;
	}

	private static void setRequestUrl(final HttpRequestBase request, final String url, final Map<String, String> params)
			throws URISyntaxException, MalformedURLException {
		final String urlWithParameters = addGetParametersToUrl(url, params);
		request.setURI(new URL(urlWithParameters).toURI());
	}

	private static DefaultHttpClient newHttpClient(final boolean isHttps) {
		if (isHttps) {
			return newHttpsClient();
		} else {
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getConnectionManager();
			return httpClient;
		}
	}

	@SuppressWarnings("deprecation")
	private static DefaultHttpClient newHttpsClient() {
		final DefaultHttpClient Client = new DefaultHttpClient();
		final HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

		final SchemeRegistry registry = new SchemeRegistry();
		final SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		final SingleClientConnManager mgr = new SingleClientConnManager(Client.getParams(), registry);
		// Set verifier
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return new DefaultHttpClient(mgr, Client.getParams());
	}

	private static void addJsonParameters(final HttpRequestBase request, final StringEntity JsonContent, final String httpMethod) {
		switch (httpMethod) {
			case HTTP_POST_METHOD:
				((HttpPost) request).setEntity(JsonContent);
				break;
			case HTTP_PUT_METHOD:
				((HttpPut) request).setEntity(JsonContent);
				break;

		}
	}

	@SuppressWarnings("deprecation")
	private HttpParams generateParams(final Map<String, String> params) {
		if (params != null) {
			HttpParams result = new BasicHttpParams();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.setParameter(entry.getKey(), entry.getValue());
			}
			return result;
		} else return null;
	}

	private static void addHeaders(final HttpRequestBase request, final Map<String, String> headersMap) {
		if (headersMap != null) {
			for (final Map.Entry<String, String> entry : headersMap.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}

	private static HttpRequestBase generateHttpRequest(final String method, final String url) {
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

	public void closeHttpClient() {
		httpClient.getConnectionManager().shutdown();
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


	private static boolean isJsonContent(final HttpResponse resp) {
		final Header contentTypeHeader = resp.getFirstHeader("Content-Type");
		return contentTypeHeader.getValue().contains("application/json");
	}

	private static String getStringResponse(final HttpResponse resp) throws IOException {
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
