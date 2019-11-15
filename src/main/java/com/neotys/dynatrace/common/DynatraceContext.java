package com.neotys.dynatrace.common;

import com.google.common.base.Optional;

import java.util.Map;

/**
 * Created by anouvel on 20/12/2017.
 */
public class DynatraceContext {
	private final String apiKey;
	private final Optional<String> dynatraceManagedHostname;
	private final String dynatraceAccountID;
	private final Optional<String> proxyname;
	private final Optional<String> tags;
// TODO remove headers from this object	
	@Deprecated
	private final Map<String, String> headers;

	public DynatraceContext(final String apiKey,
							final Optional<String> dynatraceManagedHostname,
							final String dynatraceAccountID,
							final Optional<String> proxyname,
							final Optional<String> tags,
							final Map<String, String> headers) {
		this.apiKey = apiKey;
		this.dynatraceManagedHostname = dynatraceManagedHostname;
		this.dynatraceAccountID = dynatraceAccountID;
		this.proxyname = proxyname;
		this.tags = tags;
		this.headers = headers;
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
	
	@Deprecated
	public Map<String, String> getHeaders() {
		return headers;
	}
}
