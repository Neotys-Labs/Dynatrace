package com.neotys.dynatrace.common;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;

/**
 * Created by anouvel on 20/12/2017.
 */
public class DynatraceUtils {
	private static final String DYNATRACE_URL = ".live.dynatrace.com/api/v1/";
	private static final String DYNATRACE_APPLICATION = "entity/services";
	private static final String DYNATRACE_PROTOCOL = "https://";

	private static final ImageIcon DYNATRACE_ICON;

	static {
		final URL iconURL = DynatraceUtils.class.getResource("dynatrace.png");
		if (iconURL != null) {
			DYNATRACE_ICON = new ImageIcon(iconURL);
		} else {
			DYNATRACE_ICON = null;
		}
	}

	private DynatraceUtils(){
	}

	public static ImageIcon getDynatraceIcon() {
		return DYNATRACE_ICON;
	}

	public static List<String> getApplicationEntityId(final Context context, final DynatraceContext dynatraceContext, final Optional<String> proxyName)
			throws Exception {
		final String tags = getTags(dynatraceContext.getTags());
		final String dynatraceUrl = getDynatraceApiUrl(dynatraceContext.getDynatraceManagedHostname(), dynatraceContext.getDynatraceAccountID()) + DYNATRACE_APPLICATION;
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("tag", tags);
		parameters.put("Api-Token", dynatraceContext.getApiKey());

		context.getLogger().debug("Getting application...");

		final Optional<Proxy> proxy = getProxy(context, proxyName, dynatraceUrl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_GET_METHOD, dynatraceUrl, dynatraceContext.getHeaders(), parameters, proxy);
		final List<String> applicationEntityId = new ArrayList<>();
		try {
			final JSONArray jsonArrayResponse = http.executeAndGetJsonArrayResponse();
			if (jsonArrayResponse != null) {
				extractApplicationEntityIdFromResponse(applicationEntityId, jsonArrayResponse);
			} else {
				throw new DynatraceException("No Application found in the Dynatrace Account with the name " + dynatraceContext.getTags().or(""));
			}
		} finally {
			http.closeHttpClient();
		}

		if (context.getLogger().isDebugEnabled()) {
			context.getLogger().debug("Found applications: " + applicationEntityId);
		}

		return applicationEntityId;
	}

	private static void extractApplicationEntityIdFromResponse(final List<String> applicationEntityId, final JSONArray jsonArrayResponse) {
		for (int i = 0; i < jsonArrayResponse.length(); i++) {
			final JSONObject jsonApplication = jsonArrayResponse.getJSONObject(i);
			if (jsonApplication.has("entityId") && jsonApplication.has("displayName")) {
				applicationEntityId.add(jsonApplication.getString("entityId"));
			}
		}
	}

	public static String getTags(final Optional<String> tags) {
		if (tags.isPresent()) {
			final StringBuilder result = new StringBuilder();
			final String tagsAsString = tags.get();
			if (tagsAsString.contains(",")) {
				final String[] tagsArray = tagsAsString.split(",");
				for (String tag : tagsArray) {
					result.append(tag).append("AND");
				}
				return result.substring(0, result.length() - 3);
			} else {
				return tagsAsString;
			}
		}
		return "";
	}

	public static Optional<Proxy> getProxy(final Context context, final Optional<String> proxyName, final String url) throws MalformedURLException {
		if (proxyName.isPresent()) {
			return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
		}
		return Optional.absent();
	}

	public static String getDynatraceApiUrl(final Optional<String> dynatraceManagedHostname, final String dynatraceAccountID) {
		if (dynatraceManagedHostname.isPresent()) {
			return DYNATRACE_PROTOCOL + dynatraceManagedHostname.get() + "/api/v1/";
		} else {
			return DYNATRACE_PROTOCOL + dynatraceAccountID + DYNATRACE_URL;
		}
	}

	public static boolean isSuccessHttpCode(final int httpCode) {
		return httpCode >= HttpStatus.SC_OK
				&& httpCode <= HttpStatus.SC_MULTI_STATUS;
	}
}
