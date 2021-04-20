package it.cs.contact.tracing.utils;

import android.util.Log;

import java.security.MessageDigest;

public class ConTracUtils {

    private static final String COMMON_UTILS = "ConTracUtils";

    private ConTracUtils() {
    }

    public static String secureHash(String stringToHash) {
        String gen = "";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] bytes = md.digest(stringToHash.getBytes());

            StringBuilder sb = new StringBuilder();

            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            gen = sb.toString();
        } catch (Exception e) {
            Log.e(COMMON_UTILS, "Error hashing", e);
        }

        return gen;
    }
}
