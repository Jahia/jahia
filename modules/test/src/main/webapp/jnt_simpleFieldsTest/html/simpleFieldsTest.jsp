<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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

<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>link
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


string : ${currentNode.properties.string.string}<br/>

long: ${currentNode.properties.long.string}<br/>

double: ${currentNode.properties.double.string}<br/>

<c:choose>
    <c:when test="${currentNode.properties.boolean.boolean}">
        the propertye boolean return True<br/>
    </c:when>
    <c:when test="${!currentNode.properties.boolean.boolean}">
        the propertye boolean return False<br/>
    </c:when>
    <c:otherwise>
        the propertye boolean was not set<br/>
    </c:otherwise>
</c:choose>

string with default: ${currentNode.properties.stringWithDefault.string}<br/>

string with initializer is Text: ${currentNode.properties.stringText.string}<br/>

string with initializer is Text(multiline): ${currentNode.properties.stringTextMultiline.string}<br/>

string with initializer is Richtext: ${currentNode.properties.stringRichText.string}<br/>

mandatory string: ${currentNode.properties.mandatoryString.string}<br/>

nofulltext string: ${currentNode.properties.nofulltextString.string}<br/>

alphanumeric string ((string) < '[a-zA-Z1-9]*'): ${currentNode.properties.alphanumericString.string}<br/>

integer between 1 and 10 ((long) < '[1,10]'): ${currentNode.properties.longBetween1and10.string}<br/>


multiple strings:
<jcr:nodeProperty node="${currentNode}" name= "multipleString" var="multiStrings"/>
<c:forEach items="${multiStrings}" var="i"><br/>
	${i.string}<br/>
</c:forEach><br/>




