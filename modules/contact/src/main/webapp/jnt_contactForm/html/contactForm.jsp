<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="contactform.css"/>
<div class="Form contactForm">
    <c:set var="writeable" value="${currentResource.workspace eq 'live'}" />
    <c:if test='${not writeable}'>
        <c:set var="disabled" value='disabled="true"' />
    </c:if>
    <template:tokenizedForm>
    <form action="${url.base}${currentNode.path}/*" method="post">
        <input type="hidden" name="nodeType" value="jnt:contact"/>
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
        <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <c:set var="props" value="${currentNode.properties}"/>
        <jcr:nodeType name="jnt:contact" var="contactType"/>
        <c:set var="propDefs" value="${contactType.declaredPropertyDefinitionsAsMap}"/>
        <fieldset>
            <legend><fmt:message key="jnt_contact"/></legend>
            <c:if test="${props.gender.boolean}">
                <p>
                    <label class="left" for="gender">${fn:escapeXml(jcr:label(props.gender.definition,currentResource.locale))}</label>
                    <select name="gender" id="gender" ${disabled}>
                    	<c:forEach items="${propDefs.gender.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.gender.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.title.boolean}">
                <p>
                    <label class="left" for="title">${fn:escapeXml(jcr:label(props.title.definition,currentResource.locale))}</label>
                    <select name="title" id="title" ${disabled}>
                    	<c:forEach items="${propDefs.title.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.title.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.firstname.boolean}">
                <p>
                    <label class="left" for="firstname">${fn:escapeXml(jcr:label(props.firstname.definition,currentResource.locale))}</label><input id="firstname" type="text"
                                                                               name="firstname" ${disabled} />
                </p>
            </c:if>

            <c:if test="${props.lastname.boolean}">
                <p>
                    <label class="left" for="lastname">${fn:escapeXml(jcr:label(props.lastname.definition,currentResource.locale))}</label><input id="lastname" type="text"
                                                                               name="lastname" ${disabled} />
                </p>
            </c:if>

            <c:if test="${props.age.boolean}">
                <p>
                    <label class="left" for="age">${fn:escapeXml(jcr:label(props.age.definition,currentResource.locale))}</label><input type="text" id="age" name="age" ${disabled} />
                </p>
            </c:if>
            <c:if test="${props.birthdate.boolean}">
                <p>
                    <label class="left" for="birthdate">${fn:escapeXml(jcr:label(props.birthdate.definition,currentResource.locale))}</label><input type="text" id="birthdate"
                                                                                name="birthdate" ${disabled}/>
                </p>
            </c:if>

            <c:if test="${props.profession.boolean}">
                <p>
                    <label class="left" for="profession">${fn:escapeXml(jcr:label(props.profession.definition,currentResource.locale))}</label><input type="text" id="profession"
                                                                                  name="profession" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.maritalStatus.boolean}">
                <p>
                    <label class="left" for="maritalStatus">${fn:escapeXml(jcr:label(props.maritalStatus.definition,currentResource.locale))}</label>
                    <select name="maritalStatus" id="maritalStatus" ${disabled}>
                    	<c:forEach items="${propDefs.maritalStatus.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.maritalStatus.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.hobbies.boolean}">
                <p>
                    <label class="left" for="hobbies">${fn:escapeXml(jcr:label(props.hobbies.definition,currentResource.locale))}</label><input type="text" id="hobbies" name="hobbies" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.subject.boolean}">
                <p>
                    <label class="left" for="subject">${fn:escapeXml(jcr:label(props.subject.definition,currentResource.locale))}</label>
                    <select name="subject" id="subject" ${disabled}>
                    	<c:forEach items="${propDefs.subject.valueConstraints}" var="valueOption">
                    		<option value="${valueOption}"><fmt:message key="jnt_contact.subject.${valueOption}"/></option>
                    	</c:forEach>
                    </select>
                </p>
            </c:if>
            <c:if test="${props.address.boolean}">
                <p>
                    <label class="left" for="address">${fn:escapeXml(jcr:label(props.address.definition,currentResource.locale))}</label><input type="text" id="address" name="address" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.city.boolean}">
                <p>
                    <label class="left" for="city">${fn:escapeXml(jcr:label(props.city.definition,currentResource.locale))}</label><input type="text" id="city" name="city" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.state.boolean}">
                <p>
                    <label class="left" for="state">${fn:escapeXml(jcr:label(props.state.definition,currentResource.locale))}</label><input type="text" id="state" name="state" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.zip.boolean}">
                <p>
                    <label class="left" for="zip">${fn:escapeXml(jcr:label(props.zip.definition,currentResource.locale))}</label><input type="text" id="zip" name="zip" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.country.boolean}">
                <p>
                    <label class="left" for="country">${fn:escapeXml(jcr:label(props.country.definition,currentResource.locale))}</label><input type="text" id="country" name="country" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.remarks.boolean}">
                <p>
                    <label class="left" for="remarks">${fn:escapeXml(jcr:label(props.remarks.definition,currentResource.locale))}</label><input type="text" id="remarks" name="remarks" ${disabled}/>
                </p>
            </c:if>
            <c:if test="${props.captcha.boolean}">
                <p class="field">
                    <label class="left" for="captcha"><template:captcha /></label><input type="text" id="captcha" name="captcha"/>
                </p>
            </c:if>

            <div class="divButton"><br />
                <input type="submit" tabindex="28" value="<fmt:message key='save'/>" class="button" id="submit" ${disabled}/>
                <input type="reset" tabindex="29" value="<fmt:message key='reset'/>" class="button" id="reset" ${disabled}/>
            </div>
        </fieldset>
    </form>
    </template:tokenizedForm>
</div>

<c:if test="${jcr:hasPermission(currentNode,'viewContacts')}">
    <template:addResources type="javascript" resources="jquery.js"/>
    <fieldset>
        <legend><fmt:message key="label.results"/></legend>

        <div id="results-${currentNode.identifier}" >
        </div>
    </fieldset>
<script type="text/javascript">
    $('#results-${currentNode.identifier}').load('${url.baseLive}${currentNode.path}.results.html.ajax');
</script>
</c:if>
