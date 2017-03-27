package com.cst.gpslocator;

/**********************************************************************
 **  SOURCE FILE:   LocatorService.java -  Java file for the GPS service
 **
 **  PROGRAM:       GPS Locator
 **
 **  FUNCTIONS:
 **                 void onCreate(Bundle savedInstanceState)
 **                 void onProviderDisabled(String provider)
 **                 void onProviderEnabled(String provider)
 **                 void onStatusChanged(String provider, int status, Bundle extras)
 **                 void onLocationChanged(Location location)
 **                 void checkinLocation (Location l, String time)
 **                 void handleMessage(Message msg)
 **                 IBinder onBind(Intent intent)
 **                 int onStartCommand(Intent intent, int flags, int startId)
 **                 void onDestroy()
 **
 **
 **  DATE:          March 21, 2017
 **
 **
 **  DESIGNER:      Fred Yang, John Agapeyev
 **
 **
 **  PROGRAMMER:    Fred Yang, John Agapeyev
 **
 **  NOTES:
 **  This class extends from Service, check in device's location once the
 **  location is fetched.
 *************************************************************************/

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import android.support.v4.content.ContextCompat;

public class LocatorService extends Service
{
    public static final String TAG = "Locator";
    private Connect clientNet;
    
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    boolean quit;
    Context context;
    private android.location.LocationListener mListener;
    private LocationManager locationManager;

    private final long INTERVAL = 60000; // at least 1 min interval
    private final float DISTANCE = 100; // at least 100m distance

    /*****************************************************************************
     * Function:    onCreate
     *
     * Date:        March 22, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   void onCreate(Bundle savedInstanceState)
     *              Bundle savedInstanceState: state before last close
     *
     * Returns:     void
     *
     * Notes:
     * Creates the tracking service, starts a thread to handle location finding.
     **************************************************************************/
    @Override
    public void onCreate()
    {
        context = getBaseContext();
        
        //Create thread for the service
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        PrefHandler.setPref(this, PrefHandler.TRACK_PREF, "true");

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        clientNet = new Connect(this.getApplicationContext());

        // try to connect
        if(!clientNet.foundHost())
        {
            clientNet.teardown();
            return;
        }
    }


    /*****************************************************************************
     * Function:    onStartCommand
     *
     * Date:        March 22, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   int onStartCommand(Intent intent, int flags, int startId)
     *              Intent intent: intent from the starter
     *              int flags: flags for the service
     *              int startId: which request we're stopping when we finish
     *
     * Returns:     START_STICKY
     *
     * Notes:
     * starts the service when the command issued.
     **************************************************************************/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Helper.serviceToast(this, "service starting", Toast.LENGTH_SHORT);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    /*****************************************************************************
     * Function:    onBind
     *
     * Date:        March 22, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   IBinder onBind(Intent intent)
     *              Intent intent: intent to bind on
     *
     * Returns:     null in this scenario (overriding purposes)
     *
     * Notes:
     * Mandatory for services.
     **************************************************************************/
    @Override
    public IBinder onBind(Intent intent)
    {
        // We don't provide binding, so return null
        return null;
    }


    /*****************************************************************************
     * Function:    onDestroy
     *
     * Date:        March 22, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   void onDestroy()
     *
     * Returns:     void
     *
     * Notes:
     * Destroys service. Stops thread and unregisters from the location manager.
     **************************************************************************/
    @Override
    public void onDestroy() {
        quit = true;
        locationManager.removeUpdates(mListener);
        mServiceLooper.quit();
        Helper.serviceToast(this, "service done", Toast.LENGTH_SHORT);
        PrefHandler.setPref(this, PrefHandler.TRACK_PREF, "false");
        clientNet.teardown();
        super.onDestroy();
    }


