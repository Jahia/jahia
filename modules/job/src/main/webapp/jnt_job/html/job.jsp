<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="job.css"/>
<template:addResources type="javascript" resources="job-collapse.js"/>
<c:set var="values" value="${currentNode.propertiesAsString}"/>
<div class="spacer">
    <h2>${fn:escapeXml(values['jcr:title'])}</h2>

    <div class="jobListItem">
        <div class="jobInfo">
            <p class="jobLocation">
                <span class="jobLabel"><fmt:message key="jnt_job.location"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.town)},&nbsp;<jcr:nodePropertyRenderer node="${currentNode}" name="country" renderer="flagcountry"/></span>
            </p>

            <p class="jobType">
                <span class="jobLabel"><fmt:message key="jnt_job.contract"/>: </span>
                <span class="jobtxt"><jcr:nodePropertyRenderer node="${currentNode}" name="contract" renderer="resourceBundle"/></span>
            </p>

            <p class="jobBusinessUnit">
                <span class="jobLabel"><fmt:message key="jnt_job.businessUnit"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.businessUnit)}</span>
            </p>

            <p class="jobReference">
                <span class="jobLabel"><fmt:message key="jnt_job.reference"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.reference)}</span>
            </p>

            <p class="educationLevel">
                <span class="jobLabel"><fmt:message key="jnt_job.educationLevel"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.educationLevel)}</span>
            </p>
        </div>
        <p class="jobDescription">
            <span class="jobLabel"><fmt:message key="jnt_job.description"/>:</span>
            <span class="jobtxt">${values.description}</span>
        </p>

        <p class="jobSkills">
            <span class="jobLabel"><fmt:message key="jnt_job.skills"/>:</span>
            <span class="jobtxt">${values.skills}</span>
        </p>

        <div class="jobAction">
            <a onclick="ShowHideLayer('${currentNode.identifier}'); return false;" href="#apply"
               class="jobApply">Apply</a>

            <div class="clear"></div>
        </div>

        <div class="collapsible" id="collapseBox${currentNode.identifier}">
            <div class="Form jobsApplyForm">
                <form action="${url.base}${currentNode.path}/*" method="post">
                    <input type="hidden" name="nodeType" value="jnt:jobApplication"/>
                    <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
                    <input type="hidden" name="newNodeOutputFormat" value="html"/>
                    <fieldset>
                        <p class="field">
                            <label class="left" for="job-application-firstname"><fmt:message key="jnt_jobApplication.firstname"/>:</label>
                            <input type="text" name="firstname" id="job-application-firstname"/>
                        </p>

                        <p class="field">
                            <label class="left" for="job-application-lastname"><fmt:message key="jnt_jobApplication.lastname"/>:</label>
                            <input type="text" name="lastname" id="job-application-lastname"/>
                        </p>

                        <p class="field">
                            <label class="left" for="job-application-email"><fmt:message key="jnt_jobApplication.email"/>:</label>
                            <input type="text" name="email" id="job-application-email"/>
                        </p>

                        <p class="field">
                            <label class="left" for="job-application-text"><fmt:message key="jnt_jobApplication.text"/>:</label>
                            <textarea name="text" id="job-application-text"></textarea>
                        </p>
                    <div class="formMarginLeft">
                    	<input type="submit" class="button" value="Apply"/>
                    </div>
                    </fieldset>

                </form>
            </div>
        </div>
    </div>
</div>