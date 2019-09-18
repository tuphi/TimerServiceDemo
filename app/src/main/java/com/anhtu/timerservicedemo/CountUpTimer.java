package com.anhtu.timerservicedemo;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public abstract class CountUpTimer {

    public static String displayTime(long percentSeconds) {
        long minutesDisplay = percentSeconds/6000;
        long secondsDisplay = (percentSeconds - minutesDisplay*6000)/100;
        long percentSecondsDisplay = percentSeconds - minutesDisplay*6000 - secondsDisplay*100;
        String minutesString;
        String secondsString;
        String percentSecondsString;
        if(minutesDisplay<10){
            minutesString = "0" + Long.toString(minutesDisplay);
        } else {
            minutesString = Long.toString(minutesDisplay);
        }
        if(secondsDisplay<10){
            secondsString = "0" + Long.toString(secondsDisplay);
        } else {
            secondsString = Long.toString(secondsDisplay);
        }
        if(percentSecondsDisplay<10){
            percentSecondsString = "0" + Long.toString(percentSecondsDisplay);
        } else {
            percentSecondsString = Long.toString(percentSecondsDisplay);
        }
        return minutesString + ":" + secondsString + ":" + percentSecondsString;
    }


    private final long interval;
    private long base;
    private long pauseTime;

    public CountUpTimer(long interval) {
        this.interval = interval;
    }

    public void start() {
        base = SystemClock.elapsedRealtime();
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public void resume() {
        base = SystemClock.elapsedRealtime() - pauseTime;
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public void stop() {
        handler.removeMessages(MSG);
    }

    public void pause() {
        pauseTime = SystemClock.elapsedRealtime() - base;
        handler.removeMessages(MSG);
    }


    public void reset() {
        synchronized (this) {
            base = SystemClock.elapsedRealtime();
        }
    }

    abstract public void onTick(long elapsedTime);

    private static final int MSG = 1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CountUpTimer.this) {
                long elapsedTime = SystemClock.elapsedRealtime() - base;
                onTick(elapsedTime);
                sendMessageDelayed(obtainMessage(MSG), interval);
            }
        }
    };
}
