package nl.hsac.fitnesse.fixture.util;

/**
 * Helpers for BSNs.
 */
public class BsnUtil {
    private RandomUtil randomUtil = new RandomUtil();

    /**
     * Generates random number that could be a BSN.
     * Based on: http://www.testnummers.nl/bsn.js
     * @return random BSN.
     */
    public String generateBsn() {
        String Result1 = "";
        int Nr9 = randomUtil.random(3);
        int Nr8 = randomUtil.random(10);
        int Nr7 = randomUtil.random(10);
        int Nr6 = randomUtil.random(10);
        int Nr5 = randomUtil.random(10);
        int Nr4 = randomUtil.random(10);
        int Nr3 = randomUtil.random(10);
        int Nr2 = randomUtil.random(10);
        int Nr1 = 0;
        int SofiNr = 0;
        if ((Nr9 == 0) && (Nr8 == 0) && (Nr7 == 0)) {
            Nr8 = 1;
        }
        SofiNr = 9 * Nr9 + 8 * Nr8 + 7 * Nr7 + 6 * Nr6 + 5 * Nr5 + 4 * Nr4 + 3 * Nr3 + 2 * Nr2;
        Nr1 = floor(SofiNr - (floor(SofiNr / 11)) * 11);
        if (Nr1 > 9) {
            if (Nr2 > 0) {
                Nr2 -= 1;
                Nr1 = 8;
            } else {
                Nr2 += 1;
                Nr1 = 1;
            }
        }
        Result1 += Nr9;
        Result1 += Nr8;
        Result1 += Nr7;
        Result1 += Nr6;
        Result1 += Nr5;
        Result1 += Nr4;
        Result1 += Nr3;
        Result1 += Nr2;
        Result1 += Nr1;
        return Result1;
    }

    /**
     * Checks whether BSN is valid.
     * Based on: https://mxforum.mendix.com/questions/2162/
     * @param bsn BSN to check.
     * @return true if it is structurally sound.
     */
    public boolean testBsn(String bsn) {
        try {
            Double.parseDouble(bsn);
        } catch (Exception e) {
            return false;
        }
        if (bsn.length() != 9) {
            return false;
        } else {
            int checksum = 0;
            for (int i = 0; i < 8; i++) {
                checksum += (Integer.parseInt(Character.toString(bsn.charAt(i))) * (9 - i));
            }
            checksum -= Integer.parseInt(Character.toString(bsn.charAt(8)));
            if (checksum % 11 != 0) {
                return false;
            }
        }
        return true;

    }

    private int floor(double d) {
        return (int) d;
    }
}
