package com.neotys.dynatrace.monitoring;

import java.io.IOException;
import java.util.Timer;

import org.apache.http.client.ClientProtocolException;

import com.neotys.extensions.action.engine.Context;

import io.swagger.client.ApiClient;
import io.swagger.client.api.ResultsApi;

public class DynatracePluginData {
	private final String NEOLOAD_WEB_BASEURL="https://neoload-api.saas.neotys.com/v1/";
	private final int MAXDURATION_TIME=2000;
	
	private String DynataceAPIKEY;
	private ApiClient NeoLoadWEB_API_CLIENT;
	private String PROXYHOST;
	private String PROXYPASS;
	private String PROXYUSER;
	private String PROXYPORT;
	private Context NLContext;
	private ResultsApi NLWEBresult;
	private String TestID=null;
	private NeoLoadStatAggregator NLaggregator=null;
	private static String NL_TEST_RUNNING="RUNNING";
	private String projectname;
	private NLGlobalStat NLStat=null;
	private String Dynatrace_AccountID=null;

	private String TestName=null;
	private String DynatraceApplicationNAme;
	static final int TIMERFREQUENCY=30000;
	static final int TIMERDELAY=0;
	Timer timerDynatrace= null ;
	private final String NLHost;
	private String NL_Managed_Instance;
	private String 	Dynatrace_Managed_Hostname=null;
	
	public DynatracePluginData(String strDynatraceAPIKEY, String neoLoadWEB_APIKEY, String pROXYHOST, String pROXYPASS,
			String pROXYUSER, String pROXYPORT,Context pContext, String DynatraceID,String pNLHost,String dynatracemanaged,String nlInstance) throws ClientProtocolException, DynatraceStatException, IOException {
	
		DynataceAPIKEY = strDynatraceAPIKEY;
		Dynatrace_AccountID=DynatraceID;
		
		//----define  the NLWEB API-----
		NeoLoadWEB_API_CLIENT = new ApiClient();
		NeoLoadWEB_API_CLIENT.setApiKey(neoLoadWEB_APIKEY);
		NeoLoadWEB_API_CLIENT.setBasePath(NEOLOAD_WEB_BASEURL);
		Dynatrace_Managed_Hostname=dynatracemanaged;
		NL_Managed_Instance=nlInstance;
		InitNLAPi();
		//-------------------------
		NLContext = pContext;
		PROXYHOST = pROXYHOST;
		PROXYPASS = pROXYPASS;
		PROXYUSER = pROXYUSER;
		PROXYPORT = pROXYPORT;
		NLHost=pNLHost;
		NLStat=new NLGlobalStat();
		projectname=GetProjecName();
		TestName=GetTestName();

		if(TestID==null)
		{
			setTestID(GetTestID());
			NLStat=new NLGlobalStat();
			if(NLaggregator==null)
				NLaggregator=new NeoLoadStatAggregator(DynataceAPIKEY, projectname,Dynatrace_AccountID,NLWEBresult,TestID,NLStat,GetTestScenarioName(),TestName,NLHost,Dynatrace_Managed_Hostname,NL_Managed_Instance);
		}
	}	
	
	private void setTestID(String pTestID)
	{
		TestID=pTestID;
	}
	
	public void SetProjectName(String ProjectName)
	{
		projectname=ProjectName;
	}
	
	
	
	public DynatracePluginData(String strDynatraceAPIKEY, String neoLoadWEB_APIKEY,Context pContext,String DynatraceID,String pNLHost,String dynatracemanaged,String nlInstance) throws ClientProtocolException, DynatraceStatException, IOException {
		super();
		DynataceAPIKEY = strDynatraceAPIKEY;
		Dynatrace_AccountID=DynatraceID;
		NLContext = pContext;
		//----define  the NLWEB API-----
		NeoLoadWEB_API_CLIENT = new ApiClient();
		NeoLoadWEB_API_CLIENT.setApiKey(neoLoadWEB_APIKEY);
		NeoLoadWEB_API_CLIENT.setBasePath(NEOLOAD_WEB_BASEURL);
		NL_Managed_Instance=nlInstance;
		NLHost=pNLHost;
		Dynatrace_Managed_Hostname=dynatracemanaged;
		NLStat=new NLGlobalStat();
		InitNLAPi();
		NLStat=new NLGlobalStat();
		projectname=GetProjecName();
		TestName=GetTestName();
		if(TestID==null) {
			setTestID(GetTestID());

			if(NLaggregator==null)
				NLaggregator=new NeoLoadStatAggregator(DynataceAPIKEY, projectname,Dynatrace_AccountID,NLWEBresult,TestID,NLStat,GetTestScenarioName(),TestName,NLHost,Dynatrace_Managed_Hostname,NL_Managed_Instance);
		}
	}
	
	public void StartTimer()
	{
		timerDynatrace = new Timer();
		timerDynatrace.scheduleAtFixedRate(NLaggregator,TIMERDELAY,TIMERFREQUENCY);
	}

	public void StopTimer()
	{
		timerDynatrace.cancel();
	}
	
	public void ResumeTimer() throws ClientProtocolException, DynatraceStatException, IOException
	{
		timerDynatrace = new Timer();
		NLaggregator=new NeoLoadStatAggregator(DynataceAPIKEY, projectname,Dynatrace_AccountID,NLWEBresult,TestID,NLStat,GetTestScenarioName(),TestName,NLHost,Dynatrace_Managed_Hostname,NL_Managed_Instance);
		timerDynatrace.scheduleAtFixedRate(NLaggregator,TIMERDELAY,TIMERFREQUENCY);
	}
	
	private void InitNLAPi()
	{
		NLWEBresult=new ResultsApi(NeoLoadWEB_API_CLIENT);
	}
	
	/*private boolean IsTimeCloseEnougth(long NLWebduration)
	{
		boolean result=false;
		
		long NLduration=NLContext.getElapsedTime();
		//---convert the test NLwebduration in milliseconds
		NLWebduration=NLWebduration*1000;
		if(NLduration-NLWebduration<MAXDURATION_TIME)
			result=true;
		
		return result;
	}*/
	 private String GetTestName()
	 {
		 String ProjectName;
		 return ProjectName=NLContext.getTestName();
	 }
	 private String GetTestScenarioName()
	 {
		 String ProjectName;
		 return ProjectName=NLContext.getScenarioName();
	 }
	 private String GetProjecName()
	 {
		 String ProjectName;
		 return ProjectName=NLContext.getProjectName();
	 }

	
	private String GetTestID() 
	{
		String TestID;
		TestID=NLContext.getTestId();
		return TestID;
		
	}
}
