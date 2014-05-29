package eu.gloria.gs.services.api.data.dbservices;

import java.util.Date;

public class UserVerificationEntry {

	private String alias;
	private String password;
	private String code;
	private Date creationDate;
	private Date sentDate;
	private Date verificationDate;
	private String email;
	private String status;
	private Integer reset;
	private Integer chpass;
	private String newPassword;
	private Date resetRequestDate;
	private Date resetDoneDate;
	private Date chpassRequestDate;
	private Date chpassDoneDate;

	public Integer getChpass() {
		return chpass;
	}

	public void setChpass(Integer chpass) {
		this.chpass = chpass;
	}

	public Date getChPassRequestDate() {
		return chpassRequestDate;
	}

	public void setChPassRequestDate(Date chPassRequestDate) {
		this.chpassRequestDate = chPassRequestDate;
	}

	public Date getChPassDoneDate() {
		return chpassDoneDate;
	}

	public void setChPassDoneDate(Date chPassDoneDate) {
		this.chpassDoneDate = chPassDoneDate;
	}

	public Date getResetRequestDate() {
		return resetRequestDate;
	}

	public void setResetRequestDate(Date resetRequestDate) {
		this.resetRequestDate = resetRequestDate;
	}

	public Date getResetDoneDate() {
		return resetDoneDate;
	}

	public void setResetDoneDate(Date resetDoneDate) {
		this.resetDoneDate = resetDoneDate;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String new_password) {
		this.newPassword = new_password;
	}

	public Integer getReset() {
		return reset;
	}

	public void setReset(Integer reset) {
		this.reset = reset;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public Date getVerificationDate() {
		return verificationDate;
	}

	public void setVerificationDate(Date verificationDate) {
		this.verificationDate = verificationDate;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
