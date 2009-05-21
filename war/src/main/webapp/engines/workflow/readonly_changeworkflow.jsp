<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page import="org.jahia.content.ContentObject,
                 org.jahia.data.viewhelper.principal.PrincipalViewHelper,
                 org.jahia.params.ParamBean,
                 org.jahia.services.workflow.WorkflowService,
                 java.security.Principal,
                 java.util.*" %>
<%@page import="org.jahia.services.workflow.ExternalWorkflowInstanceCurrentInfos"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final ContentObject contentObject = (ContentObject) engineMap.get("contentObject");

    final Integer userNameWidth = new Integer(15);
    request.getSession().setAttribute("userNameWidth", userNameWidth);

    final Boolean hasParent = (Boolean) engineMap.get("hasParent");
    Integer workflowMode = (Integer) engineMap.get("workflowMode");
    if (workflowMode == null) {
        workflowMode = (Integer) engineMap.get(("defaultMode"));
    }
    pageContext.setAttribute("workflowMode", workflowMode);
    
    final Map workflowNames = (Map) engineMap.get("workflowNames");
    final String workflowName = (String) engineMap.get("workflowName");
    pageContext.setAttribute("workflowName", workflowName);
    final Map processes = (Map) engineMap.get("processes");
    pageContext.setAttribute("processes", processes);
    final String process = (String) engineMap.get("process");
    pageContext.setAttribute("process", process);
    final String selectUsrGrp = (String) engineMap.get("selectUsrGrp");
    final Map rolesMapping = (Map) engineMap.get("roleMapping");
    final String theURL = jParams.settings().getJahiaEnginesHttpPath();
    final String theScreen = (String) engineMap.get("screen");
    Boolean aBoolean = (Boolean) engineMap.get("locksActive");
    ExternalWorkflowInstanceCurrentInfos infos = (ExternalWorkflowInstanceCurrentInfos) engineMap.get("infos");
    final boolean hasLocksActivated;
    if(aBoolean!=null)
        hasLocksActivated = aBoolean.booleanValue();
    else
        hasLocksActivated = true;
    int roleNb; // Store the number of application roles
    final Integer inheritedMode = (Integer) engineMap.get("inheritedMode");

%>
<utility:setBundle basename="JahiaInternalResources"/>
<!-- Begin readonly_changeworkflow.jsp -->

