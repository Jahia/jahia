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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="header.inc" %>
<%
    final Object isTomcatObj = request.getAttribute("isTomcat");
    boolean isTomcat = false;
    if (isTomcatObj != null && isTomcatObj instanceof Boolean) {
        isTomcat = ((Boolean) isTomcatObj).booleanValue();
    }
%>

<script type="text/javascript">

// AJAX variables
var req;
var jahiaReady = true;
var waitCounter = 0;
var nonAvailableIntervalID;
var availableIntervalID;

function testContextNonAvailability() {
    try {
        // correct values are "POST" or "GET" (HTTP methods).
        var method = "GET" ;

        var url = '<%= request.getContextPath()%>' + "/ping.jsp";

            // Create new XMLHttpRequest request
        if (window.XMLHttpRequest) {
            req = new XMLHttpRequest();

        } else if (window.ActiveXObject) {
            req = new ActiveXObject("Microsoft.XMLHTTP");

        } else {
            alert("Error: Your Browser does not support XMLHTTPRequests, please upgrade...");
            return;
        }

        req.open(method, url, true);

        req.onreadystatechange = function () {
            var readyState = req.readyState;
            if (req.readyState == 4) {
                // alert ("resp: " + req.responseText);
                if (req.status == 200) {
                    jahiaReady = true;
                } else {
                    jahiaReady = false;
                }
            }
        }

        req.send(null);

    } catch (e) {
        alert("Exception sending the Request: " + e);
    }

    if ((jahiaReady) && (waitCounter < 150)) {
        waitCounter += 1;

        var divElement = document.getElementById('msg');
        divElement.innerHTML = "<fmt:message key="org.jahia.waitingForShutdown.button" /> (" + waitCounter + ")...";
    } else {
        clearInterval(nonAvailableIntervalID);
        waitCounter = 0;
        jahiaReady = false;
        availableIntervalID = setInterval("testContextAvailability()", 1000);
    }
}


function testContextAvailability() {
    try {
        // correct values are "POST" or "GET" (HTTP methods).
        var method = "GET" ;

        var url = '<%= request.getContextPath()%>' + "/ping.jsp";

            // Create new XMLHttpRequest request
        if (window.XMLHttpRequest) {
            req = new XMLHttpRequest();

        } else if (window.ActiveXObject) {
            req = new ActiveXObject("Microsoft.XMLHTTP");

        } else {
            alert("Error: Your Browser does not support XMLHTTPRequests, please upgrade...");
            return;
        }

        req.open(method, url, true);

        req.onreadystatechange = function () {
            var readyState = req.readyState;
            if (req.readyState == 4) {
                // alert ("resp: " + req.responseText);
                if (req.status == 200) {
                    jahiaReady = true;
                } else {
                    jahiaReady = false;
                }
            }
        }

        req.send(null);

    } catch (e) {
        alert("Exception sending the Request: " + e);
    }

    if ((!jahiaReady) && (waitCounter < 1000)) {
        waitCounter += 1;
        var divElement = document.getElementById('msg');
        divElement.innerHTML = "<fmt:message key="org.jahia.waitingForStartup.button" /> (" + waitCounter + ")...";
    } else {
        clearInterval(availableIntervalID);
        changeButton();
    }
}

function changeButton() {
    var divElement = document.getElementById('connectButtonDiv');
    divElement.innerHTML = "<a class='ico-next' href='javascript:loadFirstPage();' title='<fmt:message key="org.jahia.nextStep.button"/>'>" +
                                                                                                                                     "<fmt:message key="org.jahia.nextStep.button"/></a>";
    jahiaReady = true;
}

function loadFirstPage() {
    var divElement = document.getElementById('connectButtonDiv');
    divElement.innerHTML = "<div class='disabledButton' id='msg'><fmt:message key='org.jahia.loading.button'/></div>";


<%
StringBuffer hostUrl = new StringBuffer(request.getScheme()).append("://").append(request.getServerName());
int port = request.getServerPort();
if (port != 80) {
    hostUrl.append(":").append(Integer.toString(port));
}
%>
    location.href = "<%=hostUrl%>";
}

nonAvailableIntervalID = setInterval("testContextNonAvailability()", 500);

</script>

<%
    // The following might be null if the license doesn't include an expiration
    // date.
    final Integer allowedDays = (Integer) request.getAttribute("allowedDays");
%>
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.congratulations.pleaseBePatient.label"/>
    </div>
</div>
<div id="pagebody">

    <!--<p>
        <fmt:message
                key="org.jahia.bin.JahiaConfigurationWizard.congratulations.jahiaSuccessfullyInstalledAndConfigured.label"/>
    </p>-->

    <!--<h5> <fmt:message
                key="org.jahia.bin.JahiaConfigurationWizard.congratulations.jahiaSuccessfullyInstalledAndConfigured.otherserver"/></h5>
    <p>

        <fmt:message
                key="org.jahia.bin.JahiaConfigurationWizard.congratulations.jahiaSuccessfullyInstalledAndConfigured.restart"/>
    </p> -->

    <!--<h5> <fmt:message
                key="org.jahia.bin.JahiaConfigurationWizard.congratulations.jahiaSuccessfullyInstalledAndConfigured.tomcat"/></h5>
       -->
    <%if (isTomcat) {%>
    <p>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.congratulations.pleaseBePatient.label"/>
    </p>
    <%} else {%>
    <p>
        <!--fmt:message
                key="org.jahia.bin.JahiaConfigurationWizard.congratulations.jahiaSuccessfullyInstalledAndConfigured.restart"/-->
    </p>
    <%}%>
    <div id="buttons">
        <span class="dex-PushButton">
         <span class="first-child" id="connectButtonDiv">
            <div class="disabledButton" id="msg"><fmt:message key="org.jahia.pleaseWait.button"/>...</div>
        </span>
        </span>
    <br/>
    <br/>
    </div>
    <br/>
    <br/>
</div>
</div>
<%@ include file="footer.inc" %>