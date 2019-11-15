package com.neotys.dynatrace.common;

import com.google.common.base.Optional;

/**
 * Created by anouvel on 20/12/2017.
 */
public class DynatraceContext {
	private final String apiKey;
	private final Optional<String> dynatraceManagedHostname;
	private final String dynatraceAccountID;
	private final Optional<String> proxyname;
	private final Optional<String> tags;

	public DynatraceContext(final String apiKey,
							final Optional<String> dynatraceManagedHostname,
							final String dynatraceAccountID,
							final Optional<String> proxyname,
							final Optional<String> tags) {
		this.apiKey = apiKey;
		this.dynatraceManagedHostname = dynatraceManagedHostname;
		this.dynatraceAccountID = dynatraceAccountID;
		this.proxyname = proxyname;
		this.tags = tags;
	}

	public String getApiKey() {
		return apiKey;
	}

	public Optional<String> getDynatraceManagedHostname() {
		return dynatraceManagedHostname;
	}

	public String getDynatraceAccountID() {
		return dynatraceAccountID;
	}

	public Optional<String> getProxyname() {
		return proxyname;
	}

	public Optional<String> getTags() {
		return tags;
	}	
}
