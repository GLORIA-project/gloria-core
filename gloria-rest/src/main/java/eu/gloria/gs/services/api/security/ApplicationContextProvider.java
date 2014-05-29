/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.security;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 *
 */
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {
	private static ApplicationContext context = null;

	public static ApplicationContext getApplicationContext() {
		return context;
	}

	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		context = ctx;
	}
}
