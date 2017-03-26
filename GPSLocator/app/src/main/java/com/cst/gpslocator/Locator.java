package com.cst.gpslocator;

/**********************************************************************
 **  SOURCE FILE:   Locator.java -  Java file for the entry page
 **
 **  PROGRAM:       GPS Locator
 **
 **  FUNCTIONS:
 **                 boolean onOptionsItemSelected(MenuItem item)
 **                 boolean onCreateOptionsMenu(Menu menu)
 **                 void onCreate(Bundle savedInstanceState)
 **                 void submitForm(View view)
 **                 void clearForm(View view)
 **                 boolean validateForm()
 **
 **  DATE:          March 24, 2017
 **
 **
 **  DESIGNER:      Fred Yang, John Agapeyev
 **
 **
 **  PROGRAMMER:    Fred Yang, John Agapeyev
 **
 **  NOTES:
 **  This Activity allows the user to input host information. A connection 
 **  is attempted when the user presses "connect" button. It shows the 
 **  tracking page once the connection succeeds.
 *************************************************************************/
 
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

public class Locator extends AppCompatActivity {
    public static final String TAG = "Locator";
    
    private final TextWatcher mTextEditorWatcher = new TextWatcher()
    {
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {}

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}

        public void afterTextChanged(Editable s)
        {
            validateForm();
        }
    };

    /*****************************************************************************
     * Function:    onCreate
     *
     * Date :       March 24, 2017
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
     * Creates the Locator activity, pre-populating the last used host
     * and port into the GUI.
     **************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);

        // once connected, start console activity
        if(PrefHandler.getPref(this, PrefHandler.TRACK_PREF).equals("true"))
        {
            Intent intent = new Intent(this, LocatorConsole.class);
            startActivity(intent);
            finish();
        }

        // check preferences
        String[] pref = PrefHandler.checkPref(this);

        // get user inputs
        EditText[] views = new EditText[2];
        views[0] = (EditText)findViewById(R.id.etHost);
        views[1] = (EditText)findViewById(R.id.etPort);

        if(pref.length == 2) {
            for(int i = 0; i < 2; i++)
            {
                views[i].setText(pref[i]);
                views[i].setSelection(views[i].getText().length());
                views[i].addTextChangedListener(mTextEditorWatcher);
            }
        }

        // validate form
        validateForm();
    }


    /*****************************************************************************
     * Function:    onCreateOptionsMenu
     *
     * Date:        March 24, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
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
        getMenuInflater().inflate(R.menu.menu_locator, menu);
        return true;
    }

    /*****************************************************************************
     * Function:    onOptionsItemSelected
     *
     * Date:        March 25, 2017
     *
     * Revision:    
     *
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   boolean  onOptionsItemSelected(MenuItem item)
     *              MenuItem item: menu item selected
     *
     * Returns:     return true on success, false on failure
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
     * Function:    clearForm
     *
     * Date :       March 22, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang
     *
     * Programmer:  Fred Yang
     *
     * Interface:   void clearForm(View view)
     *              View view: the view where the form rendering on
     *
     * Returns:     void
     *
     * Notes:
     * Called to clear the fields on the form.
     *
     **************************************************************************/
    public void clearForm(View view)
    {
        EditText[] views = new EditText[2];
        views[0] = (EditText)findViewById(R.id.etHost);
        views[1] = (EditText)findViewById(R.id.etPort);

        for(int i = 0; i < 2; i++)
        {
            views[i].setText("");
        }
    }


    /*****************************************************************************
     * Function:    submitForm
     *
     * Date:        March 19, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     *Programmer:   Fred Yang, John Agapeyev
     *
     *Interface:    void submitForm(View view)
     *              View view: the view where the form rendering on
     *
     * Returns:     void
     *
     * Notes:
     * Invoked to validate input and connect to the remote server.
     **************************************************************************/
    public void submitForm(View view)
    {
        Connect clientNet;
        String host, port;

        if(!validateForm())
        {
            Helper.makeToast(this, "Must specify a host and port!", Toast.LENGTH_LONG);
            return;
        }

        // get username, host and port
        EditText etHost = (EditText)findViewById(R.id.etHost);
        EditText etPort = (EditText)findViewById(R.id.etPort);

        host = etHost.getText().toString();
        port = etPort.getText().toString();

        // add it to shared preferences
        PrefHandler.addPref(this, host, port);

        clientNet = new Connect(this);
        
        // try to connect
        if(!clientNet.foundHost())
        {
            return;
        }
        
        clientNet.teardown();
        
        //open the console view
        Intent intent = new Intent(this, LocatorConsole.class);
        startActivity(intent);
        finish();
    }


    /*****************************************************************************
     * Function:    validateForm
     *
     * Date:        March 20, 2017
     *
     * Revision:
     *
     * Designer:    Fred Yang, John Agapeyev
     *
     * Programmer:  Fred Yang, John Agapeyev
     *
     * Interface:   boolean validateForm()
     *
     * Returns:     true if all the fields are valid; otherwise return false
     *
     * Notes:
     * Ensures all fields have been filled in and are valid.
     **************************************************************************/
    boolean validateForm()
    {
        Button submit = (Button) findViewById(R.id.submit);
        EditText etHost = (EditText)findViewById(R.id.etHost);
        EditText etPort = (EditText)findViewById(R.id.etPort);

        if (etHost.getText().toString().trim().length() > 0
                && etPort.getText().toString().trim().length() > 0)
        {
            //set color blue
            submit.setBackgroundResource(R.drawable.blue_button);
            return true;
        }
        else
        {
            // set color red
            submit.setBackgroundResource(R.drawable.red_button);
            return false;
        }
    }

}
