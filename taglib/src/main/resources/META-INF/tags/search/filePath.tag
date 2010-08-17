<%@ tag body-content="empty" description="Renders file path selection control." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value of the file path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="The initial value of the include children field." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib"%>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set target="${attributes}" property="name" value="src_filePath.value"/>
<c:set target="${attributes}" property="id" value="${functions:default(attributes.id, 'src_filePath_value')}"/>
<c:set var="value" value="${functions:default(param['src_filePath.value'], value)}"/>
<%-- by default set includeChildren to 'true' to search in subfolders --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${functions:default(param['src_filePath.includeChildren'], empty paramValues['src_filePath.value'] ? includeChildren : 'false')}"/>
<input ${functions:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${display}">
    <uiComponents:folderSelector fieldId="${attributes.id}" fieldIdIncludeChildren="src_filePath.includeChildren" includeChildren="${includeChildren}"/>
</c:if>
<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_filePath.includeChildren" value="true"/>
</c:if>