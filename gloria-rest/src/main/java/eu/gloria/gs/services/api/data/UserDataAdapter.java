package eu.gloria.gs.services.api.data;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;

import eu.gloria.gs.services.api.data.dbservices.UserDataAdapterException;
import eu.gloria.gs.services.api.data.dbservices.UserDataService;
import eu.gloria.gs.services.api.data.dbservices.UserEntry;
import eu.gloria.gs.services.api.data.dbservices.UserVerificationEntry;
import eu.gloria.gs.services.api.data.dbservices.UserVerificationService;
import eu.gloria.gs.services.api.security.SHA1;
import eu.gloria.gs.services.log.action.Action;

public class UserDataAdapter {

	private UserDataService userService;
	private UserVerificationService verificationService;
	private static SessionIdentifierGenerator tokenizer = new SessionIdentifierGenerator();

	public UserDataAdapter() {

	}

	public void setUserDataService(UserDataService service) {
		this.userService = service;
		userService.create();
	}

	public void setUserVerificationService(UserVerificationService service) {
		this.verificationService = service;
		verificationService.create();
	}

	public String createToken(String name, String password, String locale,
			String userAgent, String remote) throws UserDataAdapterException {

		String token = tokenizer.nextSessionId();
		UserEntry entry = new UserEntry();

		entry.setName(name);
		entry.setPassword(password);
		entry.setToken(token);
		entry.setTokenCreationDate(new Date());
		entry.setTokenUpdateDate(new Date());
		entry.setLocale(locale);
		entry.setRemote(remote);
		entry.setAgent(userAgent);

		this.userService.save(entry);

		return token;
	}

	public void activateToken(String token) {
		userService.setActive(token);
	}

	public void deactivateToken(String token) {
		userService.setInactive(token);
	}

	public void deactivateOtherTokens(String name, String token) {
		userService.setOthersInactive(name, token);
	}

	public boolean containsUser(String name) throws UserDataAdapterException {

		return userService.containsUser(name);
	}

	public boolean containsToken(String name) throws UserDataAdapterException {

		return userService.containsUser(name);
	}

	public List<UserEntry> getUserInformation(String name)
			throws UserDataAdapterException {

		if (this.containsUser(name)) {

			return this.userService.getActive(name);
		}

		return null;
	}

	public UserEntry getUserInformationByToken(String token)
			throws UserDataAdapterException {

		UserEntry entry = this.userService.getByToken(token);

		if (entry != null) {
			return entry;
		}

		Action log = new Action();
		log.put("message", "The token does not exist");

		throw new UserDataAdapterException(log);
	}

	public void updateLastDate(String token) throws UserDataAdapterException {

		try {
			this.userService.setTokenUpdateDate(token, new Date());
		} catch (Exception e) {

			Action log = new Action();
			log.put("message", "Error updating token date");

			throw new UserDataAdapterException(log);
		}
	}

	public String createClearVerification(String alias, String email,
			String clearPassword) throws UserDataAdapterException {

		boolean newEntry = true;
		UserVerificationEntry entry = null;

		if (this.verificationService.containsAlias(alias)) {

			if (this.verificationService.isWaitingForVerification(alias)) {
				Action log = new Action();
				log.put("message", "User verification already exists");

				throw new UserDataAdapterException(log);
			} else {
				entry = this.verificationService.getByAlias(alias);
				newEntry = false;
			}
		} else {
			entry = new UserVerificationEntry();
		}

		entry.setAlias(alias);
		entry.setEmail(email);
		entry.setPassword(SHA1.encode(clearPassword));

		String code = tokenizer.nextSessionId();
		entry.setCode(code);

		if (newEntry) {
			this.verificationService.save(entry);
		} else {
			this.verificationService.clear(alias);
			this.setPassword(alias, entry.getPassword());
		}

		return code;

	}

	public String createEncodedVerification(String alias, String email,
			String encPassword) throws UserDataAdapterException {
		UserVerificationEntry entry = new UserVerificationEntry();

		if (!this.verificationService.containsEmail(email)) {
			entry.setAlias(alias);
			entry.setEmail(email);
			if (encPassword == null) {
				encPassword = "";
			}
			entry.setPassword(encPassword);

			String code = tokenizer.nextSessionId();
			entry.setCode(code);

			this.verificationService.saveChecked(entry);

			return code;
		}

		Action log = new Action();
		log.put("message", "User verification already exists");

		throw new UserDataAdapterException(log);
	}

	public void requestReset(String alias, String email)
			throws UserDataAdapterException {

		String newPassword = tokenizer.nextSessionId().substring(0, 8);

		if (alias != null) {
			this.verificationService.setNewPasswordByAlias(alias, newPassword);
			this.verificationService.setResetRequestByAlias(alias);
		} else if (email != null) {
			this.verificationService.setNewPasswordByEmail(email, newPassword);
			this.verificationService.setResetRequestByEmail(email);
		} else {
			Action log = new Action();
			log.put("cause", "not enough identifiers");
			throw new UserDataAdapterException(log);
		}
	}

	public void requestChangePassword(String alias, String email,
			String newPassword) throws UserDataAdapterException {

		if (alias != null) {
			this.verificationService.setNewPasswordByAlias(alias, SHA1.encode(newPassword));
			this.verificationService.setChangePasswordRequestByAlias(alias);
		} else if (email != null) {
			this.verificationService.setNewPasswordByEmail(email, SHA1.encode(newPassword));
			this.verificationService.setChangePasswordRequestByEmail(email);
		} else {
			Action log = new Action();
			log.put("cause", "not enough identifiers");
			throw new UserDataAdapterException(log);
		}
	}

