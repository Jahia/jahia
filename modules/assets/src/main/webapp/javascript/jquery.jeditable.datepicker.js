/*
 DatePicker plugin for Jeditable(http://www.appelsiini.net/projects/jeditable),
 using datepicker plugin for jquery (http://www.eyecon.ro/datepicker/)
 Copyright (C) 2009 Enjalbert Vincent (WinWinWeb)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 # The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 vincent.enjalbert at gmail dot com (French, English)

 */


$.editable.addInputType('datetimepicker', {
    /* create input element */
    element : function(settings, original) {
        var input = $('<input id="datetimepicker_" readonly="true"/>');
        $(this).append(input);
        return(input);
    },


    submit: function (settings, original) {
        $("#datetimepicker_", this).val(jQuery.trim($("#datetimepicker_", this).val()).replace(" ", "T"));
    },


    content : function(string, settings, original) {
        var now;
        if(settings.loaddata.defaultValue) {
            now = settings.loaddata.defaultValue.indexOf('T') != -1 ? new Date(Date.parse(settings.loaddata.defaultValue)) : new Date(Number(settings.loaddata.defaultValue));
        } else {
            now = new Date();
        }
        $("#datetimepicker_", this).val(now.getFullYear() + '-' + (now.getMonth() + 1) + '-' + now.getDate() + ' ' + now.getHours() + ':' + now.getMinutes() + ':00');
    },


    /* attach 3rd party plugin to input element */
    plugin : function(settings, original) {
        var form = this;
        settings.onblur = null;
        $("#datetimepicker_", this).datetime({dateFormat: $.datepicker.ISO_8601, showButtonPanel: true, showOn:'focus'});
    }
});

$.editable.addInputType('datepicker', {
    /* create input element */
    element : function(settings, original) {
        var input = $('<input id="datepicker_" readonly="true"/>');

        $(this).append(input);
        return(input);
    },


    submit: function (settings, original) {
        $("#datepicker_", this).val(jQuery.trim($("#datepicker_", this).val()).replace(" ", "T"));
    },


    content : function(string, settings, original) {
        var now;
        if(settings.loaddata.defaultValue) {
            now = new Date(Date.parse(settings.loaddata.defaultValue));
        } else {
            now = new Date();
        }
        $("#datepicker_", this).val(now.getFullYear() + '-' + (now.getMonth() + 1) + '-' + now.getDate());
    },


    /* attach 3rd party plugin to input element */
    plugin : function(settings, original) {
        var form = this;
        settings.onblur = null;
        $("#datepicker_", this).datepicker({changeMonth: true,
			changeYear: true,dateFormat: $.datepicker.ISO_8601,yearRange: '1900:2050'});
    }
});
