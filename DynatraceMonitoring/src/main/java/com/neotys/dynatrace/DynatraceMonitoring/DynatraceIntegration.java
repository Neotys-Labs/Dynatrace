package com.neotys.dynatrace.DynatraceMonitoring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.neotys.NewRelic.HttpUtils.HTTPGenerator;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;

public class DynatraceIntegration {
	private DataExchangeAPIClient client;
	private ContextBuilder Context;
	private HTTPGenerator http;
	private static final String DynatraceURL=".live.dynatrace.com/api/v1/";
	private static final String DynatraceAPplication="entity/services";
	private static final String DynatraceAPIProcessGroup="entity/infrastructure/process-groups";
	private static final String DynatraceHosts="entity/infrastructure/hosts";
	private static final String DynatraceTimeSeries="timeseries";
	private static final String DynatraceProtocol="https://";
	private static final String NeoLoadLocation="Dynatrace";
	private EntryBuilder entry;
	private String DynatraceAPIKEY;
	private String DynatraceID;
	private String Dynatrace_Application;
	private List<String> Dynatrace_Application_ServiceId;
	private List<String> Dynatrace_Application_HOstId;
	private String Dynatrace_Application_Name;
	private String PROXYHOST;
	private String PROXYPASS;
	private String PROXYUSER;
	private String PROXYPORT;
	private HashMap<String,String> Header = null;
	private HashMap<String,String> Parameters=null;
	private static String DIMENSION_PROCESS_INSTANCE="PROCESS_GROUP_INSTANCE";
	private static String DIMENSION_PROCESS_GROUP="PROCESS_GROUP";
	private static String DIMENSION_HOST="HOST";
	private static List<String> RelevantDimensions = Arrays.asList(DIMENSION_PROCESS_GROUP,DIMENSION_HOST);
	private static List<String> Aggregatetype = Arrays.asList("AVG","COUNT");
	private String Dynatrace_Managed_Hostname=null;
	private static HashMap<String,String> TimeseriesInfra;
	private static HashMap<String,String> TimeseriesServices;
	private boolean Isrunning=true;
	
