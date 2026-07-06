package cs4050e.ces.db.payment;

import java.time.LocalDate;

/** Represents a company promotion that can be used to apply
 * discounts to {@code Ticket}s.
 */
public class Promotion {
    /** The string required to activate this promotion */
    String promoCode;

    /** The discount this promotion provides */
    double discountPercent = 1.0;

    /** The date this promotion expires. */
    LocalDate expirationDate;

    public Promotion() {

    } // Promotion
} // Promotion
