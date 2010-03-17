<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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

--%>
<%@ tag body-content="scriptless"
        description="Renders a date picker control using corresponding jQuery UI component (http://docs.jquery.com/UI/Datepicker). Datepicker options (see http://docs.jquery.com/UI/Datepicker#options) can be specified as a body of this tag in form {option1: value1, option2: value2 ...}. The template module that will use this tag should have Default Jahia Templates module as a dependency." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field ID to bind the date picker to." %>
<%@ attribute name="theme" required="false" type="java.lang.String"
              description="The name of the CSS file with corresponding jQuery theme. [jquery-ui.smoothness.css]" %>
<%@ attribute name="time" required="false" type="java.lang.Boolean"
              description="True if you want the time selector" %>
<%@ attribute name="hourFieldId" required="true" type="java.lang.String"
              description="The input field ID to bind the hour slider to." %>
<%@ attribute name="minFieldId" required="true" type="java.lang.String"
              description="The input field ID to bind the minute slider to." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty requestScope['org.jahia.tags.dateSelector.resources']}">
    <template:addResources type="css" resources="${not empty theme ? theme : 'jquery-ui.smoothness.css'}"/>
    <template:addResources type="javascript"
                           resources="jquery.min.js,jquery-ui.core.min.js,jquery-ui.datepicker.min.js,jquery-ui.slider.min.js"/>
    <c:set var="locale" value="${renderContext.mainResource.locale}"/>
    <c:if test="${locale != 'en_US'}">
        <template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${locale.language}.js"/>
        <c:if test="${not empty locale.country}">
            <template:addResources type="javascript"
                                   resources="i18n/jquery.ui.datepicker-${locale.language}-${locale.country}.js"/>
        </c:if>
    </c:if>
    <c:set var="org.jahia.tags.dateSelector.resources" value="true" scope="request"/>
</c:if>
<jsp:doBody var="options"/>
<c:if test="${empty options}">
    <c:set var="options" value="{dateFormat: 'dd.mm.yy', showButtonPanel: true, showOn: 'both'}"/>
</c:if>
<script type="text/javascript">
    /* <![CDATA[ */
    $(document).ready(function() {
        $('#${fieldId}').datepicker(${options});
    });
    <c:if test="${not empty time and time eq true}">
    $(document).ready(function() {

        //if the html is not yet created in the document, then do it now
        if (!$('#slider1${hourFieldId}').length) {
            $("body").append('<div id="hourctnr${hourFieldId}" style="display:none;"><div id="slider1${hourFieldId}" style="height:120px; margin:10px;"></div></div>');
        }

        if (!$('#slider2${hourFieldId}').length) {
            $("body").append('<div id="minctnr${minFieldId}" style="display:none;"><div id="slider2${minFieldId}" style="height:120px; margin:10px;"></div></div>');
        }

        var options;

        var variables = {
            clock:{
                type:24
            },
            get:{
                range: {
                    hours: function() {
                        return new Array[2](1, variables.clock.type === 12 ? 12 : 24);
                    },
                    minutes: function() {
                        return new Array[2](0, 59);
                    }
                }
            },
            options: null //hold extended options here
        };

        // Slider
        $('#slider1${hourFieldId}').slider({
            orientation: "vertical",
            value: $('#${hourFieldId}').val() == "" ? 4 : parseInt($('#${hourFieldId}').val()),
            min: 0,
            max: 23,
            step: 1,
            slide: function(event, ui) {
                $('#${hourFieldId}').val(parseInt(ui.value));
            }
        });
        // Slider
        $('#slider2${minFieldId}').slider({
            orientation: "vertical",
            value: $('#${minFieldId}').val() == "" ? 00 : parseInt($('#${minFieldId}').val()),
            min: 00,
            max: 59,
            step: 1,
            slide: function(event, ui) {
                $('#${minFieldId}').val((ui.value == 0) ? '00' : parseInt(ui.value));
            }
        });

        //Inline editor bind
        $('#${hourFieldId}').keyup(function(e) {
            if ((e.which <= 57 && e.which >= 48) && ($(this).val() >= 1 && $(this).val() <= 12 )) {
                //console.log("Which: "+e.which);
                $('#slider1${hourFieldId}').slider('value', parseInt($(this).val()));
                //console.log("Val: "+parseInt($(this).val()))
            } else {
                $(this).val($(this).val().slice(0, -1));
            }
        });
        //Inline editor bind
        $('#${minFieldId}').keyup(function(e) {
            if ((e.which <= 57 && e.which >= 48) && ($(this).val() >= 0 && $(this).val() <= 59 )) {
                //console.log("Which: "+e.which);
                $('#slider2${minFieldId}').slider('value', parseInt($(this).val()));
                //console.log("Val: "+parseInt($(this).val()))
            } else {
                $(this).val($(this).val().slice(0, -1));
            }
        });


        $("#${hourFieldId}").focus(function() {
            var ele = $("#${hourFieldId}");
            $(".isPtTimeSelectActive").removeClass("isPtTimeSelectActive");
            var cntr = $("#hourctnr${hourFieldId}");
            var i = $(ele).eq(0).addClass("isPtTimeSelectActive");
            var style = i.offset();
            style['z-index'] = 9999;
            style['position'] = 'absolute';
            style.top = (style.top + 15);
            cntr.css(style);
            cntr.slideDown("fast");
        });


        $("#${minFieldId}").focus(function() {
            var ele = $("#${minFieldId}");
            $(".isPtTimeSelectActive").removeClass("isPtTimeSelectActive");
            var cntr = $("#minctnr${minFieldId}");
            var i = $(ele).eq(0).addClass("isPtTimeSelectActive");
            var style = i.offset();
            style['z-index'] = 9999;
            style['position'] = 'absolute';
            style.top = (style.top + 15);
            cntr.css(style);
            cntr.slideDown("fast");
        });

        $("#${minFieldId}").blur(function() {
            var cntr = $("#minctnr${minFieldId}");
            cntr.slideUp("fast");
        });

        $("#${hourFieldId}").blur(function() {
            var cntr = $("#hourctnr${hourFieldId}");
            cntr.slideUp("fast");
        });
    });

    </c:if>
    /* ]]> */
</script>