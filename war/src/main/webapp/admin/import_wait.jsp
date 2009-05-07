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
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>

<%
    final Integer allowedDays = (Integer) request.getAttribute("allowedDays");
    // it's a confg_wizard step
    request.setAttribute(JahiaAdministration.CLASS_NAME + "configWizard",Boolean.TRUE);
    final String ajaxpath = request.getContextPath() + "/ajaxaction/PDisp";//url of ajax call
    final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString());
%>
<%@include file="/admin/include/header.inc"%>

<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/prototype/proto15sc17-compressed.js"></script>

<script type="text/javascript" >

    freqCall = 1;
    var waitCounter = 0;
    var watcher;

    function getElementValueFromXmlDoc(xdoc,id,def){
        if(!xdoc || !xdoc.getElementsByTagName(id)) return def;
        var currentagobject = xdoc.getElementsByTagName(id);
        if(currentagobject.length>0) return currentagobject[0].firstChild.data;
        return def;
    }

    function watch(){
        //the ajax periodical object
        watcher = new PeriodicalExecuter(monitor, freqCall);
    }

    function monitor()
    {
        var url = '<%=ajaxpath%>';
        var pars = 'cl='+10;
        do_ajax = new Ajax.Request(url, {parameters: pars, onSuccess: showResponse, onFailure: stopping});
    }

    function showResponse(request)
    {
        var xdoc = request.responseXML;
        var running = getElementValueFromXmlDoc(xdoc,'running',0);

        if (running == 0) {
            watcher.currentlyExecuting=true;
            loadFirstPage();
        } else {

            waitCounter += 1;
            var divElement = document.getElementById('msg');
            divElement.innerHTML = "<internal:message key='org.jahia.waitingForImport.button'/>" + " (" + waitCounter + ")...";
        }
    }
    function openReadmeFile() {
        var params = "width=1100,height=500,left=0,top=0,resizable=yes,scrollbars=yes,status=no";
        window.open('<%=readmefilePath%>', 'Readme', params);
    }
    // failure case
    function stopping(request) {
        document.write=request.responseText;
        watcher.currentlyExecuting=true;
        loadFirstPage();
    }
    function loadFirstPage() {



        var divElement = document.getElementById('msg');
        divElement.innerHTML = "<internal:message key='org.jahia.waitingForImport.button'/>" + " (" + waitCounter + ")...";


    <%
    StringBuffer hostUrl = new StringBuffer(request.getScheme()).append("://").append(request.getServerName());
    int port = request.getServerPort();
    if (port != 80) {
        hostUrl.append(":").append(Integer.toString(port));
    }
    %>
        location.href = "<%=hostUrl%>";
    }
    monitor();
    watch();
</script>

<%
    // The following might be null if the license doesn't include an expiration
    // date.
%>

<%if(!isConfigWizard){%>
<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit">
        <%if(!isConfigWizard){%>
        <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
        <%}else{%>
        <internal:message key="org.jahia.createSite.siteFactory"/>
        <%}%>
    </h2>
</div>
<% } %>



<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <div class="dex-TabPanelBottom-full">
                    <div id="content" class="full">
                        <div class="head">
                            <div class="object-title">
                                <internal:message key="org.jahia.bin.JahiaConfigurationWizard.congratulations.congratulationsItWork.label"/>
                            </div>
                        </div>
                        <p>
                            <internal:message
                                    key="org.jahia.bin.JahiaConfigurationWizard.congratulations.readme.label"/>&nbsp;<a href="javascript:openReadmeFile();">Readme</a>.
                        </p>


                        <% if (allowedDays != null) { %>
                        <h5><internal:message name="allowedDaysMsg"/></h5>
                        <% } %>
                        <div id="actionBar">
  <span class="dex-PushButton" id="connectButtonDiv">
    <span class="first-child" id='msg'>
    </span>
  </span>
                        </div>
                    </div>


                    <%if (isConfigWizard) {%>
                    <script type="text/javascript">
                        openReadmeFile();
                    </script>

                    <%}%>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<%@include file="/admin/include/footer.inc"%>