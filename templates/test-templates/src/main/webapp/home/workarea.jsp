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
  <c:if test="${templateDef.visible && !(templateDef.name == templatePackage.homePageName) && !(templateDef.name == templatePackage.defaultPageName)}">
    <div class="fieldTitle">
      <fmt:message key="${templateDef.displayName}"/>
    </div>
    <div class="fieldValue">
      <fmt:message key="${templateDef.description}"/>
    </div>
  </c:if>
</c:forEach>