package com.neotys.dynatrace.common.topology;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.extensions.action.engine.Context;

import static com.neotys.dynatrace.common.Constants.*;

/*
 * TOPOLOGY WALKER
 * 
 * Topology discovery modes
 * 	- TAGGED : find by Tags only ; Test administrator is responsible for tagging Services, Process Group Instances, Process Groups, Hosts
 *  - TOPDOWN : find Tagged Services and their supporting infrastructure ; Test administrator is responsible for tagging Services, discovery will identify Process Group Instances, Process Groups, Hosts
 *  - FULL : perform complete discovery ; Test administrator should only tag entry-level Services, discovery will identify called services and supporting infrastructure
 *  
*/

enum Discovery { FULL, TOPDOWN, TAGGED };

public class DynatraceTopologyWalker {
	
	private Context context = null;
	private DynatraceContext dynatracecontext = null;
	private boolean tracemode = false;
	private DynatraceTopologyCache dynatracetopologycache = new DynatraceTopologyCache();
	
	private Discovery discovery=Discovery.FULL;

	public DynatraceTopologyWalker(final Context context, final DynatraceContext dynatracecontext, final boolean tracemode/*, final Optional<String> discoverymode*/) {
		this.context = context;
		this.dynatracecontext = dynatracecontext;
		this.tracemode = tracemode;
/*
		if (discoverymode.isPresent()){
			String discoveryparam=discoverymode.get();
			if (discoveryparam.equalsIgnoreCase("TAGGED")){
				 this.discovery=Discovery.FULL;
			} else if (discoveryparam.equalsIgnoreCase("TOPDOWN")) {
				 this.discovery=Discovery.TOPDOWN;
			} // else if : this.discovery=Discovery.FULL
		}	
*/		
	}

	public void executeDiscovery() throws Exception {

		Set<String> scopedservices = findServiceEntitiesByTags();	// also populates service json cache

		if (this.discovery == Discovery.TAGGED) {
			findPgInstancesByTags();								// also populates pginstance json cache
			findProcessGroupsByTags();								// also populates processgroup displayname cache
			findHostsByTags();										// also populates host displayname cache
		} else { // FULL or TOPDOWN
			if (this.discovery == Discovery.FULL) {

				Set<String> currentservicesforevaluation = scopedservices;
				scopedservices = new HashSet<>();

				int size = currentservicesforevaluation.size();
				while (size > 0) {
					Set<String> newservicesforevaluation = new HashSet<String>();
					for (String evalservice : currentservicesforevaluation) {
						if (!scopedservices.contains(evalservice)) {
							try {
								newservicesforevaluation.addAll(findCalledServices(evalservice));  // also populates service json cache
							} catch (Exception e) {
								e.printStackTrace();
							}
							scopedservices.add(evalservice);
						}
					}
					size = newservicesforevaluation.size();
					currentservicesforevaluation = newservicesforevaluation;
				}
			} // end if FULL

			// TOPDOWN + FULL

			Set<String> scopedpginstances = new HashSet<>();
			for (String serviceid : scopedservices) {
				scopedpginstances.addAll(findPgInstancesForService(serviceid)); // uses service json cache to get pgi-ids
			}
			
			// preload pginstance json cache (single API call to fetch all process group instance details)
			findPgInstancesByIds(scopedpginstances);
			
			Set<String> scopedprocessgroups = new HashSet<>();
			Set<String> scopedhosts = new HashSet<>();
			for (String pgiid : scopedpginstances) {
				scopedprocessgroups.add(findProcessGroupForPgi(pgiid)); // uses pginstance json cache to get pgi-ids // this does NOT populate pgroup - displayname cache
				scopedhosts.add(findHostForPgi(pgiid));                 // uses pginstance json cache to get pgi-ids // this does NOT populate host - displayname cache
			}

			// preload displaynames cache (single API call each to fetch all process group / host details)
			findProcessGroupsByIds(scopedprocessgroups);
			findHostsByIds(scopedhosts);
			
			
		} // end if TOPDOWN|FULL
		
		dynatracetopologycache.setDiscoverycompleted(true);
	}

// Services Discovery
	private Set<String> findServiceEntitiesByTags() throws Exception {
		final MultivaluedMap<String, String> parameters = DynatraceUtils.generateGetTagsParameters(dynatracecontext.getTags(), true);		
		Set<String> serviceEntityIds = new HashSet<String>();

		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_SERVICE, parameters, tracemode);
		if (jsonArrayResponse != null) {
			serviceEntityIds = extractServiceEntityIdsFromResponse(jsonArrayResponse);
		}

