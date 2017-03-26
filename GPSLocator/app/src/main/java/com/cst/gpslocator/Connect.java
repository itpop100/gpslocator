package com.cst.gpslocator;

/**********************************************************************
 **  SOURCE FILE:   Connect.java -  Intiailize the connection to the 
 **                 remote hosting server and check in location of the
 **                 device.
 **
 **  PROGRAM:       GPS Locator
 **
 **  FUNCTIONS:
 **                 Connect(Context appContext)
 **                 void initSocket()
 **                 void setDevIp()
 **                 void setPacketData(String time, String latitude, String longitude)
 **                 void run()
 **                 void teardown()
 **                 boolean foundHost()
 **                 String doInBackground(Void... params)
 **                 void onPostExecute(String result)
 **
 **  DATE:          March 20, 2017
 **
 **
 **  DESIGNER:      Fred Yang, John Agapeyev
 **
 **
 **  PROGRAMMER:    Fred Yang, John Agapeyev
 **
 **  NOTES:
 **  Intiailizes the connection to the remote server.
 *************************************************************************/

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/*****************************************************************************
 * Class:       Connect implements Runnable
 *
 * Date:        March 24, 2017
 *
 * Revision:
 *
 * Designer:    Fred Yang, John Agapeyev
 *
 * Programmer:  Fred Yang, John Agapeyev
 *
 *
 * Notes:
 * The responsibility of this class is to create, send and teardown a UDP
 * Socket. Its run method will take the packetData and send it to the remote
 * server. The send state is done in a separate runnable thread.
 **************************************************************************/
public class Connect implements Runnable {
    public static final String TAG = "Locator";
    private static final int CONNECT_TIMEOUT = 500;
    private static final int  PACKET_SIZE    = 256;
    private static final char DELIMITER      = ',';

    private DatagramSocket socket;
    private InetAddress    ipAddr;
    private Context        context;

    private String devId;
    private String devName = Build.MODEL;
    private String devIp;
    private String packetData;
    private int    port;


    /*****************************************************************************
     * Function:    Connect
     *
     * Date:        March 23, 2017
     *
     * Revision:    Fred Yang, John Agapeyev
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:
     *
     * Interface:   public Connect (Context appContext)
     *              Context appContext: application context
     * Returns:
     *
     * Notes:
     * Initializes the context and deviceId.
     **************************************************************************/
    public Connect(Context appContext) {
        context = appContext;
        devId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    

    /*****************************************************************************
     * Function:    initSocket
     *
     * Date:        March 23, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   void initSocket()
     *
     * Throws:      SocketException: Socket could not be initialized.
     *              UnknownHostException: Host name could not be found.
     *              NumberFormatException: Port is invalid.
     *
     * Returns:     void
     *
     * Notes:
     * Pulls the host name and port number from the Preference Handler, instantiates a
     * UDP socket and gets the IP address of the host specified.
     **************************************************************************/
    private void initSocket() throws SocketException, UnknownHostException, 
        NumberFormatException {
        String[] pref = PrefHandler.checkPref(context);

        String host = pref[0];
        port = Integer.parseInt(pref[1]);
        
        socket = new DatagramSocket();
        ipAddr = InetAddress.getByName(host);
    }

    
    /*****************************************************************************
     * Function:    setDevIp
     *
     * Date:        March 23, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   void setDevIp()
     *
     *
     * Returns:     void
     *
     * Notes:
     * Finds the IP of the current device and stores it in dotted decimal 
     * notation.
     **************************************************************************/
    private void setDevIp()
    {
        WifiManager wifiMangr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMangr.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        devIp = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), 
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }


    /*****************************************************************************
     * Function:    setPacketData
     *
     * Date:        March 20, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   void setPacketData(String time, String latitude, String longitude)
     *              String time: time the device connects to the server
     *              String latitude: latitude of the device
     *              String longitude: longitude of the device
     *
     * Returns:     void
     *
     * Notes:
     * Sets the packet data to be sent to the server.
     * The format is:
     *      time, devIp, devId, devName, latitude, longitude,
     **************************************************************************/
    public void setPacketData(String time, String latitude, String longitude)
    {
        packetData = time + DELIMITER + devIp + DELIMITER + devId + DELIMITER + devName +
                     DELIMITER + latitude + DELIMITER + longitude + DELIMITER;
    }


