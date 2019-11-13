package com.neotys.dynatrace.common.topology;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;

public class DynatraceTopologyCache {
	private boolean discoverycompleted=false;

	private HashMap<String,JSONObject> servicecache   =new HashMap<>();
	private HashMap<String,JSONObject> pginstancecache=new HashMap<>();
	private HashMap<String,String>     pgroupcache    =new HashMap<>();
	private HashMap<String,String>     hostcache      =new HashMap<>();
	
	public boolean isDiscoverycompleted() {
		return discoverycompleted;
	}

	public void setDiscoverycompleted(boolean discoverycompleted) {
		this.discoverycompleted = discoverycompleted;
	}

	public void addService(String id,JSONObject service){
		servicecache.put(id,service);
	}

	public JSONObject lookupService(String id){
		return servicecache.get(id);
	}

	public String lookupServiceName(String id){
		if (servicecache.containsKey(id))
			return servicecache.get(id).getString("displayName");
		return null;
	}
	
	public Set<String> getServices(){
		return servicecache.keySet();
	}

	public void addProcessGroupInstance(String id,JSONObject pginstance){
		pginstancecache.put(id,pginstance);
	}

	public JSONObject lookupProcessGroupInstance(String id){
		return pginstancecache.get(id);
	}

	public Set<String> getProcessGroupInstances(){
		return pginstancecache.keySet();
	}

	public void addProcessGroup(String id,String dname){
		pgroupcache.put(id,dname);
	}

	public String lookupProcessGroupName(String id){
		return pgroupcache.get(id);
	}

	public Set<String> getProcessGroups(){
		return pgroupcache.keySet();
	}

	public void addHost(String id,String dname){
		hostcache.put(id,dname);
	}

	public String lookupHostName(String id){
		return hostcache.get(id);
	}

	public Set<String> getHosts(){
		return hostcache.keySet();
	}

}
