package com.example.q.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    Button btnStart, btnStop;
    LinearLayout layout;
    boolean started = false;
    ArrayList<AccelData> accelDatas;
    ArrayList<AccelData> gravityDatas;
    ArrayList<OrientData> orientDatas;
    ArrayList<GyroData> gyroDatas;
    SensorManager sensorManager;
    View mChart;

    SoundPool snare, crash;
    int snareId, crashId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelDatas = new ArrayList();
        orientDatas = new ArrayList();
        gyroDatas = new ArrayList<>();
        gravityDatas = new ArrayList<>();
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        layout = (LinearLayout) findViewById(R.id.container);

        snare = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        snareId = snare.load(this, R.raw.thud2, 1);
        crash = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        crashId = crash.load(this, R.raw.crash, 1);

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                orientDatas.add(new OrientData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
                return;
            case Sensor.TYPE_GYROSCOPE:
                gyroDatas.add(new GyroData(System.currentTimeMillis(), 0, 0, 0, event.values[0], event.values[1], event.values[2]));
                gyroDatas.get(gyroDatas.size()-1).setX( gyroDatas.get(gyroDatas.size()-2).getX() + gyroDatas.get(gyroDatas.size()-1).getVx() );
                gyroDatas.get(gyroDatas.size()-1).setY( gyroDatas.get(gyroDatas.size()-2).getY() + gyroDatas.get(gyroDatas.size()-1).getVy() );
                gyroDatas.get(gyroDatas.size()-1).setZ( gyroDatas.get(gyroDatas.size()-2).getZ() + gyroDatas.get(gyroDatas.size()-1).getVz() );
                return;
            case Sensor.TYPE_ACCELEROMETER:
                int last = accelDatas.size()-1;
                int glast = gravityDatas.size()-1;
                if(glast < 3) return;
                accelDatas.add(new AccelData(System.currentTimeMillis(),
                        0, 0, 0,
                        accelDatas.get(last).getVx() + (event.values[0] - gravityDatas.get(glast).getX()),
                        accelDatas.get(last).getVy() + (event.values[1] - gravityDatas.get(glast).getY()),
                        accelDatas.get(last).getVz() + (event.values[2] - gravityDatas.get(glast).getZ())));
                last = accelDatas.size()-1;
                accelDatas.get(last).setX( accelDatas.get(last-1).getX() + accelDatas.get(last).getVx() );
                accelDatas.get(last).setY( accelDatas.get(last-1).getY() + accelDatas.get(last).getVy() );
                accelDatas.get(last).setZ( accelDatas.get(last-1).getZ() + accelDatas.get(last).getVz() );
                return;
            case Sensor.TYPE_GRAVITY:
                gravityDatas.add(new AccelData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
                return;
            default :
                    break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_start:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                accelDatas = new ArrayList();
                accelDatas.add(new AccelData(System.currentTimeMillis(), 0, 0, 0));
                orientDatas = new ArrayList();
                gyroDatas = new ArrayList<>();
                gyroDatas.add(new GyroData(System.currentTimeMillis(), 0, 0, 0));
                gravityDatas = new ArrayList();
                gravityDatas.add(new AccelData(System.currentTimeMillis(), 0, 0, 0));
                started = true;
                Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor orient = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                Sensor gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
//                sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
//                sensorManager.registerListener(this, orient, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
//                sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
                break;
            case R.id.btn_stop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                started = false;
                sensorManager.unregisterListener(this);
                layout.removeAllViews();
                openChart();
                break;
            default:
                break;
        }
    }

    private void openChart() {
        if (gyroDatas != null || gyroDatas.size() > 0) {              ////
            long t = gyroDatas.get(0).getTimestamp();                    ////
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("Azimuth, x");
            XYSeries ySeries = new XYSeries("Roll, y");
            XYSeries zSeries = new XYSeries("Pitch, z");

            for (GyroData data : gyroDatas) {                               ////
                xSeries.add(data.getTimestamp() - t, data.getX());              ////
                ySeries.add(data.getTimestamp() - t, data.getY());              ////
                zSeries.add(data.getTimestamp() - t, data.getZ());              ////
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setChartValuesTextSize(20.0F);
            xRenderer.setDisplayChartValues(true);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setChartValuesTextSize(20.0F);
            yRenderer.setDisplayChartValues(true);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setChartValuesTextSize(20.0F);
            zRenderer.setDisplayChartValues(true);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < gyroDatas.size(); i++) {             ////

                multiRenderer.addXTextLabel(i + 1, ""
                        + (gyroDatas.get(i).getTimestamp() - t));               ////
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, ""+i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }
    }
}
