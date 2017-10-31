package com.neotys.dynatrace.DynatraceEvents;

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

public final class DynatraceEventAction implements Action{
	private static final String BUNDLE_NAME = "com.neotys.dynatrace.DynatraceEvents.bundle";
	private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");
	static final String Dynatrace_ID="Dynatrace_ID";
	static final String Dynatrace_API_KEY="Dynatrace_API_KEY";
	static final String Dynatrace_ApplicationName="Tags";
	static final String HTTP_PROXY_HOST="HTTP_PROXY_HOST";
	static final String HTTP_PROXY_PORT="HTTP_PROXY_PORT";
	static final String HTTP_PROXY_LOGIN="HTTP_PROXY_LOGIN";
	static final String HTTP_PROXY_PASSWORD="HTTP_PROXY_PASSWORD";
	static final String Dynatrace_Managed_Hostname="Dynatrace_Managed_Hostname";
	static final String EventStatus="EventSatus";
	static final String NL_Managed_Instance="NL_Managed_Instance";
	@Override
	public String getType() {
		return "DynatraceEvent";
	}
	private static final ImageIcon LOGO_ICON;
	static {
		final URL iconURL = DynatraceEventAction.class.getResource("dynatrace.png");
		if (iconURL != null) {
			LOGO_ICON = new ImageIcon(iconURL);
		}
		else {
			LOGO_ICON = null;
		}
		}	
	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<ActionParameter>();
		parameters.add(new ActionParameter(Dynatrace_ID,"Dynatrace ID ( part of the url of your dynatace saas)"));
		parameters.add(new ActionParameter(Dynatrace_API_KEY,"Dynatrace API KEY"));
		parameters.add(new ActionParameter(Dynatrace_ApplicationName,"tag1,tag2"));
		parameters.add(new ActionParameter(EventStatus,"START"));
		
		// TODO Add default parameters.
		return parameters;
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return DynatraceEventStopActionEngine.class;
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
		description.append("DynatraceEvent Action will retrieve all the counters mesaured by Dynatrace \n")
		.append("The parameters are : \n")
		.append("Dynatrace_ID : Dynatrace id \n")
		.append("Dynatrace_API_KEY  : Dynatrace API KEY\n")
		.append("Tags  : Dynatrace tags. link the event to all servies having the specific tags ( format: tag1,tag2\n")
		.append("EventStatus :  START OR STOP")
		.append("HTTP_PROXY_HOST : Optional - Host of the HTTP proxy\n")
		.append("HTTP_PROXY_PORT : Optional - Port of the HTTP proxy\n")
		.append("HTTP_PROXY_LOGIN : Optional - Account of the HTTP proxy\n")
		.append("HTTP_PROXY_PASSWORD :Optional - Password of the HTTP proxy\n")
		.append("Dynatrace_Managed_Hostname : Optional - Hostname of your managed Dynatrace environement")
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
