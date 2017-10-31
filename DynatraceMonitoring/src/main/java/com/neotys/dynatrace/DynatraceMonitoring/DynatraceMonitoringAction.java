package com.neotys.dynatrace.DynatraceMonitoring;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;

public final class DynatraceMonitoringAction implements Action{
	static final String NeoLoadAPIHost="NeoLoadAPIHost";
	static final String NeoLoadAPIport="NeoLoadAPIport";
	static final String NeoLoadKeyAPI="NeoLoadKeyAPI";
	static final String Dynatrace_ID="Dynatrace_ID";
	static final String Dynatrace_API_KEY="Dynatrace_API_KEY";
	static final String Dynatrace_ApplicationName="Tags";
	static final String HTTP_PROXY_HOST="HTTP_PROXY_HOST";
	static final String HTTP_PROXY_PORT="HTTP_PROXY_PORT";
	static final String HTTP_PROXY_LOGIN="HTTP_PROXY_LOGIN";
	static final String HTTP_PROXY_PASSWORD="HTTP_PROXY_PASSWORD";
	static final String Dynatrace_Managed_Hostname="Dynatrace_Managed_Hostname";
	static final String NL_Managed_Instance="NL_Managed_Instance";
	private static final String BUNDLE_NAME = "com.neotys.dynatrace.DynatraceMonitoring.bundle";
	private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");

	@Override
	public String getType() {
		return "DynatraceMonitoring";
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<ActionParameter>();
		//API key	
		parameters.add(new ActionParameter(Dynatrace_ID,"Dynatrace ID ( part of the url of your dynatace saas)"));
		parameters.add(new ActionParameter(Dynatrace_API_KEY,"Dynatrace API KEY"));
		parameters.add(new ActionParameter(Dynatrace_ApplicationName,"tag1,tag2"));
		parameters.add(new ActionParameter(NeoLoadAPIHost,"localhost"));
		parameters.add(new ActionParameter(NeoLoadAPIport,"7400"));
		parameters.add(new ActionParameter(NeoLoadKeyAPI,""));

		return parameters;
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return DynatraceMonitoringActionEngine.class;
	}
	
	private static final ImageIcon LOGO_ICON;
	static {
		final URL iconURL = DynatraceMonitoringAction.class.getResource("dynatrace.png");
		if (iconURL != null) {
			LOGO_ICON = new ImageIcon(iconURL);
		}
		else {
			LOGO_ICON = null;
		}
		}	
	@Override
	public Icon getIcon() {
		// TODO Add an icon
		return LOGO_ICON;
	}

	@Override
	public String getDescription() {
		final StringBuilder description = new StringBuilder();
		// TODO Add description
		description.append("DynatraceMonitoring Action will retrieve all the counters measured by Dynatrace \n")
		.append("The parameters are : \n")
		.append("Dynatrace_ID : Dynatrace id \n")
		.append("Dynatrace_API_KEY  : Dynatrace API KEY\n")
		.append("Tags  : Dyntrace Tags, Get All the metrics related to specific tags , format : tag1,tag2\n")
		.append("NeoLoadAPIHost : IP or Host of the NeoLoad controller\n")
		.append("NeoLoadAPIport : Port of the NeoLoad DataExchange API\n")
		.append("NeoLoadKeyAPI : Neoload DataExchange API key\n")
		.append("HTTP_PROXY_HOST : Optional - Host of the HTTP proxy\n")
		.append("HTTP_PROXY_PORT : Optional - Port of the HTTP proxy\n")
		.append("HTTP_PROXY_LOGIN : Optional - Account of the HTTP proxy\n")
		.append("HTTP_PROXY_PASSWORD :Optional - Password of the HTTP proxy\n")
		.append("Dynatrace_Managed_Hostname : Optional - Hostname of your managed Dynatrace environment")
		.append("NL_Managed_Instance : Optional - Hostname of your managed NeoLoad Instance");

		return description.toString();
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getDisplayPath() {
		return DISPLAY_PATH;
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.of("6.1");
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}
}
