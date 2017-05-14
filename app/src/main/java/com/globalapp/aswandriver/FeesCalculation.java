package com.globalapp.aswandriver;

import android.*;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeesCalculation extends Service {

    public static boolean IS_SERVICE_RUNNING = false;
    private double EXTRA_Distance = 0;
    private double EXTRA_TOTAL = 5;
    private double A = 0;
    private double B = 0;
    private double C = 0;
    private Location GPS;
    private Stopwatch Total_Timer = new Stopwatch();
    private Stopwatch Move_Timer = new Stopwatch();
    private Stopwatch Stop_Timer = new Stopwatch();
    CountDownTimer MainTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification(EXTRA_TOTAL);
        try {
            MapActivity.fabMain.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MainTimer = new CountDownTimer(10800000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                try {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                        return;
                    }
                    GPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (GPS == null) {
                        GPS = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }
                } catch (Exception ex) {
                    Toast.makeText(FeesCalculation.this, "Please Enable GPS", Toast.LENGTH_SHORT).show();
                }


                sendBroadcastMessage(GPS.getSpeed());

            }

            @Override
            public void onFinish() {

            }
        };

        MainTimer.start();
        Total_Timer.start();

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainTimer.cancel();
        IS_SERVICE_RUNNING = false;
        MapActivity.fabMain.setEnabled(true);
        stopForeground(true);

    }

    private void sendBroadcastMessage(float Speed) {

        EXTRA_Distance += (Speed / 1000);
        if (Speed > 2) {
            A = 0.008333333334;
            Move_Timer.pause();
            Stop_Timer.start();
        } else {
            if (EXTRA_Distance > 7 || EXTRA_Distance >= 15) {
                Move_Timer.start();
                Stop_Timer.pause();
                B = Speed * 0.0025;
                C = 0;
                A = 0;
            } else if (EXTRA_Distance <= 7 && EXTRA_Distance > 15) {
                Move_Timer.start();
                Stop_Timer.pause();
                C = Speed * .0035;
                B = 0;
                A = 0;
            }
        }
        EXTRA_TOTAL += A + B + C;


        try {
            MapActivity.txtSpeed.setText(String.valueOf(Speed * 3.6));
            MapActivity.txtMovingTime.setText(Move_Timer.toString());
            MapActivity.txtTotalTime.setText(Total_Timer.toString());
            MapActivity.txtStoppageTime.setText(Stop_Timer.toString());
            MapActivity.txtTotalDistance.setText(String.valueOf(EXTRA_Distance));
            MapActivity.txtTotalMoney.setText(String.valueOf(EXTRA_TOTAL));
            showNotification(EXTRA_TOTAL);
        } catch (Exception e) {
            Log.e("Counter error", e.getLocalizedMessage());
        }


    }

    private void showNotification(double value) {
        String Fees = String.format(Locale.UK, "%.2f", value) + " " + getString(R.string.le);
        Intent starter = new Intent(getApplicationContext(), MapActivity.class);
        PendingIntent Pending = PendingIntent.getActivity(getApplicationContext(), 0, starter, 0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setOngoing(true)
                .setContentTitle(getString(R.string.notification_fee))

                .setContentText(Fees)
                .setSmallIcon(R.drawable.ic_notify)

                .setContentIntent(Pending)
                .setOngoing(true);
        startForeground(1332, notification.build());
        IS_SERVICE_RUNNING = true;
    }


}

class Stopwatch {

    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    String time = "";

    /**
     * Implements a method that returns the current time, in milliseconds.
     * Used for testing
     */
    public interface GetTime {
        long now();
    }

    /**
     * Default way to get time. Just use the system clock.
     */
    private GetTime SystemTime = new GetTime() {
        @Override
        public long now() {
            return System.currentTimeMillis();
        }
    };

    /**
     * What is the stopwatch doing?
     */
    public enum State {
        PAUSED, RUNNING
    }

    private GetTime m_time;
    private long m_startTime;
    private long m_stopTime;
    private long m_pauseOffset;
    private List<Long> m_laps = new ArrayList<Long>();
    private State m_state;

    public Stopwatch() {
        m_time = SystemTime;
        reset();
    }

    public Stopwatch(GetTime time) {
        m_time = time;
        reset();
    }

    /**
     * Start the stopwatch running. If the stopwatch is already running, this
     * does nothing.
     */
    public void start() {
        if (m_state == State.PAUSED) {
            m_pauseOffset = getElapsedTime();
            m_stopTime = 0;
            m_startTime = m_time.now();
            m_state = State.RUNNING;
        }
    }

    /***
     * Pause the stopwatch. If the stopwatch is already running, do nothing.
     */
    public void pause() {
        if (m_state == State.RUNNING) {
            m_stopTime = m_time.now();
            m_state = State.PAUSED;
        }
    }

    /**
     * Reset the stopwatch to the initial state, clearing all stored times.
     */
    public void reset() {
        m_state = State.PAUSED;
        m_startTime = 0;
        m_stopTime = 0;
        m_pauseOffset = 0;
        m_laps.clear();
    }

    /**
     * Record a lap at the current time.
     */
    public void lap() {
        m_laps.add(getElapsedTime());
    }

    /***
     * @return The amount of time recorded by the stopwatch, in milliseconds
     */
    public long getElapsedTime() {
        if (m_state == State.PAUSED) {
            return (m_stopTime - m_startTime) + m_pauseOffset;
        } else {
            return (m_time.now() - m_startTime) + m_pauseOffset;
        }
    }

    /**
     * @return A list of the laps recorded. Each lap is given as a millisecond
     * value from when the stopwatch began running.
     */
    public List<Long> getLaps() {
        return m_laps;
    }

    /**
     * @return true if the stopwatch is currently running and recording
     * time, false otherwise.
     */
    public boolean isRunning() {
        return (m_state == State.RUNNING);
    }

    public String toString() {
        secs = (int) (getElapsedTime() / 1000);
        mins = secs / 60;
        mins = mins % 60;
        secs = secs % 60;
        milliseconds = (int) (getElapsedTime() % 1000);
        time = String.format("%02d", mins) + ":" + String.format("%02d", secs);


        return time;
    }
}
