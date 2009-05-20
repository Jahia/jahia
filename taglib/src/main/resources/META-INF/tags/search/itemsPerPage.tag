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
<%@ tag body-content="empty" description="Renders items per page drop down box." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.Integer" description="The initial value." %>
<%@ attribute name="options" required="false" type="java.lang.String"
              description="Allowed options as a comma separated list of value." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="value" value="${h:default(param['src_itemsPerPage'], h:default(value, '10'))}"/>
<c:set var="options" value="${h:default(options, '5,10,20,30,50,100')}"/>
<c:set target="${attributes}" property="name" value="src_itemsPerPage"/>
<c:if test="${display}">
    <select ${h:attributes(attributes)} name="src_itemsPerPage">
        <c:forTokens items="${options}" delims="," var="opt">
            <option value="${opt}" ${opt == value ? 'selected="selected"' : ''}>${opt}</option>
        </c:forTokens>
    </select>
</c:if>
<c:if test="${!display}">
    <input type="hidden" ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
</c:if>