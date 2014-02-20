<%@ page import="org.jahia.registries.ServicesRegistry, org.jahia.services.scheduler.*, java.text.*, java.util.*, org.quartz.*"
%><%
SchedulerService service = ServicesRegistry.getInstance().getSchedulerService();
Scheduler scheduler = service.getScheduler();
pageContext.setAttribute("service", service);
String jobGroup = request.getParameter("group");
pageContext.setAttribute("group", jobGroup);
%><%
List<JobDetail> allJobs = service.getAllJobs();
pageContext.setAttribute("allJobs", allJobs);
List<Object> jobs = new LinkedList<Object>();
pageContext.setAttribute("jobs", jobs);
int limitCount = 0;

for (JobDetail job : allJobs) {
    if (jobGroup != null && jobGroup.length() > 0 && !job.getGroup().equals(jobGroup)) {
        continue;
    }
    if (!String.valueOf(job.getJobDataMap().get("status")).equals("added") && scheduler.getTriggersOfJob(job.getName(), job.getGroup()).length == 0) {
        if (limitCount > 1000) {
            // we do not display more than 1000 jobs
            break;
        }
        limitCount++;
         
        jobs.add(new Object[] {job, null});
    }
}

pageContext.setAttribute("allCount", jobs.size());
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"
%><c:if test="${param.file}"><%
response.setContentType("text/csv;charset=UTF-8");
response.setHeader("Content-Disposition", "attachment; filename=\"jobs-"
        + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".csv\"");
pageContext.setAttribute("newLineChar", "\n"); 
%><c:set var="nl" value="${fn:escapeXml(newLineChar)}"/>#,Group.Name,State,Start,End,Duration${nl}
<c:forEach items="${jobs}" var="jobElement" varStatus="status">
<c:set var="job" value="${jobElement[0]}"/>
<c:set var="state" value="${job.jobDataMap.status}"/>
${status.index + 1},${fn:escapeXml(job.fullName)},${state},${job.jobDataMap.begin},${job.jobDataMap.end},${job.jobDataMap.duration}${nl}
</c:forEach>
</c:if><c:if test="${not param.file}"><%@ page contentType="text/html;charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Completed Job Info</title>
    <link rel="stylesheet" href="tools.css" type="text/css" />
</head>
<body>
	<h1>Completed Job Info</h1>
    <p>Completed job count: <strong>${allCount}</strong> <a href="#refresh" onclick="window.location.reload(); return false;" title="refresh"><img src="<c:url value='/icons/refresh.png'/>" alt="refresh" title="refresh" height="16" width="16"/></a>
    <br/><a href="?file=true&amp;group=${fn:escapeXml(param.group)}" target="_blank"><img src="<c:url value='/icons/download.png'/>" alt="download" title="download" height="16" width="16"/>download as a file</a></p>
    
    <c:if test="${not empty jobs}">
    
    <table border="1" cellspacing="0" cellpadding="5">
        <thead>
            <tr>
                <th>#</th>
                <th>Group.Name</th>
                <th>State</th>
                <th>Start</th>
                <th>End</th>
                <th>Duration</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${jobs}" var="jobElement" varStatus="status">
            <c:set var="job" value="${jobElement[0]}"/>
            <c:set var="state" value="${job.jobDataMap.status}"/>
            <tr style="${'executing' == state ? 'color: green; font-weight: bold;' : ''}">
                <td><strong>${status.index + 1}</strong></td>
                <td title="class: ${job.jobClass.name}">${fn:escapeXml(job.fullName)}</td>
                <td style="text-align: center;">${state}</td>
                <td style="text-align: center;">${job.jobDataMap.begin}</td>
                <td style="text-align: center;">${job.jobDataMap.end}</td>
                <td style="text-align: center;">${job.jobDataMap.duration}</td>
            </tr>
            </c:forEach>
        </tbody>
    </table>
    </c:if>
    
    <%@ include file="gotoIndex.jspf" %>
</body>
</html>
</c:if>