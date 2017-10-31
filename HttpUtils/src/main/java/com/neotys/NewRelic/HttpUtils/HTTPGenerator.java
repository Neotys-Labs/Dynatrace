package com.neotys.NewRelic.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */

public class HTTPGenerator 
{
	private DefaultHttpClient httpClient ;
	private String HttpMethod;
	private String URL;

	
	private final static String HTTP_GET_METHOD="GET";
	private final static String HTTP_POST_METHOD="POST";
	private final static String HTTP_OPTION_METHOD="OPTION";
	private final static String HTTP_PUT_METHOD="PUT";
	private HttpRequestBase request;
	private int StatusCode=0;
	
    @SuppressWarnings("deprecation")
	public HTTPGenerator(String Url,String Method,HashMap<String,String> headers,HashMap<String,String> Params)
    {
    	
    	HttpMethod=Method;
    	URL=Url;
    	try
    	{
    		
    		request=GenerateHTTPReques(HttpMethod, URL);
	    	request=GenerateHeaders(headers, request);
	    	if(Params!=null && !Params.isEmpty())
	    	{
	    		if(HttpMethod !="GET")	
	    			request.setParams(GenerateParams(Params));
	    		else
	    		{
	    			URL=addGetParametersToUrl(Url,Params);
	    			request.setURI(new URL(URL).toURI());
	    		}
	    	}
	    	if(URL.contains("https"))
    		{
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
    		      
   	    	 
   	    	  
    		}
    		else
    		{
    			httpClient = new DefaultHttpClient();
    			httpClient.getConnectionManager();
    		}
    	
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    
    private HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
    @SuppressWarnings("deprecation")
	public HTTPGenerator(String Url,HashMap<String,String> headers,String JSON_STRING)
    {
    	
    	HttpMethod="POST";
    	StringEntity requestEntity = new StringEntity(
    		    JSON_STRING,
    		    ContentType.APPLICATION_JSON);
    	URL=Url;
    	try
    	{
    		
    		request=GenerateHTTPReques(HttpMethod, URL);
	    	request=GenerateHeaders(headers, request);
	    	((HttpPost) request).setEntity(requestEntity);
	    	
	    	if(URL.contains("https"))
    		{
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
	    		
    		}
    		else
    		{
    			httpClient = new DefaultHttpClient();
    			httpClient.getConnectionManager();
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    private HttpRequestBase SetJsonParameter(StringEntity JsonContent,HttpRequestBase request)
    {
    	switch(HttpMethod)
    	{
    	case HTTP_POST_METHOD :
    		((HttpPost) request).setEntity(JsonContent);
    		break;
    	case HTTP_PUT_METHOD :
    		((HttpPut) request).setEntity(JsonContent);
    		break;
    		
    	}
    	return request;
    }
    @SuppressWarnings("deprecation")
	public HTTPGenerator(String Method,String Url,HashMap<String,String> headers,HashMap<String, String> Params,String JSON_STRING)
    {
    	
    	HttpMethod=Method;
    	StringEntity requestEntity = new StringEntity(
    		    JSON_STRING,
    		    ContentType.APPLICATION_JSON);
    	URL=Url;
    	try
    	{
    		
    		request=GenerateHTTPReques(HttpMethod, URL);
	    	request=GenerateHeaders(headers, request);
	    	URL=addGetParametersToUrl(Url,Params);
			request.setURI(new URL(URL).toURI());
			
			SetJsonParameter(requestEntity,request);
	    	
	    	if(URL.contains("https"))
    		{
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
	    		
    		}
    		else
    		{
    			httpClient = new DefaultHttpClient();
    			httpClient.getConnectionManager();
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    private HttpParams GenerateParams(HashMap<String, String> params)
    {
    	if(params!=null)
    	{
	    	HttpParams result = new BasicHttpParams();
	    	for(Map.Entry<String, String> entry : params.entrySet())
	    	{
	    		result.setParameter(entry.getKey(), entry.getValue());
	    	}
	    	return result;
    	}
    	else return null;
    }
    private HttpRequestBase GenerateHeaders(HashMap<String,String> head,HttpRequestBase request)
    {
    	if(head!=null)
    	{
	    	for(Map.Entry<String, String> entry : head.entrySet())
	    	{
	    		request.setHeader(entry.getKey(),entry.getValue());
	    	}
	    }
    	
    	return request;
    }

    public void NewHttpRequest(String Url,String Method,HashMap<String,String> headers,HashMap<String,String> Params)
   	{
    	HttpMethod=Method;
    	URL=Url;
    	try
    	{
    		request=GenerateHTTPReques(HttpMethod, URL);	
    		request=GenerateHeaders(headers, request);
    		if(Params!=null && !Params.isEmpty())
	    	{
	    
	    		if(HttpMethod !="GET")	
	    			request.setParams(GenerateParams(Params));
	    		else
	    		{
	    			URL=addGetParametersToUrl(Url,Params);
	    			request.setURI(new URL(URL).toURI());
	    		}
	    	}
	    }
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
  
    private HttpRequestBase GenerateHTTPReques(String Method, String Url )
    {
    	HttpRequestBase request = null;
    	switch(HttpMethod)
    	{
    	case HTTP_GET_METHOD :
    		request=new HttpGet(Url);
    		break;
    	case HTTP_POST_METHOD :
    		request= new HttpPost(Url);
    		break;
    	case HTTP_OPTION_METHOD :
    		break;
    	case HTTP_PUT_METHOD :
    		request= new HttpPut(Url);
    		break;
    		
    	}
    	return request;
    }
   
    public void CloseHttpClient()
    {
    	httpClient.getConnectionManager().shutdown();
    }
    private  String addGetParametersToUrlWithNoEncoding(String url,HashMap<String, String> params){
       
    	if(!url.endsWith("?"))
            url += "?";

       
        if(params!=null)
    	{
	    	HttpParams result = new BasicHttpParams();
	    	int i=0;
	    	int max=params.size();
	    	for(Map.Entry<String, String> entry : params.entrySet())
	    	{
	    		if(i<(max-1))
	    			url+= entry.getKey()+"="+entry.getValue()+"&";
	    		else
	    			url+= entry.getKey()+"="+entry.getValue();
	    			
	    	}
	    }
       
     
        return url;
    }
    
    private  String addGetParametersToUrl(String url,HashMap<String, String> params){
        
    	if(!url.endsWith("?"))
            url += "?";

        List<NameValuePair> parameters = new LinkedList<NameValuePair>();

        if(params!=null)
    	{
	    	HttpParams result = new BasicHttpParams();
	    	for(Map.Entry<String, String> entry : params.entrySet())
	    	{
	    		parameters.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
	        	
	    	}
	    }
       
       String paramString = URLEncodedUtils.format( parameters, HTTP.UTF_8);

        url += paramString;
        return url;
    }
    
    public void SetAllowHostnameSSL() throws NoSuchAlgorithmException
    {
    	SSLSocketFactory sf=null ;
        SSLContext sslContext = null;
        StringWriter writer;
        try {
            sslContext = SSLContext.getInstance("TLS")  ;
            sslContext.init(null,null,null);
        } catch (NoSuchAlgorithmException e) {
            //<YourErrorHandling>
        }  catch (KeyManagementException e){
            //<YourErrorHandling>
        }

        try{
            sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme sch = new Scheme("https", 443, sf);
    		httpClient.getConnectionManager().getSchemeRegistry().register(sch);

        } catch(Exception e) {
            //<YourErrorHandling>

    }
    	
    		
    }
    public JSONArray GetJSONArrayHTTPresponse() throws ClientProtocolException, IOException
    {
    
    	JSONArray json = null;
    	HttpResponse response = null;
    	
    	Header[] requestHeaders = request.getAllHeaders();
    	
    
    
    		response=httpClient.execute(request);
    		StatusCode=response.getStatusLine().getStatusCode();
    	
    		if(IsJsonConten(response))
        		json=new JSONArray(GetStringResponse(response));
        	
    		return json;
    		
    	
    		
    	
    }
   
    public JSONObject GetJSONHTTPresponse() throws ClientProtocolException, IOException
    {
    
    	JSONObject json = null;
    	HttpResponse response = null;
    
    	Header[] requestHeaders = request.getAllHeaders();
 
    
    		response=httpClient.execute(request);
    		StatusCode=response.getStatusLine().getStatusCode();

    		if(StatusCode==200)
    		{
    			if(IsJsonConten(response))
    				json=new JSONObject(GetStringResponse(response));
        	
    		}
    		
    		return json;
    		
    	
    		
    	
    }
    public int GetHttpResponseCodeFromResponse() throws ClientProtocolException, IOException
    {
    
    	JSONObject json = null;
    	HttpResponse response = null;
  
    	Header[] requestHeaders = request.getAllHeaders();
    
    
    
    
		response=httpClient.execute(request);
	
		StatusCode=response.getStatusLine().getStatusCode();
    		
    		
    	
		return StatusCode;

    		
    	
    }
    public int getStatusCode()
    {
    	return StatusCode;
    }
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    
    public boolean IsJsonConten(HttpResponse resp)
    {
    	boolean result=false;
    	Header contentTypeHeader = resp.getFirstHeader("Content-Type");
    	if (contentTypeHeader.getValue().contains("application/json")) {
    		result=true;
    	}
    	
    	return result;
    }
    
    public boolean IsXMLConten(HttpResponse resp)
    {
    	boolean result=false;
    	Header contentTypeHeader = resp.getFirstHeader("Content-Type");
    	if (("application/xml").equals(contentTypeHeader.getValue()))
    			{
    		result=true;
    	}
    	if ("text/xml".equals(contentTypeHeader.getValue())) {
    		result=true;
    	}
    	
    	return result;
    }
    
    public String GetStringResponse(HttpResponse resp)
    {
    	String result = null;
    	 try {
    	           
    	        HttpEntity entity = resp.getEntity();

    	        if (entity != null) {

    	            // A Simple JSON Response Read
    	            InputStream instream = entity.getContent();
    	            result = convertStreamToString(instream);
    	            // now you have the string representation of the HTML request
    	            // Headers
        	        org.apache.http.Header[] headers = resp.getAllHeaders();
        	      
        	        
    	            instream.close();
    	            if (resp.getStatusLine().getStatusCode() !=200) {
    	                return null;
    	            }

    	        }
    	       
    	        
    	    } catch (ClientProtocolException e1) {
    	        // TODO Auto-generated catch block
    	        e1.printStackTrace();
    	    } catch (IOException e1) {
    	        // TODO Auto-generated catch block
    	        e1.printStackTrace();
    	    }
    	    return result;
   	}
   
    public HTTPGenerator(String Url,String Method, String ProxyHost,String ProxyPort,String ProxyUser, String ProxyPass,HashMap<String,String> headers,HashMap<String,String> Params)
    {
    	httpClient = new DefaultHttpClient();
    	URL=Url;
    	try
    	{
    		HttpHost proxy = null;
    		
    		if(Url.contains("http"))
    			proxy = new HttpHost(ProxyHost, Integer.parseInt(ProxyPort), "http");  
    		else
    			if(Url.contains("https"))
    				proxy = new HttpHost(ProxyHost, Integer.parseInt(ProxyPort), "https");
    		
    		
	    	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
	                 proxy);
	       if(ProxyUser != null)
	       {
	    	 
	    	   httpClient.getCredentialsProvider().setCredentials(
	    			    new AuthScope(ProxyHost, Integer.parseInt(ProxyPort)),
	    			    new UsernamePasswordCredentials(ProxyUser, ProxyPass));
	       }
	    	
	    	
	    	HttpMethod=Method;
	    	request=GenerateHTTPReques(HttpMethod, URL);
	    	request=GenerateHeaders(headers, request);
	    	if(Params!=null)
	    		request.setParams(GenerateParams(Params));
 
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    public HTTPGenerator(String Url, String ProxyHost,String ProxyPort,String ProxyUser, String ProxyPass,HashMap<String,String> headers,String JSON_STRING)
    {
    	httpClient = new DefaultHttpClient();
    	URL=Url;
    	StringEntity requestEntity = new StringEntity(
    		    JSON_STRING,
    		    ContentType.APPLICATION_JSON);
    	try
    	{
    		HttpHost proxy = null;
    		
    		if(Url.contains("http"))
    			proxy = new HttpHost(ProxyHost, Integer.parseInt(ProxyPort), "http");  
    		else
    			if(Url.contains("https"))
    				proxy = new HttpHost(ProxyHost, Integer.parseInt(ProxyPort), "https");
    		
    		
	    	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
	                 proxy);
	       if(ProxyUser != null)
	       {
	    	 
	    	   httpClient.getCredentialsProvider().setCredentials(
	    			    new AuthScope(ProxyHost, Integer.parseInt(ProxyPort)),
	    			    new UsernamePasswordCredentials(ProxyUser, ProxyPass));
	       }
	    	
	    	
	    	HttpMethod="POST";
	    	request=GenerateHTTPReques(HttpMethod, URL);
	    	request=GenerateHeaders(headers, request);
	    	((HttpPost) request).setEntity(requestEntity);
 
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}
