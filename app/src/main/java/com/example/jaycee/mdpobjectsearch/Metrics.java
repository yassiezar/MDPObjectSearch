package com.example.jaycee.mdpobjectsearch;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.ar.core.Pose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

public class Metrics implements Runnable
{
    private static final String TAG = Metrics.class.getSimpleName();
    private static final String DELIMITER = ",";

    private WifiDataSend dataStreamer = null;

    private double timestamp;
    private double waypointX, waypointY, waypointZ;
    private double deviceX, deviceY, deviceZ;
    private double deviceQx, deviceQy, deviceQz, deviceQw;
    private Objects.Observation rawObservation, filteredObservation;
    private Objects.Observation target;

    private Handler handler = new Handler();

    private boolean stop = false;

    @Override
    public void run()
    {
        if(stop)
        {
            return;
        }

        String wifiString = timestamp + DELIMITER +
                rawObservation.getCode() + DELIMITER +
                filteredObservation.getCode() + DELIMITER +
                target.getCode() + DELIMITER +
                waypointX + DELIMITER +
                waypointY + DELIMITER +
                waypointZ + DELIMITER +
                deviceX + DELIMITER +
                deviceY + DELIMITER +
                deviceZ + DELIMITER +
                deviceQx + DELIMITER +
                deviceQy + DELIMITER +
                deviceQz + DELIMITER +
                deviceQw + DELIMITER;

        if(dataStreamer == null ||
                dataStreamer.getStatus() != AsyncTask.Status.RUNNING)
        {
            dataStreamer = new WifiDataSend();
            dataStreamer.execute(wifiString);
        }

        handler.postDelayed(this, 40);
    }

    public void stop()
    {
        this.stop = true;
        handler.removeCallbacks(this);
        handler = null;
    }

    public void updateTimestamp(double timestamp) { this.timestamp = timestamp; }

    public void updateWaypointPosition(Pose pose)
    {
        float[] pos = pose.getTranslation();

        waypointX = pos[0];
        waypointY = pos[1];
        waypointZ = pos[2];
    }

    public void updateDevicePose(Pose pose)
    {
        float[] pos = pose.getTranslation();
        float[] q = pose.getRotationQuaternion();

        deviceX = pos[0];
        deviceY = pos[1];
        deviceZ = pos[2];

        deviceQx = q[0];
        deviceQy = q[1];
        deviceQz = q[2];
        deviceQw = q[3];
    }

    public void updateRawObservation(Objects.Observation observation) { this.rawObservation = observation; }
    public void updateFilteredObservation(Objects.Observation observation) { this.filteredObservation = observation; }
    public void updateTarget (Objects.Observation target) { this.target = target; }

    private static class WifiDataSend extends AsyncTask<String, Void, Void>
    {
        private String serverIdAddress = "10.5.42.29";
        private int connectionPort = 6666;

        public WifiDataSend() { }

        @Override
        protected Void doInBackground(String... strings)
        {
            try
            {
                Socket socket = new Socket(serverIdAddress, connectionPort);
                OutputStream stream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(stream);

                int bufferLen = 1024;
                char[] tempBuffer = new char[bufferLen];

                BufferedReader bufferedReader = new BufferedReader(new StringReader(strings[0]));

                Log.d(TAG, "Writing to WiFi");
                while(bufferedReader.read(tempBuffer, 0, bufferLen) != -1)
                {
                    writer.print(tempBuffer);
                }
                writer.write("\n");

                writer.flush();
                writer.close();

                socket.close();
            }
            catch(IOException e)
            {
                Log.e(TAG, "Wifi write error: ", e);
            }

            return null;
        }
    }
}
