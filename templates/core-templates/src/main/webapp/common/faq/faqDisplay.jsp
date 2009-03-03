<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../declarations.jspf" %>
<template:containerList name="faq" id="faqList" actionMenuNamePostFix="faqs" actionMenuNameLabelKey="faqs.add">
    <template:container id="faqContainer" cacheKey="onlyQuestions" actionMenuNamePostFix="faq"
                        actionMenuNameLabelKey="faq.update">
        <template:field name="faqTitle" display="false" valueBeanID="title"/>
        <h4>
            <a href="#<c:out value="${title}"/>"><c:out value="${title}"/></a>
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
        <template:field name="faqTitle" display="false" valueBeanID="title"/>
        <h3 id="<c:out value="${title}"/>"><c:out value="${title}"/></h3>

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