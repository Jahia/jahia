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