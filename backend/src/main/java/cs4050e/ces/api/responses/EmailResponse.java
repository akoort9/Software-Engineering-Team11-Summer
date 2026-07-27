package cs4050e.ces.api.responses;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;

import cs4050e.ces.db.payment.Card;
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
	 * @param code The user's verification code. This can be null unless needed.
	 * @param code The card being modified. This can be null unless needed.
	 */
	public static boolean send(EmailTemplates.Template template, User user, String code, Card card) {
		// error handling
		if (template == null ||
			user == null ||
			!db.userExists(user.getEmail())) {
			return false;
		} // if

		Email email = null;
		switch(template) {
			case VERIFICATION:
				if (code.isEmpty()) { return false; }
				email = EmailTemplates.getVerificationEmail(user, code);
				break;
			case PASSWORD_RESET:
				if (code.isEmpty()) { return false; }
				email = EmailTemplates.getPasswordResetEmail(user, code);
				break;
			case ACCOUNT_UPDATED:
				email = EmailTemplates.getAccountUpdatedEmail(user);
				break;
			case CARD_ADDED:
				if (card == null) { return false; }
				email = EmailTemplates.getCardAddedEmail(user, null);
				break;
			case CARD_REMOVED:
				if (card == null) { return false; }
				email = EmailTemplates.getCardRemovedEmail(user, null);
				break;
			case CARD_UPDATED:
				if (card == null) { return false; }
				email = EmailTemplates.getCardUpdatedEmail(user, null);
				break;
			default:
				return false;
		} // switch

		if (email == null) {
			return false;
		} // if

		buildMailer().sendMail(email);
		return true;
	} // send

	/**
	 * Sends an email to a {@code User} for templates that don't need a code
 	* (e.g. account-update notifications).
	* @param template The email template to use.
	* @param user The user to send the email to.
	*/
	public static boolean send(Template template, User user) {
		if (!db.userExists(user.getEmail())) {
			return false;
		} // if

		Email email;
		switch (template) {
			case ACCOUNT_UPDATED:
				email = EmailTemplates.getAccountUpdatedEmail(user);
				break;
			default:
				return false;
		} // switch

		buildMailer().sendMail(email);
		return true;
	} // send

	/**
	 * Sends an email to a {@code User} confirming a new payment card was added.
	 * @param template The email template to use (must be CARD_ADDED).
	 * @param user The user to send the email to.
	 * @param card The card that was added.
	 */
	public static boolean send(Template template, User user, Card card) {
		if (!db.userExists(user.getEmail())) {
			return false;
		} // if

		Email email;
		switch (template) {
			case CARD_ADDED:
				email = EmailTemplates.getCardAddedEmail(user, card);
				break;
			case CARD_UPDATED:
				email = EmailTemplates.getCardUpdatedEmail(user, card);
				break;
			case CARD_REMOVED:
				email = EmailTemplates.getCardRemovedEmail(user, card);
				break;
			default:
				return false;
		} // switch

		buildMailer().sendMail(email);
		return true;
	} // send

	/**
	 * Sends a promotional offer email to a {@code User}.
	 * @param user The customer to send it to.
	 * @param promoCode The code the customer enters to redeem the offer.
	 * @param percentOff The percentage taken off the price.
	 * @param expiration The date the promotion expires.
	 * @return {@code true} if the email was sent, {@code false} otherwise.
	 */
	public static boolean sendPromotion(User user, String promoCode, double percentOff,
			java.time.LocalDate expiration) {
		if (user == null || !db.userExists(user.getEmail())) {
			return false;
		} // if

		Email email = EmailTemplates.getPromotionEmail(user, promoCode, percentOff, expiration);
		buildMailer().sendMail(email);
		return true;
	} // sendPromotion

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
