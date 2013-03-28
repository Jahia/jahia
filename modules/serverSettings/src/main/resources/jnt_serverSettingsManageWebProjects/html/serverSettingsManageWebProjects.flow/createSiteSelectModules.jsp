<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>


<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
    <div class="validationError">
        <ul>
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <li>${error.text}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <fieldset>
            <div>
                <label for="templateSet"><fmt:message key="serverSettings.webProjectSettings.pleaseChooseTemplateSet"/></label>

                <select name="templateSet" id="templateSet">
                    <c:forEach items="${allModules}" var="module">
                        <c:if test="${module.moduleType eq 'templatesSet'}">
                        <option value="${module.rootFolder}" ${siteBean.templateSet eq module.rootFolder ? 'selected="true"' : ''}>${module.name}&nbsp;(${module.rootFolder})</option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>

            <div>
                <fmt:message key="serverSettings.manageWebProjects.webProject.selectModules"/>
                <br/>
                <input type="hidden" name="_modules"/>
                <c:forEach items="${allModules}" var="module">
                    <c:if test="${module.moduleType ne 'templatesSet' && module.moduleType ne 'system'}">
                        <input type="checkbox" name="modules" id="${module.rootFolder}" value="${module.rootFolder}" ${functions:contains(siteBean.modules,module.rootFolder) ? 'checked="true"' : ''} />
                        <label for="${module.rootFolder}">${module.name} (${module.rootFolder})</label>
                        <br/>
                    </c:if>
                </c:forEach>
            </div>


            <div>
                <label for="language"><fmt:message key="serverSettings.manageWebProjects.webProject.selectLanguage"/></label>

                <select name="language" id="language">
                    <c:forEach items="${allLocales}" var="locale">
                        <option value="${locale}" ${siteBean.language eq locale ? 'selected="true"' : ''}>${locale.displayName}</option>
                    </c:forEach>
                </select>
            </div>


        </fieldset>

    <input type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
</form>
