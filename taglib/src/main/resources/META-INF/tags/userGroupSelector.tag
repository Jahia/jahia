<%@ tag body-content="empty" description="Renders the link/button to the GWT-based user/group selector (requires GWT Edit module to be loaded)." %>
<%@ attribute name="mode" required="false" type="java.lang.String" description="The selection mode: users, groups or both. [both]" %>
<%@ attribute name="onSelect" required="false" type="java.lang.String" description="The JavaScript function to be called after a user/group is selected. The selected principal type (u or g), principal key and principal name will be passed as arguments to this function. If the function returns true, the principal name will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="fieldId" required="false" type="java.lang.String" description="The HTML element ID of the input field, where the proncipal name should be stored. If not provided, nothing will be done by this tag." %>
<%@ attribute name="label" required="false" type="java.lang.String" description="The label of the link for openning the user/group window." %>
<%@ attribute name="multiple" required="false" type="java.lang.Boolean" description="Allow multiple principal selection? [false]" %>
<%@ tag dynamic-attributes="attributes" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set target="${attributes}" property="jahiatype" value="usergroup"/>
<c:set target="${attributes}" property="mode" value="${mode}"/>
<c:set target="${attributes}" property="onSelect" value="${onSelect}"/>
<c:set target="${attributes}" property="fieldId" value="${fieldId}"/>
<c:set target="${attributes}" property="singleSelectionMode" value="${not functions:default(multiple, false)}"/>
<c:set var="elementId" value='<%= "usergroup_" + org.apache.commons.id.IdentifierUtils.nextLongIdentifier() %>'/>
<c:set target="${attributes}" property="id" value="${functions:default(attributes.id, elementId)}"/>
<span ${functions:attributes(attributes)} label="${empty label ? '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' : label}"></span>