package eu.gloria.gs.services.api.mail;

import org.springframework.context.ApplicationContext;

import eu.gloria.gs.services.core.tasks.ServerTask;
import eu.gloria.gs.services.core.tasks.ServerThread;

public class VerificationTask extends ServerTask {

	public VerificationTask() {
		super(VerificationTask.class.getSimpleName());
	}

	@Override
	protected ServerThread createServerThread(ApplicationContext context) {

		log.info("Online");
		return (ServerThread) context.getBean("verificationMonitor");
	}

}