    /*****************************************************************************
     * This class extends from Handler to handle the messages (eg. location updated)
     ****************************************************************************/
    private final class ServiceHandler extends Handler
    {
        private Location mLocation;
        private Criteria mCriteria;
        private String mProvider;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /***********************************************************************
         * Function:    handleMessage
         *
         * Date:        March 22, 2017
         *
         * Revision:
         *
         * Designer:    Fred Yang
         *
         * Programmer:  Fred Yang
         *
         * Interface:   void handleMessage(Message msg)
         *              Message msg: message received from the service
         *
         * Returns:     void
         *
         * Notes:
         * Creates location manager and listener for location changes.
         * Whenever location listener is invoked, checkinLocation is called.
         **************************************************************************/
        @Override
        public void handleMessage(Message msg)
        {
            try {
                // check if permissions are granted
                Log.d(TAG, "handleMessage: start");
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                mListener = new MyLocationListener();
                mCriteria = new Criteria();
                mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
                mProvider = locationManager.getBestProvider(mCriteria, false);
                locationManager.requestLocationUpdates(mProvider, INTERVAL, DISTANCE, mListener);
                mLocation = locationManager.getLastKnownLocation(mProvider);

                if (mLocation != null) {
                    mListener.onLocationChanged(mLocation);
                    Log.d(TAG, "handleMessage: location changed");
                }
            } catch (SecurityException se) {
                Log.d(TAG, this.getClass().getSimpleName() + ": " + se.getMessage());
            }
            Log.d(TAG, "handleMessage: end");
        }


        /***********************************************************************
         * Function:    checkinLocation
         *
         * Date:        March 22, 2017
         *
         * Revision:
         *
         * Designer:    Fred Yang, John Agapeyev
         *
         * Programmer:  Fred Yang, John Agapeyev
         *
         *Interface:    void checkinLocation (Location loc, String time)
         *              location loc: location data
         *              String time: time stamp
         *
         *Returns:      void
         *
         * Notes:
         * Checks in the location to the the remote server.
         ***************************************************************************/
        private void checkinLocation(Location location, String time)
        {
            Log.d(TAG, "Latitude: " + String.valueOf(location.getLatitude()) );
            Log.d(TAG, "Longitude: " + String.valueOf(location.getLongitude()) );

            clientNet.setPacketData(time, String.valueOf(location.getLatitude()), 
                String.valueOf(location.getLongitude()));

            new Thread(clientNet).start();
        }

        /*****************************************************************************
        * Customized GPS location listener.
        ****************************************************************************/
        private class MyLocationListener implements android.location.LocationListener
        {
            Long time;

            /***********************************************************************
             * Function:    onLocationChanged
             *
             * Date:        March 22, 2017
             *
             * Revision:
             *
             * Designer:    Fred Yang, John Agapeyev
             *
             * Programmer:  Fred Yang, John Agapeyev
             *
             * Interface:   void onLocationChanged(Location location)
             *              Location location: new location
             *
             * Returns:     void
             *
             * Notes:
             * Gets current location, calls checkinLocation.
             **************************************************************************/
            @Override
            public void onLocationChanged(Location location) {
                // initialize the location fields
                mLocation = location;
                time = location.getTime();

                // retrieve current time
                Calendar cal = Calendar.getInstance();

                // date format
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));

                // check in location information
                checkinLocation(mLocation, dateFormat.format(cal.getTime()));

            }


            /***********************************************************************
             * Function:    onStatusChanged
             *
             * Date:        March 22, 2017
             *
             * Revision:
             *
             * Designer:    Fred Yang, John Agapeyev
             *
             * Programmer:  Fred Yang, John Agapeyev
             *
             * Interface:   void onStatusChanged(String provider, int status, Bundle extras)
             *              String provider: the provider that has changed
             *              int status: new status
             *              Bundle extras: extras with the status changed
             *
             *Returns:      void
             *
             * Notes:
             * Occurs when the status of the service has changed.
             **************************************************************************/
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                Log.d(TAG, provider + ": " + status);
            }


            /***********************************************************************
             * Function:    onProviderEnabled
             *
             * Date:        March 22, 2017
             *
             * Revision:
             *
             * Designer:    Fred Yang
             *
             * Programmer:  Fred Yang
             *
             * Interface:   void onProviderEnabled(String provider)
             *              String provider: the provider that is enabled
             *
             * Returns:     void
             *
             * Notes:
             * Occurs when the provider becomes enabled
             **************************************************************************/
            @Override
            public void onProviderEnabled(String provider)
            {
                Log.d(provider, "enabled");
            }


            /***********************************************************************
             * Function:    onProviderDisabled
             *
             * Date:        March 25, 2017
             *
             * Revision:
             *
             * Designer:    Fred Yang
             *
             * Programmer:  Fred Yang
             *
             * Interface:   void onProviderDisabled(String provider)
             *              String provider: the provider that is now enabled
             *
             * Returns:     void
             *
             * Notes:
             * Occurs when the provider becomes disabled.
             **************************************************************************/
            @Override
            public void onProviderDisabled(String provider)
            {
                Log.d(provider, "disabled");
            }
        }
    }

}

