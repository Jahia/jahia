/**
 *  @description Override jQuery load method, remove the script that were previously loaded
 */


(function($) {
    var _load1 = jQuery.fn.load;
    $.fn.load = function( url, params, callback ) {
		if ( typeof url !== "string" && _load1 ) {
			return _load1.apply( this, arguments );

		// Don't do a request if no elements are being requested
		} else if ( !this.length ) {
			return this;
		}

		var off = url.indexOf( " " );
		if ( off >= 0 ) {
			var selector = url.slice( off, url.length );
			url = url.slice( 0, off );
		}

		// Default to a GET request
		var type = "GET";

		// If the second parameter was provided
		if ( params ) {
			// If it's a function
			if ( $.isFunction( params ) ) {
				// We assume that it's the callback
				callback = params;
				params = undefined;

			// Otherwise, build a param string
			} else if ( typeof params === "object" ) {
				params = $.param( params, $.ajaxSettings.traditional );
				type = "POST";
			}
		}

		var self = this;

        rscript = /<script\b([^<]*(?:(?!<\/script>)<[^<]*)*)<\/script>/gi;
        extscript = /<script\b([^<>]*(?:(?!><\/script>)<[^<>]*)*)><\/script>/gi

		// Request the remote document
		$.ajax({
			url: url,
			type: type,
			dataType: "html",
			data: params,
			// Complete callback (responseText is used internally)
			complete: function( jqXHR, status, responseText ) {
				// Store the response as specified by the jqXHR object
				responseText = jqXHR.responseText;
				// If successful, inject the HTML into all the matched elements
				if ( jqXHR.isResolved() ) {
					// #4825: Get the actual response in case
					// a dataFilter is present in ajaxSettings
					jqXHR.done(function( r ) {
						responseText = r;
					});

                    // Get the list of all scripts that have returned by the ajax call 
					ar=[];
                    while ((match = extscript.exec(responseText)) != null) {
                        src = /src=\"([^\"]*)\"/.exec(match[1])[1]
						ar[src]=match[0];
                    }
					for (src in ar)
					{
                        // If the script was already present in the head, do not add it twice
						if ($('head:first script[src="' + src + '"]').length > 0 || (typeof jAggregatedStaticAssetsJavascript != 'undefined' && $.inArray(src, jAggregatedStaticAssetsJavascript) != -1)) {
                            responseText = responseText.replace(ar[src],"")
                        }
                    }

					// See if a selector was specified
                    self.html( selector ?
						// Create a dummy div to hold the results

						$("<div>")
							// inject the contents of the document in, removing the scripts
							// to avoid any 'Permission Denied' errors in IE
							.append(responseText.replace(rscript, ""))

							// Locate the specified elements
							.find(selector) :

						// If not, just inject the full result
						responseText );
				}

				if ( callback ) {
					self.each( callback, [ responseText, status, jqXHR ] );
				}
			}
		});

		return this;
	}
})(jQuery);
