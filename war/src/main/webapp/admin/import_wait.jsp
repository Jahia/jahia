<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
    loadFirstPage();
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
      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
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
      <a class="ico-next" href='javascript:loadFirstPage();'><internal:message key='org.jahia.connect.button'/></a>
    </span>
  </span>
</div>

<%if (isConfigWizard) {%>
<script type="text/javascript">
    openReadmeFile();
</script>
<%}%>

<%@include file="/admin/include/footer.inc"%>