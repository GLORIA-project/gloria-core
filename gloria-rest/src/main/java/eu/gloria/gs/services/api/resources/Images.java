/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import eu.gloria.gs.services.repository.image.ImageRepositoryException;
import eu.gloria.gs.services.repository.image.ImageRepositoryInterface;
import eu.gloria.gs.services.repository.image.data.ImageInformation;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */

@Singleton
@Path("/images")
public class Images extends GResource {

	public Images() {
		super(Images.class.getSimpleName());
	}

	@Context
	HttpServletRequest request;

	private static ImageRepositoryInterface images = GSClientProvider
			.getImageRepositoryClient();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response listImages() {

		this.setupRegularAuthorization(request);

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.set(Calendar.DAY_OF_YEAR,
					calendar.get(Calendar.DAY_OF_YEAR) - 10);

			List<Integer> ids = images.getAllImageIdentifiersByDate(
					calendar.getTime(), new Date(), 10);

			return this.processSuccess(ids);

		} catch (ImageRepositoryException e) {
			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list/context/{rid}")
	public Response listImagesByReservation(@PathParam("rid") int rid) {

		this.setupPublicAuthorization();

		try {

			List<ImageInformation> imgList = images
					.getAllReservationImages(rid);

			if (imgList == null) {
				imgList = new ArrayList<ImageInformation>();
			}

			return this.processSuccess(imgList);

		} catch (ImageRepositoryException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list/object/{object}")
	public Response listImagesByObject(@PathParam("object") String object) {

		this.setupPublicAuthorization();

		try {

			List<Integer> imgList = images.getAllObjectImages(object);

			if (imgList == null) {
				imgList = new ArrayList<Integer>();
			}

			return this.processSuccess(imgList);

		} catch (ImageRepositoryException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list/{year}/{month}/{day}")
	public Response listDateImages(@PathParam("year") String year,
			@PathParam("month") String month, @PathParam("day") String day,
			@QueryParam("complete") boolean complete,
			@QueryParam("maxResults") Integer maxResults) {

		this.setupRegularAuthorization(request);

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
			calendar.set(Calendar.MONTH, Integer.valueOf(month) - 1);
			calendar.set(Calendar.YEAR, Integer.valueOf(year));
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);

			Date fromDate = calendar.getTime();

			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);

			Date toDate = calendar.getTime();

			if (maxResults == null) {
				maxResults = 10;
			}
			
			List<Integer> ids = images.getRandomImageIdentifiersByDate(fromDate,
					toDate, maxResults);

			if (ids == null) {
				ids = new ArrayList<Integer>();
			}

			if (!complete) {
				return Response.ok(ids).build();
			} else {
				ArrayList<ImageInformation> imageInfos = new ArrayList<ImageInformation>();

				for (Integer id : ids) {
					ImageInformation imageInfo = images
							.getImageInformation(Integer.valueOf(id));
					imageInfos.add(imageInfo);
				}

				return this.processSuccess(imageInfos);
			}

		} catch (ImageRepositoryException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{imageId}")
	public Response getImageInformation(@PathParam("imageId") String id) {

		this.setupRegularAuthorization(request);

		try {

			ImageInformation imageInfo = images.getImageInformation(Integer
					.valueOf(id));

			return this.processSuccess(imageInfo);

		} catch (ImageRepositoryException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/random/all/{count}")
	public Response getImageInformation(@PathParam("count") int count) {

		this.setupRegularAuthorization(request);

		try {

			List<ImageInformation> imageInfos = images
					.getRandomImagesInformation(count);

			return this.processSuccess(imageInfos);

		} catch (ImageRepositoryException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/random/mine/{count}")
	public Response getUserImageInformation(@PathParam("count") int count) {

		this.setupRegularAuthorization(request);

		try {

			List<ImageInformation> imageInfos = images
					.getRandomUserImagesInformation(this.getUsername(request),
							count);

			return this.processSuccess(imageInfos);

		} catch (ImageRepositoryException e) {
			return this.processError(Status.NOT_FOUND, e);
		}
	}
}
