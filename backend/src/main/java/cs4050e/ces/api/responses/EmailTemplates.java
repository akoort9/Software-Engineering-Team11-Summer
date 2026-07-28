package cs4050e.ces.api.responses;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;

import java.time.LocalDate;

import cs4050e.ces.db.users.User;
import cs4050e.ces.db.payment.Card;

/** Holds templates of emails to send. */
public class EmailTemplates {
    /** The 'from' address for all these emails. */
    static final String CES_FROM_ADDRESS = "qwertyshepherd@gmail.com";

    /** The name of the sender for all these emails. */
    static final String CES_NAME = "Cinema E-booking System";

    /** 
     * The template you wish to use for sending an email.
     * {@code ACCOUNT_UPDATED} - Account update email.
     * {@code CARD_ADDED} - Card added email.
     * {@code CARD_REMOVED} - Card removed email.
     * {@code CARD_UPDATED} - Card updated email.
     * {@code PASSWORD_RESET} - Password reset email.
     * {@code VERIFICATION} - Account verification email.
     * {@code PROMOTION} - Promotional offer email.
     */
        public enum Template {
                ACCOUNT_UPDATED,
                CARD_ADDED,
                CARD_REMOVED,
                CARD_UPDATED,
                PASSWORD_RESET,
                VERIFICATION,
                PROMOTION,
                TICKETS_BOOKED
            };

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

    /**
     * Creates an account-update notification email.
     * @param user The user whose account was updated.
     * @return An {@code Email} ready to send.
     */
    static Email getAccountUpdatedEmail(User user) {
        return EmailBuilder.startingBlank()
            .from(CES_NAME, CES_FROM_ADDRESS)
            .to(user.getName(), user.getEmail())
            .withSubject("Cinema E-booking System: Your account was updated")
            .withPlainText("Hello " + user.getName() + ", this is a confirmation that your " +
                "account details were just updated. If you didn't make this change, please " +
                "contact support immediately.")
            .buildEmail();
    } // getAccountUpdatedEmail

    /**
     * Creates a card-added confirmation email.
     * @param user The user whose account the card was added to.
     * @param card The card that was added.
     * @return An {@code Email} ready to send.
     */
    static Email getCardAddedEmail(User user, Card card) {
        String number = card.getCardNumber();
        String lastFour = number.length() >= 4 ? number.substring(number.length() - 4) : number;

        return EmailBuilder.startingBlank()
            .from(CES_NAME, CES_FROM_ADDRESS)
            .to(user.getName(), user.getEmail())
            .withSubject("Cinema E-booking System: New card added")
            .withPlainText("Hello " + user.getName() + ", a new card ending in " + lastFour +
                " was just added to your account. If you didn't make this change, please " +
                "contact support immediately.")
            .buildEmail();
    } // getCardAddedEmail

    /**
     * Creates a card-updated confirmation email.
     * @param user The user whose card was updated.
     * @param card The card's new details.
     * @return An {@code Email} ready to send.
     */
    static Email getCardUpdatedEmail(User user, Card card) {
        String lastFour = lastFourOf(card.getCardNumber());
        return EmailBuilder.startingBlank()
            .from(CES_NAME, CES_FROM_ADDRESS)
            .to(user.getName(), user.getEmail())
            .withSubject("Cinema E-booking System: Card updated")
            .withPlainText("Hello " + user.getName() + ", the card ending in " + lastFour +
                " on your account was just updated. If you didn't make this change, please " +
                "contact support immediately.")
            .buildEmail();
    } // getCardUpdatedEmail

    /**
     * Creates a card-removed confirmation email.
     * @param user The user whose card was removed.
     * @param card The card that was removed.
     * @return An {@code Email} ready to send.
     */
    static Email getCardRemovedEmail(User user, Card card) {
        String lastFour = lastFourOf(card.getCardNumber());
        return EmailBuilder.startingBlank()
            .from(CES_NAME, CES_FROM_ADDRESS)
            .to(user.getName(), user.getEmail())
            .withSubject("Cinema E-booking System: Card removed")
            .withPlainText("Hello " + user.getName() + ", the card ending in " + lastFour +
                " was just removed from your account. If you didn't make this change, please " +
                "contact support immediately.")
            .buildEmail();
    } // getCardRemovedEmail

    /**
     * Creates a promotional offer email.
     * @param user The customer to send it to.
     * @param promoCode The code the customer enters to redeem the offer.
     * @param percentOff The percentage taken off the price.
     * @param expiration The date the promotion expires.
     * @return An {@code Email} ready to send.
     */
    static Email getPromotionEmail(User user, String promoCode, double percentOff, LocalDate expiration) {
        return EmailBuilder.startingBlank()
            .from(CES_NAME, CES_FROM_ADDRESS)
            .to(user.getName(), user.getEmail())
            .withSubject("Cinema E-booking System: A special offer for you")
            .withPlainText("Hello " + user.getName() + ", enjoy " + (int) percentOff +
                "% off your next booking with code " + promoCode + ". This offer expires " +
                expiration + ". You are receiving this because you subscribed to promotions.")
            .buildEmail();
    } // getPromotionEmail

    /** Masks a card number down to its last 4 digits. */
    private static String lastFourOf(String cardNumber) {
        return cardNumber != null && cardNumber.length() >= 4
            ? cardNumber.substring(cardNumber.length() - 4)
            : "????";
    } // lastFourOf

    /**
     * Creates a ticket confirmation email.
     * @param user The user to send it to.
     * @param ticketCount The number of tickets bought.
     * @param total The total price of the booking.
     * @return An {@code Email} ready to send.
     */
    static Email getTicketsBookedEmail(User user, int ticketCount, double total) {
        return EmailBuilder.startingBlank()
            .from(CES_NAME, CES_FROM_ADDRESS)
            .to(user.getName(), user.getEmail())
            .withSubject("Cinema E-booking System: Your Tickets are Confirmed!")
            .withPlainText("Hello " + user.getName() + ",\n\n" +
                "Your booking was successful! You have purchased " + ticketCount + 
                " tickets for a total of $" + String.format("%.2f", total) + ".\n\n" +
                "Enjoy the show!")
            .buildEmail();
    }


} // EmailTemplates
