/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import eu.gloria.gs.services.repository.rt.data.DeviceInformation;
import eu.gloria.gs.services.repository.rt.data.DeviceType;
import eu.gloria.gs.services.repository.rt.data.RTAvailability;
import eu.gloria.gs.services.repository.rt.data.RTInformation;
import eu.gloria.gs.services.utils.JSONConverter;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
@Singleton
@Path("/telescopes")
public class Telescopes extends GResource {

	public Telescopes() {
		super(Telescopes.class.getSimpleName());
	}

	@Context
	HttpServletRequest request;

	private static RTRepositoryInterface telescopes = GSClientProvider
			.getRTRepositoryClient();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/interface")
	public Response getInterface() {

		this.setupRegularAuthorization(request);

		LinkedHashMap<String, Object> operations = new LinkedHashMap<String, Object>();

		this.addInterfaceOperation(operations, "get all telescopes", "/list",
				"", "get");
		this.addInterfaceOperation(operations,
				"get all interactive telescopes", "/interactive/list", "",
				"get");

		return this.processSuccess(operations);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response getAllTelescopes() {

		this.setupPublicAuthorization();

		try {
			List<String> completeNames = new ArrayList<>();

			List<String> observatories = telescopes.getAllObservatoryNames();
			for (String observatory : observatories) {
				List<String> names = telescopes
						.getAllRTInObservatory(observatory);
				completeNames.addAll(names);
			}

			return this.processSuccess(completeNames);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/interactive/list")
	public Response getAllInteractiveTelescopes() {

		this.setupRegularAuthorization(request);

		try {
			List<String> names = telescopes.getAllInteractiveRTs();

			return this.processSuccess(names);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/batch/list")
	public Response getAllBatchTelescopes() {

		this.setupRegularAuthorization(request);

		try {
			List<String> names = telescopes.getAllBatchRTs();

			return this.processSuccess(names);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/devices")
	public Response getDevices(@PathParam("name") String name,
			@QueryParam("detailed") boolean detailed,
			@QueryParam("type") String type) {

		this.setupRegularAuthorization(request);

		try {
			List<String> names = null;

			if (type == null) {
				names = telescopes.getDeviceNames(name);
			} else {
				names = telescopes.getRTDeviceNames(name,
						DeviceType.valueOf(type));
			}

			if (detailed) {
				List<DeviceInformation> devices = new ArrayList<DeviceInformation>();

				for (String device : names) {
					DeviceInformation devInfo = telescopes
							.getRTDeviceInformation(name, device);
					devices.add(devInfo);
				}

				return this.processSuccess(devices);
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
	public Response getRTInformation(@PathParam("name") String name) {

		this.setupRegularAuthorization(request);

		try {
			Map<String, Object> rtInfoMap = new LinkedHashMap<>();

			RTInformation rtInfo = telescopes.getRTInformation(name);

			rtInfoMap.put("description", rtInfo.getDescription());
			rtInfoMap.put("owner", rtInfo.getOwner());
			rtInfoMap.put("coordinates", rtInfo.getCoordinates());
			rtInfoMap.put("image", rtInfo.getImage());
			rtInfoMap.put("date", rtInfo.getRegistrationDate());

			return this.processSuccess(rtInfoMap);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/devices")
	public Response getDeviceInformation(@PathParam("name") String name,
			Object devName) {

		this.setupRegularAuthorization(request);

		try {
			DeviceInformation devInfo = telescopes.getRTDeviceInformation(name,
					(String) devName);
			return this.processSuccess(devInfo);

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/interactive/register")
	public Response registerInteractiveTelescope(
			@QueryParam("name") String name, RegisterTelescopeRequest data) {

		this.setupRegularAuthorization(request);

		try {
			telescopes.registerInteractiveRT(name, data.getOwner(),
					data.getUrl(), data.getPort(), data.getUser(),
					data.getPassword());
			return this.processSuccess();

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/batch/register")
	public Response registerBatchTelescope(@QueryParam("name") String name,
			RegisterTelescopeRequest data) {

		this.setupRegularAuthorization(request);

		try {
			telescopes.registerBatchRT(name, data.getOwner(), data.getUrl(),
					data.getPort(), data.getUser(), data.getPassword());
			return this.processSuccess();

		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/availability")
	public Response setRTAvailability(@PathParam("name") String name,
			RTAvailability availability) {

		this.setupRegularAuthorization(request);

		try {
			telescopes.setRTAvailability(name, availability);
			return this.processSuccess();
		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/availability")
	public Response getRTAvailability(@PathParam("name") String name) {

		this.setupRegularAuthorization(request);

		try {
			RTAvailability availability = telescopes.getRTAvailability(name);

			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Map<String, String> availabilityFormatted = new LinkedHashMap<>();

			availabilityFormatted.put("startingTime",
					dateFormat.format(availability.getStartingTime()));
			availabilityFormatted.put("endingTime",
					dateFormat.format(availability.getEndingTime()));

			return this.processSuccess(availabilityFormatted);
		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/image")
	public Response setRTImage(@PathParam("name") String name, String image) {

		this.setupRegularAuthorization(request);

		try {
			telescopes.setRTImage(name,
					(String) JSONConverter.fromJSON(image, String.class, null));
			return this.processSuccess();
		} catch (RTRepositoryException | IOException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/description")
	public Response setRTDescription(@PathParam("name") String name,
			String description) {

		this.setupRegularAuthorization(request);

		try {
			telescopes.setRTDescription(name, (String) JSONConverter.fromJSON(
					description, String.class, null));
			return this.processSuccess();
		} catch (RTRepositoryException | IOException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}/image")
	public Response getRTImage(@PathParam("name") String name) {

		this.setupRegularAuthorization(request);

		try {
			String image = telescopes.getRTImage(name);
			return this.processSuccess(image);
		} catch (RTRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}
}
