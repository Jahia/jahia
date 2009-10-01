<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="values" value="${currentNode.propertiesAsString}"/>
<div class="spacer">
    <h2>${fn:escapeXml(values['jcr:title'])}</h2>

    <div class="jobListItem">
        <div class="jobInfo">
            <p class="jobLocation">
                <span class="jobLabel">Location: </span>
                <span class="jobtxt">${fn:escapeXml(values.town)},&nbsp;${fn:escapeXml(values.country)}</span>
            </p>

            <p class="jobType">
                <span class="jobLabel">Contract type: </span>
                <span class="jobtxt">${fn:escapeXml(values.contract)}</span>
            </p>

            <p class="jobBusinessUnit">
                <span class="jobLabel">Business Unit: </span>
                <span class="jobtxt">${fn:escapeXml(values.businessUnit)}</span>
            </p>

            <p class="jobReference">
                <span class="jobLabel">Offer reference: </span>
                <span class="jobtxt">${fn:escapeXml(values.reference)}</span>
            </p>

            <p class="educationLevel">
                <span class="jobLabel">Education Level: </span>
                <span class="jobtxt">${fn:escapeXml(values.educationLevel)}</span>
            </p>
        </div>
        <p class="jobDescription">
            <span class="jobLabel">Job description:</span>
            <span class="jobtxt">${values.description}</span>
        </p>

        <p class="jobSkills">
            <span class="jobLabel">Required skills:</span>
            <span class="jobtxt">${values.skills}</span>
        </p>

        <div class="jobAction">
            <a onclick="ShowHideLayer('${currentNode.identifier}'); return false;" href="#apply"
               class="jobApply">Apply</a>

            <div class="clear"></div>
        </div>

        <div class="collapsible" id="collapseBox${currentNode.identifier}">
            <div class="jobsApplyForm">
                <form action="${url.base}${currentNode.path}/jobAnswers/*" method="post">
                    <input type="hidden" name="nodeType" value="web_templates:answerJobContainer"/>
                    <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
                    <input type="hidden" name="newNodeOutputFormat" value="html"/>
                    <fieldset>
                        <p class="field">
                            <label for="job-application-firstname">First name:</label>
                            <input type="text" name="firstname" id="job-application-firstname"/>
                        </p>

                        <p class="field">
                            <label for="job-application-lastname">Last name:</label>
                            <input type="text" name="lastname" id="job-application-lastname"/>
                        </p>

                        <p class="field">
                            <label for="job-application-email">E-mail:</label>
                            <input type="text" name="email" id="job-application-email"/>
                        </p>

                        <p class="field">
                            <label for="job-application-text">Text:</label>
                            <textarea name="text" id="job-application-text"></textarea>
                        </p>
                    </fieldset>
                    <input type="submit" class="button" value="Apply"/>
                </form>
            </div>
        </div>
        <template:module path="jobAnswers" autoCreateType="web_templates:answerJobContainerList"/>
        
    </div>
</div>