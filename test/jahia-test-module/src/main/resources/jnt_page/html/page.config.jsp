<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set var="configValues" value="${functions:getConfigValues('org.jahia.modules.test')}"/>

<p>configValues.configKey1=${configValues.configKey1}</p>
<p>configValues.configKey2=${configValues.configKey2}</p>

<c:set var="configKey1Value" value="${functions:getConfigValue('org.jahia.modules.test', 'configKey1')}"/>
<p>configKey1=${configKey1Value}</p>

<c:set var="defaultFactoryConfigs" value="${functions:getConfigFactoryValues('org.jahia.modules.test.factory', 'default')}"/>
<p>defaultFactoryConfigs.configKey1=${defaultFactoryConfigs.configKey1}</p>
<p>defaultFactoryConfigs.configKey2=${defaultFactoryConfigs.configKey2}</p>

<c:set var="configPIDs" value="${functions:getConfigPids()}"/>

<h1>All Config PIDs</h1>
<p>allConfigPIDs=${configPIDs}</p>

<c:set var="testModuleFactoryIdentifiers" value="${functions:getConfigFactoryIdentifiers('org.jahia.modules.test.factory')}"/>

<h1>Test Module Factory Config Identifiers</h1>
<p>testModuleFactoryIdentifiers=${testModuleFactoryIdentifiers}</p>

<c:set var="complexConfigValues" value="${functions:getConfigValues('org.jahia.modules.test.complex')}"/>

<p>complexConfigValues=${complexConfigValues}</p>
