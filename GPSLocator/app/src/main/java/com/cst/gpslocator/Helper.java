package com.cst.gpslocator;

/**********************************************************************
 **  SOURCE FILE:   Helper.java -  Java helper file.
 **
 **  PROGRAM:       GPS Locator
 **
 **  FUNCTIONS:
 **                 void serviceToast(Context context, String text, int duration)
 **                 void makeToast(Activity activity, String text, int duration)
 **
 **  DATE:          March 24, 2017
 **
 **
 **  DESIGNER:      John Agapeyev, Fred Yang
 **
 **
 **  PROGRAMMER:    John Agapeyev, Fred Yang
 **
 **  NOTES:
 **  Helper functions.
 *************************************************************************/

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class Helper {

    /*****************************************************************************
     * Function:    makeToast
     *
     * Date:        March 24, 2017
     *
     * Revision:
     *
     * Designer:    John Agapeyev
     *
     * Programmer:  John Agapeyev
     *
     * Interface:   void makeToast(Activity activity, String text, int duration)
     *              Activity activity: current activity
     *              String text: toast text
     *              int duration: duration of the toast
     *
     * Returns:     void
     *
     * Notes:
     * Creates a fancy custom toast.
     **************************************************************************/
    public static void makeToast(Activity activity, String text, int duration)
    {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View customView = inflater.inflate(R.layout.custom_toast, null);
        Toast customToast = new Toast(activity);

        TextView tvToast = (TextView)customView.findViewById(R.id.toastView);
        tvToast.setText(text);

        customToast.setView(customView);
        customToast.setGravity(Gravity.CENTER,0, 0);
        customToast.setDuration(duration);
        customToast.show();
    }

    
    /*****************************************************************************
     * Function:    serviceToast
     *
     * Date:        March 24, 2017
     *
     * Revision:
     *
     * Designer:    John Agapeyev, Fred Yang
     *
     * Programmer:  John Agapeyev, Fred Yang
     *
     * Interface:   void serviceToast(Context context, String text, int duration)
     *              Activity context: current activity
     *              String text: toast text
     *              int duration: duration of the toast
     *
     *Returns:      void
     *
     * Notes:
     * Creates a toast on the screen.
     **************************************************************************/
    public static void serviceToast(Context context, String text, int duration)
    {
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
