<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import="org.apache.log4j.Level" %>
<%@ page import="org.apache.log4j.LogManager" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Arrays" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<% long beginPageLoadTime = System.currentTimeMillis();%>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Log4j Administration</title>
    <style type="text/css">

        <!--
        #content {
            margin: auto;
            padding: 0px;
            font-family: Arial,Helvetica,sans-serif;
            text-align: center;
            background-color: #ccc;

            border: 1px solid #000;
            width: 80%;
        }

        body {
            position: relative;
            margin: 10px;

            padding: 0px;
            color: #333;
        }

        h1 {
            margin-top: 20px;
            font: 1.5em Verdana, Arial, Helvetica sans-serif;
        }

        h2 {
            margin-top: 10px;
            font: 0.75em Verdana, Arial, Helvetica sans-serif;
            text-align: left;
        }

/*
        a, a:link, a:visited, a:active {
            color: red;
            text-decoration: none;
            text-transform: uppercase;
        }
*/        

        table {
            width: 100%;
            background-color: #000;
            padding: 3px;
            border: 0px;
        }

        th {
            font-size: 0.75em;
            background-color: #ccc;
            color: #000;
            padding-left: 5px;
            text-align: center;
            border: 1px solid #ccc;
            white-space: nowrap;

        }

        td {
            font-size: 0.75em;
            background-color: #fff;
            white-space: nowrap;

        }

        td.center {
            font-size: 0.75em;
            background-color: #fff;
            text-align: center;

            white-space: nowrap;
        }

        .filterForm {

            font-size: 0.9em;
            background-color: #000;
            color: #fff;
            padding-left: 5px;
            text-align: left;
            border: 1px solid #000;

            white-space: nowrap;
        }

        .filterText, .filterText2 {

            font-size: 0.75em;
            background-color: #fff;
            color: #000;
            text-align: left;

            border: 1px solid #ccc;
            white-space: nowrap;
        }
        
        .filterButton {
            font-size: 0.75em;

            background-color: #000;
            color: #fff;

            padding-left: 5px;
            padding-right: 5px;

            text-align: center;
            border: 1px solid #ccc;

            width: 100px;
            white-space: nowrap;
        }
        
        span.active-level {
        	color: green;
        }
        -->
    </style>
</head>
<body onLoad="javascript:document.logFilterForm.logNameFilter.focus();">
<img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
<a href="<c:url value='/tools/index.jsp'/>" style="color:#36393D; font-family: Arial,Helvetica,sans-serif; font-size: 80%; line-height:100%;">to Jahia Tools overview</a>

<%
    String containsFilter = "Contains";
    String beginsWithFilter = "Begins With";

    String[] logLevels = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};
    String targetOperation = (String) request.getParameter("operation");
    String targetLogger = (String) request.getParameter("logger");
    String targetLogLevel = (String) request.getParameter("newLogLevel");
    String logNameFilter = (String) request.getParameter("logNameFilter");
    String logNameFilterType = (String) request.getParameter("logNameFilterType");

    pageContext.setAttribute("logLevels", logLevels);
%>
<div id="content">
<h1>Log4j Administration</h1>
<p>Please note that these settings are valid only during server run time and are not persisted between server restarts.</p>
<div class="filterForm">

    <form action="" name="logFilterForm">Filter Loggers:&nbsp;&nbsp;
        <input name="logNameFilter" type="text" size="50" value="<%=(logNameFilter == null ? "":logNameFilter)%>" class="filterText"/>
        
        <input name="logNameFilterType" type="submit" value="<%=beginsWithFilter%>" class="filterButton"/>&nbsp;

        <input name="logNameFilterType" type="submit" value="<%=containsFilter%>" class="filterButton"/>&nbsp;

        <input name="logNameClear" type="submit" value="Clear" class="filterButton" onclick='javascript:document.logFilterForm.logNameFilter.value=""; return true;'/>
        <input name="logNameReset" type="reset" value="Reset" class="filterButton"/>

        <param name="operation" value="changeLogLevel"/>
        
        <br/>
        Add logger:&nbsp;&nbsp;
        <input name="logger" type="text" size="50" value="" class="filterText"/>
        &nbsp;
        <select name="newLogLevel">
            <c:forEach items="${logLevels}" var="level">
                <option value="${level}"${level == 'DEBUG' ? ' selected="selected"' : ''}>${level}</option>
            </c:forEach>
        </select>&nbsp;
        <input name="operation" type="submit" value="Add" class="filterButton"/>
    </form>
