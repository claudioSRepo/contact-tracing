package it.cs.contact.tracing.audio;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.utils.ConTracUtils;


public class DecibelMeter {

    private static final String TAG = "DecibelMeter";
    private static final double mEMA = 0.0;

    final MediaRecorder mRecorder;

    public DecibelMeter() {
        this.mRecorder = new MediaRecorder();
    }

    public Noise recordAndgetNoiseValue() {

        try {
            Log.d(TAG, "Recording and analyzing noise value...");
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mRecorder.setOutputFile(getTempFile());

            mRecorder.prepare();
        } catch (final Exception e) {
            Log.e(TAG, "Prepare Recorder error: ", e);
        }

        try {
            startRecorder();

            int noiseValue = getNoiseDbValue();
            mRecorder.stop();
            mRecorder.release();

            Log.i(TAG, "Noise Value: " + noiseValue);

            return InternalConfig.NOISE_ZONE_MAP.getOrDefault(InternalConfig.NOISE_ZONE_MAP.floorKey(noiseValue), Noise.LOW);

        } catch (final Exception e) {
            Log.e(TAG, "Recorder error: ", e);
        }

        Log.i(TAG, "Using default noise value: " + Noise.LOW);

        return Noise.LOW;
    }

    private void startRecorder() {
        ConTracUtils.wait(2);
        mRecorder.start();
        mRecorder.getMaxAmplitude();
        ConTracUtils.wait(2);
    }

    private File getTempFile() {

        try {

            final File outputDir = CovidTracingAndroidApp.getAppContext().getCacheDir();
            File file = new File(outputDir, "tempAudio.3gp");

            if (file.exists()) {
                file.delete();
            }

            return file;

        } catch (final Exception ignored) {
            Log.d(TAG, "Error while creating temp file. Proceding.. ");
        }

        return null;
    }

    private int getNoiseDbValue() {

        double amplitude = mRecorder.getMaxAmplitude();

        Log.v(TAG, "Noise max amplitude: " + amplitude);

        if (amplitude > 0 && amplitude < 1000000) {
            return (int) convertdDb(amplitude);
        }

        return 0;
    }

    private double convertdDb(double amplitude) {

        // Cellphones can catch up to 90 db + -
        // getMaxAmplitude returns a value between 0-32767 (in most phones). that means that if the maximum db is 90, the pressure
        // at the microphone is 0.6325 Pascal.
        // it does a comparison with the previous value of getMaxAmplitude.
        // we need to divide maxAmplitude with (32767/0.6325)
        //51805.5336 or if 100db so 46676.6381
        double EMA_FILTER = 0.6;

        double mEMAValue = EMA_FILTER * amplitude + (1.0 - EMA_FILTER) * mEMA;

        //Assuming that the minimum reference pressure is 0.000085 Pascal (on most phones) is equal to 0 db
        // samsung S9 0.000028251

        return 20 * (float) Math.log10((mEMAValue / 51805.5336) / 0.000028251);
    }

    public enum Noise {

        HIGH, MEDIUM, LOW
    }
}
