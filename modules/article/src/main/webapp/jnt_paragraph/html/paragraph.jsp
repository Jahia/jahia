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
<jcr:nodeProperty node="${currentNode}" name="insertPosition" var="insertPosition"/>
<jcr:nodeProperty node="${currentNode}" name="insertType" var="insertType"/>
<jcr:nodeProperty node="${currentNode}" name="insertWidth" var="insertWidth"/>
<jcr:nodeProperty node="${currentNode}" name="insertText" var="insertText"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<h3><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
<c:if test="${not empty insertText}">
<div class='${insertType.string}-top float${insertPosition.string}'
     style='width:${insertWidth.string}px'>

    <div class="${insertType.string}-bottom">
        ${insertText.string}
    </div>
</div>
</c:if>
<div class="float${currentNode.properties.align.string}">
    <c:if test="${!empty image}">
        <img src="${image.node.url}" alt="${image.node.url}" align="${currentNode.properties.align.string}"/>
    </c:if>
</div>
<div>
    ${currentNode.properties.body.string}
</div>
<div class="clear"></div>
