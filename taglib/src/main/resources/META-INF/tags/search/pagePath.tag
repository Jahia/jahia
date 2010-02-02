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
<%@ tag body-content="empty" description="Renders page selection control." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for the page path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="Initial value for the include children field." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="givenId" value="${attributes.id}"/>
<c:set target="${attributes}" property="type" value="hidden"/>
<c:set target="${attributes}" property="name" value="src_pagePath.value"/>
<c:set target="${attributes}" property="id" value="src_pagePath_value"/>
<c:set var="value" value="${functions:default(param['src_pagePath.value'], value)}"/>
<%-- by default set includeChildren to 'true' to search in subpages --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${functions:default(param['src_pagePath.includeChildren'], empty paramValues['src_pagePath.value'] ? includeChildren : 'false')}"/>
<input ${functions:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="type" value="text"/>
    <c:set target="${attributes}" property="name" value="src_pagePath.valueView"/>
    <c:set target="${attributes}" property="id" value="${functions:default(givenId, 'src_pagePath_valueView')}"/>
    <c:if test="${not empty value}">
    	<jcr:node path="${value}" var="pageNode"/>
    	<c:if test="${not empty pageNode}">
	    	<jcr:nodeProperty node="${pageNode}" name="jcr:title" var="title"/>
	    	<c:set var="pageTitle" value="${not empty title ? title.string : ''}"/>
        </c:if>
    	<c:if test="${empty pageTitle}">
        	<c:set var="pageTitle"><fmt:message key="searchForm.pagePicker.noTitle"/></c:set>
        </c:if>
    </c:if>
    <input ${functions:attributes(attributes)} value="${fn:escapeXml(pageTitle)}"/>
    <uiComponents:pageSelector fieldId="src_pagePath_value" displayFieldId="${attributes.id}" fieldIdIncludeChildren="src_pagePath.includeChildren" includeChildren="${includeChildren}"/>
</c:if>
<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_pagePath.includeChildren" value="true"/>
</c:if>