package eu.gloria.gs.services.api.mail;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMailSSL {

	private static Properties hostMailProps;
	private String hostAddress;
	private String hostPassword;
	private String apiAddress;

	static {
		hostMailProps = new Properties();
		hostMailProps.put("mail.smtp.host", "smtp.gmail.com");
		hostMailProps.put("mail.smtp.socketFactory.port", "465");
		hostMailProps.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		hostMailProps.put("mail.smtp.auth", "true");
		hostMailProps.put("mail.smtp.port", "465");
	}

	public String getApiAddress() {
		return apiAddress;
	}

	public void setApiAddress(String apiAddress) {
		this.apiAddress = apiAddress;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public String getHostPassword() {
		return hostPassword;
	}

	public void setHostPassword(String hostPassword) {
		this.hostPassword = hostPassword;
	}

	private void sendMail(String userEmail, String subject, String content) {
		Session session = Session.getDefaultInstance(hostMailProps,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(hostAddress,
								hostPassword);
					}
				});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(hostAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(userEmail));

			message.setContent(content, "text/html; charset=utf-8");
			message.setSubject(subject);

			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendNotification(String userEmail, String alias, String code)
			throws Exception {

		String subject = "Welcome to GLORIA! Please verify your address";
		String content = "<img src=http://gloria-project.eu/wp-content/uploads/2012/10/banner-transpa-notext-250.png>";
		String url = this.apiAddress + "/mail?check&code=" + code;

		content += "<p>Hi " + alias + "!</p>";
		content += "<p><a href=" + url
				+ ">Verify your email address now</a></p>";
		content += "<br>";
		content += "<p>Sincerely,<br>GLORIA Team</p>";
		content += "<p><i>Follow us in <a href=https://www.facebook.com/GLORIAProject?fref=ts>Facebook</a><i></p>";

		this.sendMail(userEmail, subject, content);
	}

	public void sendReset(String userEmail, String alias, String code,
			String password) throws Exception {

		String subject = "GLORIA account reset";
		String content = "<img src=http://gloria-project.eu/wp-content/uploads/2012/10/banner-transpa-notext-250.png>";
		String url = this.apiAddress + "/mail?reset&code=" + code;

		content += "<p>Hi " + alias + "!</p>";
		content += "<p>If you follow the link below, your new password will be <strong>"
				+ password
				+ "</strong></p><a href="
				+ url
				+ ">Click to activate the new password</a></p>";
		content += "<br>";
		content += "<p>Sincerely,<br>GLORIA Team</p>";
		content += "<p><i>Follow us in <a href=https://www.facebook.com/GLORIAProject?fref=ts>Facebook</a><i></p>";

		this.sendMail(userEmail, subject, content);
	}

	public void sendChangePassword(String userEmail, String alias, String code,
			String password) throws Exception {

		String subject = "GLORIA account password change";
		String content = "<img src=http://gloria-project.eu/wp-content/uploads/2012/10/banner-transpa-notext-250.png>";
		String url = this.apiAddress + "/mail?password&code=" + code;

		content += "<p>Hi " + alias + "!</p>";
		content += "<p>If you follow the link below, your new password will be activated.</p><a href="
				+ url + ">Click to activate the new password</a></p>";
		content += "<br>";
		content += "<p>Sincerely,<br>GLORIA Team</p>";
		content += "<p><i>Follow us in <a href=https://www.facebook.com/GLORIAProject?fref=ts>Facebook</a><i></p>";

		this.sendMail(userEmail, subject, content);
	}

}