package nl.hsac.fitnesse.fixture.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Helper for random values.
 */
public class RandomUtil {
    private Random random = new SecureRandom();
    /**
     * Generates random number below a certain value.
     * @param max max (non-inclusive) value for returned number.
     * @return random number
     */
    public int random(int max) {
        return random.nextInt(max);
    }

    /**
     * Creates a random string consisting of lowercase letters.
     * @param minLength minimum length of String to create.
     * @param maxLength maximum length (non inclusive) of String to create.
     * @return lowercase letters.
     */
    public String randomLowerMaxLength(int minLength, int maxLength) {
        int range = maxLength - minLength;
        int randomLength = 0;
        if (range > 0) {
            randomLength = random(range);
        }
        return randomLower(minLength + randomLength);
    }

    /**
     * Creates a random string consisting of lowercase letters.
     * @param length length of String to create.
     * @return lowercase letters.
     */
    public String randomLower(int length) {
        return randomString("abcdefghijklmnopqrstuvwxyz", length);
    }

    /**
     * Creates a random string consisting only of supplied characters.
     * @param permitted string consisting of permitted characters.
     * @param length length of string to create.
     * @return random string.
     */
    public String randomString(String permitted, int length) {
        StringBuilder result = new StringBuilder(length);
        int maxIndex = permitted.length();
        for (int i = 0; i < length; i++) {
            int index = random(maxIndex);
            char value = permitted.charAt(index);
            result.append(value);
        }
        return result.toString();
    }
}