<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
         <div class="object-title"><fmt:message key="org.jahia.engines.include.actionSelector.Workflow.label"/></div>
      </div>

      <% if(hasLocksActivated) {%>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <td><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.error.locks.activated.label"/></td>
          </tr>
        </table>
      <% } else { %>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.wfmode.label"/></th>
            <td>
            <%
                final int mode = workflowMode.intValue();
                switch (mode) {
                    case 0:
            %><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.noworkflow.label"/><%
                break;

            case 1:
        %><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.standard.label"/><%
                break;

            case 2:
        %><c:out value="${processes[workflowName][process]}"/><%
                break;

            case 3:
        %><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.inherited.label"/><%
                break;

            case 4:
        %><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.linked.label"/><%
                    break;
            }
        %>
            </td>
          </tr>
          <% if (workflowMode.intValue() == WorkflowService.LINKED || workflowMode.intValue() == WorkflowService.INHERITED) {  %>
            <tr>
              <% if (workflowMode.intValue() == WorkflowService.LINKED) { %>
              <th>
                  <fmt:message key="org.jahia.engines.workflow.WorkflowEngine.linkedto.label"/>
              </th>
              <td>
                <%= engineMap.get("linked") %>
            <% } else { %>
              <th>
                <fmt:message key="org.jahia.engines.workflow.WorkflowEngine.inheritfrom.label"/>:
              </th>
              <td>
                <%= engineMap.get("inheritingParent") %>
            <% } %>
                <%
                switch (inheritedMode.intValue()) {
                case WorkflowService.INACTIVE:
                %>
                (<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.noworkflow.label"/>)
                <%
                   break;
                case WorkflowService.JAHIA_INTERNAL:
                %>
                (<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.standard.label"/>)
                <%
                  break;
                case WorkflowService.EXTERNAL:
                %>
                (<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.external.label"/>
                 - <%= engineMap.get("inheritedProcess") %>)
                <%
                  break;
                }
              %>
            </td>
          </tr>
<% } %>
        </table>
        <%
            if (workflowMode.intValue() == WorkflowService.EXTERNAL || (inheritedMode != null && inheritedMode == WorkflowService.EXTERNAL)) { %>
        <%
        final List roles = (List) engineMap.get("roles");
        roleNb = roles.size();
        if (roleNb > 0) { %>
          <div class="head">
            <div class="object-title"><fmt:message key="org.jahia.engines.workflow.definesRoles.label"/></div>
          </div>

          <%
              final List<Set> roleMembersList = (List<Set>) engineMap.get("workflowRoles");
              final List<Set> roleInheritedMembersList = (List<Set>) engineMap.get("inheritedWorkflowRoles");
              for (int i = 1; i < roles.size(); i++) {

                  Set membersSet = roleMembersList.get(i);
                  Set inheritedMemberSet = roleInheritedMembersList.get(i);

                  final String[] textPattern = {"Inheritance, 2", "Principal", "Provider, 6", "Name, " + userNameWidth, "Properties, 20"};
                  final PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern);
                  principalViewHelper.setInheritance(inheritedMemberSet);
          %>
            <div class="fill padded">
              <table cellpadding="5" cellspacing="0" border="0" width="100%">
                <tr>
            <td width="45%">

                <% if (roles.get(i).equals(infos.getNextRole())) { %>
                <img src="${pageContext.request.contextPath}/engines/images/arrow_right_green.png"
                alt='<fmt:message key="org.jahia.engines.workflow.nextStep.label"/>' border="0"
                style="float: left; margin-left: -25px; margin-top: 40px;"/>
                    <strong><%=rolesMapping.get(roles.get(i))%></strong>
                <% } else { %>
                    <%=rolesMapping.get(roles.get(i))%>
                <% } %>
                <br/>
                <select class="fontfix" name="authMembers<%=i%>" size="5" multiple="multiple"
                        style="width: 100%" <%if (membersSet.size() == 0) {%> disabled="disabled" <%}%>>
                    <%
                        Iterator it = inheritedMemberSet.iterator();
                        while (it.hasNext()) {
                            final Principal p = (Principal) it.next();
                    %>
                    <option disabled="disabled" value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p)%>
                    </option>
                    <%
                        }
                        it = membersSet.iterator();
                        while (it.hasNext()) {
                            final Principal p = (Principal) it.next();
                    %>
                    <option value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p)%>
                    </option>
                    <%
                        }
                        if (inheritedMemberSet.size() + membersSet.size() == 0) {
                    %>
                    <option value="null">-----&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.noMembers.label"/>&nbsp;-----
                    </option>
                    <%}%>
                </select>
            </td>




                    <% if (i > 0 && i < roles.size() - 1 && ((String) roles.get(i + 1)).endsWith("_unlock")) {
                        i++;
                        membersSet = roleMembersList.get(i);
                        inheritedMemberSet = roleInheritedMembersList.get(i);
                    %>
                    <td width="45%">
                        <% if (roles.get(i).equals(infos.getNextRole())) { %>
                        <img src="${pageContext.request.contextPath}/engines/images/arrow_right_green.png"
                        alt='<fmt:message key="org.jahia.engines.workflow.nextStep.label"/>' border="0"
                        style="float: left; margin-left: -25px; margin-top: 40px;"/>
                            <strong><%=rolesMapping.get(roles.get(i))%></strong>
                        <% } else { %>
                            <%=rolesMapping.get(roles.get(i))%>
                        <% } %>
                        <br/>
                        <select class="fontfix" name="authMembers<%=i%>" size="5" multiple="multiple"
                                style="width: 100%" <%if (membersSet.size() == 0) {%> disabled="disabled" <%}%>>
                            <%
                                it = inheritedMemberSet.iterator();
                                while (it.hasNext()) {
                                    final Principal p = (Principal) it.next();
                            %>
                            <option disabled="disabled" value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p)%>
                            </option>
                            <%
                                }
                                it = membersSet.iterator();
                                while (it.hasNext()) {
                                    final Principal p = (Principal) it.next();
                            %>
                            <option value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p)%>
                            </option>
                            <%
                                }
                                if (inheritedMemberSet.size() + membersSet.size() == 0) {
                            %>
                            <option value="null">-----&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.noMembers.label"/>&nbsp;-----
                            </option>
                            <%}%>
                        </select>
                    </td>
                    <% } else { %>
                    <td colspan="2" width="50%">&nbsp;</td>
                    <% } %>
                </tr>
              </table>
            </div>
            <%}%>
            <%} else { %>
            <fmt:message key="org.jahia.engines.shared.Application_Field.notDefineRoles.label"/>
            <% } %>

            <div><img src="${pageContext.request.contextPath}/engines/images/arrow_right_green.png"
                        alt='<fmt:message key="org.jahia.engines.workflow.nextStep.label"/>' border="0"
                        style="float: left; margin-top: -3px;"/> - <fmt:message key="org.jahia.engines.workflow.nextStep.label"/>
           </div>

      <% } else { %>
      <% } %>
      <% } %>
  </div>
</div>
</div>
<!-- End readonly_changeworkflow.jsp -->