	private long Start_TS;
	public DynatraceIntegration(String DynataceAPIKEY, String Dynatrace_ID,String pDynatraceApplication,String NeoLoadAPIHost,String NeoLoadAPIport,String NeoLoadKeyAPI, String dynatracemanaged,long ts) throws ParseException
	{
		Start_TS=ts;
		Context = new ContextBuilder();
		Context.hardware("Dynatrace").location(NeoLoadLocation).software("OneAgent")

			.script("DynatraceMonitoring" + System.currentTimeMillis());
		DynatraceAPIKEY=DynataceAPIKEY;
		Dynatrace_Application=pDynatraceApplication;
		DynatraceID=Dynatrace_ID;
		Dynatrace_Managed_Hostname=dynatracemanaged;
		Isrunning=true;
		try {
			
			InitTimeseriesHashMap();
	
			client = DataExchangeAPIClientFactory.newClient("http://"+NeoLoadAPIHost+":"+NeoLoadAPIport+"/DataExchange/v1/Service.svc/", Context.build(), NeoLoadKeyAPI);
			InitHttpClient();
			Dynatrace_Application_ServiceId=GetApplicationID();
			GetHostsFromProcessGroup();
			GetHosts();
			GetDynatraceData();
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException|DynatraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	 private boolean IsRelevantDimension(JSONArray array)
	  {
		  boolean result= false;
		  for(String listItem : RelevantDimensions){
			  for(int i=0;i<array.length();i++)
			  {
				   if(array.getString(i).contains(listItem)){
				      return true;
				   }
			  }
			}
		  
		  return result;
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
	 
	private void configureHttpsfordynatrace() throws NoSuchAlgorithmException
	{
		http.setAllowHostnameSSL();
	}
	private void CreateEntry(String EntityName,String MetricName,String MetricValueName,double value,String unit,long ValueDate) throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException, ParseException
	{
	  entry=new EntryBuilder(Arrays.asList("Dynatrace",EntityName, MetricName,MetricValueName), ValueDate);
		entry.unit(unit);
		entry.value(value);
		client.addEntry(entry.build());
	}
	
		public  List<String> GetApplicationID() throws DynatraceException, ClientProtocolException, IOException, NoSuchAlgorithmException
		{
			JSONArray jsoobj;
			String Url;
			JSONObject jsonApplication;
			String tags=GetTags(Dynatrace_Application);

			Url=getAPiUrl()+DynatraceAPplication;
			Parameters= new HashMap<String,String>();
			Parameters.put("tag", tags);
		    SendTokenIngetParam(Parameters);
			//InitHttpClient();
			if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
				http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
			
			else
				http=new HTTPGenerator(Url, "GET", Header,Parameters );
			
			
			jsoobj=http.getJSONArrayHTTPresponse();
			if(jsoobj != null)
			{
					Dynatrace_Application_ServiceId= new ArrayList<String>();
					for(int i=0;i<jsoobj.length();i++)
					{
						jsonApplication=jsoobj.getJSONObject(i);
						if(jsonApplication.has("entityId"))
						{
							
							Dynatrace_Application_ServiceId.add(jsonApplication.getString("entityId"));

							
						}
						
					}
				
				
				
			}else
				Dynatrace_Application_ServiceId=null;
			
			if(Dynatrace_Application_ServiceId ==null)
				throw new DynatraceException("No Application find in The Dynatrace Account with the name " + Dynatrace_Application_Name); 
			
			http.closeHttpClient();
			
			return Dynatrace_Application_ServiceId;
			
		}
		
		 private HashMap<String,String> GetEntityDefinition(JSONObject entity)
		 {
			HashMap<String,String> result = null;
						
			result=new HashMap<String,String>();
			for (Object key : entity.keySet()) {
		         result.put((String)key,  (String)entity.get((String)key));
			}
			return result;
			 
		 }
		 private String GetEntityDisplayName(HashMap<String,String> map,String entity)
		 {
			String result = null;
			String[] entities=entity.split(",");
			
			for (Entry<String, String> e : map.entrySet()) {
				for(int i=0;i<entities.length;i++)
				{
					if(entities[i].equalsIgnoreCase((String)e.getKey()))
						return e.getValue();
				}
			}
			return result;
		 }
		 
		 private void GetHosts() throws ClientProtocolException, IOException, NoSuchAlgorithmException
		{
			 JSONArray jsoobj;
			 JSONObject jsonApplication;
			
			 String tags=GetTags(Dynatrace_Application);
			 String  Url=getAPiUrl()+DynatraceHosts;
			 HashMap<String,String> Parameters= new HashMap<String,String>();
			 Parameters.put("tag", tags);
			 SendTokenIngetParam(Parameters);
			 
				if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
					http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
				else
					http=new HTTPGenerator(Url, "GET", Header,Parameters );
				
			
				jsoobj=http.getJSONArrayHTTPresponse();
				if(jsoobj != null)
				{
						for(int i=0;i<jsoobj.length();i++)
						{
							jsonApplication=jsoobj.getJSONObject(i);
							if(jsonApplication.has("entityId"))
							{
								if(jsonApplication.has("displayName"))
								{
									Dynatrace_Application_HOstId.add(jsonApplication.getString("entityId"));
								}						
							}							
						}
					
				}
				
				http.closeHttpClient();
				
				
				
				
		}
		 private void GetHostsFromProcessGroup() throws ClientProtocolException, IOException, NoSuchAlgorithmException
			{
				 JSONArray jsoobj;
				 JSONObject jsonApplication;
				 JSONObject jsonfromerelation;
				 JSONArray JsonRUnon;
				 String tags=GetTags(Dynatrace_Application);
				 String  Url=getAPiUrl()+DynatraceAPIProcessGroup;
				 HashMap<String,String> Parameters= new HashMap<String,String>();
				 Parameters.put("tag", tags);
				 SendTokenIngetParam(Parameters);
				 
					if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
						http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
					else
						http=new HTTPGenerator(Url, "GET", Header,Parameters );
					
				
					jsoobj=http.getJSONArrayHTTPresponse();
					if(jsoobj != null)
					{
							Dynatrace_Application_HOstId= new ArrayList<String>();
							for(int i=0;i<jsoobj.length();i++)
							{
								jsonApplication=jsoobj.getJSONObject(i);
								if(jsonApplication.has("entityId"))
								{
									if(jsonApplication.has("fromRelationships"))
									{
										jsonfromerelation=jsonApplication.getJSONObject("fromRelationships");
										if(jsonfromerelation.has("runsOn"))
										{
											JsonRUnon=jsonfromerelation.getJSONArray("runsOn");
											if(JsonRUnon!=null)
											{
												for(int j=0;j<JsonRUnon.length();j++)
												{
													Dynatrace_Application_HOstId.add(JsonRUnon.getString(j));
												}
											}
											
										}
									}						
								}							
							}
						
					}
					
					http.closeHttpClient();
					
					
					
					
			}
		public void SendTokenIngetParam(HashMap<String, String> param)
		{
			param.put("Api-Token", DynatraceAPIKEY);
		}
		public void GetDynatraceData() throws ClientProtocolException, IOException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException
		{
			HashMap<String,String> Host;
			List<DynatraceMetric> data;
			try
			{
				if(Isrunning)
				{
					///---Send the service data of this entity-----
					for(Entry<String, String> m : TimeseriesServices.entrySet())
					{
						
							if(Isrunning)
							{
								data=GetTImeSeriesMetricData(m.getKey(),m.getKey(),m.getValue(),Dynatrace_Application_ServiceId);
								SendDynamtraceMetricEntity(data);
							}
						
					}
					//---------------------------------
					
					//----send the infrastructure entity---------------
					for(Entry<String, String> m : TimeseriesInfra.entrySet())
					{
						if(Isrunning)
						{
							data=GetTImeSeriesMetricData(m.getKey(),m.getKey(),m.getValue(),Dynatrace_Application_HOstId);
							SendDynamtraceMetricEntity(data);
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		private void InitTimeseriesHashMap()
		{
			///------requesting only infrastructure and services metrics-------------//
			TimeseriesInfra=new HashMap<String,String>();
			TimeseriesInfra.put("com.dynatrace.builtin:host.availability","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.cpu.idle","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.cpu.iowait","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.cpu.steal","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.cpu.system","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.cpu.user","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.availablespace","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.bytesread","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.byteswritten","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.freespacepercentage","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.queuelength","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.readoperations","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.readtime","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.usedspace","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.writeoperations","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.disk.writetime","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.mem.available","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.mem.availablepercentage","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.mem.pagefaults","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.mem.used","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.nic.bytesreceived","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.nic.bytessent","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:host.nic.packetsreceived","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.cpu.usage","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.jvm.committedmemory","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.jvm.garbagecollectioncount","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.jvm.garbagecollectiontime","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.jvm.threadcount","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.jvm.usedmemory","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.mem.usage","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.nic.bytesreceived","AVG");
			TimeseriesInfra.put("com.dynatrace.builtin:pgi.nic.bytessent","AVG");
			TimeseriesServices=new HashMap<String,String>();
			TimeseriesServices.put("com.dynatrace.builtin:service.clientsidefailurerate","AVG");
			TimeseriesServices.put("com.dynatrace.builtin:service.errorcounthttp4xx","COUNT");
			TimeseriesServices.put("com.dynatrace.builtin:service.errorcounthttp5xx","COUNT");
			TimeseriesServices.put("com.dynatrace.builtin:service.failurerate","AVG");
			TimeseriesServices.put("com.dynatrace.builtin:service.requestspermin","COUNT");
			TimeseriesServices.put("com.dynatrace.builtin:service.responsetime","AVG");
			TimeseriesServices.put("com.dynatrace.builtin:service.serversidefailurerate","AVG");
			
		}
		private void SendDynamtraceMetricEntity(List<DynatraceMetric> metric) throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException, ParseException
		{
			for(DynatraceMetric data : metric)
			{
				String timeseries = data.getTimeseries();
				String[] metricname=timeseries.split(":");
				CreateEntry(data.getMetricName(),metricname[0],metricname[1], data.getValue(),data.getUnit(), data.getTime());
			}
		}
		public void SetTestToStop()
		{
			Isrunning=false;
		}
		public void SettestRunning()
		{
			Isrunning=true;
		}
		
		public DynatraceIntegration(String DynataceAPIKEY, String Dynatrace_ID,String pDynatraceApplication,String NeoLoadAPIHost,String NeoLoadAPIport,String NeoLoadKeyAPI,String HttpProxyHost,String HttpProxyPort,String HttpProxyUser,String HttpProxyPass,String dynatracemanaged,long ts) throws ParseException
		{
			Start_TS=ts;
			Context = new ContextBuilder();
			Context.hardware("NewRelic").location(NeoLoadLocation).software("NewRelic")

				.script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());
			DynatraceAPIKEY=DynataceAPIKEY;
			Dynatrace_Application=pDynatraceApplication;
			DynatraceID=Dynatrace_ID;
			PROXYHOST=HttpProxyHost;
			PROXYPASS=HttpProxyPass;
			PROXYPORT=HttpProxyPort;
			PROXYUSER=HttpProxyUser;
			Dynatrace_Managed_Hostname=dynatracemanaged;
			Isrunning=true;
			
			try {
				InitTimeseriesHashMap();
				client = DataExchangeAPIClientFactory.newClient("http://"+NeoLoadAPIHost+":"+NeoLoadAPIport+"/DataExchange/v1/Service.svc/", Context.build(), NeoLoadKeyAPI);
				InitHttpClient();
				Dynatrace_Application_ServiceId=GetApplicationID();
				GetHostsFromProcessGroup();
				GetHosts();
				GetDynatraceData();
				
			} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException | DynatraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		  private long GetUTCDate()
		  {
			  
			    long timeInMillisSinceEpoch123 =  System.currentTimeMillis();
			    timeInMillisSinceEpoch123-=120000;
			  return timeInMillisSinceEpoch123;
		  }
		private List<DynatraceMetric> GetTImeSeriesMetricData(String timeseries,String EntityId,String aggregate,List<String> ListEntityId) throws ClientProtocolException, IOException, NoSuchAlgorithmException
		{
			 List<DynatraceMetric> Metrics= new ArrayList<DynatraceMetric>();
			 DynatraceMetric metric;
			 JSONArray jsoobj;
			 JSONObject jsonApplication;
			 JSONObject jsonDatapoint;
			 String entity;
			 JSONObject JsonEntity;
			 String  Url=getAPiUrl()+DynatraceTimeSeries;
			 HashMap<String,String> Parameters= new HashMap<String,String>();
			 SendTokenIngetParam(Parameters);	
			 String displayname = null;
			 HashMap<String,String> Entity= new HashMap<String,String>();
			 
			
			 String JsonEntities;
			 
			 JsonEntities="{"
			 +"\"aggregationType\": \""+aggregate.toLowerCase()+"\","
			 + "\"timeseriesId\" : \""+timeseries+"\","
			 + "\"endTimestamp\":\""+String.valueOf(System.currentTimeMillis())+"\","
			 + "\"startTimestamp\":\""+String.valueOf(GetUTCDate())+"\","
			 + "\"entities\":[";
			 
			 for(String entit:ListEntityId)
			 {
				 JsonEntities+="\""+entit+"\",";
				 
			 }
			 
			 JsonEntities=JsonEntities.substring(0, JsonEntities.length()-1);
			 JsonEntities+="]}";
			 
			
			 
				if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
					http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
				else
					http=new HTTPGenerator("POST",Url, Header,Parameters,JsonEntities );
				
			
				jsonApplication=http.getJsonHttpResponse();
				if(jsonApplication != null)
				{
					if(jsonApplication.has("result"))
					{
						jsonApplication=jsonApplication.getJSONObject("result");
						if(jsonApplication.has("dataPoints"))
						{
							if(jsonApplication.has("entities"))
							{
								 JsonEntity=jsonApplication.getJSONObject("entities");
								 Entity=GetEntityDefinition(JsonEntity);
							
								jsonDatapoint=jsonApplication.getJSONObject("dataPoints");
								Iterator<?> keys1 =jsonDatapoint.keys();
								while( keys1.hasNext() ) 
								{
							        entity = (String) keys1.next();
							        displayname=GetEntityDisplayName(Entity,entity);
							        JSONArray arr = jsonDatapoint.getJSONArray(entity);
							        
							        for(int i=0;i<arr.length();i++)
							        {
							        	JSONArray data=arr.getJSONArray(i);
							        	
							        		if(data.get(1) instanceof Double)
							        		{
							        			if(data.getLong(0)>= Start_TS)
							        			{
							        				metric=new DynatraceMetric(jsonApplication.getString("unit"), data.getDouble(1),data.getLong(0), displayname,jsonApplication.getString("timeseriesId"),entity);
							        				Metrics.add(metric);
							        			}
							        		}
							        	
							        }
						
							    }
							}
						}
					}
				}
				http.closeHttpClient();
			return Metrics;
		}
		private HashMap<String,String> GetTImeSeriesMetric(String EntityId,List<String> listEntity) throws ClientProtocolException, IOException, NoSuchAlgorithmException
		{
			HashMap<String,String> Metrics= new HashMap<String,String>();
			 JSONArray jsoobj;
			 JSONObject jsonApplication;
			 String JsonEntities;
			 HashMap<String,String> Hosts= new HashMap<String,String>();
			 String  Url=getAPiUrl()+DynatraceTimeSeries;
			 HashMap<String,String> Parameters= new HashMap<String,String>();
			 Parameters.put("aggregationType", "AVG");
			 Parameters.put("entity", EntityId);
			 Parameters.put("startTimestamp", String.valueOf(GetUTCDate()));
			 SendTokenIngetParam(Parameters);
				
			 JsonEntities="{entities:[";
			 for(String entit:listEntity)
			 {
				 JsonEntities+=entit+",";
				 
			 }
			 JsonEntities=JsonEntities.substring(0, JsonEntities.length()-1);
			 JsonEntities+="]}";
				
				if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
					http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
				else
					http=new HTTPGenerator("POST",Url, Header,Parameters, JsonEntities);
				
			
				jsoobj=http.getJSONArrayHTTPresponse();
				if(jsoobj != null)
				{
					
						for(int i=0;i<jsoobj.length();i++)
						{
							jsonApplication=jsoobj.getJSONObject(i);
							if(jsonApplication.has("timeseriesId"))
							{
								if(jsonApplication.has("dimensions"))
								{
									
									if(IsRelevantDimension(jsonApplication.getJSONArray("dimensions")))
									{
										String aggregate=GetAggregate(jsonApplication.getJSONArray("aggregationTypes"));
										if(aggregate!=null)
											Metrics.put( jsonApplication.getString("timeseriesId"),aggregate);
									}
								}
							}
						}
					
				}
				http.closeHttpClient();
			return Metrics;
			
		}
		private String GetAggregate(JSONArray arr)
		{
			String result =null;
			for(int i=0;i<arr.length();i++)
			{
				for(String entity : Aggregatetype)
				{
					if(entity.equalsIgnoreCase(arr.getString(i)))
						return entity;
				}
			}
			return result;
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
				result=DynatraceProtocol+DynatraceID+DynatraceURL;
			}
			return result;
		}
		private void InitHttpClient()
		{
			Header= new HashMap<String,String>();
			
		//	Header.put("Authorization", "Api‐Token "+DynatraceAPIKEY);
			//Header.put("Content-Type", "application/json");
			
			
		}
}
