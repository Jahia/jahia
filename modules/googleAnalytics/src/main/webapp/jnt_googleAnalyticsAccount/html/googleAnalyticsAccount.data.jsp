<%@ page import="org.jahia.modules.googleAnalytics.GoogleAnalyticsData" %>
<%@ page import="com.google.gdata.data.analytics.DataFeed" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.services.render.RenderContext" %>
<%@ page import="com.google.gdata.data.analytics.DataEntry" %>
<%@ page import="org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData" %>
<%@ page import="com.google.gdata.data.analytics.Dimension" %>
<%@ page import="com.google.gdata.data.analytics.Metric" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<a href="${url.base}${currentNode.parent.path}.html">Back to profiles list</a>
<fieldset><legend>Form</legend>
    <form name="doFilter" action="${url.base}${currentResource.node.path}.data.html">
        Start Date : <input type="text" name="startDate" value="2008-01-01"/>
        End Date : <input type="text" name="endDate" value="2011-12-12"/>
        <input type="submit"/>
    </form>
</fieldset>
<%
    JCRNodeWrapper currentNode = (JCRNodeWrapper) request.getAttribute("currentNode");
    RenderContext context = (RenderContext) request.getAttribute("renderContext");
    try {
        String webPropertyID = context.getSite().getProperty("webPropertyID").getString();
        String query = "https://www.google.com/analytics/feeds/data?ids=[tableId]&";
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        if (endDate != null && startDate != null) {
            query += "start-date="+ startDate + "&" +
                    "end-date="+ endDate + "&" +
                    "dimensions=ga:source,ga:medium&" +
                    "metrics=ga:visits,ga:bounces&" +
                    "sort=-ga:visits&" +
                    "filters=ga:medium%3D%3Dreferral&max-results=5&" +
                    "prettyprint=true";
            DataFeed feed = GoogleAnalyticsData.getData(currentNode.getProperty("login").getString(),currentNode.getProperty("password").getString(),query,webPropertyID);
            if (feed != null) {
                for (DataEntry entry : feed.getEntries()) {
                    for (Dimension dim : entry.getDimensions()) {
%><%=dim.getName().substring(3)%> : <%=dim.getValue()%><br><%
    }
    for (Metric met : entry.getMetrics()) {
%><%=met.getName().substring(3)%> : <%=met.getValue()%><br><%
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
%>
