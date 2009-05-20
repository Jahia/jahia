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

<%@ include file="../../common/declarations.jspf" %>
<template:containerList name="faq" id="faqList" actionMenuNamePostFix="faqs" actionMenuNameLabelKey="faqs.add">
    <template:container id="faqContainer" cacheKey="onlyQuestions" actionMenuNamePostFix="faq"
                        actionMenuNameLabelKey="faq.update">
        <template:field name="faqTitle" display="false" var="title"/>
        <h4>
            <a href="#<c:out value='${title}'/>"><c:out value="${title}"/></a>
        </h4>
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
    </template:container>
</template:containerList>
<hr/>
<template:containerList name="faq" id="faqList" displayActionMenu="false">
    <template:container id="faqContainer" cacheKey="allFields" displayActionMenu="false">
        <template:field name="faqTitle" display="false" var="title"/>
        <h3 id="<c:out value='${title}'/>"><c:out value="${title}"/></h3>

        <template:containerList name="faqQA" id="faqQAList" displayActionMenu="false">
            <ol>
                <template:container id="qaContainer" cacheKey="questionAndAnswer" actionMenuNamePostFix="qa"
                                    actionMenuNameLabelKey="qa.update">
                    <h5 id="ctn_<c:out value="${qaContainer.ID}"/>" class="question">
                        <fmt:message key='question'/>:
                        <template:field name="question"/>
                    </h5>

                    <p class="answer">
                        <strong><fmt:message key='answer'/></strong>:
                        <template:field name="answer"/>
                    </p>
                </template:container>
            </ol>
        </template:containerList>
    </template:container>
</template:containerList>