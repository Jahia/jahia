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
<%@ include file="../../common/declarations.jspf" %>

<div class="pressRealese"><!--start pressRealese -->
    <template:include page="modules/introduction/introductionDisplay.jsp"/>
    <div class="pressRealeseForm"><!--start pressRealeseForm -->
        <template:jahiaPageForm name="pressPageForm" method="get">
            <fieldset>
                <legend><fmt:message key="web_templates_pressContainer.legend"/></legend>
                <div class="pressdatefrom">

                <div><label><fmt:message key="web_templates_pressContainer.from"/> :</label>
                    <ui:dateSelector cssClassName="dateSelection" fieldName="pressdatefrom"
                                     value="${param.pressdatefrom}"/>
                </div>
                <div class="clear"> </div></div>
                <div class="clear"> </div><div class="pressdateto">

                <div><label><fmt:message key="web_templates_pressContainer.to"/> :</label>
                    <ui:dateSelector cssClassName="dateSelection" fieldName="pressdateto" value="${param.pressdateto}"/>
                </div>
                <div class="clear"> </div></div>
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
                            sortOrder="desc">
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