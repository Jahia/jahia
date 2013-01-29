/*!
 * jquery.defer.js 1.1
 *
 * Copyright (c) 2010 Adaptavist.com Ltd
 * Dual licensed under the MIT and GPL licenses.
 *
 * http://www.adaptavist.com/display/jQuery/Defer
 */
/* Creates callback proxy that defers the real callback for a given delay, any
 * further innvocation of the proxy during that time will reset the delay.
 *
 * Example; to trigger a 'change' event on all text inputs when the user
 * pauses typing:
 *
 *	function handler() {					// An event handler function
 *		$(this).trigger('change');
 *	}
 *	var deferred = $.defer( 333, handler ); // Defer the handler for a third of a second
 *	$(':text').keyup( deferred );			// Bind the event
 *
 * Or, in one line:
 *	$(':text').keyup( $.defer(333, function() { $(this).trigger('change'); }) );
 *
 * The proxy knows nothing and cares not about the arguments passed to it, or
 * the 'this' object, they are all merely passed straight through to the real
 * callback function. So whilst it's greatest use is for event handling, it
 * could be used in any callback situation.
 *
 * Version 1.1 adds an optional 'timerDataName' parameter, which allows you to share
 * the timer between multiple defers. The timer id is stored in a jQuery data item
 * (named by timerDataName) of the 'this' object. This is particularly useful for
 * hover events:
 *
 *  $('li')
 *    .css('opacity', 0.5)
 *    .hover(
 *      $.defer( 500, 'hoverTimer', function() { $(this).animate({opacity: 1}); } ),
 *      $.defer( 100, 'hoverTimer', function() { $(this).animate({opacity: 0.5}); } )
 *    );
 *
 * Author: Mark Gibson (jollytoad at gmail dot com)
 */
/*global jQuery, clearTimeout, setTimeout */
(function($) {

$.defer = function(delay, timerDataName, callback) {
	var timer;

	if ( !callback ) {
	    callback = timerDataName;
	    timerDataName = undefined;
	}

	// Return the callback proxy
	return function() {
		// Save the vars for the real callback
		var that = this, args = arguments;

		// Reset the delay
		clearTimeout(timerDataName ? $.data(this, timerDataName) : timer);

		// Delay the real callback
		timer = setTimeout(function() {
			callback.apply(that, args);
		}, delay);

		if ( timerDataName ) {
		    $.data(this, timerDataName, timer);
		}
	};
};

})(jQuery);
