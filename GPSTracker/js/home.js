/**********************************************************************
**  SOURCE FILE:    home.js -  Javascript for the home page
**      
**  PROGRAM:        GPS Locator
**
**  FUNCTIONS:
**
**
**  DATE:           March 25, 2017
**
**
**  DESIGNER:       Isaac Morneau
**
**
**  PROGRAMMER:     Isaac Morneau
**
**  NOTES:
**  Displays the location information of active users.
*************************************************************************/

/*******************************************************************
** Function:    $(document).ready()
**
** Date:        March 24, 2017
**
** Revisions: 
**
**
** Designer:    Isaac Morneau
**
** Programmer:  Isaac Morneau
**
** Interface:
**              $(document).ready(function()
**
** Returns: 
**
**
** Notes:
** When the document is ready it loads the location information.
**
**********************************************************************/
$(document).ready(function()
{
    // load data from the file
    loadData();
});


/*******************************************************************
** Function:    loadData
**
** Date:        March 25, 2017
**
** Revisions: 
**
**
** Designer:    Isaac Morneau
**
** Programmer:  Isaac Morneau
**
** Interface:
**              loadData()
**
** Returns:
**
**
** Notes:
** Parses the XML file for users and add them to the table.
** Called repeatedly by a timer.
**
**********************************************************************/
function loadData()
{
    // clear off the data for the refresh
    $('#users > tbody').empty();


    if (window.XMLHttpRequest)
    {
        xhttp = new XMLHttpRequest();
    }

    // open the xml file
    xhttp.open("GET", "tracker.xml", false);
    xhttp.send();
    xmlDoc = xhttp.responseXML;

    // fetch all the users
    var users = xmlDoc.getElementsByTagName("user");

    // loop through all the users
    for (var i = 0; i < users.length; i++)
    {
        // time
        var time = users[i].getElementsByTagName("time")[0].childNodes[0].nodeValue;
        
        // ip address
        var ip = users[i].getElementsByTagName("ip")[0].childNodes[0].nodeValue;
        
        // device id
        //var devId = users[i].getElementsByTagName("devId")[0].childNodes[0].nodeValue;

        // device name
        var devName = users[i].getElementsByTagName("devName")[0].childNodes[0].nodeValue;

        // latitude & longitude
        var latitude = users[i].getElementsByTagName("latitude")[0].childNodes[0].nodeValue;
        var longitude = users[i].getElementsByTagName("longitude")[0].childNodes[0].nodeValue;

        // add the information to the table
        addToTable(time, ip, devName, latitude, longitude);
    }

    // set the timer for the next update to refresh data
    window.setTimeout(loadData, 5000);

}


/*******************************************************************
** Function:    addToTable
**
** Date:        March 25, 2017
**
** Revisions: 
**
**
** Designer:    Isaac Morneau
**
** Programmer:  Isaac Morneau
**
** Interface:
**              addToTable(time, ip, devName, latitude, longitude)
**
** Returns:
**
**
** Notes:
** Adds the data to the page in a table format.
**
**********************************************************************/
function addToTable(time, ip, devName, latitude, longitude)
{
    // add the user to the table
    $('#users > tbody:last').append('<tr><td>' + time + '</td><td>'+ ip + '</td><td>'
        + devName + '</td><td>' + latitude + '</td><td>' + longitude + '</td></tr>');
}

