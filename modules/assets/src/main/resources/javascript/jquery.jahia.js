/**
 *  @description Override jQuery load method, remove the script that were previously loaded
 */


(function($) {
    var _load1 = jQuery.fn.load;
    jQuery.fn.load = function( url, params, callback ) {
        if ( typeof url !== "string" && _load1 ) {
            return _load1.apply( this, arguments );
        }

        var selector, response, type,
            self = this,
            off = url.indexOf(" ");

        if ( off >= 0 ) {
            selector = url.slice( off, url.length );
            url = url.slice( 0, off );
        }

        // If it's a function
        if ( jQuery.isFunction( params ) ) {

            // We assume that it's the callback
            callback = params;
            params = undefined;

            // Otherwise, build a param string
        } else if ( params && typeof params === "object" ) {
            type = "POST";
        }
        rscript = /<script\b([^<]*(?:(?!<\/script>)<[^<]*)*)<\/script>/gi;
        extscript = /<script\b([^<>]*(?:(?!><\/script>)<[^<>]*)*)><\/script>/gi

        // If we have elements to modify, make the request
        if ( self.length > 0 ) {
            jQuery.ajax({
                url: url,

                // if "type" variable is undefined, then "GET" method will be used
                type: type,
                dataType: "html",
                data: params
            }).done(function( responseText ) {

                    // Save response for use in complete callback
                    response = arguments;
                    // Get the list of all scripts that have returned by the ajax call
                    ar=[];
                    while ((match = extscript.exec(responseText)) != null) {
                        src = /src=\"([^\"]*)\"/.exec(match[1])[1]
                        ar[src]=match[0];
                    }
                    for (src in ar)
                    {
                        // If the script was already present in the head, do not add it twice
                        if ($('head:first script[src="' + src + '"]').length > 0 || $('aggregatedscript').filter('[src="' + src + '"]').length > 0) {
                            responseText = responseText.replace(ar[src],"")
                        }
                    }
                    self.html( selector ?

                        // If a selector was specified, locate the right elements in a dummy div
                        // Exclude scripts to avoid IE 'Permission Denied' errors
                        jQuery("<div>").append( jQuery.parseHTML( responseText ) ).find( selector ) :

                        // Otherwise use the full result
                        responseText );


                }).complete( callback && function( jqXHR, status ) {

                    self.each( callback, response || [ jqXHR.responseText, status, jqXHR ] );

                });
        }

        return this;
    };
})(jQuery);

var matched, browser;

// Use of jQuery.browser is frowned upon.
// More details: http://api.jquery.com/jQuery.browser
// jQuery.uaMatch maintained for back-compat
jQuery.uaMatch = function( ua ) {
    ua = ua.toLowerCase();

    var match = /(chrome)[ \/]([\w.]+)/.exec( ua ) ||
        /(webkit)[ \/]([\w.]+)/.exec( ua ) ||
        /(opera)(?:.*version|)[ \/]([\w.]+)/.exec( ua ) ||
        /(msie) ([\w.]+)/.exec( ua ) ||
        ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec( ua ) ||
        [];

    return {
        browser: match[ 1 ] || "",
        version: match[ 2 ] || "0"
    };
};

matched = jQuery.uaMatch( navigator.userAgent );
browser = {};

if ( matched.browser ) {
    browser[ matched.browser ] = true;
    browser.version = matched.version;
}

// Chrome is Webkit, but Webkit is also Safari.
if ( browser.chrome ) {
    browser.webkit = true;
} else if ( browser.webkit ) {
    browser.safari = true;
}

jQuery.browser = browser;
