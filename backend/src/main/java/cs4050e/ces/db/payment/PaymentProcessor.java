package cs4050e.ces.db.payment;

import java.security.SecureRandom;

/** Simulates a third-party payment gateway. It validates a card and
 * "authorizes" a charge without moving any real money, standing in for a
 * processor like Stripe so the checkout flow can be demonstrated offline.
 */
public class PaymentProcessor {
    /** Source of randomness for transaction ids. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** A test card number that always declines, for demoing the failure path. */
    public static final String DECLINE_TEST_CARD = "4000000000000002";

    /** The outcome of a processed payment. */
    public static class Result {
        /** Whether the charge was authorized. */
        public final boolean approved;

        /** The transaction id, or {@code null} if declined. */
        public final String transactionId;

        /** The decline reason, or {@code null} if approved. */
        public final String reason;

        private Result(boolean approved, String transactionId, String reason) {
            this.approved = approved;
            this.transactionId = transactionId;
            this.reason = reason;
        } // Result

        /**
         * Builds an approved result.
         * @param transactionId The authorized transaction id.
         * @return An approved {@code Result}.
         */
        static Result approved(String transactionId) {
            return new Result(true, transactionId, null);
        } // approved

        /**
         * Builds a declined result.
         * @param reason Why the charge was declined.
         * @return A declined {@code Result}.
         */
        static Result declined(String reason) {
            return new Result(false, null, reason);
        } // declined
    } // Result

    /**
     * Validates a card and authorizes a charge for the given amount.
     * @param card The card to charge.
     * @param cvv The card's security code, or {@code null} for a stored card.
     * @param amount The amount to charge.
     * @return An approved {@code Result} with a transaction id, or a declined
     * {@code Result} with a reason.
     */
    public static Result process(Card card, String cvv, double amount) {
        if (card == null) {
            return Result.declined("no card provided");
        } // if

        String number = card.getCardNumber() == null
            ? ""
            : card.getCardNumber().replaceAll("\\s+", "");

        if (amount <= 0) {
            return Result.declined("invalid amount");
        } else if (!number.matches("\\d{13,19}") || !luhnValid(number)) {
            return Result.declined("invalid card number");
        } else if (card.isExpired()) {
            return Result.declined("card is expired");
        } else if (cvv != null && !cvv.isEmpty() && !cvv.matches("\\d{3,4}")) {
            return Result.declined("invalid security code");
        } else if (number.equals(DECLINE_TEST_CARD)) {
            return Result.declined("card declined by issuer");
        } // if-else

        return Result.approved(newTransactionId());
    } // process

    /**
     * Checks a card number against the Luhn checksum.
     * @param number The digits-only card number.
     * @return {@code true} if the checksum passes, {@code false} otherwise.
     */
    private static boolean luhnValid(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                } // if
            } // if
            sum += digit;
            alternate = !alternate;
        } // for
        return sum % 10 == 0;
    } // luhnValid

    /**
     * Generates a unique-enough transaction id for a simulated charge.
     * @return The transaction id.
     */
    private static String newTransactionId() {
        return "TXN-" + Long.toHexString(System.currentTimeMillis())
            + "-" + String.format("%04d", RANDOM.nextInt(10000));
    } // newTransactionId
} // PaymentProcessor
