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

<%@ page import="org.jahia.engines.categories.CategoriesSelect_Engine"%>
<%@ page import="org.jahia.services.categories.Category" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.*" %>
<%@ page language="java" %>
<%!
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.engines.selectpage.close");
%>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    String contextId = request.getParameter("contextId");
    String name = contextId.substring(0,contextId.indexOf('@'));
    Map categoryDataMap = (Map) session.getAttribute(CategoriesSelect_Engine.ENGINE_NAME + ".categoriesDataMap." + contextId);
    List selected = (List) categoryDataMap.get("defaultSelectedCategories");
    String selectedIds = "";
    for (Iterator iterator = selected.iterator(); iterator.hasNext();) {
        String s = (String) iterator.next();
        Category c = Category.getCategory(s);
        selectedIds += (new Integer(c.getObjectKey().getIdInType()));
        if (iterator.hasNext()) {
            selectedIds += ",";
        }
    }
%>

<script type="text/javascript">
    window.close();
    if (window.opener.document.getElementById('divprop_<%= name %>')) {
        window.opener.document.getElementById('divprop_<%= name %>').innerHTML='<%= selected %>';
    } else if (window.opener.document.getElementById('<%= name %>')) {
    	window.opener.document.getElementById('<%= name %>').value = '<%= StringUtils.join(selected.iterator(), ',') %>';
    }
    if (window.opener.document.forms['mainForm'] && window.opener.document.forms['mainForm'].elements['prop_<%= name %>']) {
        window.opener.document.forms['mainForm'].elements['prop_<%= name %>'].value='<%= selectedIds %>';
    }
</script>