	public void setPassword(String alias, String password)
			throws UserDataAdapterException {
		try {
			this.verificationService.setPassword(alias, password);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void clearReset(String alias) throws UserDataAdapterException {
		try {
			this.verificationService.clearResetRequest(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void clearChangePassword(String alias)
			throws UserDataAdapterException {
		try {
			this.verificationService.clearChangePasswordRequest(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setWaitForReset(String alias) throws UserDataAdapterException {
		try {
			this.verificationService.setWaitingForReset(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setWaitForChangePassword(String alias)
			throws UserDataAdapterException {
		try {
			this.verificationService.setWaitingForChangePassword(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setResetObsolete(String alias) throws UserDataAdapterException {
		try {
			this.verificationService.setResetObsolete(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setChangePasswordObsolete(String alias)
			throws UserDataAdapterException {
		try {
			this.verificationService.setChangePasswordObsolete(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setVerificationObsolete(String alias)
			throws UserDataAdapterException {
		try {
			this.verificationService.setVerificationObsolete(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean isWaitingForReset(String alias)
			throws UserDataAdapterException {
		try {
			return this.verificationService.isWaitingForReset(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean isWaitingForChangePassword(String alias)
			throws UserDataAdapterException {
		try {
			return this.verificationService.isWaitingForChangePassword(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean isWaitingForVerification(String alias)
			throws UserDataAdapterException {
		try {
			return this.verificationService.isWaitingForVerification(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean isObsolete(String alias) throws UserDataAdapterException {
		try {
			return this.verificationService.isObsolete(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean isResetRequested(String alias)
			throws UserDataAdapterException {
		try {
			return this.verificationService.isResetRequested(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean isChangePasswordRequested(String alias)
			throws UserDataAdapterException {
		try {
			return this.verificationService.isChangePasswordRequested(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public UserVerificationEntry getVerificationByAlias(String alias)
			throws UserDataAdapterException {
		UserVerificationEntry entry = this.verificationService
				.getByAlias(alias);

		if (entry != null) {
			return entry;
		}

		Action log = new Action();
		log.put("message", "Alias does not exist");
		log.put("alias", alias);

		throw new UserDataAdapterException(log);
	}

	public UserVerificationEntry getVerificationByEmail(String email)
			throws UserDataAdapterException {
		UserVerificationEntry entry = this.verificationService
				.getByEmail(email);

		if (entry != null) {
			return entry;
		}

		Action log = new Action();
		log.put("message", "Email does not exist");
		log.put("alias", email);

		throw new UserDataAdapterException(log);
	}

	public UserVerificationEntry getVerificationByCode(String code)
			throws UserDataAdapterException {
		UserVerificationEntry entry = this.verificationService.getByCode(code);

		if (entry != null) {
			return entry;
		}

		Action log = new Action();
		log.put("message", "Code does not exist");
		log.put("code", code);

		throw new UserDataAdapterException(log);
	}

	public List<UserVerificationEntry> getPendingVerifications()
			throws UserDataAdapterException {
		List<UserVerificationEntry> entries = this.verificationService
				.getPendingVerifications();

		if (entries != null) {
			return entries;
		}

		Action log = new Action();
		log.put("message", "There are no remaining verifications");

		throw new UserDataAdapterException(log);
	}

	public List<UserVerificationEntry> getPendingResetRequests()
			throws UserDataAdapterException {
		List<UserVerificationEntry> entries = this.verificationService
				.getPendingResetRequests();

		if (entries != null) {
			return entries;
		}

		Action log = new Action();
		log.put("message", "There are no pending reset requests");

		throw new UserDataAdapterException(log);
	}

	public List<UserVerificationEntry> getPendingChangePasswordRequests()
			throws UserDataAdapterException {
		List<UserVerificationEntry> entries = this.verificationService
				.getPendingChangePasswordRequests();

		if (entries != null) {
			return entries;
		}

		Action log = new Action();
		log.put("message", "There are no pending change password requests");

		throw new UserDataAdapterException(log);
	}

	public List<UserVerificationEntry> getWaitingResetRequests()
			throws UserDataAdapterException {
		List<UserVerificationEntry> entries = this.verificationService
				.getWaitingResetRequests();

		if (entries != null) {
			return entries;
		}

		Action log = new Action();
		log.put("message", "There are no waiting reset requests");

		throw new UserDataAdapterException(log);
	}

	public List<UserVerificationEntry> getWaitingChangePasswordRequests()
			throws UserDataAdapterException {
		List<UserVerificationEntry> entries = this.verificationService
				.getWaitingChangePasswordRequests();

		if (entries != null) {
			return entries;
		}

		Action log = new Action();
		log.put("message", "There are no waiting change password requests");

		throw new UserDataAdapterException(log);
	}

	public void setVerificationSent(String alias)
			throws UserDataAdapterException {

		try {
			this.verificationService.setAlreadySent(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setVerificationReady(String alias)
			throws UserDataAdapterException {

		try {
			this.verificationService.setReadyToSend(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setVerificationChecked(String alias)
			throws UserDataAdapterException {

		try {
			this.verificationService.setVerified(alias);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public void setResetPassword(String alias, String password)
			throws UserDataAdapterException {

		try {
			this.verificationService.setNewPasswordByAlias(alias, password);
		} catch (PersistenceException e) {
			throw new UserDataAdapterException();
		}
	}

	public boolean containsVerification(String alias, String email)
			throws UserDataAdapterException {

		return verificationService.containsAlias(alias)
				|| verificationService.containsEmail(email);
	}

}
