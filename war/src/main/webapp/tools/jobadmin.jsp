<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.scheduler.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.quartz.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Job Administration</title>
    <link rel="stylesheet" href="tools.css" type="text/css" />
    <link type="text/css" href="resources/jquery.fancybox-1.3.4.css" rel="stylesheet"/>
    <script type="text/javascript" src="resources/jquery.min.js"></script>
    <script type="text/javascript" src="resources/jquery.fancybox-1.3.4.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('.dataLink').fancybox({
                        'hideOnContentClick': false,
                        'titleShow' : false,
                        'transitionOut' : 'none'
                    });
        });
        function go(id1, value1, id2, value2, id3, value3) {
        	if (id1) {
        		document.getElementById(id1).value=value1;
        	}
        	if (id2) {
        		document.getElementById(id2).value=value2;
        	}
        	if (id3) {
        		document.getElementById(id3).value=value3;
        	}
        	document.getElementById('navigateForm').submit();
        }
    </script>
</head>
<%
SchedulerService service = ServicesRegistry.getInstance().getSchedulerService();
pageContext.setAttribute("service", service);
%>
<c:set var="showActions" value="${functions:default(param.showActions, 'false')}"/>
<c:set var="showCompleted" value="${functions:default(param.showCompleted, 'false')}"/>
<body>
	<h1>Job Administration</h1>
<fieldset style="position: absolute; right: 20px;">
    <legend><strong>Settings</strong></legend>
    <p>
        <input id="cbActions" type="checkbox" ${showActions ? 'checked="checked"' : ''}
                onchange="go('showActions', '${!showActions}')"/>&nbsp;<label for="cbActions">Show actions</label><br/>
        <input id="cbCompleted" type="checkbox" ${showCompleted ? 'checked="checked"' : ''}
                onchange="go('showCompleted', '${!showCompleted}')"/>&nbsp;<label for="cbCompleted">Show completed jobs</label><br/>
    </p>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" id="showActions" name="showActions" value="${showActions}"/>
        <input type="hidden" id="showCompleted" name="showCompleted" value="${showCompleted}"/>
        <input type="hidden" id="action" name="action" value=""/>
        <input type="hidden" id="name" name="name" value=""/>
        <input type="hidden" id="group" name="group" value=""/>
    </form> 
</fieldset>
<c:if test="${'removeCompleted' == param.action}">
    <% pageContext.setAttribute("jobsflushed", service.deleteAllCompletedJobs()); %>
     <p style="color: blue">Removed <strong>${jobsflushed}</strong> completed jobs</p>
</c:if>         
<c:if test="${'cancel' == param.action && not empty param.name && not empty param.group}">
    <% 
    Trigger toDelete = service.getScheduler().getTrigger(request.getParameter("name"), request.getParameter("group"));
    if (toDelete != null) {
        JobDetail jobDetail = service.getScheduler().getJobDetail(toDelete.getJobName(), toDelete.getJobGroup());
        if (jobDetail != null) {
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_CANCELED);
            service.getScheduler().addJob(jobDetail, true);
        }
        service.getScheduler().unscheduleJob(toDelete.getName(), toDelete.getGroup());
    %>
    <p style="color: blue">Successfully unscheduled <strong>${param.group}.${param.name}</strong> job trigger</p>
    <% } else { %>
    <p style="color: red">Unable to find trigger <strong>${param.group}.${param.name}</strong></p>
    <% } %>
</c:if>         
<c:if test="${'remove' == param.action && not empty param.name && not empty param.group}">
    <% 
    if (service.getScheduler().deleteJob(request.getParameter("name"), request.getParameter("group"))) {
    %>
    <p style="color: blue">Successfully deleted job <strong>${param.group}.${param.name}</strong></p>
    <% } else { %>
    <p style="color: red">Unable to delete job <strong>${param.group}.${param.name}</strong></p>
    <% } %>
</c:if>         
<%
List<JobDetail> allJobs = service.getAllJobs();
pageContext.setAttribute("allJobs", allJobs);
List<Object> jobs = new LinkedList<Object>();
pageContext.setAttribute("jobs", jobs);
int aliveCount = 0;
if (Boolean.valueOf((String) pageContext.getAttribute("showCompleted"))) {
    int limitCount = 0;
    for (JobDetail job : allJobs) {
        if (limitCount > 1000) {
            // we do not display more than 1000 jobs
            break;
        }
        limitCount++;
        Trigger[] triggers = service.getScheduler().getTriggersOfJob(job.getName(), job.getGroup());
        jobs.add(new Object[] {job, triggers.length > 0 ? triggers[0] : null});
        if (triggers.length > 0) {
            aliveCount++;
        }
    }
} else {
    for (String triggerGroup : service.getScheduler().getTriggerGroupNames()) {
        for (String triggerName : service.getScheduler().getTriggerNames(triggerGroup)) { 
            Trigger trigger = service.getScheduler().getTrigger(triggerName, triggerGroup);
            JobDetail job = service.getScheduler().getJobDetail(trigger.getJobName(), trigger.getJobGroup());
            jobs.add(new Object[] {job, trigger});
            aliveCount++;
        }
    }
}
pageContext.setAttribute("allCount", allJobs.size());
pageContext.setAttribute("aliveCount", aliveCount);
%>
<p>Total job count: <strong>${allCount}</strong> (<strong>${aliveCount}</strong> active/scheduled) <a href="#refresh" onclick="go(); return false;" title="refresh"><img src="<c:url value='/icons/refresh.png'/>" alt="refresh" title="refresh" height="16" width="16"/></a></p>

<c:if test="${not empty jobs}">
<h2>Active/scheduled jobs</h2>

