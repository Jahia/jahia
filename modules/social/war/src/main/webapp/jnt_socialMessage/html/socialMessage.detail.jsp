<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="fromUser" value="${currentNode.properties['j:from'].node}"/>
<div class="boxsocial userMessagesDetail" id="social-message-detail-${currentNode.identifier}"><!--start boxsocial -->
    <div class="boxsocialpadding16 boxsocialmarginbottom16">
        <div class="boxsocial-inner">
            <div class="boxsocial-inner-border">
                <ul class="messageActionList">
                   <li><a class="messageActionDelete" title="<fmt:message key='deleteMessage'/>" href="#delete" rel="${currentNode.identifier}"><span><fmt:message
                        key="deleteMessage"/></span></a></li>
                    <li><a class="messageActionReply" title="<fmt:message key='replyToMessage'/>" href="#divSendMessage" rel="details-${fromUser.name}|${fn:escapeXml(currentNode.propertiesAsString['j:subject'])}"><span><fmt:message key="replyToMessage"/></span></a></li>
                </ul>
                <div class='image'>
                    <div class='itemImage itemImageLeft'>
                        <a href="${url.base}${fromUser.path}.html"><img src="${url.currentModule}/images/friendbig.png"
                                                                         alt="friend" border="0"/></a>
                    </div>
                </div>
                <h5 class="messageSenderName">
                    <a href="${usl.base}${fromUser.path}.html"><c:out value="${jcr:userFullName(fromUser)}"/></a>
                </h5><jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/><span class="timestamp"><fmt:formatDate
value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span>
                <h5>${fn:escapeXml(currentNode.propertiesAsString['j:subject'])}</h5>
                <p>${fn:escapeXml(currentNode.propertiesAsString['j:body'])}</p>

            <div class='clear'></div>
			</div>
		</div>
	</div>
	<div class='clear'></div>
</div>
