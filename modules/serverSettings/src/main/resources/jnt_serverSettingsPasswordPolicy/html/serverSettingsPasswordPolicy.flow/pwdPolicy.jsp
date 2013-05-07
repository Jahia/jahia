<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


<h2><fmt:message key="serverSettings.password.policies"/></h2>

<form action="${flowExecutionUrl}" method="post">
    <table class="table table-bordered table-hover table-striped">
        <thead>
        <tr>
            <th width="7%">
                <fmt:message key="serverSettings.password.policies.active"/>
            </th>
            <th width="50%">
                <fmt:message key="label.name"/>
            </th>
            <th width="43%">
                <fmt:message key="label.parameters"/>
            </th>
        </tr>
        </thead>
        <c:forEach items="${jahiaPasswordPolicy.rules}" var="rule" varStatus="rlzStatus">
            <tr>
                <td align="center">
                    <input type="checkbox" id="pwd-rule-${rlzStatus.index}" name="rules[<c:out value='${rlzStatus.index}'/>].active" value="true" ${rule.active ? 'checked="checked"' : ''}/>
                    <input type="hidden" name="_rules[<c:out value='${rlzStatus.index}'/>].active"/>
                </td>
                <c:set var="i18nKey" value='serverSettings.passwordPolicies.rule.${rule.name}'/>
                <td>
                    <label for="pwd-rule-${rlzStatus.index}"><fmt:message key='${i18nKey}'/></label>
                </td>
                <td>
                    <c:forEach items="${rule.conditionParameters}" var="condParam" varStatus="paramsStatus">
                            <c:set var="i18nKey" value='label.${condParam.name}'/>
                                <label for="rules_${paramsStatus.count}"><fmt:message key='${i18nKey}'/></label>
                                <input type="text" id="rules_${paramsStatus.count}" name="rules[<c:out value='${rlzStatus.index}'/>].conditionParameters[<c:out value='${paramsStatus.index}'/>].value" value="<c:out value='${condParam.value}'/>"/>
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </table>
    <button class="btn btn-primary" type="submit" name="_eventId_submitPwdPolicy">
        <i class="icon-ok icon-white"></i>
        &nbsp;<fmt:message key='label.save'/>
    </button>
</form>
