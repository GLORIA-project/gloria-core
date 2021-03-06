/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.experiment.operations;

import java.util.List;

import eu.gloria.gs.services.experiment.base.operations.ExperimentOperationException;
import eu.gloria.gs.services.experiment.base.parameters.ExperimentParameterException;
import eu.gloria.gs.services.experiment.base.parameters.NoSuchParameterException;
import eu.gloria.gs.services.experiment.base.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.repository.rt.RTRepositoryException;
import eu.gloria.gs.services.repository.rt.data.DeviceType;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
public class LoadDeviceName extends TeleOperation {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.gloria.gs.services.experiment.operations.ServiceOperation#execute()
	 */
	@Override
	public void execute() throws ExperimentOperationException {
		String rtParameter = (String) this.getArguments()[0];
		String deviceOrderParameter = (String) this.getArguments()[1];
		String deviceTypeParameter = (String) this.getArguments()[2];

		String rtName;
		try {
			rtName = (String) this.getContext().getExperimentContext()
					.getParameterValue(rtParameter);
		} catch (NoSuchParameterException | ExperimentNotInstantiatedException
				| ExperimentParameterException e) {
			throw new ExperimentOperationException(e.getAction());
		}

		int deviceOrder;
		try {
			deviceOrder = (Integer) this.getContext().getExperimentContext()
					.getParameterValue(deviceOrderParameter);
		} catch (NoSuchParameterException | ExperimentNotInstantiatedException
				| ExperimentParameterException e) {
			throw new ExperimentOperationException(e.getAction());
		}

		DeviceType deviceType;
		try {
			String deviceTypeStr = (String) this.getContext()
					.getExperimentContext()
					.getParameterValue(deviceTypeParameter);

			deviceType = DeviceType.valueOf(deviceTypeStr);
		} catch (NoSuchParameterException | ExperimentNotInstantiatedException
				| ExperimentParameterException e) {
			throw new ExperimentOperationException(e.getAction());
		}

		List<String> deviceNames = null;

		try {
			deviceNames = this.getRTRepository().getRTDeviceNames(rtName,
					deviceType);
		} catch (RTRepositoryException e) {
			throw new ExperimentOperationException(e.getAction());
		}

		String deviceName = null;

		if (deviceNames != null) {
			deviceName = deviceNames.get(deviceOrder);
		}

		try {

			this.getContext()
					.getExperimentContext()
					.setParameterValue((String) this.getArguments()[3],
							deviceName);
		} catch (NoSuchParameterException | ExperimentNotInstantiatedException
				| ExperimentParameterException e) {
			throw new ExperimentOperationException(e.getAction());
		}
	}

}
