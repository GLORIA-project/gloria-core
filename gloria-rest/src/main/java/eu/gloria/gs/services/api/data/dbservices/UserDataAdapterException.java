package eu.gloria.gs.services.api.data.dbservices;

import eu.gloria.gs.services.log.action.Action;
import eu.gloria.gs.services.log.action.ActionException;

public class UserDataAdapterException extends ActionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserDataAdapterException(Action action) {
		super(action);
	}

	public UserDataAdapterException() {
		super();
	}

}
