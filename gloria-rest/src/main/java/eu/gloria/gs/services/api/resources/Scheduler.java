/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.spi.resource.Singleton;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.scheduler.SchedulerException;
import eu.gloria.gs.services.scheduler.SchedulerInterface;
import eu.gloria.gs.services.scheduler.brain.InvalidObservingPlanException;
import eu.gloria.gs.services.scheduler.brain.MaxUserSchedulesException;
import eu.gloria.gs.services.scheduler.data.ObservingPlanInformation;
import eu.gloria.gs.services.scheduler.data.ScheduleInformation;
import eu.gloria.gs.services.scheduler.data.ScheduleNotFoundException;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */

@Singleton
@Path("/scheduler")
public class Scheduler extends GResource {

	public Scheduler() {
		super(Scheduler.class.getSimpleName());
	}

	@Context
	HttpServletRequest request;

	private static SchedulerInterface scheduler = GSClientProvider
			.getSchedulerClient();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/plans/{id}")
	public Response getOPInformation(@PathParam("id") int id) {

		this.setupRegularAuthorization(request);

		try {

			ScheduleInformation schInfo = scheduler.getScheduleInformation(id);

			return this.processSuccess(schInfo);

		} catch (SchedulerException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/plans/request")
	public Response schedule(ObservingPlanInformation opInfo) {

		String username = this.getUsername(request);

		this.setupRegularAuthorization(request);

		try {
			opInfo.setUser(username);

			if (opInfo.getTargetAltitude() == null) {
				opInfo.setTargetAltitude(15.0);
			}

			if (opInfo.getMoonAltitude() == null) {
				opInfo.setMoonAltitude(15.0);
			}

			if (opInfo.getMoonDistance() == null) {
				opInfo.setMoonDistance(15.0);
			}

			if (opInfo.getExposure() == null) {
				opInfo.setExposure(10.0);
			}

			if (opInfo.getFilter() == null) {
				opInfo.setFilter("OPEN");
			}

			if (opInfo.getDescription() == null) {
				return this.processError(Status.BAD_REQUEST, "input error",
						"description is needed");
			}

			if (opInfo.getObject() == null) {
				return this.processError(Status.BAD_REQUEST, "input error",
						"object is needed");
			}

			int id = scheduler.schedule(opInfo);

			return this.processSuccess(id);

		} catch (SchedulerException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (MaxUserSchedulesException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (InvalidObservingPlanException e) {
			return this.processError(Status.BAD_REQUEST, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/plans/active")
	public Response getMyActivePlans() {

		this.setupRegularAuthorization(request);

		try {

			List<ScheduleInformation> schInfos = scheduler.getMyActivePlans();

			if (schInfos == null) {
				schInfos = new ArrayList<>();
			}

			return this.processSuccess(schInfos);

		} catch (SchedulerException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (ScheduleNotFoundException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/plans/inactive")
	public Response getMyInactivePlans() {

		this.setupRegularAuthorization(request);

		try {

			List<ScheduleInformation> schInfos = scheduler.getMyInactivePlans();

			return this.processSuccess(schInfos);

		} catch (SchedulerException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (ScheduleNotFoundException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{rt}/plans")
	public Response getRTPlans(@PathParam("rt") String rt) {

		this.setupRegularAuthorization(request);

		try {

			List<ScheduleInformation> schInfos = scheduler.getAllRTPlans(rt);

			return this.processSuccess(schInfos);

		} catch (SchedulerException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (ScheduleNotFoundException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{rt}/plans/active")
	public Response getActiveRTPlans(@PathParam("rt") String rt) {

		this.setupRegularAuthorization(request);

		try {

			List<ScheduleInformation> schInfos = scheduler.getActiveRTPlans(rt);

			return this.processSuccess(schInfos);

		} catch (SchedulerException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (ScheduleNotFoundException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}
}