    /*****************************************************************************
     * Function:    run
     *
     * Date:        March 20, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   void run()
     *
     *
     * Returns:     void
     *
     * Notes:
     * Sends the data to the server in a datagram via UDP. This is accomplished
     * in a separate thread.
     **************************************************************************/
    public void run()
    {
        Log.d(TAG, "run start");
        byte[] data = new byte[PACKET_SIZE];

        System.arraycopy(packetData.getBytes(), 0, data, 0, packetData.length());

        DatagramPacket packet = new DatagramPacket(data, PACKET_SIZE, ipAddr, port);

        try
        {
            socket.send(packet);
        }
        catch(Exception e)
        {
            Log.d(TAG, this.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


    /*****************************************************************************
     * Function:    teardown
     *
     * Date:        March 20, 2017
     *
     * Revision:    
     *  
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   void teardown()
     *
     * Returns:     void
     *
     * Notes:
     * Closes the datagram socket.
     **************************************************************************/
    public void teardown()
    {
        socket.close();
    }


    /*****************************************************************************
     * Function:    foundHost
     *
     * Date:        March 19, 2017
     *
     * Revision:    John Agapeyev
     *
     * Designer:    John Agapeyev
     *
     * Programmer:
     *
     * Interface:   boolean foundHost()
     *
     * Returns:     true on success, false on failure
     *
     * Notes:
     * Call AsyncLookup and get the output of its doInBackground method. If the task
     * succeeded, the returned string will be empty; otherwise it will return an error message.
     *******************************************************************************************/
    public boolean foundHost()
    {
        try
        {
            String result = new AsyncLookup().execute().get(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);

            return result.equals("");
        }
        catch(CancellationException e)
        {
            // Task was cancelled
            return false;
        }
        catch(ExecutionException e)
        {
            // Exception was thrown inside task
            return false;
        }
        catch(InterruptedException e)
        {
            // Waiting thread has interrupted the task
            return false;
        }
        catch(TimeoutException e)
        {
            // Timeout has expired
            return false;
        }
    }
    

    /*****************************************************************************
     * Class:       AsyncLookup
     *
     * Date:        March 20, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     *
     * Notes:
     * This class creates a UDP socket in a background thread and allows user 
     * to add customized implementation in doInBackground() & onPostExecute()
     * methods.
     **************************************************************************/
    private class AsyncLookup extends AsyncTask<Void, Void, String>
    {
        /*****************************************************************************
         * Function:    doInBackground
         *
         * Date:        March 20, 2017
         *
         * Revision:
         *
         * Designer:    Fred Yang, John Agapeyev
         *
         * Programmer:  Fred Yang, John Agapeyev
         *
         * Interface:   String doInBackground(Void... params)
         *              params: list of parameters
         *
         *Returns:      returns empty if succeeded, otherwise returns error message.
         *
         * Notes:
         * Do in background functionality of the connection.
         *******************************************************************************************/
        @Override
        protected String doInBackground(Void... params) {
            try
            {
                initSocket();
                setDevIp();

            }
            catch(SocketException e)
            {
                return "Error Connecting, could not connect to socket";
            }
            catch(UnknownHostException e)
            {
                return "Error Connecting, could not resolve host name";
            }
            catch(NumberFormatException e)
            {
                return "Port is invalid";
            }

            return "";
        }


        /*****************************************************************************
         * Function:    onPostExecute
         *
         * Date:        March 21, 2017
         *
         * Revision:
         *
         * Designer:    Fred Yang, John Agapeyev
         *
         * Programmer:  Fred Yang, John Agapeyev
         *
         *Interface:    void onPostExecute(String result)
         *              String result: result of the background service
         *
         *
         *Returns:      void
         *
         * Notes:
         * Occurs after the execution of the service.
         *******************************************************************************************/
        @Override
        protected void onPostExecute(String result)
        {
            if(!result.equals(""))
            {
                Helper.makeToast((Activity)context, result, Toast.LENGTH_LONG);
            }
        }
    }
}
