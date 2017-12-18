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
import java.io.*;
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
	private final static String HTTP_GET_METHOD = "GET";
	private final static String HTTP_POST_METHOD = "POST";
	private final static String HTTP_OPTION_METHOD = "OPTION";
	private final static String HTTP_PUT_METHOD = "PUT";

	private final DefaultHttpClient httpClient;
	private final String httpMethod;
	private String url;
	private HttpRequestBase request;
	private int statusCode = 0;

	@SuppressWarnings("deprecation")
	public HTTPGenerator(final String url, final String method, final Map<String, String> headers, final Map<String, String> params) throws MalformedURLException, URISyntaxException {
		this.httpMethod = method;
		this.url = url;

		request = generateHttpRequest(httpMethod, this.url);
		request = generateHeaders(headers, request);
		if (params != null && !params.isEmpty()) {
			if (!Objects.equals(httpMethod, "GET"))
				request.setParams(generateParams(params));
			else {
				this.url = addGetParametersToUrl(url, params);
				request.setURI(new URL(this.url).toURI());
			}
		}
		if (this.url.contains("https")) {
			DefaultHttpClient Client = new DefaultHttpClient();
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(Client.getParams(), registry);
			httpClient = new DefaultHttpClient(mgr, Client.getParams());

			// Set verifier
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);


		} else {
			httpClient = new DefaultHttpClient();
			httpClient.getConnectionManager();
		}

	}

	@SuppressWarnings("deprecation")
	public HTTPGenerator(final String method, final String url, final Map<String, String> headers, final Map<String, String> params, final String jsonString) throws MalformedURLException, URISyntaxException {
		this.httpMethod = method;
		this.url = url;
		this.request = generateHttpRequest(httpMethod, this.url);
		this.request = generateHeaders(headers, request);
		this.url = addGetParametersToUrl(url, params);
		this.request.setURI(new URL(this.url).toURI());

		final StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
		setJsonParameter(requestEntity, request);

		if (this.url.contains("https")) {
			DefaultHttpClient Client = new DefaultHttpClient();
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(Client.getParams(), registry);
			this.httpClient = new DefaultHttpClient(mgr, Client.getParams());

			// Set verifier
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

		} else {
			this.httpClient = new DefaultHttpClient();
			this.httpClient.getConnectionManager();
		}

	}

	public HTTPGenerator(final String url, final String method, final String proxyHost, final String proxyPort,
						 final String proxyUser, final String proxyPass, final Map<String, String> headers,
						 final Map<String, String> Params) {
		this.httpClient = new DefaultHttpClient();
		this.url = url;
		this.httpMethod = method;
		HttpHost proxy = null;

		if (url.contains("http"))
			proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
		else if (url.contains("https"))
			proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "https");


		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		if (proxyUser != null) {

			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxyHost, Integer.parseInt(proxyPort)),
					new UsernamePasswordCredentials(proxyUser, proxyPass));
		}

		request = generateHttpRequest(httpMethod, this.url);
		request = generateHeaders(headers, request);
		if (Params != null) {
			request.setParams(generateParams(Params));
		}
	}

	private void setJsonParameter(final StringEntity JsonContent, final HttpRequestBase request) {
		switch (httpMethod) {
			case HTTP_POST_METHOD:
				((HttpPost) request).setEntity(JsonContent);
				break;
			case HTTP_PUT_METHOD:
				((HttpPut) request).setEntity(JsonContent);
				break;

		}
	}

	private HttpParams generateParams(final Map<String, String> params) {
		if (params != null) {
			HttpParams result = new BasicHttpParams();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.setParameter(entry.getKey(), entry.getValue());
			}
			return result;
		} else return null;
	}

	private HttpRequestBase generateHeaders(final Map<String, String> headersMap, final HttpRequestBase request) {
		if (headersMap != null) {
			for (final Map.Entry<String, String> entry : headersMap.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}
		return request;
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
				break;
		}
		return null;
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
		this.statusCode = response.getStatusLine().getStatusCode();
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
		this.statusCode = response.getStatusLine().getStatusCode();
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
