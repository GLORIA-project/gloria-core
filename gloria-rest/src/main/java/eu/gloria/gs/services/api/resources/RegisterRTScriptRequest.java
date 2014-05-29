package eu.gloria.gs.services.api.resources;

import eu.gloria.gs.services.experiment.ScriptSlot;

public class RegisterRTScriptRequest {

	private String experiment;
	private String operation;
	private ScriptSlot slot;
	private Object init;
	private Object result;
	private boolean notify;

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public Object getInit() {
		return init;
	}

	public void setInit(Object init) {
		this.init = init;
	}

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public ScriptSlot getSlot() {
		return slot;
	}

	public void setSlot(ScriptSlot slot) {
		this.slot = slot;
	}
}
