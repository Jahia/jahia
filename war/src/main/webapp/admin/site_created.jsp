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
request.setAttribute(JahiaAdministration.CLASS_NAME + "configWizard", Boolean.TRUE);
final String ajaxpath = request.getContextPath() + "/ajaxaction/PDisp";//url of ajax call
final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString()); %>
<%@ include file="/admin/include/header.inc" %>
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
<%@ include file="/admin/include/footer.inc" %>