<%@ page import="org.jahia.bin.Jahia" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

    <h2>
            <fmt:message key='<%= "serverSettings.aboutJahia.LicenceInfo.jahiaEdition." + Jahia.getEdition().toLowerCase() %>'/>&nbsp;Jahia&nbsp;xCM&nbsp;<%= Jahia.VERSION %>&nbsp;<fmt:message key="serverSettings.aboutJahia.build"/>&nbsp;<%= Jahia.getBuildNumber() %>
            <c:if test="${ licensePackage.customTermsAndConditions eq 'true' }">
                / <fmt:message key="serverSettings.aboutJahia.LicenceInfo.customTermsAndConditionsNotice" />
            </c:if>
    </h2>

    <pre class="text-center"><%=Jahia.getLicenseText()%></pre>
