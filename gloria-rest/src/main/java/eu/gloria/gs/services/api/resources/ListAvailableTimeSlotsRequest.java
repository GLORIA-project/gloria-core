/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.util.List;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
public class ListAvailableTimeSlotsRequest {

	private String experiment;
	private List<String> telescopes;

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public List<String> getTelescopes() {
		return telescopes;
	}

	public void setTelescopes(List<String> telescopes) {
		this.telescopes = telescopes;
	}
}
