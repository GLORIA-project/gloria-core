package eu.gloria.gs.services.api.resources;

import java.util.LinkedHashMap;

import eu.gloria.gs.services.experiment.base.data.OperationInformation;

public class RegisterExperimentRequest {

	private LinkedHashMap<String, NewParameterRequest> parameters;
	private LinkedHashMap<String, OperationInformation> operations;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LinkedHashMap<String, NewParameterRequest> getParameters() {
		return parameters;
	}

	public void setParameters(
			LinkedHashMap<String, NewParameterRequest> parameters) {
		this.parameters = parameters;
	}

	public LinkedHashMap<String, OperationInformation> getOperations() {
		return operations;
	}

	public void setOperations(
			LinkedHashMap<String, OperationInformation> operations) {
		this.operations = operations;
	}
}
