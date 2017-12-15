package com.neotys.dynatrace.DynatraceEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.neotys.NewRelic.HttpUtils.HTTPGenerator;
import com.neotys.extensions.action.engine.Context;



public class DynatraceEventAPI {

	private HashMap<String,String> Header = null;
	private HTTPGenerator http;
	private String Dynatrace_Application_Name;
	private String ComponentsName;
	private static final String DYNATRACE_API_URL="events";
	private static final String DynatraceURL=".live.dynatrace.com/api/v1/";
	private static final String DynatraceAPplication="entity/services";
	private static final String NLURL_START="https://";
	private static final String NLSaas="neoload.saas.neotys.com";
	private static final String NLRUL_LAST= "/#!result/overview/?benchId=";
	private static final String DynatraceProtocol="https://";
	private String Dynatrace_API_KEY;
	private String Dynatrace_AccountID;
	private String TestName;
	private final String TestID;
	private final int BAD_REQUEST=400;
	private final int UNAUTHORIZED=403;
	private final int NOT_FOUND=404;
	private final int METHOD_NOT_ALLOWED=405;
	private final int REQUEST_ENTITY_TOO_LARGE=413;
	private final int INTERNAL_SERVER_ERROR=500;
	private final int BAD_GATEWAY=502;
	private final int SERVICE_UNAVAIBLE=503;
	private final int GATEWAY_TIMEOUT=504;
	private List<String> APPLICATION_ENTITYID;
	private String NLScenarioName;
	private static final String START_NL_TEST="Start NeoLoad Test";
	private static final String STOP_NL_TEST="Stop NeoLoad Test";
	private String Dynatrace_Managed_Hostname;
	private String NLProject;
	private Context cont;
	private String NL_Instance;
	
	private void InitHttpClient()
	{
		Header= new HashMap<String,String>();
	//	Header.put("X-License-Key", NewRElicLicenseKey);
		//Header.put("Content-Type", "application/json");
		//Header.put("Accept","application/json");
		
	}
	public void SendTokenIngetParam(HashMap<String, String> param)
	{
		param.put("Api-Token", Dynatrace_API_KEY);
	}
	
	public DynatraceEventAPI(String pDynatraceAPIKEY, String pDynatraceID,String DynataceApplicationNAme,Context context,String dynatracemanaged,String NlmanagedInstance) throws ClientProtocolException, DynatraceException, IOException
	{
		Dynatrace_AccountID=pDynatraceID;
		Dynatrace_API_KEY=pDynatraceAPIKEY;
		Dynatrace_Managed_Hostname=dynatracemanaged;
		NL_Instance=NlmanagedInstance;
		InitHttpClient();	
		APPLICATION_ENTITYID=GetApplicationID(DynataceApplicationNAme);
		TestName=context.getTestName();
		NLScenarioName=context.getScenarioName();
		TestID=context.getTestId();
		NLProject=context.getProjectName();
		cont=context;
	}
	

	 public String GetTags(String ApplicationName)
		{
			String result=null;
			String[] tagstable = null;
			if(ApplicationName != null)
			{

				if(ApplicationName.contains(","))
				{
					tagstable = ApplicationName.split(",");
					result="";
					for(String tag:tagstable)
					{
						result+=tag+"AND";
					}
					result=result.substring(0, result.length()-3);
				}
				else
					result=ApplicationName;
			}
			return result;
			
		}
	
