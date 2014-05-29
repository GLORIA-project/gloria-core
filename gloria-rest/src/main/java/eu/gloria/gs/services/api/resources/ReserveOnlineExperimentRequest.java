/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.sql.Date;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
public class ReserveOnlineExperimentRequest extends
		ListAvailableTimeSlotsRequest {

	private Date begin;
	private Date end;

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

}
