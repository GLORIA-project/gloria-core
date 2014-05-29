package eu.gloria.gs.services.api.data.dbservices;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserVerificationService {

	public void create();

	public UserVerificationEntry getByAlias(
			@Param(value = "alias_") String alias);

	public UserVerificationEntry getByEmail(
			@Param(value = "email_") String email);

	public UserVerificationEntry getByCode(@Param(value = "code_") String code);

	public void save(UserVerificationEntry entry);

	public void clear(@Param(value = "alias_") String alias);

	public void saveChecked(UserVerificationEntry entry);

	public void removeByAlias(@Param(value = "alias_") String alias);

	public Date removeByCode(@Param(value = "code_") String code);

	public boolean containsAlias(@Param(value = "alias_") String alias);

	public boolean containsEmail(@Param(value = "email_") String email);

	public void setAlreadySent(@Param(value = "alias_") String alias);

	public void setVerificationObsolete(@Param(value = "alias_") String alias);

	public void setPassword(@Param(value = "alias_") String alias,
			@Param(value = "password_") String password);

	public void setNewPasswordByAlias(@Param(value = "alias_") String alias,
			@Param(value = "password_") String password);
	
	public void setNewPasswordByEmail(@Param(value = "email_") String email,
			@Param(value = "password_") String password);

	public void setReadyToSend(@Param(value = "alias_") String alias);

	public void setVerified(@Param(value = "alias_") String alias);

	public void setChangePasswordRequestByAlias(
			@Param(value = "alias_") String alias);

	public void setResetRequestByAlias(@Param(value = "alias_") String alias);

	public void clearChangePasswordRequest(@Param(value = "alias_") String alias);

	public void clearResetRequest(@Param(value = "alias_") String alias);

	public void setChangePasswordObsolete(@Param(value = "alias_") String alias);

	public void setResetObsolete(@Param(value = "alias_") String alias);

	public boolean isChangePasswordObsolete(
			@Param(value = "alias_") String alias);

	public boolean isResetObsolete(@Param(value = "alias_") String alias);

	public void setWaitingForChangePassword(
			@Param(value = "alias_") String alias);

	public void setWaitingForReset(@Param(value = "alias_") String alias);

	public boolean isWaitingForReset(@Param(value = "alias_") String alias);

	public boolean isWaitingForChangePassword(
			@Param(value = "alias_") String alias);

	public boolean isWaitingForVerification(
			@Param(value = "alias_") String alias);

	public boolean isObsolete(@Param(value = "alias_") String alias);

	public boolean isChangePasswordRequested(
			@Param(value = "alias_") String alias);

	public boolean isResetRequested(@Param(value = "alias_") String alias);

	public void setChangePasswordRequestByEmail(
			@Param(value = "email_") String email);

	public void setResetRequestByEmail(@Param(value = "email_") String email);

	public List<UserVerificationEntry> getPendingVerifications();

	public List<UserVerificationEntry> getWaitingVerifications();

	public List<UserVerificationEntry> getPendingChangePasswordRequests();

	public List<UserVerificationEntry> getPendingResetRequests();

	public List<UserVerificationEntry> getWaitingResetRequests();

	public List<UserVerificationEntry> getWaitingChangePasswordRequests();

}
