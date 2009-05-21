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
<%@ tag body-content="empty" description="Renders the link to the page selection engine (as a popup window)." %>
<%@ tag import="java.util.*,
                org.jahia.engines.selectpage.SelectPage_Engine,
                org.jahia.params.ProcessingContext,
                org.jahia.registries.EnginesRegistry" %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted page value with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do show the include children checkbox." %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children input field." %>
<%@ attribute name="useUrl" required="false" type="java.lang.Boolean"
              description="If set to true the selected page URL will be used in the field value; otherwise the ID of the selected page will be used (default)." %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The select link text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after a page is selectd. Two paramaters are passed as arguments: page ID and page URL. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ tag dynamic-attributes="attributes"
        %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="fieldIdHash"><%= Math.abs(jspContext.getAttribute("fieldId").hashCode()) %>
</c:set>
<c:set var="displayIncludeChildren" value="${not empty displayIncludeChildren ? displayIncludeChildren : 'true'}"/>
<c:set var="useUrl" value="${not empty useUrl ? useUrl : 'false'}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in subfolders --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${h:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<jsp:useBean id="engineParams" class="java.util.HashMap"/>
<c:set target="${engineParams}" property="selectPageOperation" value="selectAnyPage"/>
<c:set target="${engineParams}" property="pageID" value="<%= Integer.valueOf(-1) %>"/>
<c:set target="${engineParams}" property="parentPageID" value="<%= Integer.valueOf(-1) %>"/>
<c:set target="${engineParams}" property="contextId" value="${fieldId}"/>
<c:set target="${engineParams}" property="callback" value="setSelectedPage${fieldIdHash}"/>
<c:set var="ctx" value="${jahia.processingContext}"/>
<c:set var="link"><%= ((SelectPage_Engine) EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine")).renderLink((ProcessingContext) jspContext.getAttribute("ctx"), (Map) jspContext.getAttribute("engineParams")) %></c:set>
<c:set target="${attributes}" property="href" value="#select"/>
<c:set target="${attributes}" property="onclick" value="<%= null %>"/>
<c:if test="${empty attributes.title}"><c:set target="${attributes}" property="title"><fmt:message key="selectors.pageSelector.selectPage"/></c:set></c:if>
<c:if test="${empty label}"><c:set var="label"><fmt:message key="selectors.select"/></c:set></c:if>
<a ${h:attributes(attributes)} onclick="javascript:{var pageSelector = window.open('${fn:escapeXml(link)}', '<%="pageSelector_" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable,height=800,width=600'); pageSelector.focus(); return false;}">${fn:escapeXml(label)}</a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}"><fmt:message key="selectors.pageSelector.selectPage.includeChildren"/></label>
</c:if>
<script type="text/javascript">
    function setSelectedPage${fieldIdHash}(pid, url, title) {
    <c:if test="${not empty onSelect}">
        if ((${onSelect})(pid, url, title)) {
            document.getElementById('${fieldId}').value = ${useUrl} ? url : pid;
        }
    </c:if>
    <c:if test="${empty onSelect}">
        document.getElementById('${fieldId}').value = ${useUrl} ? url : pid;
    </c:if>
    }
</script>