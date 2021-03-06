package it.cs.contact.tracing.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.MainActivity;

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

    public static void sendNotification(final String text) {

        try {

            final NotificationManager nm = (NotificationManager) CovidTracingAndroidApp.getAppContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            final Notification not = new NotificationCompat.Builder(CovidTracingAndroidApp.getAppContext(), "notify_001")
                    .setSmallIcon(android.R.drawable.alert_light_frame)
                    .setContentTitle("App Tracciamento Contatti")
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH).build();
            nm.notify(2233, not);

            final NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(CovidTracingAndroidApp.getAppContext(), "notify_001");

            final Intent ii = new Intent(CovidTracingAndroidApp.getAppContext(), MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(CovidTracingAndroidApp.getAppContext(), 0, ii, 0);

            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(android.R.drawable.alert_light_frame);
            mBuilder.setContentTitle("Your Title");
            mBuilder.setContentText("Your text");
            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationChannel channel = new NotificationChannel(
                    "notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
            mBuilder.setChannelId("dadasdpojpja");

            nm.notify(0, mBuilder.build());

        } catch (Exception e) {
            Log.i(COMMON_UTILS, "", e);
        }
    }


}
