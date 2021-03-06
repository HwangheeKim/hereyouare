package com.example.q.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class AirDrum extends AppCompatActivity implements View.OnClickListener, SensorEventListener{
    Button recordSnare, recordCrash, recordHihat, startDrum, stopDrum;
    TextView recordStatus;
    ArrayList<AccelData> accelDatas;
    ArrayList<OrientData> orientDatas;
    ArrayList<Preset> presets;
    SensorManager sensorManager;
    boolean started;

    // snare, crash, hihat;
    SoundPool[] soundPools = {null, null, null};
    int[] soundId = {0, 0, 0};
    int recording=-1, recordingType =-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_drum);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelDatas = new ArrayList<>();
        orientDatas = new ArrayList<>();
        presets = new ArrayList<>();

        recordSnare = (Button)findViewById(R.id.record_snare);
        recordCrash = (Button)findViewById(R.id.record_crash);
        recordHihat = (Button)findViewById(R.id.record_hihat);
        startDrum = (Button)findViewById(R.id.start_drum);
        stopDrum = (Button)findViewById(R.id.stop_drum);
        recordStatus = (TextView)findViewById(R.id.record_status);

        soundPools[0] = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundId[0] = soundPools[0].load(this, R.raw.thud2, 1);
        soundPools[1] = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundId[1] = soundPools[1].load(this, R.raw.crash, 1);
        soundPools[2] = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundId[2] = soundPools[2].load(this, R.raw.hihat, 1);

        recordSnare.setOnClickListener(this);
        recordCrash.setOnClickListener(this);
        recordHihat.setOnClickListener(this);
        startDrum.setOnClickListener(this);
        stopDrum.setOnClickListener(this);
    }

    private void initSensor() {
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor orient = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, orient, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_drum:
                startDrum.setEnabled(false);
                stopDrum.setEnabled(true);
                started = true;
                initSensor();
                break;
            case R.id.stop_drum:
                startDrum.setEnabled(true);
                stopDrum.setEnabled(false);
                started = false;
                accelDatas = new ArrayList();
                orientDatas = new ArrayList();

                presets = new ArrayList();
                sensorManager.unregisterListener(this);
                break;
            case R.id.record_snare:
                recording = 0;
                recordingType = 0;
                recordStatus.setText("Snare Recording");
                initSensor();
                break;
            case R.id.record_crash:
                recording = 0;
                recordingType = 1;
                recordStatus.setText("Crash Recording");
                initSensor();
                break;
            case R.id.record_hihat:
                recording = 0;
                recordingType = 2;
                recordStatus.setText("Hihat Recording");
                initSensor();
                break;
            default:
                break;
        }
    }

    boolean swing = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            orientDatas.add(new OrientData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
            return;
        }
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        long timestamp = System.currentTimeMillis();
        AccelData data = new AccelData(timestamp, x, y, z);
        accelDatas.add(data);

        if (started || recording>=0) {
            if(x < -30) {
                swing = true;
            } else if(accelDatas.size()<2 || orientDatas.size()<2) {

            } else if((accelDatas.get(accelDatas.size()-2).getX() < accelDatas.get(accelDatas.size()-1).getX()) && swing) {
                swing = false;
                Log.d("POWER!", accelDatas.get(accelDatas.size()-1).toString());

                // If recording, set the preset
                if(recording>=0) {
                    presets.add(new Preset(accelDatas, orientDatas, recordingType));
                    recording++;
                    if(recording>=5) {
                        recording = -1;
                        recordStatus.setText("RECORD DONE!");
                        Log.d("RECORD DONE!!", "DOOOOOOOOOOOOOOONE!");
                        sensorManager.unregisterListener(this);
                    }
                } else {
                    // Compare the latest log to the presets
                    int minIndex = 0;
                    double minDistance = Double.MAX_VALUE;
                    for(int i=0 ; i<presets.size() ; i++) {
                        double distance = presets.get(i).distance(accelDatas, orientDatas);
                        if(distance < minDistance) {
                            minIndex = i;
                            minDistance = distance;
                        }
                    }

                    // Find the best match
                    Log.d("Minimum Distance", "" + minDistance + " # Sound " + minIndex);
                    soundPools[presets.get(minIndex).type].play(
                            soundId[presets.get(minIndex).type], 1.0F, 1.0F, 1, 0, 1.0F);

                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
