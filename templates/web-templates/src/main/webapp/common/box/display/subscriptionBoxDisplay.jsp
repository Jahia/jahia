<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
<%@ include file="../../declarations.jspf" %>
<template:containerList id="subscriptionList" displayActionMenu="false">
     <query:containerQuery>
        <query:selector nodeTypeName="web_templates:newsletterIssue" selectorName="subscrList"/>
        <query:descendantNode selectorName="subscrList" path="${currentSite.JCRPath}"/>
         <c:set var="newsletterPageList" value=""/>
    </query:containerQuery>
    <ul class="links">
    <template:container id="subscriptionContainer" displayActionMenu="false" displayContainerAnchor="false" cache="off">
        <c:if test="${!fn:contains(newsletterPageList, subscriptionContainer.parent.parent.key)}">
            <li class="link">
                <table>
                    <tr>
                        <td>${subscriptionContainer.parent.parent.title}</td>
                        <fmt:message key="web_templates_message.subscribe.confirmation" var="messageSubscribeConfirmation">
                            <fmt:param value="${subscriptionContainer.parent.parent.title}"/>
                        </fmt:message>
                        <fmt:message key="web_templates_message.unsubscribe.confirmation" var="messageUnsubscribeConfirmation">
                            <fmt:param value="${subscriptionContainer.parent.parent.title}"/>
                        </fmt:message>
                        <td><ui:subscribeButton source="${subscriptionContainer.parent.parent.key}" event="newsletter"
                                                messageSubscribeConfirmation="${messageSubscribeConfirmation}"
                                                messageUnsubscribeConfirmation="${messageUnsubscribeConfirmation}"

                                /></td>
                    </tr>
                </table>
            </li>
            <c:set var="newsletterPageList" value="${newsletterPageList}--${subscriptionContainer.parent.parent.key}"/>
        </c:if>
    </template:container>
    </ul>
</template:containerList>