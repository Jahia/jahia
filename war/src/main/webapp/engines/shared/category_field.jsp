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
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.shared.Category_Field" %>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    final ProcessingContext jParams = jData.getProcessingContext();
    final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");

    // selected categories
    final List selectedCategories = (List) engineMap.get(theField.getDefinition().getName() + Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY);
    final String startCategory = (String)engineMap.get(Category_Field.START_CATEGORY);

    // read only default value
    boolean readOnly = false;

%>
<internal:gwtImport module="org.jahia.ajax.gwt.module.categorypicker.CategoryPicker"/>
<internal:categorySelector selectedCategories="<%=selectedCategories%>" readonly="<%=readOnly%>" locale="<%=elh.getCurrentLanguageCode()%>" autoSelectParent='<%=theField.getDefinition().getItemDefinition().getSelectorOptions().get("autoSelectParent")%>'/>

