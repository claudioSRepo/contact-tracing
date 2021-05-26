package it.cs.contact.tracing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.app.FlutterApplication;
import it.cs.contact.tracing.config.Database;
import it.cs.contact.tracing.dao.ConfigDao;
import it.cs.contact.tracing.model.entity.Config;

import static it.cs.contact.tracing.config.InternalConfig.CF_PARAM;

public class CovidTracingAndroidApp extends FlutterApplication {

    private static volatile Context context;

    private static ExecutorService pool;

    private static volatile Database db;

    @Override
    public void onCreate() {

        super.onCreate();

        initAll();
        createForegroundChannel();
    }

    private void createForegroundChannel() {

        //Create a channel when application is started
        final NotificationChannel channel = new NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_LOW);
        final NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private void initAll() {

        context = getApplicationContext();
        pool = Executors.newFixedThreadPool(2);
        db = Database.getInstance(context);

        initRandomCF();
    }

    private void initRandomCF() {

        final ConfigDao dao = db.configDao();

        Config configEntity = dao.getConfigValue(CF_PARAM);

        if (configEntity == null) {

            final String randomCf = "RANDOM_CF_"
                    + RandomStringUtils.random(6, true, true);

            configEntity = Config.builder().key(CF_PARAM).value(
                    randomCf).build();

            dao.insert(configEntity);
        }
    }

    public static Context getAppContext() {
        return CovidTracingAndroidApp.context;
    }

    public static ExecutorService getThreadPool() {
        return CovidTracingAndroidApp.pool;
    }

    public static synchronized Database getDb() {

        return db;
    }

}