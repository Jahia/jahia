<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<form action="${flowExecutionUrl}" method="POST">

        <fieldset>
            <div>
                <label for="templateSet">Please choose a template set</label>

                <select name="templateSet" id="templateSet">
                    <c:forEach items="${allModules}" var="module">
                        <c:if test="${module.moduleType eq 'templatesSet'}">
                        <option value="${module.rootFolder}" ${siteBean.templateSet eq module.rootFolder ? 'selected="true"' : ''}>${module.name}</option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>

            <div>
                Choose modules to be deployed
                <br/>
                <input type="hidden" name="_modules"/>
                <c:forEach items="${allModules}" var="module">
                    <c:if test="${module.moduleType ne 'templatesSet'}">
                        <input type="checkbox" name="modules" id="${module.rootFolder}" value="${module.rootFolder}" ${functions:contains(siteBean.modules,module.rootFolder) ? 'checked="true"' : ''} />
                        <label for="${module.rootFolder}">${module.name}</label>
                        <br/>
                    </c:if>
                </c:forEach>
            </div>


            <div>
                <label for="language">Select the Web project default language</label>

                <select name="language" id="language">
                    <c:forEach items="${allLocales}" var="locale">
                        <option value="${locale}" ${siteBean.language eq locale ? 'selected="true"' : ''}>${locale.displayName}</option>
                    </c:forEach>
                </select>
            </div>


        </fieldset>

    <input type="submit" name="_eventId_previous" value="<fmt:message key='serverSettings.manageModules.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='serverSettings.manageModules.next'/>"/>
</form>
