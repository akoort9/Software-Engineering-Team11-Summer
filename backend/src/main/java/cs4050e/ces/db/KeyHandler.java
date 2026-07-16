package cs4050e.ces.db;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cs4050e.ces.db.payment.Card;

public class KeyHandler {
    private static SecretKey secretKey = null;

    private static final String KEY_PATH = "./db/.key";

    static Card encryptCard(Card card) throws Exception {
        int year = card.getExpirationDate().getYear();
        int month = card.getExpirationDate().getMonthValue();

        Card encryptedCard = new Card(
            encrypt(card.getCardNumber()),
            encrypt(card.getBillingAddress()),
            year, month);

        return encryptedCard;
    } // getCard

    static Card decryptCard(Card encryptedCard) throws Exception {
        int year = encryptedCard.getExpirationDate().getYear();
        int month = encryptedCard.getExpirationDate().getMonthValue();

        Card decryptedCard = new Card(
            decrypt(encryptedCard.getCardNumber()),
            decrypt(encryptedCard.getBillingAddress()),
            year, month
        );

        return decryptedCard;
    } // decryptCard

    private static void createKey(File keyFile) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        setKey(keyGenerator.generateKey());
        // store in file
        byte [] keyBytes = secretKey.getEncoded();
        byte [] base64Bytes = Base64.getEncoder().encode(keyBytes);

        try (FileOutputStream stream = new FileOutputStream(keyFile)) {
            stream.write(base64Bytes);
        } // try
    } // createKey

    private static void setKey(SecretKey key) {
        secretKey = key;
    } // setKey

    private static void getKey() throws Exception {
        if (secretKey != null) {
            return;
        } // if

        File keyFile = new File(KEY_PATH);
        if (!keyFile.isFile()) {
            // create key
            createKey(keyFile);
        } else {
            byte [] decodedKey = Base64.getDecoder().decode(Files.readAllBytes(keyFile.toPath()));
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            secretKey = key;
        } // if-else
    } // getKey

    private static String encrypt(String plaintext) throws Exception {
        getKey();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV(); // cipher generated a random IV during init
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());

        // prepend IV to the ciphertext
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    } // encrypt

    private static String decrypt(String ciphertext) throws Exception{
        getKey();
        byte[] combined = Base64.getDecoder().decode(ciphertext);

        int ivLength = 16; // AES block size is always 16 bytes, regardless of key size
        byte[] iv = new byte[ivLength];
        byte[] encryptedBytes = new byte[combined.length - ivLength];
        System.arraycopy(combined, 0, iv, 0, ivLength);
        System.arraycopy(combined, ivLength, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    } // decrypt
}
