<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Groovy Console</title>
</head>
<body>
<h1>Groovy Console</h1>
<%
long timer = System.currentTimeMillis();
ScriptEngine engine = null;
try {
    engine = ScriptEngineUtils.getInstance().scriptEngine("groovy");
%>
<c:if test="${not empty param.script}">
<%
ScriptContext ctx = new SimpleScriptContext();
ctx.setWriter(new StringWriter());
Bindings bindings = engine.createBindings();
bindings.put("log", new LoggerWrapper(LoggerFactory.getLogger("org.jahia.tools.groovyConsole"), "org.jahia.tools.groovyConsole", ctx.getWriter()));
ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
engine.eval(request.getParameter("script"), ctx);
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
<form id="groovyForm" action="?" method="post">
<p>Paste here the Groovy code you would like to execute against Jahia:</p>
<p><textarea rows="25" style="width: 100%" id="text" name="script"
    onkeyup="if ((event || window.event).keyCode == 13 && (event || window.event).ctrlKey && confirm('WARNING: You are about to execute a script, which can manipulate the repository data or execute services in Jahia. Are you sure, you want to continue?')) document.getElementById('groovyForm').submit();">${param.script}</textarea></p>
<p><input type="submit" value="Execute ([Ctrl+Enter])" onclick="if (!confirm('WARNING: You are about to execute a script, which can manipulate the repository data or execute services in Jahia. Are you sure, you want to continue?')) { return false; }" /></p>
</form>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>