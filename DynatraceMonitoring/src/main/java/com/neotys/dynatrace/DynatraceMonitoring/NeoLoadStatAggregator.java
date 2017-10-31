package com.neotys.dynatrace.DynatraceMonitoring;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.neotys.NewRelic.HttpUtils.HTTPGenerator;

import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.Point;
import io.swagger.client.model.Points;
import io.swagger.client.model.TestStatistics;


public class NeoLoadStatAggregator extends TimerTask{
	private HashMap<String,String> Header = null;
	private HTTPGenerator http;
	private String ComponentsName;
	private final String DYNATRACE_API_URL="events/";
	private static final String DynatraceURL=".live.dynatrace.com/api/v1/";
	private static final String DynatraceAPplication="entity/services";
	private static final String DynatraceTimeSeriesCreation ="timeseries/custom";
	private static final String NL_Timeseries_Prefix="neoload.";
	private static final String DynatraceNewData="entity/infrastructure/custom/";
	private static final String DynatraceTimeSeries="timeseries";
	private static final String NLURL_START="https://";
	private static final String NLSaas="neoload.saas.neotys.com";
	private static final String NLRUL_LAST= "/#!result/overview/?benchId=";	private static final String DynatraceProtocol="https://";
	private final String NLGUID="com.neotys.NeoLoad.plugin";
	NLGlobalStat NLStat;
	private String VERSION="1.0.0";
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
	private final int HTTP_RESPONSE=200;
	private final int HTTP_RESPONSE_CREATED=201;
	private final int HTTP_RESPONSE_ALREADY=200;
	private static int MIN_NEW_RELIC_DURATION=30;
	private String APPLICATION_ENTITYID;
	private String NLScenarioName;
	private String Dynatrace_Managed_Hostname;
	ResultsApi NLWEBresult;
	private String NLControllerHost;
	private boolean TimeSeriesConfigured=false;
	private static final String NL_Picture_URL="http://www.neotys.com/wp-content/uploads/2017/07/Neotys-Emblem-Primary.png";
	private static final String NLTYPE="NeoLoad";
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
	public NeoLoadStatAggregator(String pDynatraceAPIKeyY,String pĈomponentName,String StrDynatraceAccoundID,ResultsApi pNLWEBresult,String pTestID,NLGlobalStat pNLStat,String ScenarioName,String pTestName, String pNLControllerHost,String dynatracemanaged,String Nlinstance) throws ClientProtocolException, DynatraceStatException, IOException
	{
		ComponentsName="Statistics";
		Dynatrace_API_KEY=pDynatraceAPIKeyY;
		NLStat=pNLStat;
		TestID=pTestID;
		TestName=pTestName;
		NL_Instance=Nlinstance;
		NLWEBresult=pNLWEBresult;
		Dynatrace_Managed_Hostname=dynatracemanaged;
		Dynatrace_AccountID=StrDynatraceAccoundID;
		NLScenarioName=ScenarioName;
		NLControllerHost=pNLControllerHost;
		InitHttpClient();
	}
	private void SendStatsToDynatrace() throws ApiException, DynatraceStatException, ClientProtocolException, IOException
	{
		TestStatistics StatsResult;
		long utc;
		long lasduration;
		
		utc=System.currentTimeMillis()/1000;
		
		lasduration=NLStat.getLasduration();
		
		if(lasduration==0 || (utc-lasduration)>=MIN_NEW_RELIC_DURATION)
		{
		
				
			StatsResult=NLWEBresult.getTestStatistics(TestID);
			if(StatsResult != null)
			{
				
				lasduration=SendData(StatsResult,lasduration);
				NLStat.setLasduration(lasduration);
			}
			else
			{
				System.out.println("stats est null");
			}
		}
	}
	private void GetRequestStats() throws ApiException
	{
		ArrayOfElementDefinition NLElement;
		ElementValues Values;
		float value;
		
		
		Values=NLWEBresult.getTestElementsValues(TestID,"all-requests");
		NLStat.UpdateRequestStat(Values, "REQUEST");
		Values=NLWEBresult.getTestElementsValues(TestID,"all-transactions");
		NLStat.UpdateRequestStat(Values, "TRANSACTION");
					

	}
	
	public long SendData(TestStatistics stat,long LasDuration) throws DynatraceStatException, ClientProtocolException, IOException, ApiException
	{
		int time = 0;
		List<String[]> data;
		long utc;
		utc=System.currentTimeMillis()/1000;
		
		if(NLStat==null)
			NLStat=new NLGlobalStat(stat);
		else
		{
			NLStat.UpdateStat(stat);
		}
		GetRequestStats();
		data=NLStat.GetNLData();
		
		if(LasDuration==0)
			time=0;
		else
		{
			time=(int) (utc-LasDuration);
		}
		if(!TimeSeriesConfigured)
		{
			if(!IsNlDataExists(data.get(0)[1]))
			{
				for(String[] metric : data)
					CreateNLTimeSeries(metric[1],metric[0],NLTYPE,metric[2],NLStat);
			
				TimeSeriesConfigured=true;
			}
		}		
		SendMetricToTimeSeriestAPI(data,NLTYPE);
		
		
		return utc;
	}
	private long GetUTCDate()
	  {
		  
		    long timeInMillisSinceEpoch123 =  System.currentTimeMillis();
		    timeInMillisSinceEpoch123-=200000;
		  return timeInMillisSinceEpoch123;
	  }
	
