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
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="value" value="${functions:default(param['src_itemsPerPage'], functions:default(value, '10'))}"/>
<c:set var="options" value="${functions:default(options, '5,10,25,50,100')}"/>
<c:set target="${attributes}" property="name" value="src_itemsPerPage"/>
<c:if test="${display}">
    <select ${functions:attributes(attributes)} name="src_itemsPerPage">
        <c:forTokens items="${options}" delims="," var="opt">
            <option value="${opt}" ${opt == value ? 'selected="selected"' : ''}>${opt}</option>
        </c:forTokens>
    </select>
</c:if>
<c:if test="${!display}">
    <input type="hidden" ${functions:attributes(attributes)} value="${fn:escapeXml(value)}"/>
</c:if>