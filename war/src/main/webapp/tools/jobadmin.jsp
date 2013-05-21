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
<c:set var="ramScheduler" value="${param.schedulerType == 'ram'}"/>
<% boolean isRamScheduler = (Boolean) pageContext.getAttribute("ramScheduler"); %>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>${ramScheduler ? 'RAM (in-memory) ' : ''}Job Administration</title>
    <link rel="stylesheet" href="tools.css" type="text/css" />
    <link type="text/css" href="<c:url value='/modules/assets/css/jquery.fancybox.css'/>" rel="stylesheet"/>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.fancybox.pack.js'/>"></script>
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
Scheduler scheduler = isRamScheduler ? service.getRAMScheduler() : service.getScheduler();
pageContext.setAttribute("service", service);
%>
<c:set var="showActions" value="${functions:default(fn:escapeXml(param.showActions), 'false')}"/>
<c:set var="showCompleted" value="${functions:default(fn:escapeXml(param.showCompleted), 'false')}"/>
<body>
	<h1>${ramScheduler ? 'RAM (in-memory) ' : ''}Job Administration</h1>
    <p>
    <c:if test="${ramScheduler}">
        This view lists all the jobs, managed by the RAM (in-memory) scheduler.
        A RAM scheduler is a local (non-clustered) instance which has no persistence for jobs between the server restarts.
        <br/>
        <a title="Switch to persistent scheduler" href="#scheduler" onclick="go('schedulerType', ''); return false;">Switch to persistent scheduler</a>
    </c:if>
    <c:if test="${!ramScheduler}">
        This view lists all the jobs, managed by the scheduler and whose state
        is persisted in the database (maintained between server restarts).
        <br/>
        <a title="Switch to RAM (in-memory) scheduler" href="#scheduler" onclick="go('schedulerType', 'ram'); return false;">Switch to RAM (in-memory) scheduler</a>
    </c:if>
    </p>
<fieldset style="position: absolute; right: 20px;">
    <legend><strong>Settings</strong></legend>
    <p>
        <input id="cbActions" type="checkbox" ${showActions ? 'checked="checked"' : ''}
                onchange="go('showActions', '${!showActions}')"/>&nbsp;<label for="cbActions">Show actions</label><br/>
        <input id="cbCompleted" type="checkbox" ${showCompleted ? 'checked="checked"' : ''}
                onchange="go('showCompleted', '${!showCompleted}')"/>&nbsp;<label for="cbCompleted">Show all jobs</label><br/>
    </p>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" id="showActions" name="showActions" value="${showActions}"/>
        <input type="hidden" id="showCompleted" name="showCompleted" value="${showCompleted}"/>
        <input type="hidden" id="action" name="action" value=""/>
        <input type="hidden" id="name" name="name" value=""/>
        <input type="hidden" id="group" name="group" value=""/>
        <input type="hidden" id="schedulerType" name="schedulerType" value="${ramScheduler ? 'ram' : ''}"/>
    </form>
</fieldset>
<c:if test="${'removeCompleted' == param.action}">
    <% pageContext.setAttribute("jobsflushed", isRamScheduler ? service.deleteAllCompletedRAMJobs() : service.deleteAllCompletedJobs()); %>
     <p style="color: blue">Removed <strong>${jobsflushed}</strong> completed jobs</p>
</c:if>
<c:if test="${'cancel' == param.action && not empty param.name && not empty param.group}">
    <%
    Trigger toDelete = scheduler.getTrigger(request.getParameter("name"), request.getParameter("group"));
    if (toDelete != null) {
        JobDetail jobDetail = scheduler.getJobDetail(toDelete.getJobName(), toDelete.getJobGroup());
        if (jobDetail != null) {
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_CANCELED);
            scheduler.addJob(jobDetail, true);
        }
        scheduler.unscheduleJob(toDelete.getName(), toDelete.getGroup());
    %>
    <p style="color: blue">Successfully unscheduled <strong>${param.group}.${param.name}</strong> job trigger</p>
    <% } else { %>
    <p style="color: red">Unable to find trigger <strong>${param.group}.${param.name}</strong></p>
    <% } %>
</c:if>
<c:if test="${'remove' == param.action && not empty param.name && not empty param.group}">
    <%
    if (scheduler.deleteJob(request.getParameter("name"), request.getParameter("group"))) {
    %>
    <p style="color: blue">Successfully deleted job <strong>${param.group}.${param.name}</strong></p>
    <% } else { %>
    <p style="color: red">Unable to delete job <strong>${param.group}.${param.name}</strong></p>
    <% } %>
</c:if>
<c:if test="${'pause' == param.action && not empty param.name && not empty param.group}">
    <%
    scheduler.pauseJob(request.getParameter("name"), request.getParameter("group"));
    %>
    <p style="color: blue">Successfully paused <strong>${param.group}.${param.name}</strong> job</p>
</c:if>
<c:if test="${'resume' == param.action && not empty param.name && not empty param.group}">
    <%
    scheduler.resumeJob(request.getParameter("name"), request.getParameter("group"));
    %>
    <p style="color: blue">Successfully resumed <strong>${param.group}.${param.name}</strong> job</p>
</c:if>
<%
List<JobDetail> allJobs = isRamScheduler ? service.getAllRAMJobs() : service.getAllJobs();
pageContext.setAttribute("allJobs", allJobs);
List<Object> jobs = new LinkedList<Object>();
pageContext.setAttribute("jobs", jobs);
int aliveCount = 0;
int limitCount = 0;
int addedCount = 0;
boolean showCompleted = Boolean.valueOf((String) pageContext.getAttribute("showCompleted"));

