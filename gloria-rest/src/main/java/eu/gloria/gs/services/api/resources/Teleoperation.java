/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import eu.gloria.gs.services.repository.rt.data.DeviceType;
import eu.gloria.gs.services.teleoperation.base.DeviceOperationFailedException;
import eu.gloria.gs.services.teleoperation.base.Range;
import eu.gloria.gs.services.teleoperation.ccd.CCDTeleoperationException;
import eu.gloria.gs.services.teleoperation.ccd.CCDTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.ccd.ImageExtensionFormat;
import eu.gloria.gs.services.teleoperation.ccd.ImageNotAvailableException;
import eu.gloria.gs.services.teleoperation.dome.DomeOpeningState;
import eu.gloria.gs.services.teleoperation.dome.DomeTeleoperationException;
import eu.gloria.gs.services.teleoperation.dome.DomeTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.focuser.FocuserTeleoperationException;
import eu.gloria.gs.services.teleoperation.focuser.FocuserTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.fw.FilterWheelTeleoperationException;
import eu.gloria.gs.services.teleoperation.fw.FilterWheelTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.generic.GenericTeleoperationException;
import eu.gloria.gs.services.teleoperation.generic.GenericTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.mount.MountState;
import eu.gloria.gs.services.teleoperation.mount.MountTeleoperationException;
import eu.gloria.gs.services.teleoperation.mount.MountTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.scam.SCamTeleoperationException;
import eu.gloria.gs.services.teleoperation.scam.SCamTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.weather.WeatherTeleoperationException;
import eu.gloria.gs.services.teleoperation.weather.WeatherTeleoperationInterface;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
@Singleton
@Path("/teleoperation")
public class Teleoperation extends GResource {

	public Teleoperation() {
		super(Teleoperation.class.getSimpleName());
	}

	@Context
	HttpServletRequest request;

	private static MountTeleoperationInterface mounts = GSClientProvider
			.getMountTeleoperationClient();;
	private static DomeTeleoperationInterface domes = GSClientProvider
			.getDomeTeleoperationClient();;
	private static SCamTeleoperationInterface scams = GSClientProvider
			.getSCamTeleoperationClient();
	private static CCDTeleoperationInterface ccds = GSClientProvider
			.getCCDTeleoperationClient();
	private static FilterWheelTeleoperationInterface filters = GSClientProvider
			.getFilterWheelTeleoperationClient();
	private static FocuserTeleoperationInterface focusers = GSClientProvider
			.getFocuserTeleoperationClient();
	private static WeatherTeleoperationInterface weathers = GSClientProvider
			.getWeatherTeleoperationClient();
	private static GenericTeleoperationInterface generics = GSClientProvider
			.getGenericTeleoperationClient();
	private static RTRepositoryInterface telescopes = GSClientProvider
			.getRTRepositoryClient();

