<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2010 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.scheduler.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.quartz.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Process Administration</title>
</head>
<body>
	   <h1> Process Administration</h1>
	   <p>
	   	  This jsp allows to remove Quartz jobs from Database. If you remove finished jobs it will be a performance boost on Server startup. 
	   	  <br/>
	   	  If you flush not finished jobs you will loose this jobs (e.g. activated autoexport job)
	   </p>
	  <%
	  List<JobDetail> allJobs = ServicesRegistry.getInstance().getSchedulerService().getAllJobsDetails();
	  
    if(request.getParameter("flushfinish") != null) {
      
      int jobsflushed = 0;
      for(JobDetail job : allJobs) {
        //remove sucessful and failed jobs
         if(BackgroundJob.STATUS_SUCCESSFUL.equals(job.getJobDataMap().getString(BackgroundJob.JOB_STATUS)) ||
            BackgroundJob.STATUS_FAILED.equals(job.getJobDataMap().getString(BackgroundJob.JOB_STATUS)) ||
            BackgroundJob.STATUS_ABORTED.equals(job.getJobDataMap().getString(BackgroundJob.JOB_STATUS)) ) {
                 ServicesRegistry.getInstance().getSchedulerService().deleteJob(job.getName(), job.getGroup());
                 jobsflushed++;
         }
      }
      allJobs = ServicesRegistry.getInstance().getSchedulerService().getAllJobsDetails();
      %>
         <b><%=jobsflushed%> finished Jobs</b> from Job history removed<br/><br/>
      <%
  
    } else if(request.getParameter("flushwait") != null) {
      
      int jobsflushed = 0;
      for(JobDetail job : allJobs) {
         System.out.println("stat: " + job.getJobDataMap().getString(BackgroundJob.JOB_STATUS));
         //remove waiting jobs
         if(BackgroundJob.STATUS_WAITING.equals(job.getJobDataMap().getString(BackgroundJob.JOB_STATUS))) {
                 ServicesRegistry.getInstance().getSchedulerService().deleteJob(job.getName(), job.getGroup());
                 jobsflushed++;
         }
      }
      allJobs = ServicesRegistry.getInstance().getSchedulerService().getAllJobsDetails();
      %>
         <b><%=jobsflushed%> waited Jobs</b> removed<br/><br/>
      <%
  
    }  else if(request.getParameter("flushpool") != null) {
      
      int jobsflushed = 0;
      for(JobDetail job : allJobs) {
         System.out.println("stat: " + job.getJobDataMap().getString(BackgroundJob.JOB_STATUS));
         //remove waiting jobs
         if(BackgroundJob.STATUS_POOLED.equals(job.getJobDataMap().getString(BackgroundJob.JOB_STATUS))) {
                 ServicesRegistry.getInstance().getSchedulerService().deleteJob(job.getName(), job.getGroup());
                 jobsflushed++;
         }
      }
      allJobs = ServicesRegistry.getInstance().getSchedulerService().getAllJobsDetails();
      %>
         <b><%=jobsflushed%> pooled Jobs</b> removed<br/><br/>
      <%
  
    }
  %> 
	
	
	
   <h2>Currently database contains <b><%=allJobs.size()%> Jobs</b> </h2>
   
   <form action="" method="post">
   	  <h3>Available actions:</h3>
   	  <input type="submit" value="Refresh" name="refresh" /><br/>
   	  <input type="submit" value="Flush finished jobs" name="flushfinish" /> removes all successful, failed and aborted jobs<br/>
   	  <input type="submit" value="Flush waiting jobs" name="flushwait" /> removes all waiting jobs<br/>
   	  <input type="submit" value="Flush pooled jobs" name="flushpool" /> removes all pooled jobs<br/>
   	  
   	  
   </form>	
   


</body>
</html>