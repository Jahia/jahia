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

