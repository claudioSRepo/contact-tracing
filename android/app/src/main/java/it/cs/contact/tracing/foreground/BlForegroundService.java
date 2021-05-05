package it.cs.contact.tracing.foreground;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.ble.BleGattServer;
import it.cs.contact.tracing.ble.BleScanner;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.contacts.ExposureAssessmentManager;
import lombok.NoArgsConstructor;

import static it.cs.contact.tracing.config.InternalConfig.BL_FIRST_SCAN;
import static it.cs.contact.tracing.config.InternalConfig.BL_SCAN_SCHEDULING_OFFSET;
import static it.cs.contact.tracing.config.InternalConfig.EXPOSURE_ASSESSMENT_SCHEDULING;

@NoArgsConstructor
public class BlForegroundService extends Service {

    private static final String TAG = "BlForegroundService";

    public static final String ACTION_INIT = "START_INIT";

    public static final String ACTION_START_SERVER = "START_SERVER";

    public static final String ACTION_DAILY_EXPOSURE_ASSESSMENT = "DAILY_EXPOSURE_ASSESSMENT";

    public static final String ACTION_EXECUTE_FOREGROUND_SCAN = "EXECUTE_FOREGROUND_SCAN";

    @Override
    public void onCreate() {
        super.onCreate();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "messages").
                setContentText("L'applicazione è correttamente in esecuzione (version 22/04 - 13:10)").
                setContentTitle("App Tracciamento Contatti").
                setOngoing(true).
                setSmallIcon(android.R.drawable.ic_menu_mylocation);

        startForeground(101, builder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String action = intent.getAction();

        Log.i(TAG, "onHandleIntent. Action: " + action);

        switch (Objects.requireNonNull(action)) {

            case ACTION_INIT:
                startBlClientNextFire(BL_FIRST_SCAN);
                startBlServerJob();
                scheduleDailyExposureAssessmentJob();
                break;

            case ACTION_START_SERVER:
                startBlServer();
                break;

            case ACTION_EXECUTE_FOREGROUND_SCAN:
                startBlClientNextFire(BL_SCAN_SCHEDULING_OFFSET);
                startScan();
                break;

            case ACTION_DAILY_EXPOSURE_ASSESSMENT:
                CovidTracingAndroidApp.getThreadPool().execute(new ExposureAssessmentManager());
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startBlClientNextFire(final long triggerAt) {

        Log.i(TAG, "startBlClientNextFire at " + (triggerAt / 1000) + " seconds");

        final Intent forService = new Intent(this, BlForegroundService.class);
        forService.setAction(ACTION_EXECUTE_FOREGROUND_SCAN);
        final PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 0, forService, 0);

        final AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Log.i(TAG, "Starting BL client job...");
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + triggerAt, pendingIntent);
    }

    private void startBlServerJob() {

        Log.i(TAG, "Starting BL Server job...");

        final Intent forService = new Intent(this, BlForegroundService.class);
        forService.setAction(ACTION_START_SERVER);

        final PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 0, forService, 0);

        final AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 500,
                InternalConfig.BLE_RESTART_SERVER, pendingIntent);
    }

    private void scheduleDailyExposureAssessmentJob() {

        Log.i(TAG, "Scheduling daily exposure assessment...");

        final Intent forService = new Intent(this, BlForegroundService.class);
        forService.setAction(ACTION_DAILY_EXPOSURE_ASSESSMENT);

        final PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 0, forService, 0);

        final AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 500, //TODO: modify  EXPOSURE_ASSESSMENT_SCHEDULING
                InternalConfig.BLE_RESTART_SERVER, pendingIntent); //TODO: modify  EXPOSURE_ASSESSMENT_SCHEDULING
    }


    private void startScan() {
        new BleScanner().scan(this);
    }

    private synchronized void startBlServer() {

        Log.i(TAG, "Restarting BL server...");
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        CovidTracingAndroidApp.getThreadPool().execute(BleGattServer.instanceOf(mBluetoothManager, this));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
