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
<%@ page language="java" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final String contextId = (String) engineMap.get("contextId");
    final String categoryPropertyName = contextId.substring(contextId.indexOf("@") + 1);
    final List selectedCategories = (List) jParams.getSessionState().getAttribute("selectedCategories" + contextId);
%>
<input type="hidden" name="contextId" value="<%=engineMap.get("contextId")%>"/>


        <% if (engineMap.containsKey("NoCategories")) { %>
        <p>
            <b><fmt:message key="org.jahia.engines.categories.noCategoriesAvailable.label"/></b>
        </p>
        <% } else { %>
        <internal:gwtImport module="org.jahia.ajax.gwt.module.categorypicker.CategoryPicker"/>
        <internal:categorySelector selectedCategories="<%=selectedCategories%>" startCategoryKey="<%= categoryPropertyName %>" autoSelectParent="${param.autoSelectParent}"/>
        <% } %>

<div id="actionBar">
  <span class="dex-PushButton">
       <span class="first-child">
           <a href="javascript:sendFormSave();" class="ico-ok" title='<fmt:message key="org.jahia.ok.button"/>'>
                 <fmt:message key="org.jahia.ok.button"/>
            </a>
        </span>
  </span>
  <span class="dex-PushButton">
        <span class="first-child">
             <a href="javascript:window.close();" class="ico-cancel" title='<fmt:message key="org.jahia.close.button"/>'>
                   <fmt:message key="org.jahia.close.button"/>
              </a>
        </span>
  </span>
</div>