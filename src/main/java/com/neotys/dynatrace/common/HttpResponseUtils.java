package com.neotys.dynatrace.common;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import static com.neotys.dynatrace.common.HTTPGeneratorUtils.getStringResponse;
import static com.neotys.dynatrace.common.HTTPGeneratorUtils.isJsonContent;

public class HttpResponseUtils {

    public static boolean isSuccessHttpCode(final int httpCode) {
        return httpCode >= HttpStatus.SC_OK
                && httpCode <= HttpStatus.SC_MULTI_STATUS;
    }

    public static JSONArray getJsonArrayResponse(final HttpResponse httpResponse) throws IOException {
        if (isJsonContent(httpResponse)) {
            final String stringResponse = getStringResponse(httpResponse);
            if (stringResponse != null) {
                return new JSONArray(stringResponse);
            }
        }
        return null;
    }

    public static JSONObject getJsonResponse(final HttpResponse response) throws IOException {
        if (isJsonContent(response)) {
            final String stringResponse = getStringResponse(response);
            if (stringResponse != null) {
                return new JSONObject(stringResponse);
            }
        }
        return null;
    }
}
