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
request.setAttribute(JahiaAdministration.CLASS_NAME + "configWizard", Boolean.TRUE);
final String ajaxpath = request.getContextPath() + "/ajaxaction/PDisp";//url of ajax call
final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString()); %>
<%@ include file="/jsp/jahia/administration/include/header.inc" %>
<script type="text/javascript">
  function loadFirstPage(){
      var divElement = document.getElementById('connectButtonDiv');
      divElement.innerHTML = "<div class='disabledButton' id='msg'><internal:message key='org.jahia.loading.button'/></div>";
      location.href = "${not empty pageContext.request.contextPath ? pageContext.request.contextPath : '/'}";
  }
  
  function openReadmeFile(){
      var params = "width=1100,height=500,left=0,top=0,resizable=yes,scrollbars=yes,status=no";
      window.open('<%=readmefilePath%>', 'Readme', params);
  }
  
</script>
<%
// The following might be null if the license doesn't include an expiration
// date. %>
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
                <internal:message key="org.jahia.bin.JahiaConfigurationWizard.congratulations.readme.label"/>&nbsp;<a href="javascript:openReadmeFile();">Readme</a>.
              </p>
              <% if (allowedDays != null) { %>
              <p>
                <internal:message name="allowedDaysMsg"/>
              </p><% } %>
            </div>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</div>
<div id="actionBar">
  <span class="dex-PushButton" id="connectButtonDiv">
    <span class="first-child" id='msg'>
      <a class="ico-next" href='javascript:loadFirstPage();'><internal:message key='org.jahia.connect.button'/></a>
    </span>
  </span>
</div><%if (isConfigWizard) { %>
<script type="text/javascript">
  openReadmeFile();
</script>
<%} %>
<%@ include file="/jsp/jahia/administration/include/footer.inc" %>