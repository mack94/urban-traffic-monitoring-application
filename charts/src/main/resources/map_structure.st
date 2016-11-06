<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
    <meta charset="utf-8">
    <title>Traffic layer</title>
    <style>
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
        }
        #map {
            height: 100%;
        }
    </style>
</head>
<body>
<div id="map"></div>
<script src="jquery-3.1.0.js"></script>
<script>
    function initMap() {
        var routeStart = {lat: $startLat$, lng: $startLng$};
        var routeEnd = {lat: $endLat$, lng: $endLng$};
		//var routeStart = {lat: 50.07, lng: 19.94};
		//var routeEnd = {lat: 50.079, lng: 19.94};

        // Instantiate a directions service.
        var directionsService = new google.maps.DirectionsService;
        var map = new google.maps.Map(document.getElementById('map'), {
            zoom: 15,
            center: routeStart,
            streetViewControl: false
        });

        // Create a renderer for directions and bind it to the map.
        var directionsDisplay = new google.maps.DirectionsRenderer({map: map});

//        var markerStart = new google.maps.Marker({
//            position: routeStart,
//            map: map,
//            title: 'Route start'
//        });

//        var markerEnd = new google.maps.Marker({
//            position: routeEnd,
//            map: map,
//            title: 'Route end'
//        });

        var trafficLayer = new google.maps.TrafficLayer();
        trafficLayer.setMap(map);

        calculateAndDisplayRoute(
                    directionsDisplay, directionsService, routeStart, routeEnd, map);

        var mapStyle = [
            {
                featureType: "administrative",
                elementType: "labels",
                stylers: [
                    { visibility: "off" }
                ]
            },{
                featureType: "poi",
                elementType: "labels",
                stylers: [
                    { visibility: "off" }
                ]
            },{
                featureType: "water",
                elementType: "labels",
                stylers: [
                    { visibility: "off" }
                ]
            }
        ]
        map.set('styles', mapStyle);
		google.maps.event.addListenerOnce(map, 'idle', function(){
			// do something only the first time the map is loaded
			setTimeout(function () {
            console.log('mapReady----------------------------------------')
          }, 10)
		});

		function calculateAndDisplayRoute(directionsDisplay, directionsService,
                  routeStart, routeEnd, map) {

            // Retrieve the start and end locations and create a DirectionsRequest using
            // DRIVING directions.
            directionsService.route({
                origin: routeStart,
                destination: routeEnd,
                travelMode: 'DRIVING'
            }, function(response, status) {
				// Route the directions and pass the response to a function to create
				// markers for each step.
				if (status === 'OK') {
					directionsDisplay.setDirections(response);
				} else {
					window.alert('Directions request failed due to ' + status);
				}
			});
		}
	}
</script>
<script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCsYDVCEI0HyxmPmsSiIbBRVC033odkXGs&callback=initMap">
</script>
</body>
</html>
