<%@ tag body-content="empty" description="Renders the entry field for querying content object creator." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for this field." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set target="${attributes}" property="name" value="src_createdBy"/>
<c:set var="value" value="${functions:default(param['src_createdBy'], value)}"/>
<input ${functions:attributes(attributes)} value="${fn:escapeXml(value)}"/>