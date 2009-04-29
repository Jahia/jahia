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
<%@page import="org.jahia.bin.*" %>
<%@include file="/admin/include/header.inc" %>
<%@page import   = "java.util.*,org.jahia.data.applications.*" %>
<%@page import   = "org.jahia.services.sites.*" %>
<%@page import   = "org.jahia.registries.*" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@page import="org.jahia.utils.JahiaTools" %>
<%
String theURL = "";
Iterator appsList = (Iterator)request.getAttribute("appsList");
String requestURI = (String)request.getAttribute("requestURI");
String warningMsg = (String) request.getAttribute("warningMsg");
String appserverDeployerUrl = (String) request.getAttribute("appserverDeployerUrl");
String generatedFilePath = (String) request.getAttribute("generatedFilePath");
String generatedFileName = (String) request.getAttribute("generatedFileName");
Boolean isTomcat = (Boolean) request.getAttribute("isTomcat");
boolean deployed = request.getAttribute("deploy") != null;

String sub = (String) request.getParameter("sub");
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
  <h2 class="edit"><fmt:message key="org.jahia.admin.manageComponents.label"/></h2>
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
                  <fmt:message key="org.jahia.admin.manageComponents.label"/>
                </div>
              </div>
              <div class="content-body">
                  <div  class="content-item-noborder">
                  <form name="mainForm" action="<%=requestURI%>?do=sharecomponents" method="post">
                  <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <thead>
                      <tr>
                        <th width="50%">
                          <fmt:message key="org.jahia.admin.name.label"/>
                        </th>
                        <th width="25%">
                          <fmt:message key="org.jahia.admin.components.ManageComponents.context.label"/>
                        </th>
                        <th width="25%" class="lastCol">
                          <fmt:message key="org.jahia.admin.site.components.ManageComponents.actions.label"/>
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
                          <a href="javascript:doEdit(<%=app.getID()%>)" alt="<fmt:message key="org.jahia.admin.editProperties.label"/>"><%=app.getName() %></a>
                        </td>
                        <td>
                          <%=app.getContext() %>
                        </td>
                        <td class="lastCol">
                          <a href="javascript:doEdit(<%=app.getID()%>)" alt="<fmt:message key="org.jahia.admin.editProperties.label"/>"><img src="<%=URL%>images/icons/admin/adromeda/edit.png" alt="<fmt:message key='org.jahia.admin.edit.label'/>" title="<fmt:message key='org.jahia.admin.edit.label'/>" width="16" height="16" border="0"/></a>
                        </td>
                      </tr>
                      <script type="text/javascript">
                        <%
                        String url = jData.gui().html().drawUpdateApplicationLauncher(app);
                        if ( url.equals("") ){
                            url = "alert('" +
                                JahiaTools.html2text(JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.components.ManageComponents.noRightToEdit.label", jParams.getLocale())) +
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
                          <fmt:message key="org.jahia.admin.components.ManageComponents.noApplicationsFound.label"/>
                        </td>
                      </tr><%
                      } %>
                    </tbody>
                  </table>
                </form>
                </div>
              </div>
                <!--   prepare portlet -->
        <div class="head">
            <div class="object-title">
               <fmt:message key="org.jahia.admin.components.ManageComponents.deploy.preparewar.label"/>
            </div>
        </div>
        <div  class="content-item">
            <form name="preparePortlet"
                  action='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=preparePortlet")%>'
                  method="post"
                  enctype="multipart/form-data">
                  <input id="warDeploy" name="deploy" type="hidden" value="false"/>
                  <div>
                      <%if(isTomcat){%>
                       <span class="dex-PushButton">
                          <span class="first-child">
                               <a class="ico-app-scan" href="javascript:{showWorkInProgress();document.getElementById('warDeploy').value='true'; document.preparePortlet.submit();}"><fmt:message key="org.jahia.admin.components.ManageComponents.deploy.preparewarAndDeploy"/></a>
                         </span>
                     </span>
                     <%}%>
                     <span class="dex-PushButton">
                          <span class="first-child">
                              <a class="ico-app-scan" href="javascript:{showWorkInProgress(); document.preparePortlet.submit();}"><fmt:message key="org.jahia.admin.components.ManageComponents.deploy.preparewar"/></a>
                         </span>
                     </span>
                 </div>
                <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                    <tr>
                        <td>
                            <fmt:message key="org.jahia.admin.components.ManageComponents.deploy.preparewar.fileselect"/>&nbsp;
                        </td>
                        <td>
                            :&nbsp;<input type="file" name="war">
                        </td>
                    </tr>
                </table>
            </form>
            <% if (warningMsg != null && warningMsg != "" && sub.equals("preparePortlet")) { %>
            <p style="color:red">
                <%=warningMsg %>
            </p>
            <% } %>
            <% if (generatedFilePath != null && generatedFilePath != "" && sub.equals("preparePortlet")) { %>
            <p>
                <% if (deployed){%>
                   <fmt:message key="org.jahia.admin.components.ManageComponents.portletDeployed.label"/>
                <% }else{%>
                  <fmt:message key="org.jahia.admin.components.ManageComponents.portletReady.label"/>  
                <%}%>
                <ul>
                 <li>
                     <a href="<%=generatedFilePath%>"> <%=generatedFileName%></a>
                 </li>
                </ul>
            </p>
            <% } %>
        </div>
        <div class="head">
            <div class="object-title">
                <fmt:message key="org.jahia.admin.components.ManageComponents.deploy.label"/>
            </div>
        </div>
        <div  class="content-item">
            <div>

                     <%if(isTomcat){%>
                     <span class="dex-PushButton">
                          <span class="first-child">
                               <a class="ico-app-scan" href="javascript:{showWorkInProgress(); document.deployPortlet.submit();}">
                                     <fmt:message key="org.jahia.admin.components.ManageComponents.deployNewComponents.label"/>
                                </a>
                           </span>
                      </span>
                     <%}%>
                    <span class="dex-PushButton">
                       <span class="first-child">
                              <a class="ico-app-new" href="<%=appserverDeployerUrl%>"><fmt:message key="org.jahia.admin.components.ManageComponents.openAppManager.label"/></a>
                        </span>
                     </span>
                      <span class="dex-PushButton">
                           <span class="first-child">
                                 <a class="ico-help" href="#help" onclick="javascript:{document.getElementById('help').style.display='';}"><fmt:message key="org.jahia.admin.components.ManageComponents.deploy.help.label"/></a>
                            </span>
                      </span>
             </div>
            <%if(isTomcat){%>
            <form name="deployPortlet"
                  action='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=deployPortlet")%>'
                  method="post"
                  enctype="multipart/form-data">
                <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                    <tr>
                        <td>
                            <fmt:message key="org.jahia.admin.components.ManageComponents.deploy.preparewar.fileselect"/>&nbsp;
                        </td>
                        <td colspan="2" >
                            :&nbsp;<input type="file" name="war">
                        </td>
                    </tr>

                </table>
            </form>
            <%}%>
            <div id="help" style="display:none">
                    <fmt:message key="org.jahia.admin.components.ManageComponents.deploy.help"/>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>