/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.spi.resource.Singleton;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.repository.rt.RTRepositoryException;
import eu.gloria.gs.services.repository.rt.RTRepositoryInterface;
import eu.gloria.gs.services.repository.rt.data.ObservatoryInformation;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */

@Singleton
@Path("/observatories")
public class Observatories extends GResource {

	public Observatories() {
		super(Observatories.class.getSimpleName());
	}

	@Context
	HttpServletRequest request;

	private static RTRepositoryInterface telescopes = GSClientProvider.getRTRepositoryClient();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response getObservatories(@QueryParam("detailed") boolean detailed) {

		this.setupRegularAuthorization(request);

		try {

			List<String> names = null;
			List<ObservatoryInformation> observatories = new ArrayList<ObservatoryInformation>();
			names = telescopes.getAllObservatoryNames();

			if (detailed) {
				for (String name : names) {
					ObservatoryInformation obsInfo = telescopes
							.getObservatoryInformation(name);

					observatories.add(obsInfo);
				}

				return this.processSuccess(observatories);
			} else {
				return this.processSuccess(names);
			}

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}")
	public Response getObservatoryInformation(@PathParam("name") String name) {

		name = name.replace("-", " ");

		this.setupRegularAuthorization(request);

		try {
			ObservatoryInformation obsInfo = telescopes
					.getObservatoryInformation(name);
			return this.processSuccess(obsInfo);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/register")
	public Response registerObservatory(@QueryParam("name") String name,
			@QueryParam("country") String country,
			@QueryParam("city") String city) {

		this.setupRegularAuthorization(request);

		try {
			telescopes.registerObservatory(name, city, country);
			return this.processSuccess();

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/telescopes/list")
	public Response getTelescopesInObservatory(@PathParam("name") String name) {

		name = name.replace("-", " ");

		this.setupRegularAuthorization(request);

		try {
			List<String> rtNames = telescopes.getAllRTInObservatory(name);
			return this.processSuccess(rtNames);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/telescopes/add/{rt}")
	public Response addTelescope(@PathParam("name") String name,
			@PathParam("rt") String rt) {

		name = name.replace("-", " ");

		this.setupRegularAuthorization(request);

		try {
			telescopes.setRTObservatory(rt, name);
			return this.processSuccess();

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}
}
