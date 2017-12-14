package com.neotys.dynatrace.DynatraceMonitoring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DynatraceMonitoringActionTest {
	@Test
	public void shouldReturnType() {
		final DynatraceMonitoringAction action = new DynatraceMonitoringAction();
		assertEquals("DynatraceMonitoring", action.getType());
	}


/*@Test
public void TestAPI()
{
	final DynatraceMonitoringActionEngine action = new DynatraceMonitoringActionEngine();
	List<ActionParameter> parameters = new ArrayList<>();
	parameters.add(new ActionParameter("Dynatrace_API_KEY","t3FNohlfQNiuNPm0ILGLU"));
	parameters.add(new ActionParameter("Dynatrace_ApplicationName","My web application"));
	parameters.add(new ActionParameter("Dynatrace_ID","pjk26067"));
	parameters.add(new ActionParameter("NeoLoadAPIHost","localhost"));
	parameters.add(new ActionParameter("NeoLoadAPIport","7400"));
	parameters.add(new ActionParameter("NeoLoadKeyAPI",""));
	parameters.add(new ActionParameter("NeoLoadLocation","Gemenos"));
//	parameters.add(new ActionParameter("HTTP_PROXY_HOST","127.0.0.1"));
//	parameters.add(new ActionParameter("HTTP_PROXY_PORT","8888"));

	action.execute(Mockito.mock(Context.class), parameters);
	
	
}
@Test
public void HttpGeneratorTest() throws ClientProtocolException, IOException {

	final HashMap<String, String> Parameters = new HashMap();
	Parameters.put("Api-Token", "t3FNohlfQNiuNPm0ILGLU");
	
	final HTTPGenerator http = new HTTPGenerator("https://pjk26067.live.dynatrace.com/api/v1/entity/applications", "GET", new HashMap<String,String>(),Parameters);
	JSONArray jsoobj=http.getJSONArrayHTTPresponse();
	System.out.println("jsoobj: "+ jsoobj);

}
@Test
public void TestNLDATA() throws ClientProtocolException, DynatraceStatException, IOException 
{
	DynatracePluginData data;
	data= new DynatracePluginData("t3FNohlfQNiuNPm0ILGLU","15304f743f34ca33c458927a40945b7424a2066b95563774",Mockito.mock(Context.class),"pjk26067","My web application","localhost");
	
	data.StartTimer();
	
}*/



}
//t3FNohlfQNiuNPm0ILGLU
//id ;pjk
