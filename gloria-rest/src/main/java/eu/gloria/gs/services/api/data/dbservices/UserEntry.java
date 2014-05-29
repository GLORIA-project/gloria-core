package eu.gloria.gs.services.api.data.dbservices;

import java.util.Date;

public class UserEntry {

	private String name;
	private String password;
	private String roles;
	private String token;
	private Date tokenCreationDate;
	private Date tokenUpdateDate;
	private int active;
	private String locale;
	private String remote;
	private String agent;

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Date getTokenUpdateDate() {
		return tokenUpdateDate;
	}

	public void setTokenUpdateDate(Date tokenUpdateDate) {
		this.tokenUpdateDate = tokenUpdateDate;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getTokenCreationDate() {
		return tokenCreationDate;
	}

	public void setTokenCreationDate(Date tokenCreationTime) {
		this.tokenCreationDate = tokenCreationTime;
	}
}
