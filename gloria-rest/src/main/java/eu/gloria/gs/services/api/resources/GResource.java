/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.ApplicationContext;

import eu.gloria.gs.services.api.security.ApplicationContextProvider;
import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.utils.JSONConverter;
import eu.gloria.gs.services.utils.LoggerEntity;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
public abstract class GResource extends LoggerEntity {

	protected GResource(String name) {
		super(name);
	}

	private static String adminUsername;
	private static String adminPassword;
	protected static ApplicationContext context = ApplicationContextProvider
			.getApplicationContext();

	static {
		context = ApplicationContextProvider.getApplicationContext();

		String hostName = (String) context.getBean("hostAddress");
		String hostPort = (String) context.getBean("hostPort");

		adminPassword = (String) context.getBean("adminPassword");
		adminUsername = (String) context.getBean("adminUsername");

		GSClientProvider.setHost(hostName);
		GSClientProvider.setPort(hostPort);
	}

	protected void addInterfaceOperation(HashMap<String, Object> container,
			String name, String path, String description, String method) {

		LinkedHashMap<String, String> op = new LinkedHashMap<String, String>();
		op.put("path", path);
		op.put("method", method);
		op.put("description", description);

		container.put(name, op);
	}

	public static String getAdminUsername() {
		return adminUsername;
	}

	public static void setAdminUsername(String adminUsername) {
		GResource.adminUsername = adminUsername;
	}

	public static String getAdminPassword() {
		return adminPassword;
	}

	public static void setAdminPassword(String adminPassword) {
		GResource.adminPassword = adminPassword;
	}

	protected String getUsername(HttpServletRequest request) {
		return (String) request.getAttribute("user");
	}

	protected String getPassword(HttpServletRequest request) {
		return (String) request.getAttribute("password");
	}

	protected void setupRegularAuthorization(HttpServletRequest request) {
		if (request.getAttribute("user") != null) {

			GSClientProvider.setCredentials(
					(String) request.getAttribute("user"),
					(String) request.getAttribute("password"));
		}
	}

	protected void setupPublicAuthorization() {
		GSClientProvider.setCredentials(getAdminUsername(), getAdminPassword());
	}

	protected Response processError(Status status, String errorName,
			String message) {

		LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
		errorData.put("type", errorName);
		errorData.put("exception", message);

		return Response.status(status).entity(errorData)
				.type(MediaType.APPLICATION_JSON).build();
	}

	protected Response processError(Status status, Exception e) {

		String messageStr = e.getMessage();
		Object message;
		try {
			message = JSONConverter.fromJSON(messageStr,
					LinkedHashMap.class, null);
		} catch (IOException e1) {
			message = e1.getMessage();
		}

		return Response.status(status).entity(message)
				.type(MediaType.APPLICATION_JSON).build();
	}

	protected Response processSuccess(Object data) {

		if (data instanceof String) {
			try {
				data = JSONConverter.toJSON(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Response.ok(data).type(MediaType.APPLICATION_JSON).build();
	}

	protected Response processSuccess() {
		LinkedHashMap<String, Object> dummy = new LinkedHashMap<>();

		return Response.ok(dummy).type(MediaType.APPLICATION_JSON).build();
	}

}
