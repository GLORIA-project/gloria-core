package eu.gloria.gs.services.api.mail;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import eu.gloria.gs.services.api.data.UserDataAdapter;
import eu.gloria.gs.services.api.data.dbservices.UserDataAdapterException;
import eu.gloria.gs.services.api.data.dbservices.UserVerificationEntry;
import eu.gloria.gs.services.api.security.SHA1;
import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.repository.user.UserRepositoryException;
import eu.gloria.gs.services.repository.user.UserRepositoryInterface;
import eu.gloria.gs.services.repository.user.data.UserInformation;

import java.io.*;
import java.util.Map;

public class MailServlet extends HttpServlet {

	private static final long serialVersionUID = -4773473221399464567L;

	private UserDataAdapter userAdapter;
	private String password;
	private String username;
	private String redirectUrl;

	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);

		ApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		userAdapter = (UserDataAdapter) context.getBean(conf
				.getInitParameter("userAdapter"));
		username = (String) context.getBean(conf.getInitParameter("username"));
		password = (String) context.getBean(conf.getInitParameter("password"));
		redirectUrl = (String) context.getBean(conf
				.getInitParameter("redirectUrl"));
	}

	private void treatEmailVerification(HttpServletResponse res, String code)
			throws IOException, ServletException {
		try {
			UserVerificationEntry entry = userAdapter
					.getVerificationByCode(code);

			if (entry == null) {
				throw new ServletException("Invalid verification request");
			}

			if (userAdapter.isWaitingForVerification(entry.getAlias())) {
				GSClientProvider.setCredentials(this.username, this.password);

				// CREATE THE NEW USER

				UserRepositoryInterface userRepository = GSClientProvider
						.getUserRepositoryClient();
				userRepository.createUser(entry.getEmail(), entry.getAlias());
				userRepository.activateUser(entry.getEmail(), entry.getPassword());
				
				userAdapter.deactivateOtherTokens(entry.getEmail(), "");

				userAdapter.setVerificationChecked(entry.getAlias());
			}

			res.setContentType("text/html;charset=UTF-8");
			res.setHeader("pragma", "no-cache");
			res.setHeader("Cache-Control", "no-cache");

			res.sendRedirect(this.redirectUrl);

		} catch (UserDataAdapterException | UserRepositoryException e) {
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();

			out.println("<html>");
			out.println("<body>");
			out.println("<img src=http://gloria-project.eu/wp-content/uploads/2012/10/banner-transpa-notext-250.png>");
			out.println("<h1>There was a problem</h1>");
			out.println("<hr>");
			out.println("<p>" + e.getMessage() + "</p>");
			out.println("<hr>");
			out.println("<p>Sorry for the inconveniences. Please, contact <strong>webmaster@gloria-project.eu</strong> to receive support.</p>");
			out.println("<p><strong>The GLORIA team</strong></p>");
			out.println("<p><i>Follow us in <a href=https://www.facebook.com/GLORIAProject?fref=ts>Facebook</a><i></p>");
			out.println("</body>");
			out.println("</html>");
		}
	}

	private void treatAccountReset(HttpServletResponse res, String code)
			throws IOException, ServletException {
		try {
			UserVerificationEntry entry = userAdapter
					.getVerificationByCode(code);

			if (entry == null) {
				throw new ServletException("Invalid reset request");
			}

			String alias = entry.getAlias();

			if (userAdapter.isWaitingForReset(alias)) {

				GSClientProvider.setCredentials(this.username, this.password);
				UserRepositoryInterface userRepository = GSClientProvider
						.getUserRepositoryClient();

				UserInformation userInfo = userRepository
						.getUserInformation(entry.getEmail());

				if (userInfo.getPassword() == null) {
					userRepository.activateUser(entry.getEmail(),
							SHA1.encode(entry.getNewPassword()));
				} else {
					userRepository.changePassword(entry.getEmail(),
							SHA1.encode(entry.getNewPassword()));
				}

				userAdapter.deactivateOtherTokens(entry.getEmail(), "");

				userAdapter.clearReset(alias);

				res.setContentType("text/html;charset=UTF-8");
				res.setHeader("pragma", "no-cache");
				res.setHeader("Cache-Control", "no-cache");

				res.sendRedirect(this.redirectUrl);

			} else {
				throw new ServletException("Reset request has expired");
			}

		} catch (UserDataAdapterException | UserRepositoryException e) {
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();

			out.println("<html>");
			out.println("<body>");
			out.println("<img src=http://gloria-project.eu/wp-content/uploads/2012/10/banner-transpa-notext-250.png>");
			out.println("<h1>There was a problem</h1>");
			out.println("<hr>");
			out.println("<p>" + e.getMessage() + "</p>");
			out.println("<hr>");
			out.println("<p>Sorry for the inconveniences. Please, contact <strong>webmaster@gloria-project.eu</strong> to receive support.</p>");
			out.println("<p><strong>The GLORIA team</strong></p>");
			out.println("<p><i>Follow us in <a href=https://www.facebook.com/GLORIAProject?fref=ts>Facebook</a><i></p>");
			out.println("</body>");
			out.println("</html>");
		}
	}

	private void treatChangePassword(HttpServletResponse res, String code)
			throws IOException, ServletException {
		try {
			UserVerificationEntry entry = userAdapter
					.getVerificationByCode(code);

			if (entry == null) {
				throw new ServletException("Invalid change password request");
			}

			String alias = entry.getAlias();

			if (userAdapter.isWaitingForChangePassword(alias)) {

				GSClientProvider.setCredentials(this.username, this.password);
				UserRepositoryInterface userRepository = GSClientProvider
						.getUserRepositoryClient();

				UserInformation userInfo = userRepository
						.getUserInformation(entry.getEmail());

				if (userInfo.getPassword() == null) {
					throw new ServletException(
							"Change password request cannot apply");
				} else {
					userRepository.changePassword(entry.getEmail(),
							entry.getNewPassword());
				}

				userAdapter.deactivateOtherTokens(entry.getEmail(), "");

				userAdapter.clearChangePassword(alias);

				res.setContentType("text/html;charset=UTF-8");
				res.setHeader("pragma", "no-cache");
				res.setHeader("Cache-Control", "no-cache");

				res.sendRedirect(this.redirectUrl);

			} else {
				throw new ServletException(
						"Change password request has expired");
			}

		} catch (UserDataAdapterException | UserRepositoryException e) {
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();

			out.println("<html>");
			out.println("<body>");
			out.println("<img src=http://gloria-project.eu/wp-content/uploads/2012/10/banner-transpa-notext-250.png>");
			out.println("<h1>There was a problem</h1>");
			out.println("<hr>");
			out.println("<p>" + e.getMessage() + "</p>");
			out.println("<hr>");
			out.println("<p>Sorry for the inconveniences. Please, contact <strong>webmaster@gloria-project.eu</strong> to receive support.</p>");
			out.println("<p><strong>The GLORIA team</strong></p>");
			out.println("<p><i>Follow us in <a href=https://www.facebook.com/GLORIAProject?fref=ts>Facebook</a><i></p>");
			out.println("</body>");
			out.println("</html>");
		}
	}

	public void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		Map<?, ?> parameters = req.getParameterMap();
		String code = null;

		if (parameters.containsKey("code")) {
			Object[] codeList = (Object[]) parameters.get("code");
			if (codeList instanceof String[]) {
				code = (String) codeList[0];

				if (code != null) {

					if (parameters.containsKey("check")) {
						this.treatEmailVerification(res, code);
					} else if (parameters.containsKey("reset")) {
						this.treatAccountReset(res, code);
					} else if (parameters.containsKey("password")) {
						this.treatChangePassword(res, code);
					}

				} else {
					throw new ServletException("Code not found");
				}
			} else {
				throw new ServletException("Bad link");
			}
		} else {
			throw new ServletException("Bad link");
		}
	}
}