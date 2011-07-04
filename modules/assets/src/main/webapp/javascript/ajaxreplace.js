/**
 *  @description Identify elements by giving them a unique ID for the current document
 *  @author Jp Siffert
 *  @email plugin hostedAt chezouam.fr
 *  @version 1.0
 *  @param object optional object of options, see $.fn.identify.defaults
 *  @return the jQuery object for chaining
 *
 *  @example $('SPAN').identify();
 */

/**
 * This code has been modified by Jahia from its original source.
 */
(function($) {

    $.fn.identify = function(options) {
        var opts = $.extend(true, {}, $.fn.identify.defaults, options);

        return this.each(function() {
            var $this = $(this);
            var o = ($.meta ? $.extend(true, {}, opts, $this.data) : opts);

            var id = $this.attr('id');

            if (id) {
                if (o.unique == false || $('[id=' + id + ']').length <= 1) {
                    return;
                }
            }

            do {
                id = o.prefix + o.separator + o.guid(o.guidSeparator);
            } while ($('#' + id).length > 0);

            $this.attr('id', id);
        });
    };

    /**
     *  @description Object of identify plugin options
     **/
    $.fn.identify.defaults = {
        prefix : 'id'  // prefix used for the generated id
        /**
         * @description inspired from the exelent article http://note19.com/2007/05/27/javascript-guid-generator/
         * @param string sep, the separator use to return de generated guid (default = "-")
         */
        ,guid : function(sep) {
            /**
             * @description Internal function that returns a hexadecimal number
             * @return an random hexa décimal value between 0000 and FFFF
             */
            function S4() {
                return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
            }

            if (typeof sep == "undefined") {
                sep = "-";
            }

            return (S4() + S4() + sep + S4() + sep + S4() + sep + S4() + sep + S4() + S4() + S4());
        }
        ,unique : false             // If an id is found, test if it's unique or assign a new one
        ,separator : '_'            // Separator used between prefix and guid
        ,guidSeparator : '-'        // Separator unsed between guid's elements'

    }
})(jQuery);


/**
 *  @description Allows you to reload specific Js or Css during developpement phase
 *  Ease you towards developpement and prevent you from hitting F5 on each change
 *  @author Jp Siffert
 *  @email plugin hostedAt chezouam.fr feel free to send comments
 *  @version 1.0
 *  @param object optional object of options, see $.fn.reload.defaults
 *  @return the jQuery object for chaining
 *  @require identify plugin (example : http://plugins.jquery.com/project/identifyII)
 *  @example $('LINK').reload({interval: 10000});
 *  $('script[src$=debug.js]').reload();
 */

(function($) {
    $.fn.reloadCSS = function (options, originalId) {
        var opts = $.extend(true, {}, $.fn.reloadCSS.defaults, options);
        var _this = this;
        var origId = originalId;

        function _reload(obj) {
            obj.each(function() {
                var $this = $(this);

                var id = $this.identify().attr('id');
                var href = $this.attr('href');
                var media = $this.attr('media');
                if ($this.attr('tagName') == 'LINK') {
                    if ($('head:first link[href="' + href + '"]').length == 0) {
                        $('head:first link:first').before('<link id="staticAssetcss' + $('link').length + '" href="' +
                                                          href + '" type="text/css" rel="stylesheet" media="' + media +
                                                          '"/>');
                    }
                    $('#' + origId + ' link[href="' + href + '"]').remove();
                }
            });
        }

        if (opts.interval == 0) {
            _reload(_this);
        } else {
            setInterval(function() {
                _reload(_this)
            }, opts.interval);
        }

        return _this;
    };

    $.fn.reloadCSS.defaults = {
        interval : 0              // Set the interval for automatique refresh
        ,preventCache: true       // Try to prevent cache
        ,cacheVoidArg: 'void'     // Void argument passed to try to avoid cache
    }
})(jQuery);


function replace(id, url, callback) {
    var http = false;
    if (navigator.appName == "Microsoft Internet Explorer") {
        http = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        http = new XMLHttpRequest();
    }
    http.open("GET", url, true);
    http.onreadystatechange = function() {
        var result;
        if (http.readyState == 4) {
            result = http.responseText;
            $("#" + id).html(result);
            eval(callback);
        }
    };
    http.send(null);
}

function jreplace(id, url, params, callback, replaceIdContent) {
    $.get(url, params, function(data) {
        if (replaceIdContent != 'undefined' && replaceIdContent) {
            $("#" + id).replaceWith(data);
        } else {
            $("#" + id).html(data);
        }
        var links = $("#" + id + " link");
        links.reloadCSS({preventCache:false}, id);
        if (typeof callback != 'undefined') {
            eval(callback);
        }
    });
}
