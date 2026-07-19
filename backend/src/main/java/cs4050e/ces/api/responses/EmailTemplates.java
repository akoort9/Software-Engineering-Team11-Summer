package cs4050e.ces.api.responses;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;

import cs4050e.ces.db.users.User;

/** Holds templates of emails to send. */
public class EmailTemplates {
    /** The 'from' address for all these emails. */
    static final String CES_FROM_ADDRESS = "qwertyshepherd@gmail.com";

    /** The name of the sender for all these emails. */
    static final String CES_NAME = "Cinema E-booking System";

    /**
     * Creates a user verification email.
     * @param user The user to send it to.
     * @param code The user's verification code.
     * @return An {@code Email} ready to send.
     */
    static Email getVerificationEmail(User user, String code) {
        return EmailBuilder.startingBlank()
		    .from(CES_NAME, CES_FROM_ADDRESS)
		    .to(user.getName(), user.getEmail())
		    .withSubject("Cinema E-booking System: Your verification code")
		    .withPlainText("Hello " + user.getName() + ", your verification code is: " +
			    code + ". Please use this code to finish setting up your account.")
		    .buildEmail();
    } // getVerificationEmail

    /**
     * Creates a password reset email.
     * @param user The user to send it to.
     * @param code The user's verification code.
     * @return An {@code Email} ready to send.
     */
    static Email getPasswordResetEmail(User user, String code) {
        return EmailBuilder.startingBlank()
			.from(CES_NAME, CES_FROM_ADDRESS)
			.to(user.getName(), user.getEmail())
			.withSubject("Cinema E-booking System: Your password reset code")
			.withPlainText("Hello " + user.getName() + ", your password reset code is: " +
				code + ". Use this code to reset your password. If you " +
				"didn't request this, you can safely ignore this email.")
			.buildEmail();
    } // getPasswordResetEmail
} // EmailTemplates
