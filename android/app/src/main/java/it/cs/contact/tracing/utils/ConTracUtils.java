package it.cs.contact.tracing.utils;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    public static void wait(final int seconds) {

        wait((double) seconds);
    }

    public static void wait(final double seconds) {

        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (Exception ignored) {
        }
    }

    public static int dateToNumber(final LocalDate date) {
        return Integer.parseInt(date.format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    public static String dateToString(final LocalDate date) {
        return date.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public static LocalDate numberToDate(final int num) {

        try {

            return stringToDate(String.valueOf(num));
        } catch (final Exception e) {
            Log.e(COMMON_UTILS, "Error ", e);
            return null;
        }
    }

    public static LocalDate stringToDate(final String str) {

        try {

            return LocalDate.parse(str, DateTimeFormatter.BASIC_ISO_DATE);

        } catch (final Exception e) {

            Log.e(COMMON_UTILS, "Error ", e);
            return null;
        }
    }

    public static boolean isValidDate(final Integer date) {

        return isValidDate(String.valueOf(date));
    }

    public static <T> void printSaved(final T object) {
        Log.i(COMMON_UTILS, "Object Saved: " + object);
    }

    public static boolean isValidDate(final String date) {

        try {

            LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
            return true;
        } catch (final Exception e) {

            Log.e(COMMON_UTILS, "Error ", e);
            return false;
        }
    }

    public static String getString(final JSONObject jsonObject, final String key) {

        try {
            if (jsonObject.has(key)) {
                return StringUtils.trimToEmpty(jsonObject.getString(key));
            }
        } catch (Exception e) {
            Log.i(COMMON_UTILS, "", e);
        }

        return null;
    }

    public static Integer getInt(final JSONObject jsonObject, final String key) {

        try {
            if (jsonObject.has(key)) {
                return jsonObject.getInt(key);
            }
        } catch (Exception e) {
            Log.i(COMMON_UTILS, "", e);
        }

        return null;
    }

    public static <T> void put(final JSONObject jsonObject, final String key, final T value) {

        try {
            jsonObject.put(key, value);
        } catch (Exception e) {
            Log.i(COMMON_UTILS, "", e);
        }
    }

}