		if (tracemode) {
			context.getLogger().info("Found services: " + serviceEntityIds);
		}
		return serviceEntityIds;
	}

	private Set<String> extractServiceEntityIdsFromResponse(final JSONArray jsonArrayResponse) {
		Set<String> serviceentityidset = new HashSet<>();
		for (int i = 0; i < jsonArrayResponse.length(); i++) {
			final JSONObject jsonentity = jsonArrayResponse.getJSONObject(i);
			if (jsonentity.has("entityId") && jsonentity.has("displayName")) {
				String entityid = jsonentity.getString("entityId");
				dynatracetopologycache.addService(entityid, jsonentity);
				serviceentityidset.add(entityid);
			}
		}
		return serviceentityidset;
	}

	private JSONObject findServiceEntityById(String serviceid) throws Exception {
		JSONObject svc = dynatracetopologycache.lookupService(serviceid);
		if (svc == null) {
			final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
			final Set<String> serviceEntityIds = new HashSet<String>();
			
			final JSONObject jsonObjectResponse = DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, DTAPI_ENV1_EP_SERVICE + "/" + serviceid, parameters, tracemode);
			if (jsonObjectResponse != null) {
				dynatracetopologycache.addService(serviceid, jsonObjectResponse);
				svc=jsonObjectResponse;
			}			
			if (tracemode) {
				context.getLogger().info("Found service: " + serviceEntityIds);
			}
		}
		return svc;
	}

	private Set<String> findCalledServices(String serviceid) throws Exception {
		Set<String> calledservices = new HashSet<>();
		JSONObject svc = findServiceEntityById(serviceid);

		JSONObject fromRelationships = svc.getJSONObject("fromRelationships");
		try {
			JSONArray services = fromRelationships.getJSONArray("calls");
			for (int j = 0; j < services.length(); j++) {
				calledservices.add(services.getString(j));
			}
		} catch (JSONException e) {
			// TODO ----print the exception
		}
		return calledservices;
	}	

// Process Group Instance Discovery
	public Set<String> findPgInstancesForService(String serviceid) throws Exception {
		Set<String> runningpginstances = new HashSet<>();
		JSONObject svc = findServiceEntityById(serviceid);

		JSONObject fromRelationships = svc.getJSONObject("fromRelationships");
		try {
			JSONArray pginstances = fromRelationships.getJSONArray("runsOnProcessGroupInstance");
			for (int j = 0; j < pginstances.length(); j++) {
				runningpginstances.add(pginstances.getString(j));
			}
		} catch (JSONException e) {
			// TODO ----print the exception
		}
		return runningpginstances;
	}

	
	private Set<String> findPgInstancesByTags() throws Exception {
		final MultivaluedMap<String, String> parameters = DynatraceUtils.generateGetTagsParameters(dynatracecontext.getTags(), true);		
		Set<String> pginstanceids = new HashSet<String>();

		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_PROCESS, parameters, tracemode);
		if (jsonArrayResponse != null) {
			pginstanceids = extractPGInstanceIdsFromResponse(jsonArrayResponse);
		}

		if (tracemode) {
			context.getLogger().info("Found process group instances: " + pginstanceids);
		}
		return pginstanceids;
	}

	private Set<String> findPgInstancesByIds(Set<String> pgiids) throws Exception {
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
		for (String pgiid : pgiids) {
			DynatraceUtils.addGetParameterEntity(parameters,pgiid);
		}
		Set<String> pginstanceids = new HashSet<String>();
		
		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_PROCESS, parameters, tracemode);
		if (jsonArrayResponse != null) {
			pginstanceids = extractPGInstanceIdsFromResponse(jsonArrayResponse);
		}
		
		if (tracemode) {
			context.getLogger().info("Found process group instances: " + pginstanceids);
		}
		
		// assert [ pgiids == pginstanceids ]
		return pginstanceids;
	}

	private JSONObject findPgInstanceById(String pgiid) throws Exception {
		JSONObject pgi = dynatracetopologycache.lookupProcessGroupInstance(pgiid);
		if (pgi == null) {
			Set<String> fetchpgi=new HashSet<>();
			fetchpgi.add(pgiid);
			findPgInstancesByIds(fetchpgi);									// fetch and populate cache
			pgi = dynatracetopologycache.lookupProcessGroupInstance(pgiid); // get pgi json object from cache
		}
		return pgi;		
	}
	
	private Set<String> extractPGInstanceIdsFromResponse(final JSONArray jsonArrayResponse) {
		Set<String> pginstanceidset = new HashSet<>();
		for (int i = 0; i < jsonArrayResponse.length(); i++) {
			final JSONObject jsonpgi = jsonArrayResponse.getJSONObject(i);
			if (jsonpgi.has("entityId") && jsonpgi.has("displayName")) {
				String pgiid = jsonpgi.getString("entityId");
				dynatracetopologycache.addProcessGroupInstance(pgiid, jsonpgi);
				pginstanceidset.add(pgiid);
			}
		}
		return pginstanceidset;
	}
	
