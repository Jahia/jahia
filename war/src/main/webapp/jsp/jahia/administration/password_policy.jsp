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

<%@include file="include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
stretcherToOpen   = 0; %>
<script type="text/javascript">
  function doAction(action){
      if (!action) {
          action = 'reset';
      }
      document.jahiaAdmin.sub.value = action;
      document.jahiaAdmin.submit();
      return false;
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.passwordPolicies.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
              <%@include file="/jsp/jahia/administration/include/menu_server.inc" %>
              <div id="content" class="fit">
                  <div class="head">
                      <div class="object-title">
                           <internal:adminResourceBundle
                    resourceName="org.jahia.admin.passwordPolicies.mainMenu.label"/>
                      </div>
                  </div>
                  <div  class="content-item-noborder">
                <c:if test="${not empty confirmationMessage}">
                  <% String msgKey = (String)request.getAttribute("confirmationMessage"); %>
                  <div class="blueColor">
                    <internal:adminResourceBundle resourceName="<%= msgKey %>"/>
                  </div>
                </c:if>
                <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"passwordPolicies","")%>' method="post">
                  <input type="hidden" name="sub" value="reset" /><%--
                  <h2><c:out value="${policy.name}"/></h2>
                  --%>
                  <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <thead>
                      <tr>
                        <th width="7%">
                          <internal:adminResourceBundle resourceName="org.jahia.admin.passwordPolicies.active.label"/>
                        </th>
                        <th width="50%">
                          <internal:adminResourceBundle resourceName="org.jahia.admin.passwordPolicies.name.label"/>
                        </th>
                        <th width="43%" class="lastCol">
                          <internal:adminResourceBundle resourceName="org.jahia.admin.passwordPolicies.parameters.label"/>
                        </th>
                      </tr>
                    </thead>
                    <c:forEach items="${policy.rules}" var="rule" varStatus="rlzStatus">
                      <tr class="<c:if test='${rlzStatus.index % 2 == 0}'>oddLine</c:if>">
                        <td align="center">
                          <input type="checkbox" name="rules[<c:out value='${rlzStatus.index}'/>].active"
                          <c:if test="${rule.active}">
                            checked="checked"
                          </c:if>
                          value="true"/>
                        </td>
                        <c:set var="i18nKey" value='org.jahia.admin.passwordPolicies.rule.${rule.name}'/>
                        <td>
                          <internal:adminResourceBundle resourceName='<%= (String)pageContext.getAttribute("i18nKey") %>'/>
                        </td>
                        <td class="lastCol">
                          <table width="100%">
                            <c:forEach items="${rule.conditionParameters}" var="condParam" varStatus="paramsStatus">
                              <tr>
                                <c:set var="i18nKey" value='org.jahia.admin.passwordPolicies.rule.${rule.name}.param.${condParam.name}'/>
                                <td width="45%" align="right">
                                  <internal:adminResourceBundle resourceName='<%= (String)pageContext.getAttribute("i18nKey") %>'/>:
                                </td>
                                <td width="55%">
                                  <input type="text" name="rules[<c:out value='${rlzStatus.index}'/>].conditionParameters[<c:out value='${paramsStatus.index}'/>].value" value="<c:out value='${condParam.value}'/>"/>
                                </td>
                              </tr>
                            </c:forEach>
                          </table>
                        </td>
                      </tr>
                    </c:forEach>
                  </table>
                </form>
               </div>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-restore" href="#reset" onclick="return doAction('reset')"><internal:adminResourceBundle resourceName="org.jahia.admin.restore.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#save" onclick="return doAction('save')"><internal:adminResourceBundle resourceName="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/jsp/jahia/administration/include/footer.inc" %>