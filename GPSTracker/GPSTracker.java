/*-------------------------------------------------------------------------------------------------
-- SOURCE FILE: GPSTracker.java - Server side program, which receives location packages from 
--                                active users.
--
-- PROGRAM:     GPSTracker
--
-- FUNCTIONS:
--              GPSTracker(int port)
--              void main(String[] args)
--              void run()
--              void addUser(String message)
--              String getUser(String message)
--              int getNextElement(String message, int start)
--              void close(String message)
--
-- DATE:        March 18, 2017
--
-- REVISIONS:
--
-- DESIGNER:    Fred Yang, Isaac Morneau
--
-- PROGRAMMER:  Fred Yang, Isaac Morneau
--
-- NOTES:
-- This server receives a formatted string sent from client, extracts the data, and stores it
-- into an xml file.
--
-------------------------------------------------------------------------------------------------*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.util.Arrays;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class GPSTracker extends Thread
{
    private static final int PACK_SIZE = 256;    // packet size
    private static final int DEFAULT_PORT = 8910;// default udp port
    
    private String message;                      // message received
    private DatagramSocket listenSocket;         // listening socket
    private DatagramPacket packet;               // datagram packet

    private byte[] packetData;                   // packet data
    private InetAddress ipAddr;                  // IPV4 address

    /*---------------------------------------------------------------------------------------------
    -- FUNCTION:    GPSTracker
    --
    -- DATE:        March 18, 2017
    --
    -- REVISIONS:
    --
    -- DESIGNER:    Fred Yang, Isaac Morneau
    --
    -- PROGRAMMER:  Fred Yang, Isaac Morneau
    --
    -- INTERFACE:   public GPSTracker(int port) throws IOException
    --              int port: the port listening on
    --
    -- RETURNS:
    --
    -- NOTES:
    -- Constructor of the GPSTracker class, throws an exception if datagram socket fails to 
    -- create.
    --
    ---------------------------------------------------------------------------------------------*/
    public GPSTracker(int port) throws IOException
    {
        listenSocket = new DatagramSocket(port);
    }

    /*---------------------------------------------------------------------------------------------
    -- FUNCTION:    main
    --
    -- DATE:        March 25, 2017
    --
    -- REVISIONS:
    --
    -- DESIGNER:    Fred Yang, Isaac Morneau
    --
    -- PROGRAMMER:  Fred Yang, Isaac Morneau
    --
    -- INTERFACE:   public static void main (String [] args)
    --              String [] args // command line arguments
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- Main entry of the program. It takes a user defined port, or assigns the default port
    -- if not specified, will start the server tracking thread.
    --
    ---------------------------------------------------------------------------------------------*/
    public static void main(String[] args)
    {
        // specify port
        int port = 0;
        
        if(args.length < 1)
            port = DEFAULT_PORT;
        else
            port = Integer.parseInt(args[0]);
       
        // start server tracking thread
        try
        {
            Thread thread = new GPSTracker(port);
            thread.start();
        }
        
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /*---------------------------------------------------------------------------------------------
    -- FUNCTION:    run
    --
    -- DATE:        March 25, 2017
    --
    -- REVISIONS: 
    --
    -- DESIGNER:    Fred Yang, Isaac Morneau
    --
    -- PROGRAMMER:  Fred Yang, Isaac Morneau
    --
    -- INTERFACE:   public void run()
    --
    -- RETURNS:     void
    --
    -- NOTES:
    -- run method of the tracking thread.
    --
    ---------------------------------------------------------------------------------------------*/
    public void run()
    {
        while (true)
        {
            packetData = new byte[PACK_SIZE];
            packet = new DatagramPacket(packetData, PACK_SIZE);
            
            try
            {
                // listen for oncoming datagrams
                System.out.println("Port: " + listenSocket.getLocalPort());
                listenSocket.receive(packet);
                ipAddr = packet.getAddress();
                System.out.println("Packet from: " + ipAddr + ":" + packet.getPort());
                
                // message received
                message = new String(packetData, 0, packet.getLength());
                System.out.println ("Message: " + message.trim());
                addUser(message);
            }
    
            catch (SocketTimeoutException se)
            {
                System.out.println("Socket timed out!");
                listenSocket.close();
                break;
            }
  
            catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    /*---------------------------------------------------------------------------------------------
    -- FUNCTION:    addUser
    --
    -- DATE:        March 17, 2017
    --
    -- REVISIONS: 
    --
    -- DESIGNER:    Fred Yang, Isaac Morneau
    --
    -- PROGRAMMER:  Fred Yang, Isaac Morneau
    --
    -- INTERFACE:   public void addUser(String message)
    --              String message: formatted string containing user data
    --
    -- RETURNS:     void
    --
    -- NOTES:
    -- Takes a message string, parses it and then adds it to an xml file.
    --
    ---------------------------------------------------------------------------------------------*/
    protected void addUser(String message)
    {
        File record = new File("tracker.xml");  // record xml file
        FileInputStream in;     // file input stream
        FileOutputStream out;   // file output stream
        
        int remain = 0;         // # remaining bytes to read
        int offset = 8;         // offset to skip
        String element;         // element in xml

        try
        {
            if(!record.exists()) {
                record.createNewFile();
            }
            
            in = new FileInputStream(record);
            remain = in.available() - offset;
            byte[] bRemain = new byte[remain];
            in.read(bRemain);
            in.close();

            out = new FileOutputStream(record);
            element = getUser(message);
            byte bElement[] = element.getBytes();

            byte[] result = new byte[bRemain.length + bElement.length];
            System.arraycopy(bRemain, 0, result, 0, bRemain.length);
            System.arraycopy(bElement, 0, result, bRemain.length, bElement.length);

            out.write(result);
        }
        catch(Exception e)
        {
            System.out.println(e);
            close("addUser failure.");
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- FUNCTION:    getUser
    --
    -- DATE:        March 24, 2017
    --
    -- REVISIONS:
    --
    -- DESIGNER:    Isaac Morneau, Fred Yang
    --
    -- PROGRAMMER:  Isaac Morneau, Fred Yang
    --
    -- INTERFACE:   public String getUser(String message)
    --              String message: formatted string containing user data
    --
    -- RETURNS:     a xml formatted string containing user data
    --
    -- NOTES:
    -- Takes a formatted string, reformats and stores it to the xml file.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    protected String getUser(String message)
    {
        String err = "File format incorrect.";

        String time;
        String ip;
        String devId;
        String devName;
        String latitude;
        String longitude;

        int index = 0;
        int nextIndex = 0;

        nextIndex = getNextElement(message, index);
        if (nextIndex < 0) close(err);
        time = message.substring(index, nextIndex);
        index = nextIndex + 1;

        nextIndex = getNextElement(message, index);
        if (nextIndex < 0) close(err);
        ip = message.substring(index, nextIndex);
        index = nextIndex + 1;

        nextIndex = getNextElement(message, index);
        if (nextIndex < 0) close(err);
        devId = message.substring(index, nextIndex);
        index = nextIndex + 1;

        nextIndex = getNextElement(message, index);
        if (nextIndex < 0) close(err);
        devName = message.substring(index, nextIndex);
        index = nextIndex + 1;
        
        nextIndex = getNextElement(message, index);
        if (nextIndex < 0) close(err);
        latitude = message.substring(index, nextIndex);
        index = nextIndex + 1;

        nextIndex = getNextElement(message, index);
        if (nextIndex < 0) close(err);
        longitude = message.substring(index, nextIndex);

        return "    <user>\n" +
               "        <time>" + time + "</time>\n" +
               "        <ip>" + ip  + "</ip>\n" +
               "        <devId>" + devId + "</devId>\n" +
               "        <devName>" + devName + "</devName>\n" +
               "        <latitude>" + latitude  + "</latitude>\n" +
               "        <longitude>" + longitude  + "</longitude>\n" +
               "    </user>\n" +
               "</users>";
    }

    /*---------------------------------------------------------------------------------------------
    -- FUNCTION:    getNextElement
    --
    -- DATE:        March 18, 2017
    --
    -- REVISIONS: 
    --
    -- DESIGNER:    Isaac Morneau, Fred Yang
    --
    -- PROGRAMMER:  Isaac Morneau, Fred Yang
    --
    -- INTERFACE:   public int getNextElement(String message, int start)
    --              String message: formatted string containing user data
    --              int start: position to parse from
    --
    -- RETURNS:     returns the start position of the next element; returns -1
    --              if not found
    --
    -- NOTES:
    -- Finds the next delimiter char ',' in the string from the start index.
    --
    ---------------------------------------------------------------------------------------------*/
    protected int getNextElement(String message, int start)
    {
        for (int i = start; i < message.length(); i++)
        {
            if (message.charAt(i) == ',') return i;
        }

        return -1;
    }

    /*---------------------------------------------------------------------------------------------
    -- FUNCTION:    close
    --
    -- DATE:        March 18, 2017
    --
    -- REVISIONS:
    --
    -- DESIGNER:    Isaac Morneau, Fred Yang
    --
    -- PROGRAMMER:  Isaac Morneau, Fred Yang
    --
    -- INTERFACE:   public void close(String message)
    --              String message: error message
    --
    -- RETURNS:     void
    --
    -- NOTES:
    -- Prints an error message and kills the program.
    --
    ---------------------------------------------------------------------------------------------*/
    protected void close(String message)
    {
        System.out.println(message);
        System.err.println(message);
        System.exit(-1);
    }

}