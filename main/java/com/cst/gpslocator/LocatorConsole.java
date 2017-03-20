package com.cst.gpslocator;

/**********************************************************************
 **  SOURCE FILE:   LocatorConsole.java -  Java file for the console
 **
 **  PROGRAM:       GPS Locator
 **
 **  FUNCTIONS:
 **                 boolean onOptionsItemSelected(MenuItem item)
 **                 boolean onCreateOptionsMenu(Menu menu)
 **                 void onCreate(Bundle savedInstanceState)
 **                 void showMap(View view)
 **                 void toggleTracking(View view)
 **                 void goBack(View view)
 **
 **  DATE:
 **
 **
 **  DESIGNER:
 **
 **
 **  PROGRAMMER: 
 **
 **  NOTES:
 **  This console provides: 
 **  1) turn tracking on/off
 **  2) show Google map
 **  3) back to entry page
 **
 *************************************************************************/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class LocatorConsole extends AppCompatActivity
{
    public static final String TAG = "Locator";
    private final String START_TRACKING = "Start Tracking";
    private final String STOP_TRACKING = "Stop Tracking";
    private static final String SUBURL = "/~pi/";
    private boolean tracking;

    /*****************************************************************************
     * Function:    onCreate
     * Date:
     * Revision:
     *
     * Designer: 
     *
     * Programmer:
     *
     * Interface:   void onCreate(Bundle savedInstanceState)
     *              Bundle savedInstanceState: state before last close
     *
     * Returns:     void
     *
     * Notes:
     * Creates the Locator Console activity.
     **************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);

        // check if tracking turned on/off
        tracking = (PrefHandler.getPref(this, PrefHandler.TRACK_PREF).equals("true"));

        Button toggle = (Button)findViewById(R.id.toggleTracking);
        String msg = (tracking ? STOP_TRACKING : START_TRACKING);
        toggle.setText(msg);
    }


    /*****************************************************************************
     * Function:    onCreateOptionsMenu
     * Date:
     * Revision:
     *
     * Designer:
     *
     * Programmer:
     *
     * Interface:   boolean onCreateOptionsMenu(Menu menu)
     *              Menu menu: the menu items to be inflated
     *
     * Returns:     return true on success, false on failure
     *
     * Notes:
     * Creates the options menu bar.
     **************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, adding items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_console, menu);
        return true;
    }


    /*****************************************************************************
     * Function:    onOptionsItemSelected
     * Date:
     * Revision:
     *
     * Designer:
     *
     *Programmer:
     *
     *Interface:    boolean  onOptionsItemSelected(MenuItem item)
     *              MenuItem item: item selected in the menu
     *
     * Returns:     return true on success, false on failure
     *
     *
     * Notes:
     * Invoked when an item is selected.
     **************************************************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*****************************************************************************
     * Function:    toggleTracking
     * Date:
     * Revision:
     *
     * Designer:
     *
     * Programmer:
     *
     * Interface:   void toggleTracking(View view)
     *              View view: the view where the form rendering on
     *
     * Returns:     void
     *
     * Notes:
     * Starts or stops tracking service.
     **************************************************************************/
    public void toggleTracking(View view)
    {
        Intent intent;
        
        // start or stop tracking
        if(tracking)
        {
            // stop service
            intent = new Intent(this, LocatorService.class);
            PrefHandler.setPref(this, PrefHandler.TRACK_PREF, "false");
            stopService(intent);
        }
        else
        {
            //start service
            intent = new Intent(this, LocatorService.class);
            PrefHandler.setPref(this, PrefHandler.TRACK_PREF, "true");
            startService(intent);
        }

        // update button
        tracking = !tracking;
        Button toggle = (Button)findViewById(R.id.toggleTracking);
        String msg = (tracking) ? STOP_TRACKING : START_TRACKING;
        toggle.setText(msg);
    }


    /*****************************************************************************
     * Function:    goBack
     * Date:
     * Revision:
     *
     * Designer: 
     *
     * Programmer: 
     *
     * Interface:   void goBack(View view)
     *              View view: the view where the form rendering on
     *
     * Returns:     void
     *
     * Notes:
     * Go back to the entry view.
     **************************************************************************/
    public void goBack(View view)
    {
        Intent intent;
        
        // go back to Locator activity
        intent = new Intent(this, LocatorService.class);
        stopService(intent);
        PrefHandler.setPref(this, PrefHandler.TRACK_PREF, "false");

        intent = new Intent(this, Locator.class);
        startActivity(intent);
        finish();
    }


    /*****************************************************************************
     * Function:    showMap
     * Date:
     * Revision:
     *
     * Designer:
     *
     * Programmer: 
     *
     * Interface:   void showMap(View view)
     *              View view: the view where the form rendering on
     *
     * Returns:     void
     *
     * Notes:
     * Called to load the Google map view.
     **************************************************************************/
    public void showMap(View view)
    {
        // open in-app browser activity to show active users
        String url = "http://" + PrefHandler.getPref(this, PrefHandler.HOST_PREF) + SUBURL;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
