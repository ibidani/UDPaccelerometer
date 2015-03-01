package com.idan.networkaxel;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;


public class MyActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
//    private Sensor mMagentic;
    private float mSensorX;
    private float mSensorY;
    private float mSensorZ;
//    Socket pcserver = null;
    DatagramSocket pcserver = null;
    DataOutputStream os = null;
    DataInputStream is = null;
    private final int PORT = 9999;
    InetAddress address = null;

    private EditText mIpEditText;
    private Button mConnectButton;
    private Button mCloseButton;
    private Button mQuitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener( this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        getAddr();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void getAddr(){
        //Connect Button Pressed - open connection to server
        // Get our EditText object.
        // Initialize the compose field with a listener for the return key
        mIpEditText = (EditText) findViewById(R.id.editipaddr);
        // mIpEditText.setOnEditorActionListener(mWriteListener);

        // Connect Button
        mConnectButton = (Button) findViewById(R.id.conbut);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.editipaddr);
                String ipAddr = view.getText().toString();
                setupConnectionUDP(ipAddr);
            }
        });

        // Close Connection button
        mCloseButton = (Button) findViewById(R.id.closebut);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeShop();
            }
        });
        // Quit APP Connection button
        mQuitButton = (Button) findViewById(R.id.quitbut);
        mQuitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                quitShop();
            }
        });
    }

   /* private void setupConnectionTCP(String ipAddr){
        TextView view = (TextView) findViewById(R.id.msgView);
        try {

            pcserver = new DatagramSocket();

            os = new DataOutputStream(pcserver.getOutputStream());
            is = new DataInputStream(pcserver.getInputStream());
        } catch (UnknownHostException e) {
            view.setText("Don't know about host: hostname");
        } catch (ConnectException e){
            view.setText("Connection Refused");
        }
        catch (IOException e) {
            view.setText("Couldn't get I/O for the connection to: hostname");
        }

        //TextView view = (TextView) findViewById(R.id.xval);
        //view.setText("Connected or at least tried...");
    }*/
    private void setupConnectionUDP(String ipAddr){
        TextView view = (TextView) findViewById(R.id.msgView);
        try {
            pcserver = new DatagramSocket();
            address = InetAddress.getByName(ipAddr);
//            os = new DataOutputStream(pcserver.getOutputStream());
//            is = new DataInputStream(pcserver.getInputStream());
        } catch (UnknownHostException e) {
            view.setText("Don't know about host: hostname");
        } catch (ConnectException e){
            view.setText("Connection Refused");
        }
        catch (IOException e) {
            view.setText("Couldn't get I/O for the connection to: hostname");
        }

        //TextView view = (TextView) findViewById(R.id.xval);
        //view.setText("Connected or at least tried...");
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    static final float ALPHA = 0.1f;

    protected float[] accelVals;

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void onSensorChanged(SensorEvent event) {


       if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        /*
         * record the accelerometer data, the event's timestamp as well as
         * the current time. The latter is needed so we can calculate the
         * "present" time during rendering. In this application, we need to
         * take into account how the screen is rotated with respect to the
         * sensors (which always return data in a coordinate space aligned
         * to with the screen in its native orientation).
         */
        accelVals = lowPass( event.values, accelVals );
        mSensorX = event.values[0];
        mSensorY = event.values[1];
        mSensorZ = event.values[2];
        String sensorsX = new Float(mSensorX).toString();
        String sensorsY = new Float(mSensorY).toString();
        String sensorsZ = new Float(mSensorZ).toString();
        TextView viewx = (TextView) findViewById(R.id.xval);
        viewx.setText(sensorsX);
        TextView viewy = (TextView) findViewById(R.id.yval);
        viewy.setText(sensorsY);
        TextView viewz = (TextView) findViewById(R.id.zval);
        viewz.setText(sensorsZ);

        sendingInfo(sensorsX, sensorsY, sensorsZ);//added float


    }

    private void sendingInfo(String sensorsX, String sensorsY, String sensorsZ){
        TextView view = (TextView) findViewById(R.id.msgView);
        if (pcserver != null) {
            try {
//                byte[] xBuffer = ByteBuffer.allocate(4).putFloat(sensorsX).array();
//                byte[] yBuffer = ByteBuffer.allocate(4).putFloat(sensorsY).array();
//                byte[] zBuffer = ByteBuffer.allocate(4).putFloat(sensorsZ).array();
                String strPacket = sensorsX+","+sensorsY+","+sensorsZ;
                byte[] buffer = strPacket.getBytes();
//                byte[] buffer = new byte[mSensorsX]
                pcserver.send(new DatagramPacket(buffer,buffer.length,address,PORT));
                //send the X sensor data
                //os.writeBytes(sensorsY);
                // clean up:
                // close the output stream
                // close the input stream
                // close the socket
                //os.close();
                //is.close();
//                pcserver.close();
            } catch (UnknownHostException e) {
                view.setText("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                view.setText("IOException:  " + e);
            }
            //view.setText("Sent stuff I guess");
        }
    }

    private void closeShop(){
        TextView view = (TextView) findViewById(R.id.msgView);

        if (pcserver != null) {

            //os.writeBytes(sensorsX);
            //os.writeBytes(sensorsY);
            // clean up:
            // close the output stream
            // close the input stream
            // close the socket
//                os.close();
            pcserver.close();
        }
    }
    private void quitShop(){
        TextView view = (TextView) findViewById(R.id.msgView);
        view.setText("Exiting App");
        onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
