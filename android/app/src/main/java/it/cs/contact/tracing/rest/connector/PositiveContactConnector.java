package it.cs.contact.tracing.rest.connector;

import android.util.Log;

import java.time.LocalDate;

public class PositiveContactConnector {

    private static final String TAG = "PositiveContactConnector";


    public boolean verifyIfPositive(String key, LocalDate localDate) {

        Log.v(TAG, "Key " + key + " positive : " + true);


        return true;
    }
}
