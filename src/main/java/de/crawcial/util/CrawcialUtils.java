package de.crawcial.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class.
 *
 * @author Sebastian Lauber
 */
public class CrawcialUtils {
    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * Verifies the signature of an incoming real time update packet from Facebook.
     *
     * @param payload   the request payload
     * @param signature the request signature
     * @param secret    the app secret, that is used for signing
     * @return true, if signature is valid
     */
    public static boolean verifyFbSignature(String payload, String signature, String secret) {
        boolean isValid = false;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(payload.getBytes());

            String expected = signature.substring(5);
            String actual = new String(encode(rawHmac));

            isValid = expected.equals(actual);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            // does not matter
        }

        return isValid;
    }

    /**
     * Helper method for encoding byte array to char array.
     *
     * @param array byte array to encode
     * @return string of byte array
     */
    private static char[] encode(byte[] array) {
        final int amount = array.length;
        char[] result = new char[2 * amount];
        int j = 0;
        for (byte aByte : array) {
            result[j++] = HEX[(0xF0 & aByte) >>> 4];
            result[j++] = HEX[(0x0F & aByte)];
        }
        return result;
    }

    /**
     * Helper method for password encoding to hex.
     *
     * @param array byte array to encode
     * @return string of byte array
     * @throws NoSuchAlgorithmException if an error occurred during access
     */
    public static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    /**
     * Helper method for password encoding from hex.
     *
     * @param hex string to encode
     * @return encoded byte array
     * @throws NoSuchAlgorithmException if an error occurred during access
     */
    public static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }


}
