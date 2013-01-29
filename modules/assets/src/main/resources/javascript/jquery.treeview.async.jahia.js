/*
 * Based on the work, done by J�rn Zaefferer in: Async Treeview 0.1
 *   
 * Async Treeview 0.1 - Lazy-loading extension for Treeview
 * 
 * http://bassistance.de/jquery-plugins/jquery-plugin-treeview/
 *
 * Copyright (c) 2007 J�rn Zaefferer
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *   
 */
;
(function($) {

    function load(url, callback, child, container,preview,previewPath) {
        $.getJSON(url, function(response) {
            function createNode(parent) {
                // fix display
                if (child.attr("style") == "display: none; ") {
                    child.attr("style", "display: block;");
                }
                var path = this.path || "";
                var current = $("<li/>").attr("id", "id-" + this.id).attr("rel",
                        path).html("<span class='treepreview' src='"+previewPath + path + "'>" + this.text +
                        "</span>").appendTo(parent);
                if (this.classes) {
                    current.children("span").addClass(this.classes);
                    if (this.classes.indexOf("selectable") != -1) {
                        var trigger = $("<input/>").attr("type", "radio").attr("name", "treeviewSelectedItem");
                        if (typeof(callback) == 'function') {
                            var $this = this;
                            trigger.click(function () {
                                callback($this.id, $this.path, $this.text);
                            });
                        }
                        trigger.prependTo(current.children("span"));
                    }
                }
                if (this.expanded) {
                    current.addClass("open");
                }
                if (this.hasChildren || this.children && this.children.length) {
                    var branch = $("<ul/>").appendTo(current);
                    if (this.hasChildren) {
                        current.addClass("hasChildren");
                        createNode.call({
                            text:"placeholder",
                            id:"placeholder",
                            children:[]
                        }, branch);
                    }
                    if (this.children && this.children.length) {
                        $.each(this.children, createNode, [branch]);
                    }
                }
            }

            $.each(response, createNode, [child]);
            $(container).treeview({add: child});
            if(preview)
                imagePreview();
        });
    }

    var jQueryTreeViewProxied = $.fn.treeview;
    $.fn.treeview = function(settings) {
        if (!settings.urlBase) {
            return jQueryTreeViewProxied.apply(this, arguments);
        }
        var container = this;
        load(settings.urlStartWith, settings.callback, this, container,settings.preview,settings.previewPath);
        var userToggle = settings.toggle;
        return jQueryTreeViewProxied.call(this, $.extend({}, settings, {
                    collapsed: true,
                    toggle: function() {
                        var $this = $(this);
                        if ($this.hasClass("hasChildren")) {
                            var childList = $this.removeClass("hasChildren").find("ul");
                            childList.empty();
                            load(settings.urlBase + this.getAttribute('rel') + settings.urlExtension, settings.callback,
                                    childList, container,settings.preview,settings.previewPath);
                        }
                        if (userToggle) {
                            userToggle.apply(this, arguments);
                        }
                    }
                }));
    };
    // starting the script on page load
    function imagePreview() {
        var xOffset = 10;
        var yOffset = 30;

        // these 2 variable determine popup's distance from the cursor
        // you might want to adjust to get the right result

        /* END CONFIG */
        $(".treepreview.selectable").hover(function(e) {
                    var extension = $(this).attr("src").substr( ($(this).attr("src").lastIndexOf('.') +1) );
                    if (extension == "png" || extension == "jpg" || extension == "gif") {
                        this.t = this.title;
                        this.title = "";
                        var c = (this.t != "") ? "<br/>" + this.t : "";
                        $("body").append("<p id='treepreview'><img src='" + $(this).attr("src") + "' alt='Image preview' />" + c + "</p>");
                        $("#treepreview").css("top", (e.pageY - xOffset) + "px").css("left", (e.pageX + yOffset) + "px").fadeIn("fast");
                    }
                }, function() {
                    this.title = this.t;
                    $("#treepreview").remove();
                });
        $(".treepreview.selectable").mousemove(function(e) {
            $("#treepreview").css("top", (e.pageY - xOffset) + "px").css("left", (e.pageX + yOffset) + "px");
        });
    }
})(jQuery);

