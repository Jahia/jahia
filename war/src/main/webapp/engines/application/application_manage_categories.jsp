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
<%@ page language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.data.applications.EntryPointDefinition" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<%
    // common declaration
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final Map portletCategoriesMap = (Map) engineMap.get("portletCategoriesMap");
    final List selectedPortletsList = (List) engineMap.get("selectedObjectList");

%>
<%
    // display tag declaration
    request.setAttribute("resultRows", selectedPortletsList);
    String titleNameColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletNameColumnTitle.label", jData.getProcessingContext().getUILocale());
    String titleDescriptionColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletDescriptionColumnTitle.label", jData.getProcessingContext().getUILocale());
    String defaultDescription = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletNoDescription.label", jData.getProcessingContext().getUILocale());
    String titleCategorieColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.portletCategories.displayTag.categoryColumnTitle", jData.getProcessingContext().getUILocale());
    String tableId = "porltet_webapps";

    // current application
    boolean isPortlet = false;

    // catgeroy tree
    String titleCategoriesTree = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.portletCategories.categoriesTreeName", jData.getProcessingContext().getUILocale());
    String noCatgeories = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.portletCategories.noCatgeories", jData.getProcessingContext().getUILocale());

%>
<h3 class="template">
    <fmt:message key="org.jahia.engines.portletCategories.label"/>
</h3>
<!-- End Langs -->
<!-- Buttons -->
<jsp:include page="../buttons.jsp" flush="true"/>
<!-- End Buttons -->

<!-- add selected object-->
<br/>
<br/>
<br/>
<display:table style="width:100%" class="evenOddTable" id="<%=tableId%>" name="resultRows" export="false" defaultsort="1" pagesize="5">
    <%
        EntryPointDefinition definition = (EntryPointDefinition) pageContext.getAttribute(tableId);
        //get id
        String epID = "";

        // get name of the current portlet Definition
        String definitionName = "";
        if (definition != null) {
            definitionName = definition.getName();
            epID = "" + definition.getApplicationID();
        }
        // get description
        String description = defaultDescription;
        if (isPortlet) {
            description = definition.getDescription(jData.getProcessingContext().getUILocale());
        }

        //get Category
        String currentObjectKey = epID + "_" + definitionName;
        List categoriesList = (List) portletCategoriesMap.get(currentObjectKey);
        String category;
        if (categoriesList != null) {
            category = categoriesList.toString();
        } else {
            category = noCatgeories + " [" + currentObjectKey + "]";
        }

    %>
    <display:column title="<%=titleNameColumn%>" sortable="true" sortProperty="name" headerClass="sortable"
                    sortName="page">
        <%=definitionName %>
    </display:column>
    <display:column title="<%=titleDescriptionColumn%>" sortable="true" sortProperty="name" headerClass="sortable"
                    sortName="page"><%=description %>
    </display:column>
    <display:column title="<%=titleCategorieColumn%>" sortable="true" sortProperty="name" headerClass="sortable"
                    sortName="page"><%=category %>
    </display:column>
    <display:setProperty name="paging.banner.placement" value="bottom"/>
</display:table>

<!-- add category tree -->
<br/>
<br/>
<br/>

<div>
</div>
<div class="clearing"></div>