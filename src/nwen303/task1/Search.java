package nwen303.task1;

import nwen303.util.Blowfish;

import java.math.BigInteger;


/**
 * This class decrypts a Base64 ciphertext given a particular key (integer).
 * <p>
 * No error checking on inputs!
 */
public class Search {

    /**
     * Example illustrating how to search a range of keys.
     * <p>
     * NO ERROR CHECKING, EXCEPTIONS JUST PROPAGATED.
     *
     * @param args starting key represented as a big integer value, key size, number of keys to check, ciphertext encoded as base64 value
     */
    public static void main(String[] args) throws Exception {
        // Extract the key, turn into an array (of right size) and convert the base64 ciphertext into an array
        BigInteger bi = new BigInteger(args[0]);
        int keySize = Integer.parseInt(args[1]);
        long searchSize = Long.valueOf(args[2]);
        byte[] ciphertext = Blowfish.fromBase64(args[3]);

        // Go into a loop where we try a range of keys starting at the given one
        // Search from the key that will give us our desired ciphertext

        for (int i = 0; i < searchSize; i++) {

            // tell user which key is being checked
            String keyStr = bi.toString();
            System.out.print(keyStr);
            Thread.sleep(100);

            System.out.print("\r");

            // decrypt and compare to known plaintext
            byte[] key = Blowfish.asByteArray(bi, keySize);
            Blowfish.setKey(key);
            String plaintext = Blowfish.decryptToString(ciphertext);
            if (plaintext.equals("May good flourish; Kia hua ko te pai")) {
                System.out.println("Plaintext found!");
                System.out.println(plaintext);
                System.out.println("key is (hex) " + Blowfish.toHex(key) + " " + bi);
                System.exit(-1);
            }

            // try the next key
            bi = bi.add(BigInteger.ONE);
        }
        System.out.println("No key found!");
    }
}