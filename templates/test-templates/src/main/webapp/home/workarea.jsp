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
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ResourceBundle"%>

<%@page import="org.jahia.data.beans.SiteBean"%>
<%@page import="org.jahia.data.templates.JahiaTemplateDef"%>
<%@page import="org.jahia.data.templates.JahiaTemplatesPackage"%>
<%@page import="org.jahia.params.ParamBean"%>
<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@page import="org.jahia.testtemplate.sorter.LocalizedTemplateNameSorter"%>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>

<%@ include file="../common/declarations.jspf"%>


<div class="expectedResult">
  <fmt:message key="label.template.descriptions"/>
</div>
<%
ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
int siteID = jParams.getSiteID();
JahiaTemplatesPackage templatePackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(siteID);
List<JahiaTemplateDef> templateList = new ArrayList(templatePackage.getTemplates());
Collections.sort(templateList, new LocalizedTemplateNameSorter(new JahiaResourceBundle("jahiatemplates.Test_templates", jParams.getLocale())));
%>
<c:set var="templatePackage" value="<%=templatePackage%>"/>
<c:set var="templateList" value="<%=templateList%>"/>    
<c:forEach items="${templateList}" var="templateDef">
  <c:if test="${templateDef.visible}">
    <div class="fieldTitle">
      <fmt:message key="${templateDef.displayName}"/>
    </div>
    <div class="fieldValue">
      <fmt:message key="${templateDef.description}"/>
    </div>
  </c:if>
</c:forEach>