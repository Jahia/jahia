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
<%@page import   = "java.util.*,org.jahia.data.applications.*" %>
<%@page import="org.jahia.bin.*" %>
<%
String theURL = "";
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
Integer val = (Integer)request.getAttribute("autoDeploy");
boolean autoDeploy = false;
if ( val != null ){
autoDeploy = (val.intValue()==1);
} %>
<script language="javascript">
  
  function sendForm(subAction){
      document.mainForm.subaction.value = subAction;
      document.mainForm.action = "<%=requestURI%>?do=components&sub=options";
      document.mainForm.submit();
  }
  
</script>
<tr>
  <td align="center" class="text">
    <img name="component" src="<%=URL%>images/icons/admin/application.gif" width="48" height="48" border="0" align="middle">
  </td>
  <td align="left" class="text">
    <h3><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageComponents.label"/>
      <br>
      <% if ( currentSite!= null ){ %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %>
    </h3>
  </td>
</tr>
</table>
<br>
<table cellpadding="2" cellspacing="0" border="0" width="100%">
  <tr>
    <td colspan="2" width="530">
      &nbsp;
    </td>
  </tr>
  <tr>
    <td width="100">
      &nbsp;&nbsp;&nbsp;
    </td>
    <td width="100%">
      <form name="mainForm" action="" method="post">
        <table border="0" cellpadding="0" width="90%">
          <tr>
            <td colspan="2">
              <br>
              <br>
            </td>
          </tr>
          <tr>
            <td valign="top" align="left" colspan="2">
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td class="text"align="left" valign="top">
                    <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.generalOptions.label"/>&nbsp;:</b>
                  </td>
                  <td class="text"align="left" valign="top">
                  </td>
                </tr>
                <tr>
                  <td colspan="2" height="5">
                    <br>
                    <br>
                  </td>
                </tr>
                <tr>
                  <td valign="top" align="left" nowrap>
                    <font class="text">
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.automaticDeployment.label"/>
                    </font>
                  </td>
                  <td valign="top" align="left">
                    <font class="text">
                      :&nbsp;
                    </font>
                    <input type="checkbox" name="autoDeploy" value="1"<% if ( autoDeploy ) { %>checked<% } %>>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" height="5">
                  </td>
                </tr>
              </table>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <br>
              <br>
              <br>
              <br>
            </td>
          </tr>
          <tr>
            <td align="right" colspan="2">
              &nbsp;
              <br>
              <input type="hidden" name="subaction" value=""><a href="javascript:document.mainForm.reset()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Cancel','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.resetChangesOn.button"/>',1)"><img name="Cancel" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.resetChangesOff.button"/>" width="133" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.resetChanges.label"/>"></a><a href="javascript:sendForm('save');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('save','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.saveOn.button"/>',1)"><img name="save" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.saveOff.button"/>" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.save.label"/>"></a>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <br>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              &nbsp;
              <br>
              <br>
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td nowrap width="145" valign="top">
                    <font class="text">
                      <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b>
                    </font>
                  </td>
                  <td valign="top">
                    <font class="text">
                      <%if(!isLynx){ %>
                      <li>
                        <%} %><a href='<%=JahiaAdministration.composeActionURL(request,response,"components","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
                        <br>
                        <%if(!isLynx){ %>
                        <li>
                          <%} %><a href='<%=JahiaAdministration.composeActionURL(request,response,"components","&sub=add")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.manuallyAddNewComponent.label"/></a>
                          <br>
                          <%if(!isLynx){ %>
                          <li>
                            <%} %><a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
                            <br>
                            </font>
                          </td>
                          </tr>
                        </table>
                        </td>
                      </tr>
                      </table>
                    </form>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" align="right">
                    <table border="0" width="100%">
                      <tr>
                        <td width="48">
                          <img name="logo" src="../css/images/logo/logo-jahia.gif" border="0" width="45" height="34">
                        </td>
                        <td>
                          <img src="<%=URL%>images/pix.gif" border="0" width="1" height="10">
                          <div id="copyright">
                            <%=copyright %>
                          </div>
                          <span class="version">
                            Jahia <%=Jahia.VERSION %>.<%=Jahia.getPatchNumber() %> r<%=Jahia.getBuildNumber() %>
                          </span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
              <%@include file="/admin/include/footer.inc" %>