	private boolean IsNlDataExists(String timeseries) throws ClientProtocolException, IOException
	{
		boolean results=false;
		int httpcode;
		 String  Url=getAPiUrl()+DynatraceTimeSeries;
		 HashMap<String,String> Parameters= new HashMap<String,String>();
		 SendTokenIngetParam(Parameters);
		 String displayname = null;
		 Parameters.put("timeseriesId", NL_Timeseries_Prefix+":"+ timeseries);
		 Parameters.put("startTimestamp", String.valueOf(GetUTCDate()));
		 Parameters.put("endTimestamp", String.valueOf(System.currentTimeMillis()));
		 
		http=new HTTPGenerator(Url, "GET", Header,Parameters );
			
		httpcode=http.GetHttpResponseCodeFromResponse();
		if(httpcode!=HTTP_RESPONSE)		
			results=false;
		else 
			results=true;
					
		http.CloseHttpClient();
		
		return results;
		
	}
	
	
	///---------to update after the feedback from ANdy
		private void CreateNLTimeSeries(String TimeseriesName ,String DisplayName,String Type,String unit,NLGlobalStat stat) throws DynatraceStatException
		{
			int httpcode;
			HashMap<String,String> head = null;
			HashMap<String,String> Parameters = null;
			HTTPGenerator Insight_HTTP; 
			String URL ;
			head= new HashMap<String,String>();
			Parameters= new HashMap<String,String>();
			SendTokenIngetParam(Parameters);
			URL=getAPiUrl()+DynatraceTimeSeriesCreation+":"+TimeseriesName;
			String Exceptionmessage=null;
				
		
			String JSON_String="{\"displayName\":\""+DisplayName+"\","
			+ "\"unit\":\""+unit+"\","
			+ "\"dimensions\": [\"Neoload\"],"
	        + "\"types\":[\""+Type+"\"]}";
			
			Insight_HTTP=new HTTPGenerator("PUT",URL,  head,Parameters,JSON_String );

			try {
				httpcode=Insight_HTTP.GetHttpResponseCodeFromResponse();
				if(httpcode==HTTP_RESPONSE_CREATED||httpcode==HTTP_RESPONSE_ALREADY)		
					//------change the code to give a status if the data has been properly created...---review this pieece of code
					stat.SetStatus(TimeseriesName);
					//throw new DynatraceStatException("Unable to create TImeseries : "+DynatraceTimeSeriesCreation+":"+TimeseriesName);
				
					 
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
	///---------to update after the feedback from ANdy
	private void SendMetricToTimeSeriestAPI(List<String[]> data,String Type) throws DynatraceStatException
	{
		int httpcode;
		HashMap<String,String> head = null;
		HashMap<String,String> Parameters = null;
		HTTPGenerator Insight_HTTP; 
		String URL ;
		head= new HashMap<String,String>();
		Parameters= new HashMap<String,String>();
		SendTokenIngetParam(Parameters);
		URL=getAPiUrl()+DynatraceNewData+"NeoLoadData";
		String Exceptionmessage=null;
		long time=System.currentTimeMillis();
	
		if(NLControllerHost.equalsIgnoreCase("localhost"))
			NLControllerHost="10.0.1.0";
		
		String JSON_String="{\"displayName\" : \"NeoLoad Data\","
			 + "\"ipAddresses\" : [\""+NLControllerHost+"\"],"
			 + "\"listenPorts\" : [\""+7400+"\"],"
			 + "\"type\" : \""+Type+"\","
			 + "\"favicon\" : \""+NL_Picture_URL+"\","
			 + "\"configUrl\" : \""+GetNLURL()+TestID+"\","
			 + "\"tags\": [\"Loadtest\", \"NeoLoad\"],"
			 + "\"properties\" : { \"TestName\" : \""+TestName+"\" ,\"ScenarioName\" : \""+NLScenarioName+"\"  },"
			 + "\"series\" : [";
			
		String constr;
		
			int i=0;
			int totalsize=data.size()-1;
			for(String[] metric : data)
			{
				if(metric[4].equalsIgnoreCase("true"))
				{
					
					constr=  "{"
		   		     +  "\"timeseriesId\" : \"custom:"+metric[1]+"\","
					 +   "\"dimensions\" : { \"Neoload\" : \""+metric[0]+"\"  },"
					 +   "\"dataPoints\" : [ ["+String.valueOf(time)+"  , "+metric[3]+" ] ]"
					  +  "}";
								
					
					JSON_String+=constr;
					if(i<(totalsize))
							JSON_String+=",";
					
					i++;
				}
				else
					totalsize--;
			}
		
			if(JSON_String.substring(JSON_String.length() - 1).equalsIgnoreCase(","))
				JSON_String=JSON_String.substring(0, JSON_String.length()-1);
			
			JSON_String+= "]}";
			
			if(i>0)
			{
				
				Insight_HTTP=new HTTPGenerator("POST",URL,  head,Parameters,JSON_String );
			
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
							throw new DynatraceStatException(Exceptionmessage);
				 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Insight_HTTP.CloseHttpClient();
			}
	}

	
	@Override
	 public void run() {
		 try {
			 SendStatsToDynatrace();
		} catch (ApiException | DynatraceStatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
class DynatraceStatException extends Exception
{
      //Parameterless Constructor
      public DynatraceStatException() {}

      //Constructor that accepts a message
      public DynatraceStatException(String message)
      {
         super(message);
      }
 }
