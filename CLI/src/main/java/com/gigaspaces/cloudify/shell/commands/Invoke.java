package com.gigaspaces.cloudify.shell.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
import com.gigaspaces.cloudify.shell.rest.InvocationResult;

@Command(scope = "cloudify", name = "invoke", description = "invokes a custom command")
public class Invoke extends AdminAwareCommand {

	@Argument(index = 0, name = "service-name", required = true, description = " the service to invoke the command on")
	private String serviceName;

	@Argument(index = 1, name = "command-name", required = true, description = "the name of the command to invoke")
	private String commandName;

	@Option(name = "-beanname", description = "bean name")
	private String beanName = "universalServiceManagerBean";

	@Option(name = "-instanceid", description = "If provided, the command will be invoked only on that specific instance")
	private Integer instanceId;
	
	@Argument(index = 2, multiValued = true, name = "params", required = false, description = "Command Custom parameters.")
	private List<String> params = new ArrayList<String>();
	
	@Override
	protected Object doExecute() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("Invocation results: \n");
		
		String applicationName = this.getCurrentApplicationName();
		if(applicationName == null) {
			applicationName = "default";
		}
		
		Map<String, String> paramsMap = new HashMap<String, String>();
		if (params != null){
			paramsMap = getParamsMap(params);
		}
		
		if (instanceId == null) {
			Map<String, com.gigaspaces.cloudify.shell.rest.InvocationResult> result = adminFacade
					.invokeServiceCommand(applicationName, serviceName, beanName,
							commandName, paramsMap);
			
			Collection<InvocationResult> values = result.values();
			List<InvocationResult> valuesList = new ArrayList<InvocationResult>(values);
			Collections.sort(valuesList);
			
			for (InvocationResult invocationResult : valuesList) {

				sb.append( createInvocationMessage(invocationResult));

				sb.append("\n");

			}
		} else {

			InvocationResult invocationResult = adminFacade
					.invokeInstanceCommand(applicationName, serviceName, beanName,
							instanceId, commandName, paramsMap);
			sb.append( createInvocationMessage(invocationResult));
		}
		
		return sb;
	}

	//TODO: look at karaf's MultiValue option
	private Map<String, String> getParamsMap(List<String> parameters) {
		Map<String, String> returnMap = new HashMap<String, String>();
		returnMap.put(CloudifyConstants.INVOCATION_PARAMETERS_KEY, parameters.toString());
		return returnMap;
	}
	
	private String createInvocationMessage(
			InvocationResult invocationResult) {
		if (invocationResult.isSuccess()) {
			return getFormattedMessage("invocation_success", invocationResult.getInstanceId(), invocationResult.getInstanceName(), invocationResult.getResult());
		} 
		return getFormattedMessage("invocation_failed", invocationResult.getInstanceId(), invocationResult.getInstanceName(), invocationResult.getExceptionMessage());
		
	}


}
