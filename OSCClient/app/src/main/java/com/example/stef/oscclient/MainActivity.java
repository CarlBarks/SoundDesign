package com.example.stef.oscclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.net.*;
import java.util.*;

import com.illposed.osc.*;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    /* These two variables hold the IP address and port number.
     * You should change them to the appropriate address and port.
     */
    private String myIP = "192.168.1.4";
    private int myPort = 57120; //Supercollider's port

    // This is used to send messages
    private OSCPortOut oscPortOut;

    private SensorManager _sensorManager;
    private Sensor _accelerometer;

    private float _sensorX;
    private float _sensorY;
    private float _sensorZ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add accelerometer sensor
        _sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        _accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Start the thread that sends messages
        oscThread.start();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startSimulation();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        stopSimulation();
    }

    public void startSimulation() {
        _sensorManager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void stopSimulation() {
        _sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        _sensorX = sensorEvent.values[0];
        _sensorY = sensorEvent.values[1];
        _sensorZ = sensorEvent.values[2];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // This thread will contain all the code that pertains to OSC
    private Thread oscThread = new Thread() {
        @Override
        public void run() {

            Looper.prepare();
            try {
                // Connect to some IP address and port
                oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);
            } catch(UnknownHostException e) {
                // Error handling when your IP isn't found
                Toast.makeText(MainActivity.this, "IP not found", Toast.LENGTH_SHORT).show();
                return;
            } catch(Exception e) {
                // Error handling for any other errors
                Toast.makeText(MainActivity.this, "IP Error", Toast.LENGTH_SHORT).show();
                return;
            }


      /* The second part of the run() method loops infinitely and sends messages every 500
       * milliseconds.
       */
            while (true) {
                if (oscPortOut != null) {
                    // Creating the message

                    ArrayList<Object> accelerometerDataToSend = new ArrayList<>();

                    if (_sensorManager!=null) {
                        accelerometerDataToSend.add(_sensorX);
                        accelerometerDataToSend.add(_sensorY);
                        accelerometerDataToSend.add(_sensorZ);
                        accelerometerDataToSend.add("Testing Message");

                        OSCMessage message = new OSCMessage("/" + myIP, accelerometerDataToSend);
                        //OSCMessage message2 = new OSCMessage(myIP, moreThingsToSend.toArray());

                        try {
                            // Send the message
                            oscPortOut.send(message);

                            // Pause for half a second
                            sleep(500);
                        } catch (Exception e) {
                            // Error handling for some error
                            Toast.makeText(MainActivity.this, "OSC Message could not be sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
