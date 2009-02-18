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

<%@ page language="java" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

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
            <b><internal:engineResourceBundle
                    resourceName="org.jahia.engines.categories.noCategoriesAvailable.label"/></b>
        </p>
        <% } else { %>
        <internal:gwtImport module="org.jahia.ajax.gwt.subengines.categoriespicker.CategoriesPicker"/>
        <internal:categorySelector selectedCategories="<%=selectedCategories%>" startCategoryKey="<%= categoryPropertyName %>" autoSelectParent="${param.autoSelectParent}"/>
        <% } %>

<div id="actionBar">
  <span class="dex-PushButton">
       <span class="first-child">
           <a href="javascript:sendFormSave();" class="ico-ok" title='<internal:engineResourceBundle resourceName="org.jahia.ok.button"/>'>
                 <internal:engineResourceBundle resourceName="org.jahia.ok.button"/>
            </a>
        </span>
  </span>
  <span class="dex-PushButton">
        <span class="first-child">
             <a href="javascript:window.close();" class="ico-cancel" title='<internal:engineResourceBundle resourceName="org.jahia.close.button"/>'>
                   <internal:engineResourceBundle resourceName="org.jahia.close.button"/>
              </a>
        </span>
  </span>
</div>