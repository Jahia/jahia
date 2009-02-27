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

<%@page import="org.jahia.bin.*" %>
<%@include file="/admin/include/header.inc" %>
<%@page import   = "java.util.*,org.jahia.data.applications.*" %>
<%@page import   = "java.util.*,org.jahia.services.sites.*" %>
<%@page import   = "java.util.*,org.jahia.registries.*" %>
<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@page import="org.jahia.utils.JahiaTools" %>
<%
String theURL = "";
Iterator appsList = (Iterator)request.getAttribute("appsList");
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
ProcessingContext jParams = jData.getProcessingContext();
stretcherToOpen   = 0; %>
<script type="text/javascript">
  
  var myActionLauncher = new ActionLauncher();
  
  function addAction(theResourceID, theActionName, theActionUrl){
      myActionLauncher.AddAction(theResourceID, theActionName, theActionUrl);
  }
  
  function doEdit(appid){
      myActionLauncher.LaunchAction(appid, 'edit', 'false');
  }
  
  function doDelete(appid){
      myActionLauncher.LaunchAction(appid, 'delete', 'false');
  }
  
  function doSwap(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'swap', 'false');
  }
  
  function doPages(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'pages', 'false');
  }
  
  function doSC(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'SC', 'true');
  }
  
  function doSH(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'SH', 'true');
  }
  
  function doMR(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'MR', 'true');
  }
  
  function doJB(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'JB', 'true');
  }
  
  function doPV(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'PV', 'true');
  }
  
  function doEV(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'EV', 'true');
  }
  
  function doDD(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'DD', 'true');
  }
  
  function handleKey(e){
      if (e.altKey && e.ctrlKey) {
          doDelete();
      }
      else {
          doEdit();
      }
  }
  
  function submitFormular(sub, go, appid){
      document.mainForm.action = '<%=requestURI%>?do=sharecomponents&sub=' + sub + '&subaction=' + go + '&appid=' + appid;
      document.mainForm.method = 'POST';
      document.mainForm.submit();
  }
  
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageComponents.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/admin/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
            <div id="content" class="fit">
              <div class="head">
                <div class="object-title">
                  <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageComponents.label"/>
                </div>
              </div>
              <div class="content-body">                  
                  <div  class="content-item-noborder">
                  <form name="mainForm" action="<%=requestURI%>?do=sharecomponents" method="post">
                  <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <thead>
                      <tr>
                        <th width="50%">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.name.label"/>
                        </th>
                        <th width="25%">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.context.label"/>
                        </th>
                        <th width="25%" class="lastCol">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.components.ManageComponents.actions.label"/>
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      <%
                      if ( appsList != null ){
                      ApplicationBean app = null;
                      int lineCounter = 0;
                      while (appsList.hasNext()){
                      app = (ApplicationBean)appsList.next();
                      String lineClass = "oddLine";
                      if (lineCounter % 2 == 0) {
                      lineClass = "evenLine";
                      }
                      lineCounter++; %>
                      <tr class="<%=lineClass%>">
                        <td>
                          <a href="javascript:doEdit(<%=app.getID()%>)" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.editProperties.label"/>"><%=app.getName() %></a>
                        </td>
                        <td>
                          <%=app.getContext() %>
                        </td>
                        <td class="lastCol">
                          <a href="javascript:doEdit(<%=app.getID()%>)" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.editProperties.label"/>"><img src="<%=URL%>images/icons/admin/adromeda/edit.png" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.edit.label'/>" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.edit.label'/>" width="16" height="16" border="0"/></a>
                        </td>
                      </tr>
                      <script type="text/javascript">
                        <%
                        String url = jData.gui().html().drawUpdateApplicationLauncher(app);
                        if ( url.equals("") ){
                            url = "alert('" +
                                JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.components.ManageComponents.noRightToEdit.label", jParams, jParams.getLocale())) +
                                  "')";
                        }
                        %>
                        // add actions to javascript action launcher
                        var theID = <%=app.getID()%>;
                        var theUrl = "<%=url%>";

                        // edit action
                        addAction(theID,'edit',theUrl);

                        // delete action
                        addAction(theID,'delete',"submitFormular('edit','confirmdelete',<%=app.getID()%>)");                                                   
                      </script>
                      <%
                      }
                      } else { %>
                      <tr>
                        <td colspan="3" class="text">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.noApplicationsFound.label"/>
                        </td>
                      </tr><%
                      } %>
                    </tbody>
                  </table>
                </form>
                </div>
              </div>
            </div>
            </td>
          </tr>
          </tbody>
        </table>
        </div>
        <div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>