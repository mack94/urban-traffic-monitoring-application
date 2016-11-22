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
        .labels {
             color: white;
             background-color: #73B9FF;
             font-family:"Lucida Grande", "Arial", sans-serif;
             font-size: 12px;
             text-align: center;
             width: 30px;
             white-space: nowrap;
         }
    </style>
</head>
<body>
<div id="map"></div>
<script src="jquery-3.1.0.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?v=3&key=AIzaSyCsYDVCEI0HyxmPmsSiIbBRVC033odkXGs"></script>
<script src="https://cdn.rawgit.com/googlemaps/v3-utility-library/master/markerwithlabel/src/markerwithlabel.js"></script>
<script src="epoly.js" type="text/javascript"> </script>
<script>
	var polyline = null;
	var totalDist = 0;
	var defaultTimeout = 100;
    var timeout = defaultTimeout;
    var timeoutStep = 50;
    function initMap() {

        var directionsService = new google.maps.DirectionsService;
        var map = new google.maps.Map(document.getElementById('map'), {
            zoom: 15,
            center: {lat: 50.07, lng: 19.94},
			minZoom: 12,
            streetViewControl: false
        });

        var trafficLayer = new google.maps.TrafficLayer();
        var trafficControlDiv = document.createElement('div');
        var trafficControl = new trafficControl(trafficControlDiv, map);
        trafficControlDiv.index = 1;
        map.controls[google.maps.ControlPosition.TOP_RIGHT].push(trafficControlDiv)

		$routesInstructions$

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

		var allowedBounds = new google.maps.LatLngBounds(
             new google.maps.LatLng(49.9667, 19.7170),
             new google.maps.LatLng(50.1404, 20.1989)
        );
        var lastValidCenter = map.getCenter();

        google.maps.event.addListener(map, 'center_changed', function() {
            if (allowedBounds.contains(map.getCenter())) {
                // still within valid bounds, so save the last valid position
                lastValidCenter = map.getCenter();
                return;
            }

            // not valid anymore => return to last valid position
            map.panTo(lastValidCenter);
        });
		function pinSymbol(color) {
			return {
				path: 'M 0,0 C -2,-20 -10,-22 -10,-30 A 10,10 0 1,1 10,-30 C 10,-22 2,-20 0,0 z',
				fillColor: color,
				fillOpacity: 1,
				strokeColor: '#000',
				strokeWeight: 2,
				scale: 2
			};
		};
        function trafficControl(controlDiv, map) {
              // Set CSS for the control border.
              var controlUI = document.createElement('div');
              controlUI.style.backgroundColor = '#fff';
              controlUI.style.border = '2px solid #fff';
              controlUI.style.borderRadius = '3px';
              controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
              controlUI.style.cursor = 'pointer';
              controlUI.style.marginBottom = '22px';
              controlUI.style.marginTop = '3px';
              controlUI.style.marginRight = '3px';
              controlUI.style.textAlign = 'center';
              controlUI.title = 'Click to enable/disable the traffic layer';
              controlDiv.appendChild(controlUI);

              // Set CSS for the control interior.
              var controlText = document.createElement('div');
              controlText.style.color = 'rgb(25,25,25)';
              controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
              controlText.style.fontSize = '16px';
              controlText.style.lineHeight = '38px';
              controlText.style.paddingLeft = '5px';
              controlText.style.paddingRight = '5px';
              controlText.innerHTML = 'Enable traffic layer';
              controlUI.appendChild(controlText);

              // Setup the click event listener
              controlUI.addEventListener('click', function() {
                if( controlText.innerHTML == 'Disable traffic layer' ) {
                    trafficLayer.setMap(null);
                    controlText.innerHTML = 'Enable traffic layer';
                }
                else if( controlText.innerHTML == 'Enable traffic layer' ) {
                    trafficLayer.setMap(map);
                    controlText.innerHTML = 'Disable traffic layer';
                }
              });

		}
		function calculateAndDisplayRoute(directionsService,
                  routeStart, routeEnd, routeId, color, map) {
            // Retrieve the start and end locations and create a DirectionsRequest using
            // DRIVING directions.
            directionsService.route({
                origin: routeStart,
                destination: routeEnd,
                travelMode: 'DRIVING'
            }, function(response, status) {
				if (status === 'OK') {
				    timeout = defaultTimeout;
				    var directionsDisplay = new google.maps.DirectionsRenderer({
				        map: map,
				        suppressMarkers: true,
				        preserveViewport: true,
				        polylineOptions: { strokeColor: color, strokeWeight: 8.0 }
				    });


					directionsDisplay.setDirections(response);
					var polyline = new google.maps.Polyline({
					  path: [],
					  strokeColor: '#FF0000',
					  strokeWeight: 3
					});
					//var bounds = new google.maps.LatLngBounds();


					var legs = response.routes[0].legs;
					for (i=0;i<legs.length;i++) {
					  var steps = legs[i].steps;
					  for (j=0;j<steps.length;j++) {
						var nextSegment = steps[j].path;
						for (k=0;k<nextSegment.length;k++) {
						  polyline.getPath().push(nextSegment[k]);
						  //bounds.extend(nextSegment[k]);
						}
					  }
					}
					computeTotalDistance(response);
					function computeTotalDistance(result) {
					  totalDist = 0;
					  var myroute = result.routes[0];
					  for (i = 0; i < myroute.legs.length; i++) {
						totalDist += myroute.legs[i].distance.value;
					  }
					  putMarkerOnRoute(50);

					  totalDist = totalDist / 1000.
					  }

					  function putMarkerOnRoute(percentage) {
						var distance = (percentage/100) * totalDist;
						new MarkerWithLabel({
							position: polyline.GetPointAtDistance(distance),
							map: map,
							draggable: false,
							labelContent: routeId,
							labelAnchor: new google.maps.Point(15, 65),
							labelClass: "labels", // the CSS class for the label
							labelStyle: {backgroundColor: color},
							labelInBackground: false,
							icon: pinSymbol(color)
						});
					  }
				}else if( status == google.maps.DirectionsStatus.OVER_QUERY_LIMIT ) {
                    setTimeout( function() {
                 	    calculateAndDisplayRoute(directionsService, routeStart, routeEnd, routeId, color, map);
                 	}, timeout+=timeoutStep);
                 }
				else {
				    window.alert('Directions request failed due to ' + status);
				}
			});
		}
	}
	google.maps.event.addDomListener(window, 'load', initMap);


    //epoly.js code, webEngine doesnt seem to load it indirectly
	// === first support methods that don't (yet) exist in v3
    google.maps.LatLng.prototype.distanceFrom = function(newLatLng) {
      var EarthRadiusMeters = 6378137.0; // meters
      var lat1 = this.lat();
      var lon1 = this.lng();
      var lat2 = newLatLng.lat();
      var lon2 = newLatLng.lng();
      var dLat = (lat2-lat1) * Math.PI / 180;
      var dLon = (lon2-lon1) * Math.PI / 180;
      var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
          Math.cos(lat1 * Math.PI / 180 ) * Math.cos(lat2 * Math.PI / 180 ) *
          Math.sin(dLon/2) * Math.sin(dLon/2);
      var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      var d = EarthRadiusMeters * c;
      return d;
    }

    google.maps.LatLng.prototype.latRadians = function() {
      return this.lat() * Math.PI/180;
    }

    google.maps.LatLng.prototype.lngRadians = function() {
      return this.lng() * Math.PI/180;
    }

    // === A method for testing if a point is inside a polygon
    // === Returns true if poly contains point
    // === Algorithm shamelessly stolen from http://alienryderflex.com/polygon/
    google.maps.Polygon.prototype.Contains = function(point) {
      var j=0;
      var oddNodes = false;
      var x = point.lng();
      var y = point.lat();
      for (var i=0; i < this.getPath().getLength(); i++) {
        j++;
        if (j == this.getPath().getLength()) {j = 0;}
        if (((this.getPath().getAt(i).lat() < y) && (this.getPath().getAt(j).lat() >= y))
            || ((this.getPath().getAt(j).lat() < y) && (this.getPath().getAt(i).lat() >= y))) {
          if ( this.getPath().getAt(i).lng() + (y - this.getPath().getAt(i).lat())
              /  (this.getPath().getAt(j).lat()-this.getPath().getAt(i).lat())
              *  (this.getPath().getAt(j).lng() - this.getPath().getAt(i).lng())<x ) {
            oddNodes = !oddNodes
          }
        }
      }
      return oddNodes;
    }

    // === A method which returns the approximate area of a non-intersecting polygon in square metres ===
    // === It doesn't fully account for spherical geometry, so will be inaccurate for large polygons ===
    // === The polygon must not intersect itself ===
    google.maps.Polygon.prototype.Area = function() {
      var a = 0;
      var j = 0;
      var b = this.Bounds();
      var x0 = b.getSouthWest().lng();
      var y0 = b.getSouthWest().lat();
      for (var i=0; i < this.getPath().getLength(); i++) {
        j++;
        if (j == this.getPath().getLength()) {j = 0;}
        var x1 = this.getPath().getAt(i).distanceFrom(new google.maps.LatLng(this.getPath().getAt(i).lat(),x0));
        var x2 = this.getPath().getAt(j).distanceFrom(new google.maps.LatLng(this.getPath().getAt(j).lat(),x0));
        var y1 = this.getPath().getAt(i).distanceFrom(new google.maps.LatLng(y0,this.getPath().getAt(i).lng()));
        var y2 = this.getPath().getAt(j).distanceFrom(new google.maps.LatLng(y0,this.getPath().getAt(j).lng()));
        a += x1*y2 - x2*y1;
      }
      return Math.abs(a * 0.5);
    }

    // === A method which returns the length of a path in metres ===
    google.maps.Polygon.prototype.Distance = function() {
      var dist = 0;
      for (var i=1; i < this.getPath().getLength(); i++) {
        dist += this.getPath().getAt(i).distanceFrom(this.getPath().getAt(i-1));
      }
      return dist;
    }

    // === A method which returns the bounds as a GLatLngBounds ===
    google.maps.Polygon.prototype.Bounds = function() {
      var bounds = new google.maps.LatLngBounds();
      for (var i=0; i < this.getPath().getLength(); i++) {
        bounds.extend(this.getPath().getAt(i));
      }
      return bounds;
    }

    // === A method which returns a GLatLng of a point a given distance along the path ===
    // === Returns null if the path is shorter than the specified distance ===
    google.maps.Polygon.prototype.GetPointAtDistance = function(metres) {
      // some awkward special cases
      if (metres == 0) return this.getPath().getAt(0);
      if (metres < 0) return null;
      if (this.getPath().getLength() < 2) return null;
      var dist=0;
      var olddist=0;
      for (var i=1; (i < this.getPath().getLength() && dist < metres); i++) {
        olddist = dist;
        dist += this.getPath().getAt(i).distanceFrom(this.getPath().getAt(i-1));
      }
      if (dist < metres) {
        return null;
      }
      var p1= this.getPath().getAt(i-2);
      var p2= this.getPath().getAt(i-1);
      var m = (metres-olddist)/(dist-olddist);
      return new google.maps.LatLng( p1.lat() + (p2.lat()-p1.lat())*m, p1.lng() + (p2.lng()-p1.lng())*m);
    }

    // === A method which returns an array of GLatLngs of points a given interval along the path ===
    google.maps.Polygon.prototype.GetPointsAtDistance = function(metres) {
      var next = metres;
      var points = [];
      // some awkward special cases
      if (metres <= 0) return points;
      var dist=0;
      var olddist=0;
      for (var i=1; (i < this.getPath().getLength()); i++) {
        olddist = dist;
        dist += this.getPath().getAt(i).distanceFrom(this.getPath().getAt(i-1));
        while (dist > next) {
          var p1= this.getPath().getAt(i-1);
          var p2= this.getPath().getAt(i);
          var m = (next-olddist)/(dist-olddist);
          points.push(new google.maps.LatLng( p1.lat() + (p2.lat()-p1.lat())*m, p1.lng() + (p2.lng()-p1.lng())*m));
          next += metres;
        }
      }
      return points;
    }

    // === A method which returns the Vertex number at a given distance along the path ===
    // === Returns null if the path is shorter than the specified distance ===
    google.maps.Polygon.prototype.GetIndexAtDistance = function(metres) {
      // some awkward special cases
      if (metres == 0) return this.getPath().getAt(0);
      if (metres < 0) return null;
      var dist=0;
      var olddist=0;
      for (var i=1; (i < this.getPath().getLength() && dist < metres); i++) {
        olddist = dist;
        dist += this.getPath().getAt(i).distanceFrom(this.getPath().getAt(i-1));
      }
      if (dist < metres) {return null;}
      return i;
    }

    // === A function which returns the bearing between two vertices in decgrees from 0 to 360===
    // === If v1 is null, it returns the bearing between the first and last vertex ===
    // === If v1 is present but v2 is null, returns the bearing from v1 to the next vertex ===
    // === If either vertex is out of range, returns void ===
    google.maps.Polygon.prototype.Bearing = function(v1,v2) {
      if (v1 == null) {
        v1 = 0;
        v2 = this.getPath().getLength()-1;
      } else if (v2 ==  null) {
        v2 = v1+1;
      }
      if ((v1 < 0) || (v1 >= this.getPath().getLength()) || (v2 < 0) || (v2 >= this.getPath().getLength())) {
        return;
      }
      var from = this.getPath().getAt(v1);
      var to = this.getPath().getAt(v2);
      if (from.equals(to)) {
        return 0;
      }
      var lat1 = from.latRadians();
      var lon1 = from.lngRadians();
      var lat2 = to.latRadians();
      var lon2 = to.lngRadians();
      var angle = - Math.atan2( Math.sin( lon1 - lon2 ) * Math.cos( lat2 ), Math.cos( lat1 ) * Math.sin( lat2 ) - Math.sin( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) );
      if ( angle < 0.0 ) angle  += Math.PI * 2.0;
      angle = angle * 180.0 / Math.PI;
      return parseFloat(angle.toFixed(1));
    }




    // === Copy all the above functions to GPolyline ===
    google.maps.Polyline.prototype.Contains             = google.maps.Polygon.prototype.Contains;
    google.maps.Polyline.prototype.Area                 = google.maps.Polygon.prototype.Area;
    google.maps.Polyline.prototype.Distance             = google.maps.Polygon.prototype.Distance;
    google.maps.Polyline.prototype.Bounds               = google.maps.Polygon.prototype.Bounds;
    google.maps.Polyline.prototype.GetPointAtDistance   = google.maps.Polygon.prototype.GetPointAtDistance;
    google.maps.Polyline.prototype.GetPointsAtDistance  = google.maps.Polygon.prototype.GetPointsAtDistance;
    google.maps.Polyline.prototype.GetIndexAtDistance   = google.maps.Polygon.prototype.GetIndexAtDistance;
    google.maps.Polyline.prototype.Bearing              = google.maps.Polygon.prototype.Bearing;
</script>
</body>
</html>