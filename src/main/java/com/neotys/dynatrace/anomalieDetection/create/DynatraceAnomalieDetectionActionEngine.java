package com.neotys.dynatrace.anomalieDetection.create;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.neotys.action.result.ResultFactory;
import com.neotys.dynatrace.anomalieDetection.NeoLoadAnomalieDetectionApi;
import com.neotys.dynatrace.anomalieDetection.data.DynatraceAnomalies;
import com.neotys.dynatrace.common.Constants;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public class DynatraceAnomalieDetectionActionEngine implements ActionEngine {
	private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_ANOMALIE_ACTION-01";
	private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_ANOMALIE_ACTION-02";
	private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_ANOMALIE_ACTION-03";
	public static final ImmutableList<String> OPERATOR = ImmutableList.of("ABOVE", "BELOW");
	public static final ImmutableList<String> TYPE_ANOMALIE = ImmutableList.of("AVAILABILITY", "CUSTOM_ALERT", "ERROR",
			"PERFORMANCE", "RESOURCE_CONTENTION");

	@Override
	public SampleResult execute(Context context, List<ActionParameter> list) {
		final SampleResult sampleResult = new SampleResult();
		final Map<String, Optional<String>> parsedArgs;
		try {
			parsedArgs = parseArguments(list, DynatraceAnomalieDetectionOption.values());
		} catch (final IllegalArgumentException iae) {
			return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ",
					iae);
		}
		final Logger logger = context.getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("Executing " + this.getClass().getName() + " with parameters: "
					+ getArgumentLogString(parsedArgs, DynatraceAnomalieDetectionOption.values()));
		}

		final String dynatraceId = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceId.getName()).get();
		final String dynatraceApiKey = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceApiKey.getName()).get();
		final Optional<String> dynatraceManagedHostname = parsedArgs
				.get(DynatraceAnomalieDetectionOption.DynatraceManagedHostname.getName());
		final Optional<String> proxyName = parsedArgs.get(DynatraceAnomalieDetectionOption.NeoLoadProxy.getName());
		final Optional<String> optionalTraceMode = parsedArgs.get(DynatraceAnomalieDetectionOption.TraceMode.getName());
		final Optional<String> dynatraceTags = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceTags.getName());
		final Optional<String> dynatracemetric = parsedArgs
				.get(DynatraceAnomalieDetectionOption.DynatraceMetricName.getName());
		;
		Optional<String> operator = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceOperator.getName());
		;
		final Optional<String> threshold = parsedArgs
				.get(DynatraceAnomalieDetectionOption.DynatraceMericValue.getName());
		;
		Optional<String> typeOfAnomalie = parsedArgs
				.get(DynatraceAnomalieDetectionOption.DynatraceTypeofAnomalie.getName());
		boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());
		final Optional<String> jsonAnomalieDetection = parsedArgs
				.get(DynatraceAnomalieDetectionOption.JsonAnomalieDetection.getName());
		final Optional<String> jsonAnomalieDetectionFile = parsedArgs
				.get(DynatraceAnomalieDetectionOption.JsonAnomalieDetectionFile.getName());

		List<String> listofids;
		// ----------validate the value----------------

		if (!jsonAnomalieDetection.isPresent() && !jsonAnomalieDetectionFile.isPresent()) {
			if (operator.isPresent()) {
				// ----valid Operator------------------
				if (!isPartOftheList(operator.get(), OPERATOR))
					return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
							"Invalid value of Operator: " + operator, null);
			} else {
				return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
						"Invalid value of Operator: cannot be null if the json reference is not set", null);

			}

			if (threshold.isPresent()) {
				// ----valid value ---------------------
				if (!isaDigit(threshold.get()))
					return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
							"Threshold needs to be a digit: " + threshold, null);
			} else {
				return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
						"Invalid value of Threshold: cannot be null if the json reference is not set", null);

			}
			// ----valid type of anomaly------------
			if (typeOfAnomalie.isPresent()) {
				if (!isPartOftheList(typeOfAnomalie.get(), TYPE_ANOMALIE))
					return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
							"Invalid value of type of anomalie: " + typeOfAnomalie, null);
			} else {
				return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
						"Invalid value of type of anomalie: cannot be null if the json reference is not set", null);

			}
			if (!dynatracemetric.isPresent()) {
				return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
						"Invalid value of dynatracemetricname: cannot be null if the json reference is not set", null);

			}
		} else {
			if (jsonAnomalieDetection.isPresent()) {
				if (Strings.isNullOrEmpty(jsonAnomalieDetection.get())) {
					return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
							"Invalid argument: jsonAnomalieDetection cannot be null "
									+ DynatraceAnomalieDetectionOption.JsonAnomalieDetection + ".",
							null);
				}
			} else {
				if (jsonAnomalieDetectionFile.isPresent()) {
					if (Strings.isNullOrEmpty(jsonAnomalieDetectionFile.get())) {
						return ResultFactory
								.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
										"Invalid argument: jsonAnomalieDetectionFile cannot be null "
												+ DynatraceAnomalieDetectionOption.JsonAnomalieDetectionFile + ".",
										null);
					} else {
						if (!Files.exists(Paths.get(jsonAnomalieDetectionFile.get().trim()))) {
							context.getLogger().error(jsonAnomalieDetectionFile.get() + " does not exists");
							return ResultFactory
									.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER,
											"Invalid argument: jsonAnomalieDetectionFile needs to exist "
													+ DynatraceAnomalieDetectionOption.JsonAnomalieDetectionFile + ".",
											null);

						}
					}
				}
			}
		}
		try {
			listofids = (List<String>) context.getCurrentVirtualUser().get(Constants.DYNATRACE_ANOMALIES);
			if (listofids == null)
				listofids = new ArrayList<>();

			DynatraceAnomalies dynatraceAnomalies = getDynatraceAnomalie(jsonAnomalieDetectionFile,
					jsonAnomalieDetection);
			NeoLoadAnomalieDetectionApi anomalieDetectionApi = new NeoLoadAnomalieDetectionApi(dynatraceApiKey,
					dynatraceId, dynatraceManagedHostname, proxyName, context, traceMode);

			if (dynatraceAnomalies != null) {
				List<String> templistid = dynatraceAnomalies.getDynatraceAnomalieList().stream()
						.map(dynatraceAnomalie -> {
							try {
								String id = anomalieDetectionApi.createAnomalie(
										dynatraceAnomalie.getDynatraceMetricName(), dynatraceAnomalie.getOperator(),
										dynatraceAnomalie.getTypeOfAnomalie(), dynatraceAnomalie.getValue(),
										dynatraceTags);
								if (id != null) {
									return id;
								} else
									return null;
							} catch (Exception e) {
								context.getLogger().error("Technical error while creation Anomalies : ", e);
								return null;
							}
						}).filter(Objects::nonNull).collect(Collectors.toList());

				listofids = new ArrayList<String>(listofids);
				listofids.addAll(templistid);
				context.getCurrentVirtualUser().put(Constants.DYNATRACE_ANOMALIES, listofids);
			} else {
				String id = anomalieDetectionApi.createAnomalie(dynatracemetric.get(), operator.get().toUpperCase(),
						typeOfAnomalie.get().toUpperCase(), threshold.get(), dynatraceTags);
				if (id != null) {
					listofids.add(id);
					context.getCurrentVirtualUser().put(Constants.DYNATRACE_ANOMALIES, listofids);
				}
			}
		} catch (Exception e) {
			return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error: ", e);
		}
		return sampleResult;
	}

	private DynatraceAnomalies getDynatraceAnomalie(Optional<String> jsonAnomalieDetectionFile,
			Optional<String> jsonAnomalieDetection) throws FileNotFoundException {
		Gson gson = new Gson();

		if (jsonAnomalieDetectionFile.isPresent()) {
			JsonReader reader = new JsonReader(new FileReader(jsonAnomalieDetectionFile.get().trim()));
			return gson.fromJson(reader, DynatraceAnomalies.class);
		} else {
			if (jsonAnomalieDetection.isPresent()) {
				return gson.fromJson(jsonAnomalieDetection.get(), DynatraceAnomalies.class);

			}
		}
		return null;

	}

	private boolean isaDigit(String numeric) {
		try {
			Integer.parseInt(numeric);
			return true;
		} catch (NumberFormatException e) {
			return false;
		} catch (Exception e) {
			return false;
		}

	}

	private boolean isPartOftheList(String key, ImmutableList<String> list) {
		if (list.contains(key.toUpperCase()))
			return true;
		else
			return false;
	}

	@Override
	public void stopExecute() {

	}
}