// Process Group Discovery		
	private String findProcessGroupForPgi(String pgiid) throws Exception {
		JSONObject pgi = findPgInstanceById(pgiid);

		JSONObject fromrelationships = pgi.getJSONObject("fromRelationships");
		try {
			JSONArray processgroups = fromrelationships.getJSONArray("isInstanceOf");
			//assert [ processgroups.length() == 1 ]
			return processgroups.getString(0);
		} catch (JSONException e) {
			// TODO ----print the exception
		}
		return null;
	}

	private Set<String> findProcessGroupsByTags() throws Exception {
		final MultivaluedMap<String, String> parameters = DynatraceUtils.generateGetTagsParameters(dynatracecontext.getTags(), true);
		Set<String> pgroupids = new HashSet<String>();

		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_PROCESSGROUP, parameters, tracemode);
		if (jsonArrayResponse != null) {
			pgroupids = extractProcessGroupIdsFromResponse(jsonArrayResponse);
		}
		
		if (tracemode) {
			context.getLogger().info("Found process groups: " + pgroupids);
		}
		return pgroupids;
	}

	private Set<String> findProcessGroupsByIds(Set<String> pgids) throws Exception {
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();		
		for (String pgid : pgids) {
			DynatraceUtils.addGetParameterEntity(parameters,pgid);
		}
		Set<String> pgroupids = new HashSet<String>();

		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_PROCESSGROUP, parameters, tracemode);
		if (jsonArrayResponse != null) {
			pgroupids = extractProcessGroupIdsFromResponse(jsonArrayResponse);
		}
		
		if (tracemode) {
			context.getLogger().info("Found process groups: " + pgroupids);
		}
		// assert [ pgids == pgroupids ]
		return pgroupids;
	}
		
	private Set<String> extractProcessGroupIdsFromResponse(final JSONArray jsonArrayResponse) {
		Set<String> pgroupidset = new HashSet<>();
		for (int i = 0; i < jsonArrayResponse.length(); i++) {
			final JSONObject jsonpg = jsonArrayResponse.getJSONObject(i);
			if (jsonpg.has("entityId") && jsonpg.has("displayName")) {
				String pgid = jsonpg.getString("entityId");
				dynatracetopologycache.addProcessGroup(pgid,jsonpg.getString("displayName"));
				pgroupidset.add(pgid);
			}
		}
		return pgroupidset;
	}
		
// Host Discovery
	private String findHostForPgi(String pgiid) throws Exception {
		JSONObject pgi = findPgInstanceById(pgiid);

		JSONObject fromrelationships = pgi.getJSONObject("fromRelationships");
		try {
			JSONArray hosts = fromrelationships.getJSONArray("isProcessOf");
			//assert that hosts.length() == 1
			return hosts.getString(0);
		} catch (JSONException e) {
			// TODO ----print the exception
		}
		return null;
	}
	
	private Set<String> findHostsByTags() throws Exception {
		final MultivaluedMap<String, String> parameters = DynatraceUtils.generateGetTagsParameters(dynatracecontext.getTags(), true);
		Set<String> hostids = new HashSet<String>();

		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_HOST, parameters, tracemode);
		if (jsonArrayResponse != null) {
			hostids = extractHostIdsFromResponse(jsonArrayResponse);
		}

		if (tracemode) {
			context.getLogger().info("Found hosts: " + hostids);
		}
		return hostids;
	}

	private Set<String> findHostsByIds(Set<String> hids) throws Exception {
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();		
		for (String hostid : hids) {
			DynatraceUtils.addGetParameterEntity(parameters,hostid);
		}
		
		Set<String> hostids = new HashSet<String>();

		final JSONArray jsonArrayResponse = DynatraceUtils.executeDynatraceAPIGetArrayRequest(context, dynatracecontext, DTAPI_ENV1_EP_HOST, parameters, tracemode);
		if (jsonArrayResponse != null) {
			hostids = extractHostIdsFromResponse(jsonArrayResponse);
		}

		if (tracemode) {
			context.getLogger().info("Found hosts: " + hostids);
		}
		// assert [ hids == hostids ]
		return hostids;
	}
	
	private Set<String> extractHostIdsFromResponse(final JSONArray jsonArrayResponse) {
		Set<String> hostidset = new HashSet<>();
		for (int i = 0; i < jsonArrayResponse.length(); i++) {
			final JSONObject jsonhost = jsonArrayResponse.getJSONObject(i);
			if (jsonhost.has("entityId") && jsonhost.has("displayName")) {
				String hostid = jsonhost.getString("entityId");
				dynatracetopologycache.addHost(hostid,jsonhost.getString("displayName"));
				hostidset.add(hostid);
			}
		}
		return hostidset;
	}
	
// Data accessors
	public DynatraceTopologyCache getDiscoveredData(){
		return dynatracetopologycache;
	}

}
