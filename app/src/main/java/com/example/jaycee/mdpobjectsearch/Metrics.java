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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;

public class Metrics implements Runnable
{
    private static final String TAG = Metrics.class.getSimpleName();
    private static final String DELIMITER = ",";

    private static final String SERVER_IP = "10.42.0.1";
    private static final int PORT = 6666;

    private WifiDataSend dataStreamer = null;

    private double timestamp;
    private double waypointX, waypointY, waypointZ;
    private double deviceX, deviceY, deviceZ;
    private double deviceQx, deviceQy, deviceQz, deviceQw;
    private ArrayList<Objects.Observation> rawObservations = new ArrayList<>();// = Objects.Observation.O_NOTHING;
    private ArrayList<Objects.Observation> filteredObservations = new ArrayList<>();// = Objects.Observation.O_NOTHING;
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

        String rawObs = rawObservations.isEmpty() ? "0;" : rawObservations.stream().map(n -> Integer.toString(n.getCode())).collect(Collectors.joining(";"));
        String filteredObs = filteredObservations.isEmpty() ? "0;" : filteredObservations.stream().map(n -> Integer.toString(n.getCode())).collect(Collectors.joining(";"));
        String wifiString = timestamp + DELIMITER +
                rawObs + DELIMITER +
                filteredObs + DELIMITER +
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

        rawObservations.clear();
        filteredObservations.clear();

        if(dataStreamer == null ||
                dataStreamer.getStatus() != AsyncTask.Status.RUNNING)
        {
            dataStreamer = new WifiDataSend();
            dataStreamer.execute(wifiString);
        }

/*        try
        {
            Socket socket = new Socket(SERVER_IP, PORT);
            OutputStream stream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(stream);

            int bufferLen = 1024;
            char[] tempBuffer = new char[bufferLen];

            BufferedReader bufferedReader = new BufferedReader(new StringReader(wifiString));

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
        }*/

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

    public void addRawObservation(Objects.Observation observation)
    {
        rawObservations.add(observation);
/*        Objects.Observation[] observations = new Objects.Observation[markers.length];
        for(int i = 0; i < observations.length; i++)
        {
            observations[i] = getObservation(markers[i].getId());
        }
        this.rawObservations = observations;*/
    }
    public void addFilteredObservation(Objects.Observation observation)
    {
        filteredObservations.add(observation);
/*        Objects.Observation[] observations = new Objects.Observation[markers.length];
        for(int i = 0; i < observations.length; i++)
        {
            observations[i] = getObservation(markers[i].getId());
        }
        this.filteredObservations = observations;*/
    }
    public void updateTarget (Objects.Observation target) { this.target = target; }

    private static class WifiDataSend extends AsyncTask<String, Void, Void>
    {
        public WifiDataSend() { }

        @Override
        protected Void doInBackground(String... strings)
        {
            try
            {
                Socket socket = new Socket(SERVER_IP, PORT);
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
