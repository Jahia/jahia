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
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.data.applications.EntryPointDefinition" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
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
    String titleNameColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletNameColumnTitle.label", jData.getProcessingContext().getLocale());
    String titleDescriptionColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletDescriptionColumnTitle.label", jData.getProcessingContext().getLocale());
    String defaultDescription = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletNoDescription.label", jData.getProcessingContext().getLocale());
    String titleCategorieColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.portletCategories.displayTag.categoryColumnTitle", jData.getProcessingContext().getLocale());
    String tableId = "porltet_webapps";

    // current application
    boolean isPortlet = false;

    // catgeroy tree
    String titleCategoriesTree = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.portletCategories.categoriesTreeName", jData.getProcessingContext().getLocale());
    String noCatgeories = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.portletCategories.noCatgeories", jData.getProcessingContext().getLocale());

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
            description = definition.getDescription();
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