</div>

<table cellspacing="1" cellpadding="3">
    <tr>
        <th width="35%">Logger</th>
        <th width="15%">Parent Logger</th>
        <th width="15%">Effective Level</th>
        <th width="35%">Change Log Level To</th>
    </tr>

    <c:if test="${param.operation == 'Add' && not empty param.logger}">
        <%
        LogManager.getLogger(targetLogger).setLevel(Level.toLevel(targetLogLevel));
        %>
    </c:if>
    <%
        Enumeration loggers = LogManager.getCurrentLoggers();

        HashMap loggersMap = new HashMap(128);
        Logger rootLogger = LogManager.getRootLogger();

        if (!loggersMap.containsKey(rootLogger.getName())) {

            loggersMap.put(rootLogger.getName(), rootLogger);
        }

        while (loggers.hasMoreElements()) {
            Logger logger = (Logger) loggers.nextElement();

            if (logNameFilter == null || logNameFilter.trim().length() == 0) {

                loggersMap.put(logger.getName(), logger);
            } else if (containsFilter.equals(logNameFilterType)) {

                if (logger.getName().toUpperCase().indexOf(logNameFilter.toUpperCase()) >= 0) {

                    loggersMap.put(logger.getName(), logger);
                }

            } else {
// Either was no filter in IF, contains filter in ELSE IF, or begins with in ELSE
                if (logger.getName().startsWith(logNameFilter)) {

                    loggersMap.put(logger.getName(), logger);
                }

            }
        }
        Set loggerKeys = loggersMap.keySet();

        String[] keys = new String[loggerKeys.size()];

        keys = (String[]) loggerKeys.toArray(keys);

        Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < keys.length; i++) {

            Logger logger = (Logger) loggersMap.get(keys[i]);

// MUST CHANGE THE LOG LEVEL ON LOGGER BEFORE GENERATING THE LINKS AND THE
// CURRENT LOG LEVEL OR DISABLED LINK WON'T MATCH THE NEWLY CHANGED VALUES
            if ("changeLogLevel".equals(targetOperation) && targetLogger.equals(logger.getName())) {

                Logger selectedLogger = (Logger) loggersMap.get(targetLogger);

                selectedLogger.setLevel(Level.toLevel(targetLogLevel));
            }

            String loggerName = null;
            String loggerEffectiveLevel = null;
            String loggerParent = null;
            if (logger != null) {
                loggerName = logger.getName();
                loggerEffectiveLevel = String.valueOf(logger.getEffectiveLevel());
                loggerParent = (logger.getParent() == null ? "-" : logger.getParent().getName());

            }
    %>
    <tr>
        <td align="left"><%=loggerName%></td>

        <td align="left"><%=loggerParent%></td>
        
        <td>
        	<% if (loggerName.equals(targetLogger)) {%>
        	<span class="active-level"><%=loggerEffectiveLevel%></span>
        	<% } else {%><%=loggerEffectiveLevel%><% } %>
        </td>
        <td class="center">
            <%
                for (int cnt = 0; cnt < logLevels.length; cnt++) {

                    String url = "?operation=changeLogLevel&logger=" + loggerName + "&newLogLevel=" + logLevels[cnt] + "&logNameFilter=" + (logNameFilter != null ? logNameFilter : "") + "&logNameFilterType=" + (logNameFilterType != null ? logNameFilterType : "");

                    if (logger.getLevel() == Level.toLevel(logLevels[cnt]) || logger.getEffectiveLevel() == Level.toLevel(logLevels[cnt])) {

            %>
            <span class="active-level">[<%=logLevels[cnt].toUpperCase()%>]</span>&nbsp;
            <%
            } else {
            %>
            <a href='<%=url%>'>[<%=logLevels[cnt]%>]</a>&nbsp;
            <%
                    }
                }
            %>
        </td>
    </tr>

    <%
        }
    %>
</table>
<h2>
	Source: <a href="http://ananthkannan.blogspot.com/2009/10/how-to-change-log-levels-on-fly-using.html" target="_blank" style="text-transform: none;">Ananth Kannan's blog</a><br/>
    Revision: 1.1 (Jahia)<br/>
    Page load time: <%=(System.currentTimeMillis() - beginPageLoadTime)%> ms
</h2>
</div>
</body>
</html> 