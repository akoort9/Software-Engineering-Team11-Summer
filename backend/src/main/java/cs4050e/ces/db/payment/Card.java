package cs4050e.ces.db.payment;

import java.time.YearMonth;

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
} // Card