	public  List<String> GetApplicationID(String ApplicaitoNAme) throws DynatraceException, ClientProtocolException, IOException
	{
		JSONArray jsoobj;
		String Url;
		JSONObject jsonApplication;
		HashMap<String, String> Parameters;
		String tags=GetTags(ApplicaitoNAme);
		Url=getAPiUrl()+DynatraceAPplication;
		Parameters= new HashMap<String,String>();
		Parameters.put("tag", tags);
	    SendTokenIngetParam(Parameters);
		//InitHttpClient();
	/*	if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
			http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
		else*/
		http=new HTTPGenerator(Url, "GET", Header,Parameters );
		
		
		jsoobj=http.GetJSONArrayHTTPresponse();
		if(jsoobj != null)
		{
				APPLICATION_ENTITYID= new ArrayList<String>();
				for(int i=0;i<jsoobj.length();i++)
				{
					jsonApplication=jsoobj.getJSONObject(i);
					if(jsonApplication.has("entityId"))
					{
						if(jsonApplication.has("displayName"))
						{
							
								APPLICATION_ENTITYID.add(jsonApplication.getString("entityId"));

						}
						
					}
					
				}
			
			
			
		}else
			APPLICATION_ENTITYID=null;
		
		if(APPLICATION_ENTITYID ==null)
			throw new DynatraceException("No Application find in The Dynatrace Account with the name " + Dynatrace_Application_Name); 
		
		http.CloseHttpClient();
		
		return APPLICATION_ENTITYID;
		
	}
	public void SendStartTest() throws DynatraceException
	{
		long start;
		start = System.currentTimeMillis()-cont.getElapsedTime();
		SendMetricToEventAPI(START_NL_TEST,start,System.currentTimeMillis());
	}
	public void SendStopTest() throws DynatraceException
	{
		long start;
		start = System.currentTimeMillis()-cont.getElapsedTime();
		SendMetricToEventAPI(STOP_NL_TEST,start,System.currentTimeMillis());
	}
	private String GetNLURL()
	{
		String result;
		if(NL_Instance!= null)
		{
			result=NLURL_START+NL_Instance+NLRUL_LAST;
		}
		else
		{
			result=NLURL_START+NLSaas+NLRUL_LAST;
		}
		return result;
	}
	private void SendMetricToEventAPI(String Message, long StartDuration, long Endduration) throws DynatraceException
	{
		int httpcode;
		HashMap<String,String> Parameters = null;
		HTTPGenerator Insight_HTTP; 
		String URL ;
		Parameters= new HashMap<String,String>();
		SendTokenIngetParam(Parameters);
		URL=getAPiUrl()+DYNATRACE_API_URL;
		String Exceptionmessage=null;
		long Duration=System.currentTimeMillis();
		String Entities= "";
		
		for(String service:APPLICATION_ENTITYID)
		{
			Entities+="\""+service+"\",";
		}
		Entities=Entities.substring(0, Entities.length()-1);
		
		String JSON_String="{\"start\":"+StartDuration+","
		+ "\"end\":"+Endduration+","
		+ "\"eventType\": \"CUSTOM_ANNOTATION\","
		+ "\"annotationType\": \"NeoLoad Test"+TestName+"\","
		+ "\"annotationDescription\": \""+Message+" "+TestName+"\","
        + "\"attachRules\":"
        + "{ \"entityIds\":["+Entities+"] ,"
        + "\"tagRule\" : {"
        + "\"meTypes\": \"SERVICE\","
		 + "\"tags\": [\"Loadtest\", \"NeoLoad\"]"
		+	"}},"
		+ "\"source\":\"NeoLoadWeb\"," 
		+ "\"customProperties\":"
		+ "{ \"ScriptName\": \""+NLProject+"\","
		+ "\"NeoLoad_TestName\":\""+TestName+"\","
	//	+ "\"NeoLoad_URL\":\""+NLURL_START+NLSaas+"\","
//-----------wait for patch on the dynatrace UI to send the exact link
		+ "\"NeoLoad_URL\":\""+GetNLURL()+TestID+"\","
		+ "\"NeoLoad_Scenario\":\""+NLScenarioName+"\"}"
			+ "}";
			  
		System.out.println(" Payload : " +JSON_String);
	
		
		Insight_HTTP=new HTTPGenerator("POST",URL,  Header,Parameters,JSON_String );
		try {
			httpcode=Insight_HTTP.GetHttpResponseCodeFromResponse();
			switch(httpcode)
			{
				
				  case BAD_REQUEST :
					  Exceptionmessage="The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					  break;
				  case UNAUTHORIZED :
					  Exceptionmessage="Authentication error (no license key header, or invalid license key).";
					  break;
				  case NOT_FOUND :
					  Exceptionmessage="Invalid URL.";
					  break;
				  case METHOD_NOT_ALLOWED :
					  Exceptionmessage="Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					  break;
				  case REQUEST_ENTITY_TOO_LARGE :
					  Exceptionmessage="Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					  break;
				  case INTERNAL_SERVER_ERROR :
					  Exceptionmessage="Unexpected server error";
					  break;
				  case BAD_GATEWAY :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case SERVICE_UNAVAIBLE :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case GATEWAY_TIMEOUT :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				
			}
		    if(Exceptionmessage!=null)
		    	throw new DynatraceException(Exceptionmessage);
				 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Insight_HTTP.CloseHttpClient();

	}
	private String getAPiUrl()
	{
		String result;
		
		if(Dynatrace_Managed_Hostname!= null)
		{
			result=DynatraceProtocol+Dynatrace_Managed_Hostname+"/api/v1/";
		}
		else
		{
			result=DynatraceProtocol+Dynatrace_AccountID+DynatraceURL;
		}
		return result;
	}
}
class DynatraceException extends Exception
{
      //Parameterless Constructor
      public DynatraceException() {}

      //Constructor that accepts a message
      public DynatraceException(String message)
      {
         super(message);
      }
 }
