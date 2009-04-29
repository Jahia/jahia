<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ include file="../declarations.jspf" %>
<template:containerList name="faq" id="faqList" actionMenuNamePostFix="faqs" actionMenuNameLabelKey="faqs.add">
<div class="faqSummary">
    <template:container id="faqContainer" cacheKey="onlyQuestions" actionMenuNamePostFix="faq"
                       actionMenuNameLabelKey="faq.update">
        <div class="faqSummaryItem">
        <template:field name="faqTitle" display="false" var="title"/>
        <h3>
            <a href="#<c:out value='${title}'/>"><c:out value="${title}"/></a>
        </h3>
        <template:containerList name="faqQA" id="faqQAList"
                               actionMenuNamePostFix="qas" actionMenuNameLabelKey="qas.add">
            <ol>
                <template:container id="qaContainer" cacheKey="onlyTheQuestion" displayActionMenu="false">
                    <li>
                        <ui:actionMenu contentObjectName="qaContainer" namePostFix="qa" labelKey="qa.update">
                            <a href="#ctn_<c:out value="${qaContainer.ID}"/>"><template:field name="question"/></a>
                        </ui:actionMenu>
                    </li>
                </template:container>
            </ol>
        </template:containerList>
    </div>
    </template:container>
</div>
</template:containerList>

<template:containerList name="faq" id="faqList" displayActionMenu="false">
<div class="topicItem">
    <template:container id="faqContainer" cacheKey="allFields" displayActionMenu="false">
        <template:field name="faqTitle" display="false" var="title"/>
        <h3 id="<c:out value='${title}'/>"><c:out value="${title}"/></h3>
        <template:containerList name="faqQA" id="faqQAList" displayActionMenu="false">
            <ol>
                <template:container id="qaContainer" cacheKey="questionAndAnswer" actionMenuNamePostFix="qa" actionMenuNameLabelKey="qa.update">
                    <li id="ctn_<c:out value="${qaContainer.ID}"/>">
                        <h5 class="question"><strong><fmt:message key='question'/></strong>
                        <template:field name="question"/></h5>
                    

                    <div class="answer">
                        <strong><fmt:message key='answer'/></strong>
                        <template:field name="answer"/></div>
                    </li>
                </template:container>
            </ol>
        </template:containerList>
    </template:container>
</div>    
</template:containerList>