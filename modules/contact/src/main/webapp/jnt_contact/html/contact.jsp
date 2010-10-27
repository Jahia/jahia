<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:remove var="itemsList" scope="request"/>
<c:forTokens items="firstname,lastname,title,age,birthdate,gender,profession,maritalStatus,hobbies,contact,address,city,state,zip,country,remarks" delims="," var="propName" varStatus="status">
    <c:if test="${currentNode.parent.properties[propName].boolean}">
        <c:if test="${not empty itemsList}">
            <c:set scope="request" var="itemsList" value="${itemsList},${propName}"/>
        </c:if>
        <c:if test="${empty itemsList}">
            <c:set scope="request" var="itemsList" value="${propName}"/>
        </c:if>
    </c:if>
</c:forTokens>
<div class="mainContent">
    <div class="peopleBody">
    <c:set var="props" value="${currentNode.properties}"/>
    <c:forTokens items="${itemsList}" delims="," var="propName">
    	<p><span class="peopleLabel">${fn:escapeXml(jcr:label(props[propName].definition,currentResource.locale))}:</span>&nbsp;<c:if test="${fn:contains('title,gender,maritalStatus,contact', propName)}" var="selector"><fmt:message key="jnt_contact.${props[propName].definition.name}.${props[propName].string}"/></c:if><c:if test="${not selector}">${fn:escapeXml(props[propName].string)}</c:if></p>
    </c:forTokens>
    </div>
</div>    