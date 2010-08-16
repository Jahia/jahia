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
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<div class="pressRealese"><!--start pressRealese -->
    <div class="pressRealeseForm"><!--start pressRealeseForm -->
        <form action="${url.base}${renderContext.mainResource.node.path}.html" method="get">
            <fieldset>
                <legend><fmt:message key="press.search"/></legend>
                <div class="pressdatefrom">

                    <div><label><fmt:message key="date.from"/> :</label>
                        date time selector here <%-- ui:dateTimeSelector cssClassName="dateSelection" fieldName="pressdatefrom"
                                         value="${param.pressdatefrom}" --%>
                    </div>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
                <div class="pressdateto">

                    <div><label><fmt:message key="date.to"/> :</label>
                        date time selector here <%-- ui:dateTimeSelector cssClassName="dateSelection" fieldName="pressdateto"
                                         value="${param.pressdateto}" --%>
                    </div>
                    <div class="clear"></div>
                </div>
                <div class="divButton"><input type="submit" name="submit" id="submit" class="button"
                                              value='<fmt:message key="search"/>'
                                              tabindex="7"/>
                </div>
            </fieldset>
            <input type="hidden" name="pressContainer_press_windowsize" value="5"/>
        </form>
    </div>
    <!--stop pressRealeseForm -->
    <c:set var="doSearch" value="false"/>
    <c:if test="${!empty param.pressdatefrom || !empty param.pressdateto }">
        <c:set var="doSearch" value="true"/>
    </c:if>
    <jcr:jqom var="results">
        <query:selector nodeTypeName="jnt:press"/>
        <jcr:nodeProperty node="${currentNode}" name="searchPath" var="path"/>
        <query:descendantNode path="${path.node.path}"/>
        <c:if test="${doSearch == 'true'}">

            <c:if test="${!empty param.pressdatefrom}">
                <utility:dateUtil currentDate="${param.pressdatefrom}" var="today" hours="0" minutes="0"
                                  seconds="0"/>
                <c:if test="${today.time != -1}">
                    <query:greaterThanOrEqualTo propertyName="date" value="${today.time}"/>
                </c:if>
            </c:if>
            <c:if test="${!empty param.pressdateto}">
                <utility:dateUtil currentDate="${param.pressdateto}" var="today" hours="0" minutes="0"
                                  seconds="0"/>
                <c:if test="${today.time != -1}">
                    <query:lessThanOrEqualTo propertyName="date" value="${today.time}"/>
                </c:if>
            </c:if>
            <c:if test="${!empty param.pressword}">
                <query:fullTextSearch searchExpression="${param.pressword}"/>
            </c:if>
        </c:if>
        <query:sortBy propertyName="date" order="desc"/>
    </jcr:jqom>
    <ul class="pressRealeseList"><!--start pressRealeses List -->
        <c:forEach items="${results.nodes}" var="node">
            <li class="pressRealeseItem">
                <template:module node="${node}" template="default"/>
            </li>
        </c:forEach>
    </ul>
    <!--stop pressRealeseList -->
    <div class="clear"></div>
</div>