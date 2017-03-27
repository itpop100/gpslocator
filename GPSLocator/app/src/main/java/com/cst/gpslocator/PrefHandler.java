package com.cst.gpslocator;

/***********************************************************************
 **  SOURCE FILE:   PrefHandler.java -  Java preference handler.
 **
 **  PROGRAM:       GPS Locator
 **
 **  FUNCTIONS:
 **                 String[] checkPref(Context c)
 **                 void addPref(Context c, String host, String port)
 **                 String getPref(Context c, String key)
 **                 void setPref(Context c, String key, String value)
 **
 **  DATE:          March 23, 2017
 **
 **
 **  DESIGNER:      Fred Yang, John Agapeyev
 **
 **
 **  PROGRAMMER:    Fred Yang, John Agapeyev
 **
 **  NOTES:
 **  This Static class handles the interaction with the Android
 **  Shared Preferences.
 ***************************************************************************/

import android.content.Context;
import android.content.SharedPreferences;

public class PrefHandler
{
    public static final String HOST_PREF = "HOST";
    public static final String PORT_PREF = "PORT";
    public static final String TRACK_PREF = "TRACKING";
    public static final String PrefName = "locator";

    /*****************************************************************************
     * Function:    checkPref
     *
     * Date:        March 23, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   String[] checkPref(Context c)
     *              Context c: Current context
     *
     * Returns:     String[]: array of shared preferences
     *
     * Notes:
     * Checks for current host and port preferences, returns array with these values
     **************************************************************************/
   static String[] checkPref(Context c)
   {
       String[] pref = new String[2];
       //SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_WORLD_READABLE);
       SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_PRIVATE);
       pref[0] = sharedpreferences.getString(HOST_PREF, "");
       pref[1] = sharedpreferences.getString(PORT_PREF, "");

       return pref;
   }


    /*****************************************************************************
     * Function:    addPref
     *
     * Date:        March 24, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   void addPref(Context c, String host, String port)
     *              Context c: current context
     *              String host: host name
     *              String port: port name
     *
     * Returns:     void
     *
     * Notes:
     * Adds given host and port preferences to the shared preferences file.
     **************************************************************************/
    static void addPref(Context c, String host, String port)
    {
        //SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_WORLD_READABLE);
        SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HOST_PREF, host);
        editor.putString(PORT_PREF, port);
        editor.commit();
    }


    /*****************************************************************************
     * Function:    getPref
     *
     * Date:        March 24, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   String getPreference(Context c, String key)
     *              Context: current context
     *              String key: shared preference key
     *
     * Returns:     return preferences string
     *
     * Notes:
     * Returns preference for this key, or empty if no preference found
     **************************************************************************/
    static String getPref(Context c, String key)
    {
        //SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_WORLD_READABLE);
        SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_PRIVATE);
        return sharedpreferences.getString(key, "");
    }


    /*****************************************************************************
     * Function:    setPref
     *
     * Date:        March 24, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   void setPref(Context c, String key, String value))
     *              Context c: current app context
     *              String key: shared preference key
     *              string value: value to set in the preference
     *
     *Returns:      void
     *
     * Notes:
     * Sets preference for this key
     **************************************************************************/
    static void setPref(Context c, String key, String value)
    {
        //SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_WORLD_READABLE);
        SharedPreferences sharedpreferences = c.getSharedPreferences(PrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
