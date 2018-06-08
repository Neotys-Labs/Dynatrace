package com.neotys.dynatrace.common;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpResponse;
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
import static com.neotys.dynatrace.common.HttpResponseUtils.getJsonArrayResponse;

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

    private DynatraceUtils() {
    }

    public static ImageIcon getDynatraceIcon() {
        return DYNATRACE_ICON;
    }

    public static List<String> getApplicationEntityIds(final Context context, final DynatraceContext dynatraceContext, final Optional<String> proxyName)
            throws Exception {
        final String dynatraceUrl = getDynatraceApiUrl(dynatraceContext.getDynatraceManagedHostname(), dynatraceContext.getDynatraceAccountID()) + DYNATRACE_APPLICATION;
        final Map<String, String> parameters = new HashMap<>();
        if(dynatraceContext.getTags().isPresent()) {
            parameters.put("tag", dynatraceContext.getTags().get());
        }
        parameters.put("Api-Token", dynatraceContext.getApiKey());

        context.getLogger().debug("Getting application...");

        final Optional<Proxy> proxy = getProxy(context, proxyName, dynatraceUrl);
        final HTTPGenerator http = new HTTPGenerator(HTTP_GET_METHOD, dynatraceUrl, dynatraceContext.getHeaders(), parameters, proxy);
        final List<String> applicationEntityIds = new ArrayList<>();
        try {
            final HttpResponse httpResponse = http.execute();

            if (HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
                final JSONArray jsonArrayResponse = getJsonArrayResponse(httpResponse);
                if (jsonArrayResponse != null) {
                    extractApplicationEntityIdsFromResponse(applicationEntityIds, jsonArrayResponse);
                }
            } else {
                final String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
                throw new DynatraceException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ dynatraceUrl + " - " + stringResponse);
            }
        } finally {
            http.closeHttpClient();
        }

        if (context.getLogger().isDebugEnabled()) {
            context.getLogger().debug("Found applications: " + applicationEntityIds);
        }

        return applicationEntityIds;
    }


    private static void extractApplicationEntityIdsFromResponse(final List<String> applicationEntityId, final JSONArray jsonArrayResponse) {
        for (int i = 0; i < jsonArrayResponse.length(); i++) {
            final JSONObject jsonApplication = jsonArrayResponse.getJSONObject(i);
            if (jsonApplication.has("entityId") && jsonApplication.has("displayName")) {
                applicationEntityId.add(jsonApplication.getString("entityId"));
            }
        }
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
}
