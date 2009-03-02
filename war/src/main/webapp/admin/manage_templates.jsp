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

<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.params.*" %>
<%@page import="org.jahia.resourcebundle.*" %>
<%@page import="org.jahia.utils.*" %>
<%@page import   = "java.util.*,org.jahia.services.pages.*" %>
<%@page import   = "java.util.*,org.jahia.data.JahiaData" %>
<%
String theURL = "";
Iterator templList = (Iterator)request.getAttribute("templList");
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
JahiaSite site = (JahiaSite)request.getAttribute("site");
ProcessingContext jParams = jData.getProcessingContext(); %>
<script type="text/javascript">
  <!--
  
  // do use actionlauncher.js
  
  var myActionLauncher = new ActionLauncher();
  
  function addAction(theResourceID, theActionName, theActionUrl){
      myActionLauncher.AddAction(theResourceID, theActionName, theActionUrl);
  }
  
  function doEdit(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'edit', 'false');
  }
  
  function doDelete(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'delete', 'false');
  }
  
  function doSwap(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'swap', 'false');
  }
  
  function doPages(){
      myActionLauncher.LaunchActionForSelectBox(document.mainForm.templid, 'pages', 'false');
  }
  
  function handleKey(e){
      if (e.altKey && e.ctrlKey) {
          doDelete();
      }
      else {
          doEdit();
      }
  }
  
  function submitFormular(sub, go){
      document.mainForm.action = '<%=requestURI%>?do=templates&sub=' + sub + '&subaction=' + go;
      document.mainForm.method = 'POST';
      document.mainForm.submit();
  }
  
  
  //-->
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.manageTemplates.label"/><% if ( site!= null ){ %><fmt:message key="org.jahia.admin.site.label"/>&nbsp;:&nbsp;<%=site.getServerName() %><%} %></h2>
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
                <jsp:param name="mode" value="site"/>
            </jsp:include>
          <div id="content" class="fit">
            <div class="head">
              <div class="object-title">
                <fmt:message key="org.jahia.admin.manageTemplates.label"/>
              </div>
            </div>
            <div class="content-body">
              <div id="operationMenu">
                <span class="dex-PushButton">
                  <span class="first-child">
                     <a class="ico-tpl-view" href="javascript:doEdit()" alt="<fmt:message key="org.jahia.admin.templates.ManageTemplates.editSelectedTemplate.label"/>"><fmt:message key="org.jahia.admin.edit.label"/></a>
                  </span>
                </span>
<%-- 
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-tpl-delete" href="javascript:doDelete()" alt="<fmt:message key="org.jahia.admin.templates.ManageTemplates.deleteSelectedTemplate.label"/>"><fmt:message key="org.jahia.admin.delete.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-tpl-new" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=displaynewlist")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.deployNewTemplates.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-tpl-add" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=add")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.manuallyAddNewTemplate.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-tpl-opts" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=options")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.templatesManagementOptions.label"/></a>
                  </span>
                </span>
--%>            
              </div>
            </div>
            <form name="mainForm" action="<%=requestURI%>?do=templates" method="post">
              <table border="0" cellpadding="5" cellspacing="0" width="100%" class="topAlignedTable">
                <tr>
                  <td>
                    <select ondblclick="javascript:handleKey(event);" class="input" name="templid" size="20" style="width : 30em" multiple="true">
                      <%
                      JahiaPageDefinition templ = null;
                      // the following variable is needed because we can't run through an Iterator twice
                      List templVec = new ArrayList();
                      while (templList.hasNext()){
                      templ = (JahiaPageDefinition)templList.next();
                      templVec.add(templ);
                      String url = jData.gui().html().drawUpdateTemplateLauncher(templ);
                      if ( url.equals("") ){
                      url = "alert('" +
                      JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.templates.ManageTemplates.noRightToEdit.label", jParams, jParams.getLocale())) +
                      "')";
                      } %>
                      <option value="<%=templ.getID()%>"><%=templ.getName() %></option>
                      <%
                      } %>
                    </select>
                    <script type="text/javascript">
                                  <!--
                                  <%
                                      Iterator templVecEnum = templVec.iterator();
                                      while (templVecEnum.hasNext()){
                                          templ = (JahiaPageDefinition)templVecEnum.next();
                        
                                          String url = jData.gui().html().drawUpdateTemplateLauncher(templ);
                                          if ( url.equals("") ){
                                              url = "alert('" +
                                                  JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.templates.ManageTemplates.noRightToEdit.label", jParams, jParams.getLocale())) +
                                                    "')";
                                          }
                        
                                  %>
                                          // add actions to javascript action launcher
                                          var theID = <%=templ.getID()%>;
                                          var theUrl = "<%=url%>";
                        
                                          // edit action
                                          addAction(theID,'edit',theUrl);
                        
                                          // delete action
                                          addAction(theID,'delete',"submitFormular('edit','confirmdelete')");
                        
                                  <%
                                      }
                                  %>
                                  //-->
                                  
                    </script>
                  </td>
                  
                </tr>
              </table>
            </form>
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
