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
code.append("import java.util.*\n");

code.append("import javax.jcr.*\n");
code.append("import javax.jcr.query.*\n");
code.append("import javax.jcr.version.*\n");

code.append("import org.apache.commons.lang.StringUtils\n");

code.append("import org.jahia.services.content.*\n");
code.append("import org.jahia.services.usermanager.*\n");
code.append("import org.jahia.utils.LanguageCodeConverters\n");

code.append("\n");
code.append("def log = log;\n");
code.append("def logger = log;\n");
code.append("JCRTemplate.getInstance().doExecuteWithSystemSession(null, \"").append(pageContext.getAttribute("workspace")).append("\", LanguageCodeConverters.getLocaleFromCode(\"").append(pageContext.getAttribute("locale")).append("\"), new JCRCallback<Boolean>() {\n");
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
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>