package cs4050e.ces.db.payment;

import java.time.YearMonth;

/** Represents a {@code User}'s credit/debit card.  */
public class Card {
    /** The card number. */
    private String cardNumber;
    
    /** The billing address for this card's owner. */
    private String billingAddress;
    
    /** The expiration date for this card. */
    private YearMonth expirationDate;

    /**
     * Initializes a new {@code Card} object.
     * @param cardNumber The card's number.
     * @param billingAddress The owner's billing address.
     * @param year The year of the expiration date.
     * @param month The month of the expiration date.
     */
    public Card(String cardNumber, String billingAddress, int year, int month) {
	    this.cardNumber = cardNumber;
	    this.billingAddress = billingAddress;
	    this.expirationDate = YearMonth.of(year, month);
    } // Card

    /**
     * Returns the card number.
     * @return The card number.
     */
    public String getCardNumber() {
        return cardNumber;
    } // getCardNumber

    /**
     * Returns the billing address.
     * @return The billing address.
     */
    public String getBillingAddress() {
        return billingAddress;
    } // getBillingAddress

    /**
     * Returns the expiration date.
     * @return The expiration date.
     */
    public YearMonth getExpirationDate() {
        return expirationDate;
    } // getExpirationDate

    /**
     * Returns whether the card is expired or not.
     * @return {@code true} if the current date is past the expiration date,
     * and {@code false} otherwise.
     */
    public boolean isExpired() {
        return YearMonth.now().isAfter(this.expirationDate);
    } // isExpired
} // Card