<table border="1" cellspacing="0" cellpadding="5">
    <thead>
        <tr>
            <th>#</th>
            <th>?</th>
            <th>Group.Name</th>
            <th>State</th>
            <th>Durable</th>
            <th>Next fire</th>
            <c:if test="${showActions}">
            <th>&nbsp;</th>
            </c:if>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${jobs}" var="jobElement" varStatus="status">
        <c:set var="job" value="${jobElement[0]}"/>
        <c:set var="trigger" value="${jobElement[1]}"/>
        <c:set var="state" value="${job.jobDataMap.status}"/>
        <tr style="${'executing' == state ? 'color: green; font-weight: bold;' : ''}">
            <td><strong>${status.index + 1}</strong></td>
            <td>
                <a class="dataLink" title="Job data" href="#jobData${status.index}"><img src="<c:url value='/css/images/andromeda/icons/help.png'/>" width="16" height="16" alt="?" title="Job data"/></a>
                <div style="display: none;">
                    <div id="jobData${status.index}">
                        <h3>${fn:escapeXml(job.fullName)}</h3>
                        <table border="1" cellspacing="0" cellpadding="5">
                            <tbody>
                                <tr>
                                    <td><strong>Name:</strong></td><td>${job.name}</td>
                                </tr>
                                <tr>
                                    <td><strong>Group:</strong></td><td>${job.group}</td>
                                </tr>
                                <tr>
                                    <td><strong>Class:</strong></td><td>${job.jobClass.name}</td>
                                </tr>
                                <tr>
                                    <td><strong>Description:</strong></td><td>${fn:escapeXml(job.description)}</td>
                                </tr>
                                <tr>
                                    <td><strong>State:</strong></td><td>${state}</td>
                                </tr>
                                <c:if test="${not empty trigger && not empty trigger.nextFireTime}">
                                <tr>
                                    <td><strong>Next fire time:</strong></td><td><fmt:formatDate value="${trigger.nextFireTime}" pattern="yyyy-MM-dd HH:mm"/></td>
                                </tr>
                                </c:if>
                                <c:if test="${not empty trigger && trigger.class.name == 'org.quartz.CronTrigger'}">
                                <tr>
                                    <td><strong>Cron:</strong></td><td>${fn:escapeXml(trigger.cronExpression)}</td>
                                </tr>
                                </c:if>
                                <tr>
                                    <td colspan="2">&nbsp;</td>
                                </tr>
                                <tr>
                                    <td colspan="2"><center><strong>Job data</strong></center></td>
                                </tr>
                                <c:forEach items="${job.jobDataMap}" var="data">
                                <tr>
                                    <td><strong>${data.key}:</strong></td>
                                    <td><c:if test="${data.key == 'node'}"><a title="Open in JCR Browser" href="<c:url value='/tools/jcrBrowser.jsp?uuid=${data.value}&workspace=live'/>" target="_blank">${fn:escapeXml(data.value)}</a></c:if><c:if test="${data.key != 'node'}">${fn:escapeXml(data.value)}</c:if></td>
                                </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </td>
            <td title="class: ${job.jobClass.name}">${fn:escapeXml(job.fullName)}</td>
            <td style="text-align: center;">${state}</td>
            <td style="text-align: center;">${job.durable ? 'yes' : 'no'}</td>
            <td><c:if test="${not empty trigger && not empty trigger.nextFireTime}"><fmt:formatDate value="${trigger.nextFireTime}" pattern="yyyy-MM-dd HH:mm"/></c:if></td>
            <c:if test="${showActions}">
            <td>
                <c:if test="${not empty trigger}">
                    <c:if test="${job.durable}">
                        <a title="Cancel job (unschedule)" href="#cancel" onclick="if (confirm('You are about to cancel (unschedule) the job ${job.fullName}. Continue?')) { go('action', 'cancel', 'name', '${trigger.name}', 'group', '${trigger.group}'); } return false;"><img src="<c:url value='/css/images/andromeda/icons/cancel.png'/>" width="16" height="16" alt="cancel" title="Cancel job (unschedule)"/></a>
                    </c:if>
                    <c:if test="${not job.durable}">
                        <a title="Cancel job (unschedule) and delete its data" href="#cancelAndRemove" onclick="if (confirm('You are about to cancel (unschedule) and permanently remove the job data for job ${job.fullName}. Continue?')) { go('action', 'cancel', 'name', '${trigger.name}', 'group', '${trigger.group}'); } return false;"><img src="<c:url value='/css/images/andromeda/icons/delete.png'/>" width="16" height="16" alt="cancel" title="Cancel job (unschedule) and delete its data"/></a>
                    </c:if>
                </c:if>
                <c:if test="${empty trigger}">
                    <a title="Delete job data" href="#remove" onclick="if (confirm('You are about to permanently delete the data of the job ${job.fullName}. Continue?')) { go('action', 'remove', 'name', '${job.name}', 'group', '${job.group}'); } return false;"><img src="<c:url value='/icons/showTrashboard.png'/>" width="16" height="16" alt="remove" title="Remove job data"/></a>
                </c:if>
            </td>
            </c:if>
        </tr>
        </c:forEach>
    </tbody>
</table>
</c:if>

<c:if test="${showActions && (allCount - aliveCount > 0)}">
<p>
    <img src="<c:url value='/icons/showTrashboard.png'/>" alt=" " height="16" width="16"/>&nbsp;<a href="#removeCompleted" onclick="if (confirm('You are about to permanently remove the data of completed jobs. Continue?')) { go('action', 'removeCompleted'); } return false;">remove all completed jobs (${allCount - aliveCount})</a>
</p>
</c:if>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>