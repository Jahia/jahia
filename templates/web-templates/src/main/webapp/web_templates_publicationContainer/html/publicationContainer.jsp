<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
	<jcr:nodeProperty node="${currentNode}" name='file' var="file"/>
	<jcr:nodeProperty node="${currentNode}" name='preview' var="preview"/>
	<jcr:nodeProperty node="${currentNode}" name='author' var="author"/>
	<jcr:nodeProperty node="${currentNode}" name='source' var="source"/>
	<jcr:nodeProperty node="${currentNode}" name='date' var="date"/>

	<div class="publicationListItem"><!--start publicationListItem -->
		<div class="publicationListSpace"><!--start publicationListSpace -->
			<div class="publicationPhoto">
                <a href="${file.node.url}" >
                    <c:if test="${not empty preview}">
                        <img src="${preview.node.url}" alt="${preview.node.propertiesAsString['jcr:title']}">
                    </c:if>
                    <c:if test="${empty preview}">
                    <img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/no_preview.png'/>" alt="no preview"/>
                    </c:if>
                </a>
            </div>

				<div class="publicationBody"><!--start publicationBody -->
					    <h5><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
					    <p class="publicationAuthor"><c:if test="${!empty author}"><fmt:message key="web_templates_publicationContainer.author"/>: ${author.string}</c:if></p>
					    <p class="publicationSource"><c:if test="${!empty source}"><fmt:message key="web_templates_publicationContainer.source"/>: ${source.string}</c:if></p>
					    <p class="publicationDate"><c:if test="${!empty date && date !=''}"> </c:if></p>
					    <div class="publicationDescription"><jcr:nodeProperty node="${currentNode}" name="body"/></div>
					    <div class="publicationAction">
					        <c:if test="${file.node.fileContent.contentLength > 0}">
                                <fmt:formatNumber var="num" pattern="### ### ###.##" type="number" value="${(file.node.fileContent.contentLength/1024)}"/>
                                <a class="publicationDownload" href="${file.node.url}" ><fmt:message key="web_templates_publicationContainer.download"/></a><span class="publicationDocSize">(${num} KB)</c:if>
                        </span>
					</div>
					<div class="clear"> </div>
				</div><!--stop publicationBody -->

			<div class="clear"> </div>
		</div><!--stop publicationListSpace -->
		<div class="clear"> </div>
	</div><!--stop publicationListItem -->
    <c:choose>
        <c:when test="${(renderContext.moduleParams.loop mod 2) == 1}">
           <div class="clear"> </div>
        </c:when>
    </c:choose>