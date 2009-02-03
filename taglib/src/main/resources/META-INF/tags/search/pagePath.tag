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
<%@ tag body-content="empty" description="Renders page selection control." 
    import="javax.servlet.jsp.PageContext,
            org.jahia.data.beans.JahiaBean,
            org.jahia.params.ProcessingContext,
            org.jahia.services.pages.ContentPage" %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for the page path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="Initial value for the include children field." %>
<c:set target="${attributes}" property="type" value="hidden"/>
<c:set target="${attributes}" property="name" value="src_pagePath.value"/>
<c:set target="${attributes}" property="id" value="src_pagePath.value"/>
<c:set var="value" value="${h:default(param['src_pagePath.value'], value)}"/>
<%-- by default set includeChildren to 'true' to search in subpages --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${h:default(param['src_pagePath.includeChildren'], empty paramValues['src_pagePath.value'] ? includeChildren : 'false')}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="type" value="text"/>
    <c:set target="${attributes}" property="name" value="src_pagePath.valueView"/>
    <c:set target="${attributes}" property="id" value="src_pagePath.valueView"/>
    <c:if test="${not empty value}">
        <% String pageTitle = ContentPage.getPage(Integer.parseInt((String)jspContext.getAttribute("value")), false, false).getTitle(((JahiaBean)jspContext.getAttribute("jahia", PageContext.REQUEST_SCOPE)).getProcessingContext().getEntryLoadRequest(), false);
           if (pageTitle != null && pageTitle.length() > 0) {
               jspContext.setAttribute("pageTitle", pageTitle);
           } else { 
           %><c:set var="pageTitle"><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.workflow.display.notitle" defaultValue="n.a."/></c:set><%
           }
        %>
    </c:if>
    <input ${h:attributes(attributes)} value="${fn:escapeXml(pageTitle)}"/>
    <ui:pageSelector fieldId="src_pagePath.value" fieldIdIncludeChildren="src_pagePath.includeChildren"
        onSelect="function (pid, url, title) { document.getElementById('src_pagePath.valueView').value=title; return true; }"/>
</c:if>
<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_pagePath.includeChildren" value="true"/>
</c:if>