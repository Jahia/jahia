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

<%@ include file="../../common/declarations.jspf" %>

<div class="pressRealese"><!--start pressRealese -->
    <template:include page="modules/introduction/introductionDisplay.jsp"/>
    <div class="pressRealeseForm"><!--start pressRealeseForm -->
        <template:jahiaPageForm name="pressPageForm" method="get">
            <fieldset>
                <legend><fmt:message key="web_templates_pressContainer.legend"/></legend>
                <p class="pressdatefrom">

                <div><label><fmt:message key="web_templates_pressContainer.from"/> :</label>
                    <ui:dateSelector cssClassName="dateSelection" fieldName="pressdatefrom"
                                     value="${param.pressdatefrom}"/>
                </div>
                </p>
                <p class="pressdateto">

                <div><label><fmt:message key="web_templates_pressContainer.to"/> :</label>
                    <ui:dateSelector cssClassName="dateSelection" fieldName="pressdateto" value="${param.pressdateto}"/>
                </div>
                </p>
                <div class="divButton"><input type="submit" name="submit" id="submit" class="button"
                                              value='<fmt:message key="search"/>'
                                              tabindex="7"/>
                </div>
            </fieldset>
            <input type="hidden" name="pressContainer_press_windowsize" value="5">
        </template:jahiaPageForm>
    </div>
    <!--stop pressRealeseForm -->
    <template:containerList name="press" id="pressContainer" actionMenuNamePostFix="press"
                            actionMenuNameLabelKey="pressReleases" windowSize="10" sortByField="date"
                            enforceDefinedSort="true" sortOrder="desc">
        <c:set var="doSearch" value="false"/>
        <c:if test="${!empty param.pressdatefrom || !empty param.pressdateto }">
            <c:set var="doSearch" value="true"/>
        </c:if>
        <c:if test="${doSearch == 'true'}">
            <query:containerQuery>
                <query:selector nodeTypeName="web_templates:pressContainer" selectorName="pressSelector"/>
                <query:childNode selectorName="pressSelector" path="${pressContainer.JCRPath}"/>
                <c:if test="${!empty param.pressdatefrom}">
                    <utility:dateUtil currentDate="${param.pressdatefrom}" valueID="today" hours="0" minutes="0"
                                      seconds="0"/>
                    <c:if test="${today.time != -1}">
                        <query:greaterThanOrEqualTo numberValue="true" propertyName="date" value="${today.time}"/>
                    </c:if>
                </c:if>
                <c:if test="${!empty param.pressdateto}">
                    <utility:dateUtil currentDate="${param.pressdateto}" valueID="today" hours="0" minutes="0"
                                      seconds="0"/>
                    <c:if test="${today.time != -1}">
                        <query:lessThanOrEqualTo numberValue="true" propertyName="date" value="${today.time}"/>
                    </c:if>
                </c:if>
                <c:if test="${!empty param.pressword}">
                    <query:fullTextSearch searchExpression="${param.pressword}"/>
                </c:if>
                <query:sortBy propertyName="date" order="${queryConstants.ORDER_DESCENDING}"/>
            </query:containerQuery>
        </c:if>
        <ul class="pressRealeseList"><!--start pressRealeses List -->
            <%@ include file="pressDisplay.jspf" %>
        </ul>
        <!--stop pressRealeseList -->
    </template:containerList>
    <div class="clear"> </div>
</div>
<!--stop pressRealeses list -->