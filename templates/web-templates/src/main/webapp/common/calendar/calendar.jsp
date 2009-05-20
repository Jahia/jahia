<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ include file="../declarations.jspf" %>

<div class="box2 box2-style1"><!--start box 2 style1 -->
        <div class="box2-topright"> </div>
        <div class="box2-topleft"> </div>
        <h3 class="box2-header"><span><fmt:message key='statictitle.calendar.events'/></span></h3>
            <div class="box2-text">
                <p><fmt:message key='static.calendar.viewfromdate'/></p>
                <form action="" name="calendarStartDate" method="get">
                    <%--this is needed for event template--%>
                    <input type="hidden" name="eventsSort" value="${param.eventsSort}"/>
                    <input type="hidden" name="startDate" id="startDate"/>
                </form>
            <!--start calendar-->
                <script type="text/javascript">
                    function setDate(date) {
                        document.calendarStartDate.startDate.value=date;
                        document.calendarStartDate.submit();
                    }
                </script>
                <%--
                this is GWT calendar
                <ui:calendar callback="setDate"/>
                --%>

                <script type="text/javascript">
                    jQuery(document).ready(function(jQuery) {
                        jQuery("#datepicker").datepicker({onSelect: function(dateText){setDate(dateText)},showButtonPanel: true, altField: '#startDate', dateFormat: 'dd/mm/yy'},jQuery.datepicker.regional['${requestScope.currentRequest.locale}']);
                    });
                </script>

                <div id="datepicker"></div>
                
    <!--stop calendar-->
            </div>
        <div class="box2-bottomright"> </div>
        <div class="box2-bottomleft"> </div>
    <div class="clear"> </div>
</div>
<!--stop box 2 style 1-->