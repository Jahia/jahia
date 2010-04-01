/*

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

*/
/*
 * Based on the work, done by Jörn Zaefferer in: Async Treeview 0.1
 *   
 * Async Treeview 0.1 - Lazy-loading extension for Treeview
 * 
 * http://bassistance.de/jquery-plugins/jquery-plugin-treeview/
 *
 * Copyright (c) 2007 Jörn Zaefferer
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *   
 */
;(function($) {

function load(url, callback, child, container) {
	$.getJSON(url, function(response) {
		function createNode(parent) {
			var path = this.path || "";
			var current = $("<li/>").attr("id", "id-" + this.id).attr("rel", path).html("<span>" + this.text + "</span>").appendTo(parent);
			if (this.classes) {
				current.children("span").addClass(this.classes);
				if (this.classes.indexOf("selectable") != -1) {
					var trigger = $("<input/>").attr("type", "radio").attr("name", "treeviewSelectedItem");
					if (typeof(callback) == 'function') {
						var $this = this;
						trigger.click(function () { callback($this.id, $this.path, $this.text); });
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
    });
}

var jQueryTreeViewProxied = $.fn.treeview;
$.fn.treeview = function(settings) {
	if (!settings.urlBase) {
		return jQueryTreeViewProxied.apply(this, arguments);
	}
	var container = this;
	load(settings.urlStartWith, settings.callback, this, container);
	var userToggle = settings.toggle;
	return jQueryTreeViewProxied.call(this, $.extend({}, settings, {
		collapsed: true,
		toggle: function() {
			var $this = $(this);
			if ($this.hasClass("hasChildren")) {
				var childList = $this.removeClass("hasChildren").find("ul");
				childList.empty();
				load(settings.urlBase + this.getAttribute('rel') + settings.urlExtension, settings.callback, childList, container);
			}
			if (userToggle) {
				userToggle.apply(this, arguments);
			}
		}
	}));
};

})(jQuery);