	@GET
	@Path("/weather/{rt}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWeather(@PathParam("rt") String rt) {

		this.setupPublicAuthorization();

		try {

			boolean alarm = weathers.isOnWeatherAlarm(rt);

			List<String> rtDeviceNames = telescopes.getRTDeviceNames(rt,
					DeviceType.WIND_SPEED_SENSOR);
			String name = null;
			if (rtDeviceNames != null && rtDeviceNames.size() > 0) {
				name = rtDeviceNames.get(0);
			}

			LinkedHashMap<String, Object> weather = new LinkedHashMap<String, Object>();
			weather.put("alarm", alarm);

			if (name != null) {
				alarm = weathers.isWindOnAlarm(rt, name);
				double value = weathers.getWindSpeed(rt, name);
				LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
				response.put("value", value);
				response.put("alarm", alarm);
				weather.put("wind", response);
			}

			rtDeviceNames = telescopes.getRTDeviceNames(rt,
					DeviceType.RH_SENSOR);
			name = null;
			if (rtDeviceNames != null && rtDeviceNames.size() > 0) {
				name = rtDeviceNames.get(0);
			}

			if (name != null) {
				alarm = weathers.isRHOnAlarm(rt, name);
				double value = weathers.getRelativeHumidity(rt, name);
				LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
				response.put("value", value);
				response.put("alarm", alarm);
				weather.put("rh", response);
			}

			return this.processSuccess(weather);
		} catch (WeatherTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (RTRepositoryException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		}
	}

	@GET
	@Path("/weather/pressure/{rt}/{barometer}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPressure(@PathParam("rt") String rt,
			@PathParam("barometer") String barometer) {

		this.setupRegularAuthorization(request);

		try {
			double pressure = weathers.getPressure(rt, barometer);
			boolean alarm = weathers.isPressureOnAlarm(rt, barometer);

			LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("value", pressure);
			response.put("alarm", alarm);

			return this.processSuccess(response);
		} catch (WeatherTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/weather/wind/{rt}/{wind}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWindSpeed(@PathParam("rt") String rt,
			@PathParam("wind") String wind) {

		this.setupRegularAuthorization(request);

		try {
			double windSpeed = weathers.getWindSpeed(rt, wind);
			boolean alarm = weathers.isWindOnAlarm(rt, wind);

			LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("value", windSpeed);
			response.put("alarm", alarm);

			return this.processSuccess(response);
		} catch (WeatherTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/weather/rh/{rt}/{rh}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRelativeHumidity(@PathParam("rt") String rt,
			@PathParam("rh") String rh) {

		this.setupRegularAuthorization(request);

		try {
			double humidity = weathers.getRelativeHumidity(rt, rh);
			boolean alarm = weathers.isRHOnAlarm(rt, rh);

			LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("value", humidity);
			response.put("alarm", alarm);

			return this.processSuccess(response);
		} catch (WeatherTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/filter/list/{rt}/{fw}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFilters(@PathParam("rt") String rt,
			@PathParam("fw") String fw) {

		this.setupRegularAuthorization(request);

		try {
			List<String> filterList = filters.getFilters(rt, fw);

			return this.processSuccess(filterList);
		} catch (FilterWheelTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/generic/startInteractive/{rt}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startInteractive(@PathParam("rt") String rt,
			@QueryParam("seconds") Long seconds) {

		this.setupRegularAuthorization(request);

		try {

			if (seconds == null) {
				generics.startTeleoperation(rt);
			} else {
				generics.notifyTeleoperation(rt, seconds);
			}

			return this.processSuccess();
		} catch (GenericTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		}
	}

	@GET
	@Path("/generic/stopInteractive/{rt}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startInteractive(@PathParam("rt") String rt) {

		this.setupRegularAuthorization(request);

		try {

			generics.stopTeleoperation(rt);

			return this.processSuccess();
		} catch (GenericTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		}
	}

	@GET
	@Path("/focus/move/{rt}/{focus}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response focusIn(@PathParam("rt") String rt,
			@PathParam("focus") String focus, @QueryParam("steps") Long steps) {

		this.setupRegularAuthorization(request);

		if (steps == null) {
			steps = (long) 100;
		}

		try {
			focusers.moveRelative(rt, focus, steps);

			return this.processSuccess();
		} catch (FocuserTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/focus/range/{rt}/{focus}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response focusIn(@PathParam("rt") String rt,
			@PathParam("focus") String focus) {

		this.setupRegularAuthorization(request);

		try {
			Range range = focusers.getRange(rt, focus);

			return this.processSuccess(range);
		} catch (FocuserTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/ccd/startContinue/{rt}/{ccd}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startContinueMode(@PathParam("rt") String rt,
			@PathParam("ccd") String ccd) {

		this.setupRegularAuthorization(request);

		ccd = ccd.replace("-", " ");

		try {
			String id = ccds.startContinueMode(rt, ccd);

			return this.processSuccess(id);

		} catch (CCDTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/ccd/range/{rt}/{ccd}/{object}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExposureRange(@PathParam("rt") String rt,
			@PathParam("ccd") String ccd, @PathParam("object") String object) {

		this.setupRegularAuthorization(request);

		ccd = ccd.replace("-", " ");

		try {
			Range range = ccds.getExposureRange(rt, ccd, object);
			return this.processSuccess(range);
		} catch (CCDTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/ccd/attributes/{rt}/{ccd}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setAttributes(@PathParam("rt") String rt,
			@PathParam("ccd") String ccd,
			@QueryParam("exposure") Double exposure,
			@QueryParam("brightness") Long brightness,
			@QueryParam("gain") Long gain, @QueryParam("gamma") Long gamma) {

		this.setupRegularAuthorization(request);

		ccd = ccd.replace("-", " ");

		if (exposure != null) {

			try {
				ccds.setExposureTime(rt, ccd, exposure);

				return this.processSuccess();
			} catch (CCDTeleoperationException e) {
				return this.processError(Status.NOT_ACCEPTABLE, e);
			} catch (DeviceOperationFailedException e) {
				return this.processError(Status.INTERNAL_SERVER_ERROR, e);
			}
		}

		if (brightness != null) {

			try {
				ccds.setBrightness(rt, ccd, brightness);
			} catch (CCDTeleoperationException e) {
				return Response.serverError().entity(e.getMessage()).build();
			} catch (DeviceOperationFailedException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).build();
			}
		}

		if (gamma != null) {

			try {
				ccds.setGamma(rt, ccd, gamma);
			} catch (CCDTeleoperationException e) {
				return Response.serverError().entity(e.getMessage()).build();
			} catch (DeviceOperationFailedException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).build();
			}
		}

		if (gain != null) {

			try {
				ccds.setGain(rt, ccd, gain);
			} catch (CCDTeleoperationException e) {
				return Response.serverError().entity(e.getMessage()).build();
			} catch (DeviceOperationFailedException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).build();
			}
		}

		if (exposure == null && brightness == null && gamma == null
				&& gain == null) {
			Map<String, Object> values = new LinkedHashMap<String, Object>();
			try {

				values.put("exposure", ccds.getExposureTime(rt, ccd));

				Map<String, Object> complexValue = new LinkedHashMap<String, Object>();
				complexValue.put("modifiable", ccds.gainIsModifiable(rt, ccd));
				complexValue.put("value", ccds.getGain(rt, ccd));

				values.put("gain", complexValue);
				complexValue = new LinkedHashMap<String, Object>();

				complexValue.put("modifiable", ccds.gammaIsModifiable(rt, ccd));
				complexValue.put("value", ccds.getGamma(rt, ccd));
				values.put("gamma", complexValue);

			} catch (CCDTeleoperationException e) {
				return Response.serverError().entity(e.getMessage()).build();
			} catch (DeviceOperationFailedException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).build();
			}

			return this.processSuccess(values);
		} else {
			return this.processSuccess();
		}
	}

	@GET
	@Path("/ccd/startExposure/{rt}/{ccd}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startExposure(@PathParam("rt") String rt,
			@PathParam("ccd") String ccd,
			@QueryParam("exposure") Double exposure) {

		this.setupRegularAuthorization(request);

		ccd = ccd.replace("-", " ");

		try {
			if (exposure != null) {
				ccds.setExposureTime(rt, ccd, exposure);
			}
			String id = ccds.startExposure(rt, ccd);

			return this.processSuccess(id);

		} catch (CCDTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/ccd/url/{rt}/{ccd}/{lid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCCDImageUrl(@PathParam("rt") String rt,
			@PathParam("ccd") String ccd, @PathParam("lid") String lid,
			@QueryParam("format") String format) {

		this.setupRegularAuthorization(request);

		ccd = ccd.replace("-", " ");

		if (format == null) {
			format = "JPG";
		}

		try {
			String url = ccds.getImageURL(rt, ccd, lid,
					ImageExtensionFormat.valueOf(format));

			return this.processSuccess(url);
		} catch (CCDTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (ImageNotAvailableException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Path("/ccd/stopContinue/{rt}/{ccd}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopContinueMode(@PathParam("rt") String rt,
			@PathParam("ccd") String ccd) {

		this.setupRegularAuthorization(request);

		try {
			ccds.stopContinueMode(rt, ccd.replace("-", " "));

			return this.processSuccess();
		} catch (CCDTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/scam/url/{rt}/{scam}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSCamUrl(@PathParam("rt") String rt,
			@PathParam("scam") String scam) {

		this.setupPublicAuthorization();

		try {
			String url = scams.getImageURL(rt, scam);

			return this.processSuccess(url);
		} catch (SCamTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/scam/url/{rt}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSCamUrl(@PathParam("rt") String rt) {

		this.setupPublicAuthorization();

		try {
			List<String> rtDeviceNames = telescopes.getRTDeviceNames(rt,
					DeviceType.SURVEILLANCE_CAMERA);
			String name = null;
			if (rtDeviceNames != null && rtDeviceNames.size() > 0) {
				name = rtDeviceNames.get(0);
			}

			String url = scams.getImageURL(rt, name);

			return this.processSuccess(url);
		} catch (SCamTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (RTRepositoryException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		}
	}

	@GET
	@Path("/mount/slewObject/{rt}/{mount}")
	public Response slewToObject(@PathParam("rt") String rt,
			@PathParam("mount") String mount,
			@QueryParam("object") String object) {

		this.setupRegularAuthorization(request);

		try {
			mounts.slewToObject(rt, mount, object);

			return this.processSuccess();
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/mount/park/{rt}/{mount}")
	public Response parkMount(@PathParam("rt") String rt,
			@PathParam("mount") String mount) {

		this.setupRegularAuthorization(request);

		try {
			mounts.park(rt, mount);

			return this.processSuccess();
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/mount/slewRADEC/{rt}/{mount}")
	public Response slewToRADEC(@PathParam("rt") String rt,
			@PathParam("mount") String mount, @QueryParam("ra") double ra,
			@QueryParam("dec") double dec) {

		this.setupRegularAuthorization(request);

		try {
			mounts.slewToCoordinates(rt, mount, ra, dec);

			return this.processSuccess();
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/mount/move/{rt}/{mount}/{direction}")
	public Response moveDirection(@PathParam("rt") String rt,
			@PathParam("mount") String mount,
			@PathParam("direction") String direction) {

		this.setupRegularAuthorization(request);

		try {
			if (direction.toLowerCase().equals("north")) {
				mounts.moveNorth(rt, mount);
			} else if (direction.toLowerCase().equals("south")) {
				mounts.moveSouth(rt, mount);
			} else if (direction.toLowerCase().equals("east")) {
				mounts.moveEast(rt, mount);
			} else if (direction.toLowerCase().equals("west")) {
				mounts.moveWest(rt, mount);
			}

			return this.processSuccess();
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/mount/slewRate/{rt}/{mount}")
	public Response manageSlewRate(@PathParam("rt") String rt,
			@PathParam("mount") String mount, @QueryParam("rate") String rate) {

		this.setupRegularAuthorization(request);

		try {
			if (rate != null) {
				mounts.setSlewRate(rt, mount, rate);
			} else {
				// String currentRate = mounts.getSlewRate(rt, mount);
			}

			return this.processSuccess();
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/mount/status/{rt}/{mount}")
	public Response getMountStatus(@PathParam("rt") String rt,
			@PathParam("mount") String mount) {

		this.setupPublicAuthorization();

		try {
			MountState state = mounts.getState(rt, mount);
			LinkedHashMap<String, Object> stateResult = new LinkedHashMap<String, Object>();
			stateResult.put("state", state.name());

			return this.processSuccess(stateResult);
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/mount/status/{rt}")
	public Response getMountStatus(@PathParam("rt") String rt) {

		this.setupPublicAuthorization();

		try {

			List<String> rtDeviceNames = telescopes.getRTDeviceNames(rt,
					DeviceType.MOUNT);
			String name = null;
			MountState state = MountState.UNDEFINED;
			if (rtDeviceNames != null && rtDeviceNames.size() > 0) {
				name = rtDeviceNames.get(0);
				state = mounts.getState(rt, name);
			}

			LinkedHashMap<String, Object> stateResult = new LinkedHashMap<String, Object>();
			stateResult.put("state", state.name());

			return this.processSuccess(stateResult);
		} catch (MountTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (RTRepositoryException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		}
	}

	@GET
	@Path("/dome/status/{rt}/{dome}")
	public Response getDomeStatus(@PathParam("rt") String rt,
			@PathParam("dome") String dome) {

		this.setupPublicAuthorization();

		try {
			DomeOpeningState state = domes.getState(rt, dome);
			LinkedHashMap<String, Object> stateResult = new LinkedHashMap<String, Object>();
			stateResult.put("state", state.name());

			return this.processSuccess(stateResult);
		} catch (DomeTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/dome/status/{rt}")
	public Response getDomeStatus(@PathParam("rt") String rt) {

		this.setupPublicAuthorization();

		try {

			List<String> rtDeviceNames = telescopes.getRTDeviceNames(rt,
					DeviceType.DOME);
			String name = null;
			DomeOpeningState state = DomeOpeningState.UNDEFINED;
			if (rtDeviceNames != null && rtDeviceNames.size() > 0) {
				name = rtDeviceNames.get(0);
				state = domes.getState(rt, name);
			}

			LinkedHashMap<String, Object> stateResult = new LinkedHashMap<String, Object>();
			stateResult.put("state", state.name());

			return this.processSuccess(stateResult);
		} catch (DomeTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (RTRepositoryException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		}
	}

	@GET
	@Path("/dome/open/{rt}/{dome}")
	public Response openDome(@PathParam("rt") String rt,
			@PathParam("dome") String dome) {

		this.setupRegularAuthorization(request);

		try {
			domes.open(rt, dome);

			return this.processSuccess();
		} catch (DomeTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/dome/close/{rt}/{dome}")
	public Response closeDome(@PathParam("rt") String rt,
			@PathParam("dome") String dome) {

		this.setupRegularAuthorization(request);

		try {
			domes.close(rt, dome);

			return this.processSuccess();
		} catch (DomeTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/dome/park/{rt}/{dome}")
	public Response parkDome(@PathParam("rt") String rt,
			@PathParam("dome") String dome) {

		this.setupRegularAuthorization(request);

		try {
			domes.park(rt, dome);

			return this.processSuccess();
		} catch (DomeTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Path("/dome/azimuth/{rt}/{dome}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDomeAzimuth(@PathParam("rt") String rt,
			@PathParam("dome") String dome) {

		this.setupRegularAuthorization(request);

		try {
			double azimuth = domes.getAzimuth(rt, dome);

			return this.processSuccess(azimuth);
		} catch (DomeTeleoperationException e) {
			return this.processError(Status.NOT_ACCEPTABLE, e);
		} catch (DeviceOperationFailedException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}
}
