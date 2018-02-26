package com.neotys.dynatrace.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import static com.neotys.dynatrace.common.HTTPGeneratorUtils.convertStreamToString;
import static com.neotys.dynatrace.common.HTTPGeneratorUtils.isJsonContent;

public class HttpResponseUtils {

	public static boolean isSuccessHttpCode(final int httpCode) {
		return httpCode >= HttpStatus.SC_OK
				&& httpCode <= HttpStatus.SC_MULTI_STATUS;
	}

	public static String getStringResponse(final HttpResponse resp) throws IOException {
		final HttpEntity entity = resp.getEntity();
		if (entity != null) {
			// A Simple JSON Response Read
			try (final InputStream inputStream = entity.getContent()) {
				return convertStreamToString(inputStream);
			}
		}
		return null;
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
