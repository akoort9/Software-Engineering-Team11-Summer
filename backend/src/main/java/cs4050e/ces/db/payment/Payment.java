package cs4050e.ces.db.payment;

/** Represents a recorded payment transaction for a booking. */
public class Payment {
    /** Database id; -1 when not yet stored. */
    private int id = -1;

    /** The id of the user who made the payment. */
    private int userId;

    /** The amount charged. */
    private double amount;

    /** The last four digits of the card that was charged. */
    private String cardLastFour;

    /** The outcome of the payment, e.g. "APPROVED". */
    private String status;

    /** The processor's transaction id for this payment. */
    private String transactionId;

    /**
     * Initializes a new {@code Payment}.
     * @param userId The id of the paying user.
     * @param amount The amount charged.
     * @param cardLastFour The last four digits of the card charged.
     * @param status The outcome of the payment.
     * @param transactionId The processor's transaction id.
     */
    public Payment(int userId, double amount, String cardLastFour, String status, String transactionId) {
        this.userId = userId;
        this.amount = amount;
        this.cardLastFour = cardLastFour;
        this.status = status;
        this.transactionId = transactionId;
    } // Payment

    /**
     * Returns the database id.
     * @return The id, or -1 if not stored.
     */
    public int getId() {
        return id;
    } // getId

    /**
     * Sets the database id.
     * @param id The id.
     */
    public void setId(int id) {
        this.id = id;
    } // setId

    /**
     * Returns the id of the paying user.
     * @return The user id.
     */
    public int getUserId() {
        return userId;
    } // getUserId

    /**
     * Returns the amount charged.
     * @return The amount.
     */
    public double getAmount() {
        return amount;
    } // getAmount

    /**
     * Returns the last four digits of the card charged.
     * @return The last four digits.
     */
    public String getCardLastFour() {
        return cardLastFour;
    } // getCardLastFour

    /**
     * Returns the outcome of the payment.
     * @return The status.
     */
    public String getStatus() {
        return status;
    } // getStatus

    /**
     * Returns the processor's transaction id.
     * @return The transaction id.
     */
    public String getTransactionId() {
        return transactionId;
    } // getTransactionId
} // Payment
