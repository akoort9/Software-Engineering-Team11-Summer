package cs4050e.ces.db.payment;

import java.time.LocalDate;

/** Represents a company promotion that can be used to apply
 * discounts to {@code Ticket}s.
 */
public class Promotion {
    /** Database id. */
    private int id = -1;

    /** The string required to activate this promotion. */
    private String promoCode;

    /** The discount this promotion provides, as a price multiplier
     * (e.g. 0.9 for 10% off). Defaults to 1.0, meaning no discount.
     */
    private double discountPercent = 1.0;

    /** The date this promotion expires. */
    private LocalDate expirationDate;

    /**
     * Initializes a new {@code Promotion}.
     * @param promoCode The code required to activate this promotion.
     * @param discountPercent The price multiplier this promotion applies.
     * @param expirationDate The date this promotion expires.
     */
    public Promotion(String promoCode, double discountPercent, LocalDate expirationDate) {
        this.promoCode = promoCode;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
    } // Promotion

    /**
     * Returns the promotion's database id.
     * @return The database id.
     */
    public int getId() {
        return id;
    } // getId

    /**
     * Sets the promotion's database id.
     * @param id The database id.
     */
    public void setId(int id) {
        this.id = id;
    } // setId

    /**
     * Returns the code required to activate this promotion.
     * @return The promo code.
     */
    public String getPromoCode() {
        return promoCode;
    } // getPromoCode

    /**
     * Returns the price multiplier this promotion applies.
     * @return The discount multiplier.
     */
    public double getDiscountPercent() {
        return discountPercent;
    } // getDiscountPercent

    /**
     * Returns the date this promotion expires.
     * @return The expiration date.
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    } // getExpirationDate
} // Promotion
