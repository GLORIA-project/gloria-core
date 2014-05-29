package eu.gloria.gs.services.api.resources;

public class NewParameterRequest {

	private String type;
	private Object init;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getInit() {
		return init;
	}

	public void setInit(Object init) {
		this.init = init;
	}

}
