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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.applications.ApplicationBean" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle"%>
<%@ page import="org.jahia.params.ProcessingContext"%>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="java.security.Principal" %>
<%@ page import="org.jahia.engines.shared.Application_Field" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");
    final int appID = ((Integer) engineMap.get(theField.getDefinition().getName() + "_appID")).intValue();
    final Iterator appList = (Iterator) engineMap.get("appList");
    final Integer userNameWidth = new Integer(15);

    String appName = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field.applicationNotSet.label", jParams.getLocale());

    if (appID != -1) {
        while (appList.hasNext()) {
            final ApplicationBean appBean = (ApplicationBean) appList.next();
            if (appID == appBean.getID()) {
                appName = appBean.getName();
            }
        }
    }
%>
<utility:setBundle basename="JahiaInternalResources"/>
<p>
<fmt:message key="org.jahia.engines.shared.Application_Field.application.label"/>&nbsp;:&nbsp;<%=appName%>
</p>

<table width="100%">
    <%
        final List roles = (List) engineMap.get("roles");
        if (roles.size() > 0) { %>
    <tr>
        <td colspan="1">
            <fmt:message key="org.jahia.engines.shared.Application_Field.definesRoles.label"/>:
        </td>
    </tr><%
    Map applicationRoles = (Map) engineMap.get(Application_Field.APPLICATION_ROLES);
    List roleMembersList = (List) applicationRoles.get(new Integer(appID));
    for (int i = 0; i < roles.size(); i++) {
%><tr><td><br/>
    <fieldset style="padding: 5px;">
        <legend>
            &nbsp;<b><%=(String) roles.get(i)%></b>&nbsp;
        </legend>
        <%
            Set membersSet = (Set) roleMembersList.get(i);
            String[] textPattern = {"Principal", "Provider, 6", "Name, " + userNameWidth, "Properties, 20"};
            PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern);
        %>
        <select class="fontfix" name="authMembers<%=i%>" size="5" style="width:100%" multiple="multiple" disabled="disabled">
            <%
                Iterator it = membersSet.iterator();
                while (it.hasNext()) {
                    Principal p = (Principal) it.next();
            %><option value="<%=principalViewHelper.getPrincipalValueOption(p)%>">
            <%=principalViewHelper.getPrincipalTextOption(p)%>
        </option><%
            }
            if (membersSet.size() == 0) {
        %><option value="null">-- - -&nbsp;- <fmt:message key="org.jahia.engines.shared.Application_Field.noMembersDefined.label"/> -&nbsp;&nbsp;-&nbsp;&nbsp;-
            - --</option><%
            } %>
        </select>
    </fieldset>
</td>
    <td>&nbsp;&nbsp;</td>

    <td>

    </td>
</tr>
    <% }
    } else { %>
    <b><fmt:message key="org.jahia.engines.shared.Application_Field.notDefineRoles.label"/></b>
    <% } %>
</table>



