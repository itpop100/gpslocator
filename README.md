---------------------------------------------------------------
               Android GPS Locator - Readme
---------------------------------------------------------------

This readme outlines the steps on how to run the application.

---------------------------------------------------------------
                    Android Client App
---------------------------------------------------------------
1. Install GPSLocator.apk file on your device. 
2. Run the app.
3. Enter host IP and port number (8910) on the Entry View, then
   press the "Connect" button.
4. Once "Locator Console" view appears, press "Start Tracking" 
   button to check in your location.
5. Press "Show Map" button to display Google map on your device.
6. Press "Stop Tracking" to stop the tracking service.
7. Press "Go Back" button to return to the Entry View.

---------------------------------------------------------------
                    Server Application
---------------------------------------------------------------
1. Place "GPSTracker.java" in the same directory as index.html
   on the Apache Web Server.
2. Compile and run the java file: 
   javac GPSTracker.java 
   java GPSTracker [8910] (port 8910 is optional)
3. Make sure port forwarding is correct. GPSTracker will add 
   data to the tracker.xml file automatically.


---------------------------------------------------------------
                    Apache Web Server
---------------------------------------------------------------
1. Place all web files in the directory of choice on your Apache
   Web Server.
2. Make sure Apache is running and virtual hosting is set up 
   correctly.
3. Point your browser @ http://pi.ca
   username: pi     password: pi

