package eu.gloria.gs.services.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class LoggerNameFilter extends LogThresholdFilter {

	String name;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		FilterReply reply = super.decide(event);
		if (event.getLoggerName().equals(name)) {
			return reply;
		} else {
			return FilterReply.DENY;
		}
	}

	public void setName(String name) {
		this.name = name;
	}
}