<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="contactform.css"/>
<div class="Form contactForm">
    <form action="${url.base}${currentNode.path}/*" method="post">
        <input type="hidden" name="nodeType" value="jnt:contact"/>
        <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
        <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <c:set var="props" value="${currentNode.properties}"/>
        <jcr:nodeType name="jnt:contact" var="contactType"/>
        <c:set var="propDefs" value="${contactType.declaredPropertyDefinitionsAsMap}"/>
        <fieldset>
            <legend><fmt:message key="jnt_contact"/></legend>
            <c:if test="${props.firstname.boolean}">
                <p>
                    <label class="left" for="firstname">${fn:escapeXml(jcr:label(props.firstname.definition))}</label><input id="firstname" type="text" 
                                                                               name="firstname"/>
                </p>
            </c:if>

            <c:if test="${props.lastname.boolean}">
                <p>
                    <label class="left" for="lastname">${fn:escapeXml(jcr:label(props.lastname.definition))}</label><input id="lastname" type="text"
                                                                               name="lastname"/>
                </p>
            </c:if>
            <c:if test="${props.title.boolean}">
                <p>
                    <label class="left" for="title">${fn:escapeXml(jcr:label(props.title.definition))}</label>
                    <select name="title" id="title">
                    	<c:forEach items="${propDefs.title.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.title.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.age.boolean}">
                <p>
                    <label class="left" for="age">${fn:escapeXml(jcr:label(props.age.definition))}</label><input type="text" id="age" name="age"/>
                </p>
            </c:if>
            <c:if test="${props.birthdate.boolean}">
                <p>
                    <label class="left" for="birthdate">${fn:escapeXml(jcr:label(props.birthdate.definition))}</label><input type="text" id="birthdate"
                                                                                name="birthdate"/>
                </p>
            </c:if>
            <c:if test="${props.gender.boolean}">
                <p>
                    <label class="left" for="gender">${fn:escapeXml(jcr:label(props.gender.definition))}</label>
                    <select name="gender" id="gender">
                    	<c:forEach items="${propDefs.gender.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.gender.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.profession.boolean}">
                <p>
                    <label class="left" for="profession">${fn:escapeXml(jcr:label(props.profession.definition))}</label><input type="text" id="profession"
                                                                                  name="profession"/>
                </p>
            </c:if>
            <c:if test="${props.maritalStatus.boolean}">
                <p>
                    <label class="left" for="maritalStatus">${fn:escapeXml(jcr:label(props.maritalStatus.definition))}</label>
                    <select name="maritalStatus" id="maritalStatus">
                    	<c:forEach items="${propDefs.maritalStatus.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.maritalStatus.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.hobbies.boolean}">
                <p>
                    <label class="left" for="hobbies">${fn:escapeXml(jcr:label(props.hobbies.definition))}</label><input type="text" id="hobbies" name="hobbies"/>
                </p>
            </c:if>
            <c:if test="${props.contact.boolean}">
                <p>
                    <label class="left" for="contact">${fn:escapeXml(jcr:label(props.contact.definition))}</label>
                    <select name="contact" id="contact">
                    	<c:forEach items="${propDefs.contact.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.contact.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.address.boolean}">
                <p>
                    <label class="left" for="address">${fn:escapeXml(jcr:label(props.address.definition))}</label><input type="text" id="address" name="address"/>
                </p>
            </c:if>
            <c:if test="${props.city.boolean}">
                <p>
                    <label class="left" for="city">${fn:escapeXml(jcr:label(props.city.definition))}</label><input type="text" id="city" name="city"/>
                </p>
            </c:if>
            <c:if test="${props.state.boolean}">
                <p>
                    <label class="left" for="state">${fn:escapeXml(jcr:label(props.state.definition))}</label><input type="text" id="state" name="state"/>
                </p>
            </c:if>
            <c:if test="${props.zip.boolean}">
                <p>
                    <label class="left" for="zip">${fn:escapeXml(jcr:label(props.zip.definition))}</label><input type="text" id="zip" name="zip"/>
                </p>
            </c:if>
            <c:if test="${props.country.boolean}">
                <p>
                    <label class="left" for="country">${fn:escapeXml(jcr:label(props.country.definition))}</label><input type="text" id="country" name="country"/>
                </p>
            </c:if>
            <c:if test="${props.remarks.boolean}">
                <p>
                    <label class="left" for="remarks">${fn:escapeXml(jcr:label(props.remarks.definition))}</label><input type="text" id="remarks" name="remarks"/>
                </p>
            </c:if>
            <div class="divButton"><br />
				<input type="submit" tabindex="28" value="<fmt:message key='save'/>" class="button" id="submit"/>
                <input type="reset" tabindex="29" value="<fmt:message key='reset'/>" class="button" id="reset"/>
              </div>
        </fieldset>
    </form>
</div>
<c:forEach items="${currentNode.children}" var="subchild" varStatus="status">
        <div class="forum-box forum-box-style${(status.index mod 2)+1}">
            <template:module node="${subchild}" template="small"/>
        </div>
</c:forEach>
<a href="${url.base}${currentNode.path}.csv" target="_new">csv export</a>