for (JobDetail job : allJobs) {
    Trigger[] triggers = scheduler.getTriggersOfJob(job.getName(), job.getGroup());
    if (triggers.length > 0) {
        aliveCount++;
    } else if (String.valueOf(job.getJobDataMap().get("status")).equals("added")) {
        addedCount++;
    }
	if (showCompleted || triggers.length > 0) {
        if (limitCount > 1000) {
            // we do not display more than 1000 jobs
            break;
         }
         limitCount++;
         
	     jobs.add(new Object[] {job, triggers.length > 0 ? triggers[0] : null});
	}
}

pageContext.setAttribute("allCount", allJobs.size());
pageContext.setAttribute("aliveCount", aliveCount);
pageContext.setAttribute("addedCount", addedCount);
pageContext.setAttribute("limitReached", limitCount > 1000);
%>
<p>Total job count: <strong>${allCount}</strong> (<strong><c:if test="${limitReached}">more than </c:if>${aliveCount}</strong> active/scheduled) <a href="#refresh" onclick="go(); return false;" title="refresh"><img src="<c:url value='/icons/refresh.png'/>" alt="refresh" title="refresh" height="16" width="16"/></a></p>

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
        <c:remove var="triggerState"/>
        <c:if test="${not empty trigger}">
            <%
                Trigger tr = (Trigger) pageContext.getAttribute("trigger");
                pageContext.setAttribute("triggerState", scheduler.getTriggerState(tr.getName(), tr.getGroup()));
            %>
        </c:if>
        <tr style="${'executing' == state ? 'color: green; font-weight: bold;' : ''}">
            <td><strong>${status.index + 1}</strong></td>
            <td>
                <a class="dataLink" title="Job data" href="#jobData${status.index}"><img src="<c:url value='/icons/help.png'/>" width="16" height="16" alt="?" title="Job data"/></a>
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
            <td style="text-align: center;">${state}${triggerState == 1 ? ' (paused)' : ''}</td>
            <td style="text-align: center;">${job.durable ? 'yes' : 'no'}</td>
            <td><c:if test="${not empty trigger && not empty trigger.nextFireTime}"><fmt:formatDate value="${trigger.nextFireTime}" pattern="yyyy-MM-dd HH:mm"/></c:if></td>
            <c:if test="${showActions}">
            <td>
                <c:if test="${not empty trigger}">
                    <c:if test="${triggerState == 0}">
                        <a title="Pause job" href="#pause" onclick="if (confirm('You are about to temporary pause the job ${job.fullName}. Continue?')) { go('action', 'pause', 'name', '${functions:escapeJavaScript(job.name)}', 'group', '${job.group}'); } return false;"><img src="<c:url value='/icons/media_pause.png'/>" width="16" height="16" alt="cancel" title="Pause job"/></a>
                    </c:if>
                    <c:if test="${triggerState == 1}">
                        <a title="Resume job" href="#resume" onclick="if (confirm('You are about to resume suspended job ${job.fullName}. If its trigger missed the fire time it will fire immediately. Continue?')) { go('action', 'resume', 'name', '${functions:escapeJavaScript(job.name)}', 'group', '${job.group}'); } return false;"><img src="<c:url value='/icons/media_play_green.png'/>" width="16" height="16" alt="cancel" title="Resume job"/></a>
                    </c:if>
                    <c:if test="${job.durable}">
                        <a title="Cancel job (unschedule)" href="#cancel" onclick="if (confirm('You are about to cancel (unschedule) the job ${job.fullName}. Continue?')) { go('action', 'cancel', 'name', '${trigger.name}', 'group', '${trigger.group}'); } return false;"><img src="<c:url value='/icons/cancel.png'/>" width="16" height="16" alt="cancel" title="Cancel job (unschedule)"/></a>
                    </c:if>
                    <c:if test="${not job.durable}">
                        <a title="Cancel job (unschedule) and delete its data" href="#cancelAndRemove" onclick="if (confirm('You are about to cancel (unschedule) and permanently remove the job data for job ${job.fullName}. Continue?')) { go('action', 'cancel', 'name', '${trigger.name}', 'group', '${trigger.group}'); } return false;"><img src="<c:url value='/icons/delete.png'/>" width="16" height="16" alt="cancel" title="Cancel job (unschedule) and delete its data"/></a>
                    </c:if>
                </c:if>
                <c:if test="${empty trigger}">
                    <a title="Delete job data" href="#remove" onclick="if (confirm('You are about to permanently delete the data of the job ${job.fullName}. Continue?')) { go('action', 'remove', 'name', '${functions:escapeJavaScript(job.name)}', 'group', '${job.group}'); } return false;"><img src="<c:url value='/icons/showTrashboard.png'/>" width="16" height="16" alt="remove" title="Remove job data"/></a>
                </c:if>
            </td>
            </c:if>
        </tr>
        </c:forEach>
    </tbody>
</table>
</c:if>

<c:if test="${showActions && (allCount - aliveCount - addedCount > 0)}">
<p>
    <img src="<c:url value='/icons/showTrashboard.png'/>" alt=" " height="16" width="16"/>&nbsp;<a href="#removeCompleted" onclick="if (confirm('You are about to permanently remove the data of completed jobs. Continue?')) { go('action', 'removeCompleted'); } return false;">remove all completed jobs (${allCount - aliveCount - addedCount})</a>
</p>
</c:if>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>