/**
 * jQuery Jahia Google Maps v3 plugin
 *
 * @url		http://www.jahia.org/
 * @author	Sergiy Shyrkov <sergiy.shyrkov@jahia.com>
 * @version	1.0.0
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 */
(function($) {
	var geo = null;
	
	$.fn.googleMaps = function(options) {
		var opts = $.extend({}, $.fn.googleMaps.defaults, options);
		var placeHolders = this;
		// do detect map center
		opts = _detectCenter(opts);
			
		if (opts.center) {
			// we have center coordinates
			_renderMaps(placeHolders, opts);
		} else {
			// we got address -> geocode
			_getGeocoder().geocode({'address': opts.address}, function(results, status) {
		        if (status == google.maps.GeocoderStatus.OK) {
		        	var loc = results[0].geometry.location;
		        	_renderMaps(placeHolders, $.extend(opts, {center: loc}));
		        } else {
		        	var errorMsg = "Geocoding was not successful for address '" + opts.address + "'  for the following reason: " + status;
		    		placeHolders.each(function() {
		    		    alert(errorMsg);
		    		    $(this).append('<p>' + errorMsg + '</p>')
		    		});
		        }
			});
		}
		return this;
	}

	$.fn.googleMaps.defaults = {
			mapTypeId:			google.maps.MapTypeId.ROADMAP,
			zoom:				16
	}
	
	// private methods go here
	
	function _detectCenter(opts) {
		if (opts.center || opts.address) {
			// we have it
			return opts;
		}
		
		// are center coordinates specified via options? 
		var point = opts.latitude && opts.longitude ? new google.maps.LatLng(opts.latitude, opts.longitude) : null;
		if (point) {
			opts = $.extend(opts, {center: point});
		} else {
			// no, check the first marker
			var firstMarker = opts.markers && opts.markers.length > 0 ? opts.markers[0] : null;
			if (firstMarker) {
				// we have a marker -> check its coordinates
				point = firstMarker.latitude && firstMarker.longitude ? new google.maps.LatLng(firstMarker.latitude, firstMarker.longitude) : null;
				if (point) {
					opts = $.extend(opts, {center: point});
				} else if (firstMarker.address) {
					// we have a marker address
					opts = $.extend(opts, {address: firstMarker.address});
				} else {
					// we have nothing -> use default
					opts = $.extend(opts, {center: new google.maps.LatLng(46.1893, 6.12831)});
				}
			}
		}
		
		return opts;
	}
	
	function _renderMaps(placeHolders, opts) {
		placeHolders.each(function() {
		    var myMap = new google.maps.Map(this, opts);
		    if (opts.markers && opts.markers.length > 0) {
		    	$(opts.markers).each(function () {
			    	_setMarker(this, myMap, opts);
		    	});
		    }
		});
	}
	
	function _setMarker(marker, myMap, opts) {
    	var latlng = marker.latitude && marker.longitude ? new google.maps.LatLng(marker.latitude, marker.longitude) : (marker.address && opts.address && opts.address == marker.address ? opts.center : null);
    	
    	var markerOpts = {map: myMap, icon: marker.icon, title: marker.title};
    	if (latlng != null) {
    		var myMarker = new google.maps.Marker($.extend(markerOpts, {position: latlng}));
    		if (marker.info) {
    			google.maps.event.addListener(myMarker, 'click', function() {
    				new google.maps.InfoWindow({content: marker.info}).open(myMap, myMarker);
    			});
    		}
    	} else if (marker.address) {
    		// do geocode the address
			_getGeocoder().geocode({'address': marker.address}, function(results, status) {
		        if (status == google.maps.GeocoderStatus.OK) {
		        	var loc = results[0].geometry.location;
		        	var myMarker = new google.maps.Marker($.extend(markerOpts, {position: loc}));   			    		
		    		if (marker.info) {
		    			google.maps.event.addListener(myMarker, 'click', function() {
		    				new google.maps.InfoWindow({content: marker.info}).open(myMap, myMarker);
		    			});
		    		}
		        } else {
		        	alert("Geocoding was not successful for address '" + marker.address + "'  for the following reason: " + status);
		        }
			});
    	}
	}
	
	function _getGeocoder() {
		if (geo == null) {
			geo = new google.maps.Geocoder();
		}
		
		return geo;
	}
	
})(jQuery);