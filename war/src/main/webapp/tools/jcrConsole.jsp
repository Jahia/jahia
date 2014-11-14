<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.Locale"%>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="javax.script.Bindings" %>
<%@ page import="javax.script.SimpleScriptContext" %>
<%@ page import="javax.script.ScriptContext" %>
<%@ page import="javax.script.ScriptEngine" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.Writer" %>
<%@ page import="javax.script.ScriptException" %>
<%@ page import="org.jahia.tools.patches.LoggerWrapper" %>
<%@ page import="org.jahia.utils.ScriptEngineUtils" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>JCR Console</title>
</head>
<c:set var="workspace" value="${functions:default(fn:escapeXml(param.workspace), 'default')}"/>
<c:set var="locale" value="${functions:default(fn:escapeXml(param.locale), 'en')}"/>
<%
Locale currentLocale = LanguageCodeConverters.languageCodeToLocale((String) pageContext.getAttribute("locale"));
pageContext.setAttribute("locales", LanguageCodeConverters.getSortedLocaleList(Locale.ENGLISH));
%>
<body>
<h1>JCR Console</h1>
<%
long timer = System.currentTimeMillis();
ScriptEngine engine = null;
try {
    engine = ScriptEngineUtils.getInstance().scriptEngine("groovy");
%>
<c:if test="${param.action == 'execute' && not empty param.script}">
<%
StringBuilder code = new StringBuilder(512);
code.append("import com.google.common.collect.*\n");
code.append("import com.google.common.io.*\n");
code.append("import com.sun.enterprise.web.connector.grizzly.comet.*\n");
code.append("import com.sun.grizzly.comet.*\n");
code.append("import com.sun.grizzly.tcp.*\n");
code.append("import com.sun.grizzly.websockets.*\n");
code.append("import com.sun.image.codec.jpeg.*\n");
code.append("import com.sun.medialib.mlib.*\n");
code.append("import com.sun.net.httpserver.*\n");
code.append("import com.sun.syndication.feed.synd.*\n");
code.append("import com.sun.syndication.fetcher.*\n");
code.append("import com.sun.syndication.fetcher.impl.*\n");
code.append("import com.sun.syndication.io.*\n");
code.append("import eu.infomas.annotation.*\n");
code.append("import groovy.lang.*\n");
code.append("import groovy.util.*\n");
code.append("import groovy.util.slurpersupport.*\n");
code.append("import groovy.xml.*\n");
code.append("import javax.annotation.security.*\n");
code.append("import javax.ejb.*\n");
code.append("import javax.enterprise.context.*\n");
code.append("import javax.enterprise.context.spi.*\n");
code.append("import javax.enterprise.event.*\n");
code.append("import javax.enterprise.inject.*\n");
code.append("import javax.enterprise.inject.spi.*\n");
code.append("import javax.enterprise.util.*\n");
code.append("import javax.inject.*\n");
code.append("import javax.interceptor.*\n");
code.append("import javax.jcr.*\n");
code.append("import javax.jcr.nodetype.*\n");
code.append("import javax.jcr.observation.*\n");
code.append("import javax.jcr.query.*\n");
code.append("import javax.jcr.query.qom.*\n");
code.append("import javax.jcr.version.*\n");
code.append("import javax.mail.*\n");
code.append("import javax.mail.internet.*\n");
code.append("import javax.mail.util.*\n");
code.append("import javax.persistence.*\n");
code.append("import javax.servlet.*\n");
code.append("import javax.servlet.annotation.*\n");
code.append("import javax.servlet.http.*\n");
code.append("import javax.servlet.resources.*\n");
code.append("import javax.validation.*\n");
code.append("import name.fraser.neil.plaintext.*\n");
code.append("import net.htmlparser.jericho.*\n");
code.append("import nu.xom.*\n");
code.append("import oauth.signpost.*\n");
code.append("import oauth.signpost.basic.*\n");
code.append("import oauth.signpost.commonshttp.*\n");
code.append("import oauth.signpost.exception.*\n");
code.append("import oauth.signpost.http.*\n");
code.append("import oracle.xml.parser.*\n");
code.append("import oracle.xml.parser.v2.*\n");
code.append("import org.aopalliance.aop.*\n");
code.append("import org.aopalliance.intercept.*\n");
code.append("import org.apache.camel.*\n");
code.append("import org.apache.camel.builder.*\n");
code.append("import org.apache.camel.component.mail.*\n");
code.append("import org.apache.camel.impl.*\n");
code.append("import org.apache.camel.model.*\n");
code.append("import org.apache.camel.spring.*\n");
code.append("import org.apache.camel.util.*\n");
code.append("import org.apache.catalina.connector.*\n");
code.append("import org.apache.catalina.util.*\n");
code.append("import org.apache.catalina.websocket.*\n");
code.append("import org.apache.commons.beanutils.*\n");
code.append("import org.apache.commons.codec.binary.*\n");
code.append("import org.apache.commons.codec.digest.*\n");
code.append("import org.apache.commons.collections.*\n");
code.append("import org.apache.commons.collections.iterators.*\n");
code.append("import org.apache.commons.collections.keyvalue.*\n");
code.append("import org.apache.commons.collections.list.*\n");
code.append("import org.apache.commons.collections.map.*\n");
code.append("import org.apache.commons.httpclient.*\n");
code.append("import org.apache.commons.httpclient.auth.*\n");
code.append("import org.apache.commons.httpclient.methods.*\n");
code.append("import org.apache.commons.httpclient.methods.multipart.*\n");
code.append("import org.apache.commons.httpclient.params.*\n");
code.append("import org.apache.commons.httpclient.protocol.*\n");
code.append("import org.apache.commons.id.*\n");
code.append("import org.apache.commons.lang.*\n");
code.append("import org.apache.commons.lang.builder.*\n");
code.append("import org.apache.commons.lang.exception.*\n");
code.append("import org.apache.commons.lang.math.*\n");
code.append("import org.apache.commons.lang.time.*\n");
code.append("import org.apache.commons.logging.*\n");
code.append("import org.apache.coyote.http11.upgrade.*\n");
code.append("import org.apache.jackrabbit.commons.query.*\n");
code.append("import org.apache.jackrabbit.util.*\n");
code.append("import org.apache.jackrabbit.value.*\n");
code.append("import org.apache.log4j.*\n");
code.append("import org.apache.oro.text.regex.*\n");
code.append("import org.apache.pdfbox.pdmodel.*\n");
code.append("import org.apache.pluto.container.*\n");
code.append("import org.apache.regexp.*\n");
code.append("import org.apache.solr.client.solrj.response.*\n");
code.append("import org.apache.tika.io.*\n");
code.append("import org.apache.tomcat.util.http.mapper.*\n");
code.append("import org.apache.tools.ant.*\n");
code.append("import org.apache.velocity.tools.generic.*\n");
code.append("import org.apache.xerces.dom.*\n");
code.append("import org.apache.xerces.jaxp.*\n");
code.append("import org.apache.xerces.parsers.*\n");
code.append("import org.artofsolving.jodconverter.document.*\n");
code.append("import org.artofsolving.jodconverter.office.*\n");
code.append("import org.codehaus.groovy.runtime.*\n");
code.append("import org.codehaus.groovy.runtime.typehandling.*\n");
code.append("import org.cyberneko.html.parsers.*\n");
code.append("import org.dom4j.*\n");
code.append("import org.dom4j.io.*\n");
code.append("import org.dom4j.tree.*\n");
code.append("import org.drools.*\n");
code.append("import org.drools.spi.*\n");
code.append("import org.drools.util.*\n");
code.append("import org.eclipse.jetty.continuation.*\n");
code.append("import org.eclipse.jetty.websocket.*\n");
code.append("import org.glassfish.grizzly.*\n");
code.append("import org.glassfish.grizzly.comet.*\n");
code.append("import org.glassfish.grizzly.filterchain.*\n");
code.append("import org.glassfish.grizzly.http.*\n");
code.append("import org.glassfish.grizzly.http.server.*\n");
code.append("import org.glassfish.grizzly.http.server.util.*\n");
code.append("import org.glassfish.grizzly.http.util.*\n");
code.append("import org.glassfish.grizzly.servlet.*\n");
code.append("import org.glassfish.grizzly.utils.*\n");
code.append("import org.glassfish.grizzly.websockets.*\n");
code.append("import org.hibernate.*\n");
code.append("import org.hibernate.cfg.*\n");
code.append("import org.hibernate.classic.*\n");
code.append("import org.hibernate.criterion.*\n");
code.append("import org.jahia.admin.*\n");
code.append("import org.jahia.admin.sites.*\n");
code.append("import org.jahia.ajax.gwt.client.widget.contentengine.*\n");
code.append("import org.jahia.ajax.gwt.client.widget.edit.sidepanel.*\n");
code.append("import org.jahia.ajax.gwt.client.widget.publication.*\n");
code.append("import org.jahia.ajax.gwt.client.widget.subscription.*\n");
code.append("import org.jahia.ajax.gwt.client.widget.toolbar.action.*\n");
code.append("import org.jahia.ajax.gwt.helper.*\n");
code.append("import org.jahia.ajax.gwt.utils.*\n");
code.append("import org.jahia.api.*\n");
code.append("import org.jahia.bin.*\n");
code.append("import org.jahia.bin.errors.*\n");
code.append("import org.jahia.data.*\n");
code.append("import org.jahia.data.applications.*\n");
code.append("import org.jahia.data.beans.portlets.*\n");
code.append("import org.jahia.data.templates.*\n");
code.append("import org.jahia.data.viewhelper.principal.*\n");
code.append("import org.jahia.defaults.config.spring.*\n");
code.append("import org.jahia.engines.*\n");
code.append("import org.jahia.exceptions.*\n");
code.append("import org.jahia.modules.visibility.rules.*\n");
code.append("import org.jahia.params.*\n");
code.append("import org.jahia.params.valves.*\n");
code.append("import org.jahia.pipelines.*\n");
code.append("import org.jahia.pipelines.valves.*\n");
code.append("import org.jahia.registries.*\n");
code.append("import org.jahia.security.license.*\n");
code.append("import org.jahia.services.*\n");
code.append("import org.jahia.services.applications.*\n");
code.append("import org.jahia.services.atmosphere.*\n");
code.append("import org.jahia.services.cache.*\n");
code.append("import org.jahia.services.channels.*\n");
code.append("import org.jahia.services.channels.providers.*\n");
code.append("import org.jahia.services.content.*\n");
code.append("import org.jahia.services.content.decorator.*\n");
code.append("import org.jahia.services.content.nodetypes.*\n");
code.append("import org.jahia.services.content.nodetypes.initializers.*\n");
code.append("import org.jahia.services.content.nodetypes.renderer.*\n");
code.append("import org.jahia.services.content.rules.*\n");
code.append("import org.jahia.services.image.*\n");
code.append("import org.jahia.services.importexport.*\n");
code.append("import org.jahia.services.logging.*\n");
code.append("import org.jahia.services.mail.*\n");
code.append("import org.jahia.services.notification.*\n");
code.append("import org.jahia.services.preferences.user.*\n");
code.append("import org.jahia.services.pwdpolicy.*\n");
code.append("import org.jahia.services.query.*\n");
code.append("import org.jahia.services.render.*\n");
code.append("import org.jahia.services.render.filter.*\n");
code.append("import org.jahia.services.render.filter.cache.*\n");
code.append("import org.jahia.services.render.scripting.*\n");
code.append("import org.jahia.services.scheduler.*\n");
code.append("import org.jahia.services.search.*\n");
code.append("import org.jahia.services.seo.*\n");
code.append("import org.jahia.services.seo.jcr.*\n");
code.append("import org.jahia.services.seo.urlrewrite.*\n");
code.append("import org.jahia.services.sites.*\n");
code.append("import org.jahia.services.tags.*\n");
code.append("import org.jahia.services.tasks.*\n");
code.append("import org.jahia.services.templates.*\n");
code.append("import org.jahia.services.transform.*\n");
code.append("import org.jahia.services.translation.*\n");
code.append("import org.jahia.services.uicomponents.bean.*\n");
code.append("import org.jahia.services.uicomponents.bean.contentmanager.*\n");
code.append("import org.jahia.services.uicomponents.bean.editmode.*\n");
code.append("import org.jahia.services.uicomponents.bean.toolbar.*\n");
code.append("import org.jahia.services.usermanager.*\n");
code.append("import org.jahia.services.usermanager.jcr.*\n");
code.append("import org.jahia.services.visibility.*\n");
code.append("import org.jahia.services.workflow.*\n");
code.append("import org.jahia.settings.*\n");
code.append("import org.jahia.tools.files.*\n");
code.append("import org.jahia.tools.jvm.*\n");
code.append("import org.jahia.utils.*\n");
code.append("import org.jahia.utils.comparator.*\n");
code.append("import org.jahia.utils.i18n.*\n");
code.append("import org.jahia.utils.zip.*\n");
code.append("import org.jaxen.*\n");
code.append("import org.jaxen.jdom.*\n");
code.append("import org.jbpm.api.activity.*\n");
code.append("import org.jbpm.api.model.*\n");
code.append("import org.jbpm.api.task.*\n");
code.append("import org.joda.time.*\n");
code.append("import org.joda.time.format.*\n");
code.append("import org.mortbay.util.ajax.*\n");
code.append("import org.quartz.*\n");
code.append("import org.springframework.aop.*\n");
code.append("import org.springframework.aop.framework.*\n");
code.append("import org.springframework.aop.support.*\n");
code.append("import org.springframework.beans.*\n");
code.append("import org.springframework.beans.factory.*\n");
code.append("import org.springframework.beans.factory.annotation.*\n");
code.append("import org.springframework.beans.factory.config.*\n");
code.append("import org.springframework.beans.factory.support.*\n");
code.append("import org.springframework.beans.factory.xml.*\n");
code.append("import org.springframework.beans.propertyeditors.*\n");
code.append("import org.springframework.context.*\n");
code.append("import org.springframework.context.event.*\n");
code.append("import org.springframework.context.support.*\n");
code.append("import org.springframework.core.*\n");
code.append("import org.springframework.core.enums.*\n");
code.append("import org.springframework.core.io.*\n");
code.append("import org.springframework.core.io.support.*\n");
code.append("import org.springframework.dao.*\n");
code.append("import org.springframework.jdbc.core.*\n");
code.append("import org.springframework.orm.*\n");
code.append("import org.springframework.orm.hibernate3.*\n");
code.append("import org.springframework.orm.hibernate3.annotation.*\n");
code.append("import org.springframework.orm.hibernate3.support.*\n");
code.append("import org.springframework.scheduling.quartz.*\n");
code.append("import org.springframework.ui.context.*\n");
code.append("import org.springframework.ui.context.support.*\n");
code.append("import org.springframework.util.*\n");
code.append("import org.springframework.util.xml.*\n");
code.append("import org.springframework.web.context.*\n");
code.append("import org.springframework.web.context.support.*\n");
code.append("import org.springframework.web.servlet.*\n");
code.append("import org.springframework.web.servlet.mvc.*\n");
code.append("import org.springframework.webflow.core.collection.*\n");
code.append("import sun.awt.image.*\n");
code.append("import sun.awt.image.codec.*\n");
code.append("import sun.security.action.*\n");
code.append("import ucar.nc2.util.net.*\n");
code.append("\n");
code.append("def log = log;\n");
code.append("def logger = log;\n");
code.append("JCRTemplate.getInstance().doExecuteWithSystemSession((JahiaUser) null, \"").append(pageContext.getAttribute("workspace")).append("\", LanguageCodeConverters.getLocaleFromCode(\"").append(pageContext.getAttribute("locale")).append("\"), new JCRCallback<Boolean>() {\n");
code.append("public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {\n");
code.append(request.getParameter("script"));
code.append("}\n");
code.append("});\n");

//LoggerFactory.getLogger("org.jahia.tools.groovyConsole").info(code.toString());

ScriptContext ctx = new SimpleScriptContext();
ctx.setWriter(new StringWriter());
Bindings bindings = engine.createBindings();
bindings.put("log", new LoggerWrapper(LoggerFactory.getLogger("org.jahia.tools.groovyConsole"), "org.jahia.tools.groovyConsole", ctx.getWriter()));
ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
engine.eval(code.toString(), ctx);
pageContext.setAttribute("result", ((StringWriter) ctx.getWriter()).getBuffer().toString());
pageContext.setAttribute("took", System.currentTimeMillis() - timer);
%>
<fieldset>
    <legend style="color: blue">Successfully executed in ${took} ms</legend>
    <p><strong>Result:</strong><br/>
    <pre>${not empty result ? fn:escapeXml(result) : '<empty>'}</pre>
    </p>
</fieldset>
</c:if>
<%
} catch (ScriptException e) {
    if (e instanceof ScriptException && e.getMessage() != null && e.getMessage().startsWith("Script engine not found for extension")) {
        %><p>Groovy engine is not available.</p><%
    } else {
        Throwable ex = e.getCause() != null ? e.getCause() : e;
        if (ex instanceof ScriptException && e.getCause() != null) {
            ex = ex.getCause();
        }
        pageContext.setAttribute("error", ex);
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        sw.flush();
        pageContext.setAttribute("stackTrace", sw.getBuffer().toString());
        %>
        <fieldset>
            <legend style="color: red">Error</legend>
            <p style="color: red">${fn:escapeXml(error)}</p>
            <a href="#show-stacktrace" onclick="var st=document.getElementById('stacktrace').style; st.display=st.display == 'none' ? '' : 'none'; return false;">show stacktrace</a>
            <pre id="stacktrace" style="display:none">${stackTrace}</pre>
        </fieldset>
        <%
    }
}
%>
<c:set var="switchToWorkspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
<fieldset>
    <legend>
        <strong>${workspace}</strong>&nbsp;workspace&nbsp;(<a href="#switchWorkspace" onclick="document.getElementById('workspace').value='${switchToWorkspace}'; document.getElementById('action').value=''; document.getElementById('groovyForm').submit(); return false;">switch to ${switchToWorkspace}</a>)
        <select name="localeSelector" onchange="document.getElementById('locale').value=this.value;">
            <c:forEach items="${locales}" var="loc">
                <% pageContext.setAttribute("localeLabel", ((Locale) pageContext.getAttribute("loc")).getDisplayName(Locale.ENGLISH)); %>
                <option value="${loc}"${loc == locale ? 'selected="selected"' : ''}>${fn:escapeXml(localeLabel)}</option>
            </c:forEach>
        </select>
    </legend>

    <form id="groovyForm" action="?" method="post">
    <input type="hidden" name="workspace" id="workspace" value="${workspace}"/>
    <input type="hidden" name="locale" id="locale" value="${locale}"/>
    <input type="hidden" name="action" id="action" value="execute"/>
    <p>Paste here the Groovy code you would like to execute in a JCRTemplate against JCR repository:</p>
    <p><textarea rows="25" style="width: 100%" id="text" name="script"
        onkeyup="if ((event || window.event).keyCode == 13 && (event || window.event).ctrlKey && confirm('WARNING: You are about to execute a script, which can manipulate the repository data or execute services in Jahia. Are you sure, you want to continue?')) document.getElementById('groovyForm').submit();">${param.script}</textarea></p>
    <p><input type="submit" value="Execute ([Ctrl+Enter])" onclick="if (!confirm('WARNING: You are about to execute a script, which can manipulate the repository data or execute services in Jahia. Are you sure, you want to continue?')) { return false; }" /></p>
    </form>
</fieldset>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>