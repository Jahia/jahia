<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.jahia.services.content.JCRSessionWrapper" buffer="16kb" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <script type="text/javascript" src="<c:url value='/modules/jquery/javascript/jquery.min.js'/>"></script>
    <title>JCR Sessions</title>
</head>
<body>
<h1>JCR Sessions</h1>

<p>There are currently <%= JCRSessionWrapper.getActiveSessionsObjects().size() %> open sessions.<br/>
There is <%= JCRSessionWrapper.getActiveSessions() %> non system and active sessions.</p>

<p>List of session not hold/created by this page/request:</p>
<script type="text/javascript">
    $(document).ready(function(){
        $(".exception").click(function(){
            $(".exceptionCode").hide();
            var exceptionToDisplay=$(this).data("session");
            $("#"+exceptionToDisplay).show();
        })
    })
</script>
<ol>
    <%
        final PrintWriter s = new PrintWriter(pageContext.getOut());
        Set<Map.Entry<UUID, JCRSessionWrapper>> entries = JCRSessionWrapper.getActiveSessionsObjects().entrySet();
        ArrayList<Map.Entry<UUID, JCRSessionWrapper>> list = new ArrayList<Map.Entry<UUID, JCRSessionWrapper>>(entries);
        Collections.sort(list, new Comparator<Map.Entry<UUID, JCRSessionWrapper>>() {
            public int compare(Map.Entry<UUID, JCRSessionWrapper> o1, Map.Entry<UUID, JCRSessionWrapper> o2) {
                String message = o1.getValue().getSessionTrace().getMessage();
                String message2 = o2.getValue().getSessionTrace().getMessage();
                if(message.contains("created")){
                    return message.split("created")[1].compareTo(message2.split("created")[1]);
                }
                return o1.getKey().toString().compareTo(o2.getKey().toString());
            }
        });
        for (Map.Entry<UUID, JCRSessionWrapper> entry : list) {
            Exception sessionTrace = entry.getValue().getSessionTrace();
            String sessionTraceMessage = sessionTrace.getMessage();
            if (!sessionTraceMessage.contains(Thread.currentThread().getName() + "_" + Thread.currentThread().getId())) {
    %>
    <li><a href="#" class="exception" data-session="<%=entry.getKey()%>"><%=sessionTraceMessage%></a>
<pre id="<%=entry.getKey()%>" style="display: none" class="exceptionCode"><%
    sessionTrace.printStackTrace(s);%>
        </pre>
    </li>
    <%
            }
            pageContext.getOut().flush();
        }
    %>

</ol>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>