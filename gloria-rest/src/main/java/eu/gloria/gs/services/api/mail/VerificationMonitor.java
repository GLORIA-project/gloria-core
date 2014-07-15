package eu.gloria.gs.services.api.mail;

import java.util.Date;
import java.util.List;

import eu.gloria.gs.services.api.data.UserDataAdapter;
import eu.gloria.gs.services.api.data.dbservices.UserDataAdapterException;
import eu.gloria.gs.services.api.data.dbservices.UserVerificationEntry;
import eu.gloria.gs.services.core.LogEntry;
import eu.gloria.gs.services.core.LogStore;
import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.core.tasks.ServerThread;
import eu.gloria.gs.services.log.action.Action;
import eu.gloria.gs.services.log.action.LogType;
import eu.gloria.gs.services.repository.user.UserRepositoryException;
import eu.gloria.gs.services.repository.user.UserRepositoryInterface;
import eu.gloria.gs.services.repository.user.data.UserInformation;

public class VerificationMonitor extends ServerThread {

	private UserDataAdapter adapter;
	private LogStore logStore;
	private String username;
	private String password;
	private SendMailSSL mailSender;
	private static boolean analyzeWaitingOnes = true;
	private static int waitingCount = 0;
	private static int waitingOld = 0;
	private static int MS_PER_5DAY = 5 * 86400000;
	private UserRepositoryInterface users = null;

	/**
	 * @param name
	 */
	public VerificationMonitor() {
		super(VerificationMonitor.class.getSimpleName());
	}

	public void setAdapter(UserDataAdapter adapter) {
		this.adapter = adapter;
	}

	public void setLogStore(LogStore logStore) {
		this.logStore = logStore;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public SendMailSSL getMailSender() {
		return mailSender;
	}

	public void setMailSender(SendMailSSL mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void end() {
		super.end();
	}

	@Override
	protected void doWork() {

		try {
			Thread.sleep(5000);

			if (waitingCount < 2) {
				waitingCount++;
			} else {
				analyzeWaitingOnes = true;
				waitingCount = 0;
			}

			waitingOld++;
		} catch (InterruptedException e) {
			log(LogType.WARNING, e.getMessage());
		}

		/*try {
			GSClientProvider.setCredentials(this.username, this.password);
		} catch (Exception e) {
			log(LogType.ERROR, e.getMessage());
		}

		try {
			List<UserVerificationEntry> remaining = adapter
					.getPendingVerifications();

			for (UserVerificationEntry entry : remaining) {
				this.mailSender.sendNotification(entry.getEmail(),
						entry.getAlias(), entry.getCode());
				this.adapter.setVerificationSent(entry.getAlias());
			}
		} catch (UserDataAdapterException e) {
			log(LogType.ERROR, e.getAction());
		} catch (Exception e) {
			log(LogType.ERROR, e.getMessage());
		}

		try {
			List<UserVerificationEntry> resets = adapter
					.getPendingResetRequests();

			for (UserVerificationEntry entry : resets) {
				this.mailSender.sendReset(entry.getEmail(), entry.getAlias(),
						entry.getCode(), entry.getNewPassword());
				this.adapter.setWaitForReset(entry.getAlias());
			}
		} catch (UserDataAdapterException e) {
			log(LogType.ERROR, e.getAction());
		} catch (Exception e) {
			log(LogType.ERROR, e.getMessage());
		}

		try {
			List<UserVerificationEntry> resets = adapter
					.getPendingChangePasswordRequests();

			for (UserVerificationEntry entry : resets) {
				this.mailSender.sendChangePassword(entry.getEmail(),
						entry.getAlias(), entry.getCode(),
						entry.getNewPassword());
				this.adapter.setWaitForChangePassword(entry.getAlias());
			}
		} catch (UserDataAdapterException e) {
			log(LogType.ERROR, e.getAction());
		} catch (Exception e) {
			log(LogType.ERROR, e.getMessage());
		}

		if (analyzeWaitingOnes) {
			analyzeWaitingOnes = false;

			Date now = new Date();

			try {

				List<UserVerificationEntry> pendingResets = adapter
						.getPendingResetRequests();

				for (UserVerificationEntry entry : pendingResets) {
					if (now.getTime() - entry.getSentDate().getTime() > MS_PER_5DAY) {
						adapter.setVerificationObsolete(entry.getAlias());
					}
				}
			} catch (UserDataAdapterException e) {
				log(LogType.ERROR, e.getAction());
			} catch (Exception e) {
				log(LogType.ERROR, e.getMessage());
			}

			try {

				List<UserVerificationEntry> waitingResets = adapter
						.getWaitingResetRequests();

				for (UserVerificationEntry entry : waitingResets) {
					if (now.getTime() - entry.getResetRequestDate().getTime() > MS_PER_5DAY) {
						adapter.setResetObsolete(entry.getAlias());
					}
				}

			} catch (UserDataAdapterException e) {
				log(LogType.ERROR, e.getAction());
			} catch (Exception e) {
				log(LogType.ERROR, e.getMessage());
			}

			try {

				List<UserVerificationEntry> waitingResets = adapter
						.getWaitingChangePasswordRequests();

				for (UserVerificationEntry entry : waitingResets) {
					if (now.getTime() - entry.getChPassRequestDate().getTime() > MS_PER_5DAY) {
						adapter.setChangePasswordObsolete(entry.getAlias());
					}
				}

			} catch (UserDataAdapterException e) {
				log(LogType.ERROR, e.getAction());
			} catch (Exception e) {
				log(LogType.ERROR, e.getMessage());
			}
		}*/

		// BORRAR ESTO ----------------------------------
		/*if (waitingOld == 10) {
			String alias = null;
			List<String> olds = adapter.getOldUsers();
			if (olds != null && olds.size() > 0) {
				String email = olds.get(0);

				try {
					if (users == null) {
						users = GSClientProvider.getUserRepositoryClient();
					}

					UserInformation userInfo = users.getUserInformation(email);

					if (alias == null || userInfo.getAlias() == null
							|| userInfo.getAlias().equals("")) {
						alias = userInfo.getName();
					}

					if (userInfo.getAlias() != null) {
						alias = userInfo.getAlias();
					}

					if (!adapter.containsVerification(alias, email)) {
						adapter.createEncodedVerification(alias, email,
								userInfo.getPassword());
					}

				} catch (UserDataAdapterException e) {
				} catch (UserRepositoryException e) {
				}

				try {
					adapter.requestReset(alias, email);
				} catch (UserDataAdapterException e) {
				} catch (Exception e) {
				}
			}

			waitingOld = 0;
		}*/
	}

	private void processLogEntry(LogEntry entry, Action action) {
		entry.setUsername(this.username);
		entry.setDate(new Date());

		entry.setAction(action);
		this.logStore.addEntry(entry);
	}
}
