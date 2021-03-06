/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.experiment.operations;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.experiment.base.parameters.NoSuchParameterException;
import eu.gloria.gs.services.experiment.base.operations.ExperimentOperationException;
import eu.gloria.gs.services.experiment.base.parameters.ExperimentParameterException;
import eu.gloria.gs.services.experiment.base.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.teleoperation.base.DeviceOperationFailedException;
import eu.gloria.gs.services.teleoperation.focuser.FocuserTeleoperationException;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
public class GetFocuserPosition extends TeleOperation {

	@Override
	public void execute() throws ExperimentOperationException {
		try {
			String rtNameParameter = (String) this.getArguments()[0];
			String rtName = (String) this.getContext().getExperimentContext()
					.getParameterValue(rtNameParameter);

			String focuserNameParameter = (String) this.getArguments()[1];
			String focuserName = (String) this.getContext()
					.getExperimentContext()
					.getParameterValue(focuserNameParameter);

			String positionParameter = (String) this.getArguments()[2];

			GSClientProvider.setCredentials(this.getUsername(),
					this.getPassword());			

			long position = -1;
			
			try {
				position = this.getFocuserTeleoperation().getPosition(rtName,
						focuserName);
			} catch (FocuserTeleoperationException e) {
				throw new ExperimentOperationException(e.getAction());
			} catch (DeviceOperationFailedException e) {
			}

			this.getContext().getExperimentContext()
					.setParameterValue(positionParameter, position);

		} catch (ExperimentParameterException | NoSuchParameterException
				| ExperimentNotInstantiatedException e) {
			throw new ExperimentOperationException(e.getAction());
		}
	}
}
