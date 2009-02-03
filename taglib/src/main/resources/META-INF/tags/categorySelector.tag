<%--
/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
 * Version 1.0 (the "License"), or (at your option) any later version; you may
 * not use this file except in compliance with the License. You should have
 * received a copy of the License along with this program; if not, you may obtain
 * a copy of the License at
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
--%>
<%@ tag body-content="empty" description="Renders the link to the category selection engine (as a popup window)." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted category value with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do show the include children checkbox." %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children input field." %>
<%@ attribute name="root" type="java.lang.String" description="The root category to start with." %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="displayIncludeChildren" value="${not empty displayIncludeChildren ? displayIncludeChildren : 'true'}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in subfolders --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${functions:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<c:set var="root" value="${not empty root ? root : 'root'}"/>
&nbsp;<a href="#select"
onclick="javascript:{var categoriesSelector = window.open('${pageContext.request.contextPath}/jsp/jahia/engines/categories/launcher.jsp?pid=${jahia.page.ID}&contextId=${fieldId}@${root}&selectedCategories=' + document.getElementById('${fieldId}').value, '<%="categoriesSelector_" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable=yes,scrollbars=yes,height=800,width=600'); categoriesSelector.focus(); return false;}"
title='<utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.selectCategories"
                                      defaultValue="Select categories"/>'><utility:resourceBundle resourceBundle="JahiaEnginesResources"
        resourceName="org.jahia.engines.search.select" defaultValue="select"/></a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}"><utility:resourceBundle resourceBundle="JahiaEnginesResources"
        resourceName="org.jahia.engines.search.selectCategories.includeChildren" defaultValue="include subcategories"/></label>
</c:if>
