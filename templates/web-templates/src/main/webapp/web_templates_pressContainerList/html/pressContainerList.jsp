<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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
                <legend><fmt:message key="web_templates_pressContainer.legend"/></legend>
                <div class="pressdatefrom">

                    <div><label><fmt:message key="web_templates_pressContainer.from"/> :</label>
                        <ui:dateSelector cssClassName="dateSelection" fieldName="pressdatefrom"
                                         value="${param.pressdatefrom}"/>
                    </div>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
                <div class="pressdateto">

                    <div><label><fmt:message key="web_templates_pressContainer.to"/> :</label>
                        <ui:dateSelector cssClassName="dateSelection" fieldName="pressdateto"
                                         value="${param.pressdateto}"/>
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
        <query:selector nodeTypeName="web_templates:pressContainer" selectorName="pressSelector"/>
        <query:descendantNode selectorName="pressSelector" path="${currentNode.path}"/>
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
        <query:sortBy propertyName="date" order="desc" selectorName="pressSelector"/>
    </jcr:jqom>
    <ul class="pressRealeseList"><!--start pressRealeses List -->
        <c:forEach items="${results.nodes}" var="node">
            <template:module node="${node}" template="${currentResource.resolvedTemplate}" nodeTypes="web_templates:pressContainer"/>
        </c:forEach>
    </ul>
    <!--stop pressRealeseList -->
    <div class="clear"></div>
</div>