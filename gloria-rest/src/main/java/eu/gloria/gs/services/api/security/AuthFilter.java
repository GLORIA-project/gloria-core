package eu.gloria.gs.services.api.security;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.ApplicationContext;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import eu.gloria.gs.services.api.data.UserDataAdapter;
import eu.gloria.gs.services.api.data.dbservices.UserDataAdapterException;
import eu.gloria.gs.services.api.data.dbservices.UserEntry;
import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.log.action.Action;
import eu.gloria.gs.services.repository.user.UserRepositoryException;
import eu.gloria.gs.services.repository.user.UserRepositoryInterface;
import eu.gloria.gs.services.utils.JSONConverter;
import eu.gloria.gs.services.utils.LoggerEntity;

public class AuthFilter extends LoggerEntity implements ContainerRequestFilter {

	private static UserRepositoryInterface userRepository = null;

	private static String adminUsername;
	private static String adminPassword;
	private static UserDataAdapter userAdapter;

	public AuthFilter() {
		super(AuthFilter.class.getSimpleName());
	}

	static {

		ApplicationContext context = ApplicationContextProvider
				.getApplicationContext();

		String hostName = (String) context.getBean("hostAddress");
		String hostPort = (String) context.getBean("hostPort");

		GSClientProvider.setHost(hostName);
		GSClientProvider.setPort(hostPort);

		adminUsername = (String) context.getBean("adminUsername");
		adminPassword = (String) context.getBean("adminPassword");

		userAdapter = (UserDataAdapter) context.getBean("userDataAdapter");
		userRepository = GSClientProvider.getUserRepositoryClient();
	}

	@Context
	HttpServletRequest sr;

	/**
	 * Apply the filter : check input request, validate or not with user auth
	 * 
	 * @param containerRequest
	 *            The request from Tomcat server
	 */
	@Override
	public ContainerRequest filter(ContainerRequest containerRequest)
			throws WebApplicationException {
		// GET, POST, PUT, DELETE, ...
		String method = containerRequest.getMethod();
		// myresource/get/56bCA for example
		// String path = containerRequest.getPath(true);

		if (method.equals("OPTIONS")) {
			throw new WebApplicationException(Status.OK);
		}

		Action action = new Action();

		action.put("method", method);

		// Get the authentification passed in HTTP headers parameters
		String auth = containerRequest.getHeaderValue("authorization");

		GSClientProvider.setCredentials("dummy", "neh");

		String userAgent = sr.getHeader(ContainerRequest.USER_AGENT);
		String remote = sr.getRemoteAddr();
		String language = sr.getLocale().getLanguage();

		action.put("remote", remote);
		action.put("request", sr.getRequestURI());

		if (auth != null) {

			if (userAgent == null) {
				userAgent = "";
			}

			// lap : loginAndPassword
			String[] lap = BasicAuth.decode(auth);

			// If login or password fail
			if (lap == null || lap.length != 2) {
				action.put("auth", "unauthorized");
				throw new WebApplicationException(Status.UNAUTHORIZED);
			}

			if (lap[0].equals("public") && lap[1].equals("public")) {
				action.put("auth", "public");
				return containerRequest;
			}

			UserEntry entry = null;
			String name = null;
			String actualPassword = null;

			try {				
				entry = userAdapter.getUserInformationByToken(lap[1]);
			} catch (UserDataAdapterException e) {
			}

			boolean authenticated = false;
			
			if (entry != null && entry.getActive() > 0) {
				action.put("token", lap[1]);
				if (new Date().getTime() - entry.getTokenUpdateDate().getTime() < 10800000) {
					name = entry.getName();
					actualPassword = entry.getPassword();
					try {
						userAdapter.updateLastDate(entry.getToken());
						authenticated = true;
					} catch (UserDataAdapterException e) {
						log.error(e.getMessage());
					}
				} else {					
					userAdapter.deactivateToken(entry.getToken());					
				}
			}

			if (!authenticated) {
				actualPassword = SHA1.encode(lap[1]);
				name = lap[0];

				try {
					GSClientProvider.setCredentials(adminUsername,
							adminPassword);

					if (!userRepository.authenticateUser(name, actualPassword)) {
						if (!userRepository.authenticateUser(name, lap[1])) {
							throw new WebApplicationException(
									Status.UNAUTHORIZED);
						} else {
							actualPassword = lap[1];							
						}
					}

					if (language != null) {
						String[] languages = language.split(";");

						if (languages.length > 0) {
							language = languages[0];
						}
					}

					List<UserEntry> actives = userAdapter
							.getUserInformation(name);

					boolean newToken = false;

					if (actives != null && actives.size() > 0) {
						for (UserEntry user : actives) {
							if (new Date().getTime()
									- user.getTokenUpdateDate().getTime() > 10800000) {
								newToken = true;
							} else {
								if (!remote.equals(user.getRemote())
										|| !userAgent.equals(user.getAgent())) {
									userAdapter
											.deactivateToken(user.getToken());
									newToken = true;
								}
							}
						}
					} else {
						newToken = true;
					}

					if (newToken) {
						String token = userAdapter.createToken(name,
								actualPassword, language, userAgent, remote);

						userAdapter.activateToken(token);
						userAdapter.deactivateOtherTokens(name, token);
						
						action.put("new-token", token);

					} else {
						userAdapter.updateLastDate(actives.get(0).getToken());
						action.put("token", actives.get(0).getToken());
					}

					sr.setAttribute("agent", userAgent);

				} catch (UserRepositoryException e) {
					log.error(e.getMessage());
				} catch (UserDataAdapterException e) {
					log.error(e.getMessage());
				}
			}

			sr.setAttribute("agent", userAgent);
			sr.setAttribute("remote", remote);
			sr.setAttribute("language", language);

			sr.setAttribute("user", name);
			action.put("user", name);
			sr.setAttribute("password", actualPassword);
		} else {
			action.put("auth", "empty");
		}

		info(action);
		
		return containerRequest;
	}
	
	private void info(Action action) {
		String message = stringifyAction(action);
		if (message != null)
			log.info(message);
	}
	
	private void error(Action action) {
		String message = stringifyAction(action);
		if (message != null)
			log.error(message);
	}
	
	private String stringifyAction(Action action) {
		try {
			return JSONConverter.toJSON(action);
		} catch (IOException e) {
			log.error("stringifying json");
		}
		
		return null;
	}
}
