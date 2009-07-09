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
<internal:gwtImport module="categorypicker"/>
<internal:categorySelector startCategoryKey="<%=startCategory%>" multiple="<%=theField.getDefinition().getPropertyDefinition().isMultiple()%>" selectedCategories="<%=selectedCategories%>" readonly="<%=readOnly%>" locale="<%=elh.getCurrentLanguageCode()%>" autoSelectParent='<%=theField.getDefinition().getItemDefinition().getSelectorOptions().get("autoSelectParent")%>'/>

