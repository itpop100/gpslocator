/**********************************************************************
**  SOURCE FILE:    map-script.js -  Javascript for the Google map view
**      
**  PROGRAM:        GPS Locator
**
**  FUNCTIONS:
**
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
**  Displays the customized Google map.
*************************************************************************/

/** Google map **/
var map;

/** geolocation coder **/
var geocoder;

/** Array of markers **/
var markers = new Array();

var MY_MAPTYPE_ID = 'custom_style';


var pin = {
    url: "../assets/marker.png",             // marker image
    scaledSize: new google.maps.Size(17, 32),// scaled size
    origin: new google.maps.Point(0, 0),     // origin
    anchor: new google.maps.Point(17.5, 32)  // anchor
};


/*******************************************************************
** Function:    $(document).ready()
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
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
    initialize();
    
    //load data from the file
    loadData();

});


/*******************************************************************
** Function:    initialize
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
**
** Interface:
**              initialize()
**
** Returns:
**
**
** Notes:
** Initializes the Google map and focuses the view on user's location.
**
**********************************************************************/
function initialize() 
{
    var featureOpts = [
    {
        "featureType": "all",
        "elementType": "labels.text",
        "stylers": [
            {
                //"color": "#ff0000"
            }
        ]
    },
    {
        "featureType": "all",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                //"color": "#ffffff"
            }
        ]
    }
    ];

    var mapOptions = {
        zoom: 6,
        mapTypeControlOptions: {
            mapTypeIds: [google.maps.MapTypeId.ROADMAP, MY_MAPTYPE_ID]
        },
        mapTypeId: MY_MAPTYPE_ID
    };

    // set the styling on the google map
    map = new google.maps.Map(document.getElementById('googleMap'), mapOptions);
    var styledMapOptions = {name: 'Custom Style'};
    var customMapType = new google.maps.StyledMapType(featureOpts, styledMapOptions);
    map.mapTypes.set(MY_MAPTYPE_ID, customMapType);

    // initialize the geocoder
    geocoder = new google.maps.Geocoder();

    // receive the location from HTML
    if(navigator.geolocation)
    {
        navigator.geolocation.getCurrentPosition(function(position) 
        {
            var pos = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
            map.setCenter(pos);
        },

        function() 
        {
            handleNoGeolocation(true);
        });
    } else {
        // Browser doesn't support Geolocation
        handleNoGeolocation(false);
    }
}

/*******************************************************************
** Function:    handleNoGeolocation
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
**
** Interface:
**              handleNoGeolocation(errorFlag) 
**              errorFlag: true - geolocation service failed
**                         false - browser not supported
**
** Returns:
**
**
** Notes:
** Handles no geolocation error.
**
**********************************************************************/
function handleNoGeolocation(errorFlag) 
{
    if (errorFlag) {
        var content = 'Error: The Geolocation service failed.';
    } else {
        var content = 'Error: Your browser doesn\'t support geolocation.';
    }

    var options = {
        map: map,
        position: new google.maps.LatLng(60, 105),
        content: content
    };

    var infowindow = new google.maps.InfoWindow(options);
    map.setCenter(options.position);
}


/*******************************************************************
** Function:    loadData
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
**
** Interface:
**              loadData()
**
** Returns:
**
**
** Notes:
** Parses the XML file for users.
**
**********************************************************************/
function loadData()
{
    for (var i = 0; i < markers.length; i++)
    {
        markers[i].setMap(null);
    }

    if (window.XMLHttpRequest)
    {
        xhttp = new XMLHttpRequest();
    }

    // open the file
    xhttp.open("GET", "../tracker.xml", false);
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

        // add the information to the map
        addMarker(time, ip, devName, latitude, longitude);
    }

    // set the timer for the next update to refresh data
    window.setTimeout(loadData, 5000);

}


/*******************************************************************
** Function:    addMarker
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
**
** Interface:
**              addMarker(time, ip, devName, latitude, longitude)
**              time: time stamp
**              ip: device ip address
**              devName: device name
**              latitude: latitude of the pin
**              longitude: longitude of the pin
**
** Returns:
**
**
** Notes:
**  Fetches the address of the marker and adds it
**
**********************************************************************/
function addMarker(time, ip, devName, latitude, longitude)
{
    // create the latLng object
    var location = new google.maps.LatLng(parseFloat(latitude), parseFloat(longitude));

    // fetch the address via lat long object
    getAddress(time, ip, devName, latitude, longitude);
}


/*******************************************************************
** Function:    getAddress
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
**
** Interface:
**              getAddress(time, ip, devName, latitude, longitude)
**              time: time stamp
**              ip: device ip address
**              devName: device name
**              latitude: latitude of the pin
**              longitude: longitude of the pin
**
** Returns:
**          void
**
** Notes:
** Fetches the address from the specified location and creates a marker.
**
**********************************************************************/
function getAddress(time, ip, devName, latitude, longitude)
{
    // fetch the latitude and longitude as floats
    var lat = parseFloat(latitude);
    var lng = parseFloat(longitude);
    var location = new google.maps.LatLng(lat, lng);

    geocoder.geocode({'latLng': location}, function(results, status) 
    {
        if (status == google.maps.GeocoderStatus.OK) 
        {
            if (results[0]) 
            {
              createMarker(time, ip, devName, location, results[0].formatted_address);
            }
            else if (result[1])
            {
                createMarker(time, ip, devName, location, results[1].formatted_address);
            }
        } 
        else
        {
             createMarker(time, ip, devName, location, "Unable to get address");
        }
    });
}


/*******************************************************************
** Function:    createMarker
**
** Date:
**
** Revisions: 
**
**
** Designer:
**
** Programmer:
**
** Interface:
**              createMarker(time, ip, devName, location, addr)
**              time: time stamp
**              ip: device ip address
**              devName: device name
**              location: latLng format of the device
**              addr: address of the device
**
** Returns:
**
**
** Notes:
** Creates a marker at the device's approximate location.
** On hover it displays the address and device information,
** When a marker is clicked it zooms to its location.
**********************************************************************/
function createMarker(time, ip, devName, location, addr)
{
    // make a marker
    var marker = new google.maps.Marker({ position: location, map: map, title: String(name), icon: pin});

    // user details
    var contentString = time + "<br/>" + devName + "<br/>" + ip + "<br/>" + addr;
    
    marker['infowindow'] = new google.maps.InfoWindow({content: contentString });

    // add a listener to open the info window on hover
    google.maps.event.addListener(marker, 'mouseover', function() 
    {
        this['infowindow'].open(map, this);
    });

    // add a listener to zoom on click
    google.maps.event.addListener(marker, 'click', function() 
    {
       map.setCenter(new google.maps.LatLng(marker.position.lat(), marker.position.lng())); 
        map.setZoom(18); 
    });

    // add the marker to the array
    markers.push(marker);
}
