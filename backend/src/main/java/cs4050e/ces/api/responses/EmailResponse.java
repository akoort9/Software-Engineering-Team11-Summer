package cs4050e.ces.api.responses;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;

import cs4050e.ces.api.responses.EmailTemplates.Template;
import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.users.User;

/** Represents a response using an email. */
public class EmailResponse implements Response {
	/** Access to the database. */
	private static final DataHandler db = DataHandler.getInstance();

	/**
	 * Sends an email to a {@code User} with a given code.
	 * @param template The email template to use.
	 * @param user The user to send the email to.
	 * @param code The user's verification code. This can be null
	 * if the email does not require it (EDIT_PROFILE)
	 */
	public static boolean send(Template template, User user, String code) {
		if (!db.userExists(user.getEmail())) {
			return false;
		} // if

		Email email = null;
		switch(template) {
			case VERIFICATION:
				if (!code.isEmpty()) {
					email = EmailTemplates.getVerificationEmail(user, code);
					break;
				} else {
					return false;
				} // if-else
			case PASSWORD_RESET:
				if (!code.isEmpty()) {
					email = EmailTemplates.getPasswordResetEmail(user, code);
					break;
				} else {
					return false;
				} // if-else
			case EDIT_PROFILE:
				email = EmailTemplates.getEditProfileEmail(user);
				break;
			default:
				return false;
		} // switch

		buildMailer().sendMail(email);
		return true;
	} // send

    /**
	 * Builds a {@code Mailer} configured to send through the project's
	 * Gmail account.
	 * @return A configured {@code Mailer}.
	 */
	private static Mailer buildMailer() {
		return MailerBuilder
			// i'll actually kill you if you try and use this to get into my account
			.withSMTPServer("smtp.gmail.com", 587, "qwertyshepherd@gmail.com", "cqbw bpvx xtpk befo")
			.withTransportStrategy(TransportStrategy.SMTP_TLS)
			.buildMailer();
	} // buildMailer
} // EmailResponse
