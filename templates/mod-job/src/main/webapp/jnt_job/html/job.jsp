<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="values" value="${currentNode.propertiesAsString}"/>
<div class="job">
	<h2>${fn:escapeXml(values.title)}</h2>
	<div class="job-item">
		<span class="label">reference:</span>
		<span class="value">${fn:escapeXml(values.reference)}</span>
	</div>
	<div class="job-item">
		<span class="label">businessUnit:</span>
		<span class="value">${fn:escapeXml(values.businessUnit)}</span>
	</div>
	<div class="job-item">
		<span class="label">contract type:</span>
		<span class="value">${fn:escapeXml(values.contract)}</span>
	</div>
	<div class="job-item">
		<span class="label">location:</span>
		<span class="value">${fn:escapeXml(values.town)},${fn:escapeXml(values.country)}</span>
	</div>
	<div class="job-item">
		<span class="label">education level:</span>
		<span class="value">${fn:escapeXml(values.educationLevel)}</span>
	</div>
	<div class="job-item">
		<span class="label">description:</span>
		<span class="value">${values.description}</span>
	</div>
	<div class="job-item">
		<span class="label">skills:</span>
		<span class="value">${values.skills}</span>
	</div>

	<c:if test="${renderContext.editMode}">
		<h3>applications</h3>
	    <div class="job-applications">
	    	<c:if test="${currentNode.nodes.size == 0}">no applications were submitted yet</c:if>
	        <c:forEach items="${currentNode.nodes}" var="child">
				<template:module node="${child}"/>
	        </c:forEach>
		</div>
	</c:if>

    <div class="new-job-application">
    	<h3>Apply</h3>
		<form action="${url.base}${currentNode.path}/*" method="post">
		    <input type="hidden" name="nodeType" value="jnt:jobApplication"/>
		    <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
		    <input type="hidden" name="newNodeOutputFormat" value="html"/>
            <fieldset>
            	<p class="field">
            		<label for="job-application-firstname">firstname:</label>
                	<input type="text" name="firstname" id="job-application-firstname"/>
                </p>
            	<p class="field">
            		<label for="job-application-lastname">lastname:</label>
                	<input type="text" name="lastname" id="job-application-lastname"/>
                </p>
            	<p class="field">
            		<label for="job-application-email">e-mail:</label>
                	<input type="text" name="email" id="job-application-email"/>
                </p>
            	<p class="field">
            		<label for="job-application-text">text:</label>
                	<textarea name="text" id="job-application-text"></textarea>
                </p>
            </fieldset>
            <input type="submit" class="button" value="Apply"/>
		</form> 
	</div>
</div>
