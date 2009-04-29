<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.jahia.admin.search.ManageSearch" %>
<%@ page import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="org.jahia.content.ObjectKey" %>
<%@ page import="org.jahia.content.TreeOperationResult" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.search.*" %>
<%@ page import="org.jahia.services.importexport.ImportJob" %>
<%@ page import="org.jahia.services.importexport.ProductionJob" %>
<%@ page import="org.jahia.services.scheduler.BackgroundJob" %>
<%@ page import="org.jahia.services.scheduler.SchedulerService" %>
<%@ page import="org.jahia.services.timebasedpublishing.TimeBasedPublishingJob" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.services.workflow.ActivationJob" %>
<%@ page import="org.quartz.JobDataMap" %>
<%@ page import="org.quartz.JobDetail" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<jsp:useBean id="subAction"		class="java.lang.String"    scope="request"/>     <% // the default screen %>

<%!


    /**
     * jsp pages to display process
     * $Id: processing.jsp 16772 2007-03-15 10:55:20Z knguyen $
     */

    // declarations
    private static final Logger logger = Logger.getLogger("jsp.jahia.engines");
    private static SchedulerService service = ServicesRegistry.getInstance().getSchedulerService();
    //private static JahiaSitesService siteService = ServicesRegistry.getInstance().getJahiaSitesService();


    /**
     * convenient method to grab jobdetails infos
     *
     * @param detail
     * @return
     * @throws JahiaException
     */
    public String[] getJobInfos(JobDetail detail) throws JahiaException {
        String[] infos = new String[20];

        JobDataMap data = detail.getJobDataMap();
        if (data == null) {
            logger.debug("jobdata of " + detail.getName() + " is null");
            throw new IllegalArgumentException("data is NULL!");
        }

        infos[0] = data.getString(BackgroundJob.JOB_TYPE);
        infos[1] = data.getString(BackgroundJob.JOB_CREATED);

        JahiaUser user;
        try {
            user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(data.getString(BackgroundJob.JOB_USERKEY));
            infos[2] = user.getUsername();
        } catch (NullPointerException e) {
            infos[2] = "NA";
        }

        infos[3] = data.getString(BackgroundJob.JOB_SCHEDULED);
        infos[4] = data.getString(BackgroundJob.JOB_BEGIN);
        infos[5] = data.getString(BackgroundJob.JOB_END);
        infos[6] = data.getString(BackgroundJob.JOB_STATUS);
        infos[7] = data.getString(BackgroundJob.JOB_DURATION);

        // specific infos for each type
        if (infos[0].equalsIgnoreCase("siteindexation")) {
            infos[10] = data.getString(BackgroundJob.JOB_SITEKEY);
            infos[11] = "";
            infos[12] = "";
        } else {
            //default
            infos[10] = "";
            infos[11] = "";
        }

        //duration
        if (data.get(BackgroundJob.JOB_DURATION) != null && !data.get(BackgroundJob.JOB_DURATION).equals("")) {
            infos[8] = (String) data.get(BackgroundJob.JOB_DURATION);
        } else if (data.get(BackgroundJob.JOB_BEGIN) != null && data.get(BackgroundJob.JOB_END) != null
                  && !data.get(BackgroundJob.JOB_BEGIN).equals("") && !data.get(BackgroundJob.JOB_END).equals("")) {
            long b = Long.parseLong((String) data.get(BackgroundJob.JOB_BEGIN));
            long e = Long.parseLong((String) data.get(BackgroundJob.JOB_END));
            infos[8] = "" + ((e - b) / 1000);//in sec
        }

        return infos;
    }

       /**
     * to format a process date in a more friendly way (i.e using the locale)
     * and using prefix & postfix ressource's bundlelized messages and refactoring
     * @param date
     * @param l
     * @return a date string
     */
    public String printFriendlyDate(Date date, Locale l) {
        String s = "NA";
        if (date == null) return s;
        long d = 0;

        //formatters
        SimpleDateFormat df = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT, l);
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm", l);
        //SimpleDateFormat tf = new SimpleDateFormat("mm 'min' ss 'sec'", l);

        //calendar points
        GregorianCalendar now = new GregorianCalendar(l);
        now.set(Calendar.HOUR_OF_DAY, now.getActualMinimum(Calendar.HOUR_OF_DAY));
        now.set(Calendar.MINUTE, now.getActualMinimum(Calendar.MINUTE));
        now.set(Calendar.SECOND, now.getActualMinimum(Calendar.SECOND));

        GregorianCalendar hier = new GregorianCalendar(l);
        hier.roll(Calendar.DATE, false);// yesterday
        hier.set(Calendar.HOUR_OF_DAY, hier.getActualMinimum(Calendar.HOUR_OF_DAY));
        hier.set(Calendar.MINUTE, hier.getActualMinimum(Calendar.MINUTE));
        hier.set(Calendar.SECOND, hier.getActualMinimum(Calendar.SECOND));


        Calendar c = Calendar.getInstance(l);

        c.setTime(date);
        if (c.after(now)) {
            //today
            long diftime = System.currentTimeMillis() - date.getTime();
            long difsec = diftime / 1000;
            long difsec1 = difsec % 60;
            long difmin = diftime / 60000;
            if (difmin < 60) {
                return difmin + " " + getRessourceForTime(".minutes", l)+ " " + difsec1 + " " + getRessourceForTime(".seconds", l) + " " + getRessourceForTime(".today.postfix", l);
            } else {
                String pref = getRessourceForTime(".today.prefix", l);
                return pref + "&nbsp;" + sd.format(date);
            }
        } else if (c.after(hier) && c.before(now)) {
            //yesterday
            String pref = getRessourceForTime(".yesterday.prefix", l);
            return pref + "&nbsp;" + sd.format(date);
        }
        // all other dates in the past
        s = df.format(date);


        return s;
    }

    /**
     * a convenient method to print friendly range for future and past date
     * @param time
     * @param l
     * @return a string
     */
    public String printFriendlyRange(long time, Locale l) {
        String s = "NA";
        if (time == 0) return s;
        long nowtime = System.currentTimeMillis();
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm", l);
        SimpleDateFormat df = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT, l);

        //calendar point for today
        // same as above method but with check for maximum limits for today and tomorrow
        GregorianCalendar now = new GregorianCalendar(l);
        now.set(Calendar.HOUR_OF_DAY, now.getActualMaximum(Calendar.HOUR_OF_DAY));
        now.set(Calendar.MINUTE, now.getActualMaximum(Calendar.MINUTE));
        now.set(Calendar.SECOND, now.getActualMaximum(Calendar.SECOND));


        if (time > nowtime) {
            Calendar c = Calendar.getInstance(l);
            c.setTimeInMillis(time);
            if (c.before(now)) {
                //future range for today
                long dif = time - nowtime;
                long difsec = dif / 1000;
                long difsec1 = difsec % 60;
                long difmin = dif / 60000;
                if (difmin < 60) {
                    String pref = getRessourceForTime(".today.futurerangeprefix", l);
                    return pref + "&nbsp;" + difmin + " " + getRessourceForTime(".minutes", l) + " " + difsec1 + " " + getRessourceForTime(".seconds", l) + " ";
                } else {
                    String pref = getRessourceForTime(".today.prefix", l);
                    return pref + "&nbsp;" + sd.format(new Date(time));
                }
            } else {
                //future range for tomorrow and the following days
                GregorianCalendar tom = new GregorianCalendar(l);
                tom.roll(Calendar.DATE, true);// tomorrow
                tom.set(Calendar.HOUR_OF_DAY, now.getActualMaximum(Calendar.HOUR_OF_DAY));
                tom.set(Calendar.MINUTE, now.getActualMaximum(Calendar.MINUTE));
                tom.set(Calendar.SECOND, now.getActualMaximum(Calendar.SECOND));
                if(c.before(tom)){
                String pref = getRessourceForTime(".tomorrow.prefix", l);
                s = pref+"&nbsp;"+sd.format(new Date(time));
                } else {
                s = df.format(new Date(time));
                }
                return s;
            }

        } else {
            //past range are supported by friendlydate method
            return printFriendlyDate(new Date(time), l);
        }
    }
	private String getFriendlyDuration(int duration,Locale l){
		int min=0;
		int sec;
		int hr=0;
		String timeduration="";



        if(duration<=0){
                timeduration=getRessource("org.jahia.engines.processDisplay.executed.label", l)+"&nbsp;"+getRessourceForTime(".duration.shortime", l);
                return timeduration;
                } else if(duration<60){
                sec=duration;
                timeduration = ""+sec+" sec";
                } else if(duration>60 && duration<3600){
				// duration is more than one minute and less one hour
                min=duration/60;
                sec=duration%60;
                timeduration = ""+min+" min "+sec+" sec";
                } else if(duration>3600){
                //durationis more than one hour
                hr=duration/3600;
                min=duration%3600;


                if(min>60){
                sec=min%60;
                min=min/60;

                } else {
				sec=min;
				min=0;
                }

                timeduration = ""+hr+ " h:"+min+" m:"+sec+" s";
                }
                return getRessource("org.jahia.engines.processDisplay.executed.label", l) + "&nbsp;"+timeduration;

	}
    private String getRessourceForTime(String label, Locale locale) {
        String fullLabel = "org.jahia.time" + label;
        return getRessource(fullLabel, locale, fullLabel);
    }

    /**
     * internal method to render bundle resources
     * @return a string empty if resource is non existent
     */
    private String getRessource(String label, Locale l) {
      return getRessource(label, l, "");
    }

    /**
     * internal method to render bundle resources
     * @return a string empty if resource is non existent
     */
    private String getRessource(String label, Locale l, String defaultValue) {
        try {
          return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
        } catch (Exception e) {
            return defaultValue;
        }
    }

%>
<%
    String  URL = (String) request.getAttribute("URL");

    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, post-check=0, pre-check=0");

    final int currentSiteID = ((ParamBean) request.getAttribute("org.jahia.params.ParamBean")).getSiteID();
    
    String command = request.getParameter("command");//to choose the process to display
    String size = request.getParameter("size");//to choose the size of process to display
    String order = request.getParameter("order");//sort order
    String cronlimit = request.getParameter("cl");//cronlimit
    if(cronlimit ==null) cronlimit="1";

    if(order == null && (command==null || command.equalsIgnoreCase("current"))) order="1";//def descending for current
    if (order == null) order = "-1";//default:ascending
    JahiaUser theUser = (JahiaUser) session.getAttribute(ParamBean.SESSION_USER);
    Locale locale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE);
    if (locale == null) locale = request.getLocale();//fail-over locale

    // to delete jobs
    String deljob;
    if (request.getParameter("del") != null) {
        deljob = request.getParameter("del");
        try {
            service.deleteJob(deljob, request.getParameter("delgroup"));
        } catch (JahiaException e) {
            logger.error(e);
        }
    }

    boolean dispOK = true;//error flag
    String errorMessage = "initialization error!";
    //check for real users loggedIn
    if(theUser==null || theUser.getUsername().equals("guest")) {
        dispOK=false;
        errorMessage = "please this page is reserved to logged users!";
    }
    // load lists
    List crons = new ArrayList();

    // load my running and waitings
    List runningprocess = new ArrayList();
    List wait = new ArrayList();

    // passed jobs
    List past = new ArrayList();
    String theUserKey = null;
    if (theUser == null) {
        dispOK = false;
    } else {
        theUserKey = theUser.getUserKey();
    }

    List alls = null;
    try {
        alls = ManageSearch.getJobsDetails(currentSiteID, command == null || command.equalsIgnoreCase("current"));
    } catch (JahiaException e) {
        logger.debug("error",e);
        dispOK = false;
    }
    if (dispOK) {
    //looping to get all jobs
    for (java.util.Iterator iterator = alls.iterator(); iterator.hasNext();) {
        JobDetail jd = (JobDetail) iterator.next();

        JobDataMap data = jd.getJobDataMap();
        if(data!=null){
          String status = data.getString(BackgroundJob.JOB_STATUS);
          String userkey = data.getString(BackgroundJob.JOB_USERKEY);
          //ignore production or timebased job
          if(data.getString(BackgroundJob.JOB_TYPE) == null){
              continue;
          }
          if(status== null || userkey==null) {
              logger.debug("pbms on status and userkey are null");
              continue;
          }
          if (status.equals(BackgroundJob.STATUS_POOLED)) {
              long crontime=System.currentTimeMillis()+(3600000*Integer.parseInt(cronlimit));
              if(Long.parseLong(data.getString(BackgroundJob.JOB_SCHEDULED))<crontime) crons.add(jd);
          } else if (status.equals(BackgroundJob.STATUS_RUNNING) || status.equals(BackgroundJob.STATUS_INTERRUPTED)) {
              runningprocess.add(jd);
          } else if (status.equals(BackgroundJob.STATUS_WAITING)) {
              wait.add(jd);
          } else {
              past.add(jd);
          }
        } else {
            logger.debug("datamap of "+jd.getFullName()+" is null, ignoring...");
        }
    }

    final int finalorder = Integer.parseInt(order);
    Comparator comp = new Comparator() {

        /**
         * Compare two JobDetail by scheduled time. Callback for sort or TreeMap.
         * effectively returns a-b; default order by descending weight (i.e the recent first)
         *
         * @param a first object a to be compared
         * @param b second object b to be compared
         * @return +1 if a>b, 0 if a=b, -1 if a<b
         */
        public final int compare(Object a, Object b) {
            Object t_a = ((JobDetail) a).getJobDataMap().get("scheduled");
            long scheduled_a = 0;
            if (t_a != null && !"".equals(t_a)) scheduled_a = Long.parseLong((String) t_a);
            Object t_b = ((JobDetail) b).getJobDataMap().get("scheduled");
            long scheduled_b = 0;
            if (t_b != null && !"".equals(t_b)) scheduled_b = Long.parseLong((String) t_b);

            /* need signum to convert long to int, (int)will not do! */
            return signum(scheduled_a - scheduled_b);
        } // end compare

        /**
         * Collapse number down to +1 0 or -1 depending on sign.
         *
         * @param diff usually represents the difference of two long.
         * @return signum of diff, +1, 0 or -1.
         */
        private int signum(long diff) {
            if (diff > 0) return finalorder;
            if (diff < 0) return -finalorder;
            else return 0;
        }

    }; // end ByScheduledComparator inner class

    Collections.sort(past, comp);
    Collections.sort(wait, comp);
    }

    // page generation
    if (!dispOK) {
        //ERROR SOMEWHERE see above user null, process null etc...
%>
<%=errorMessage%><br>
<%
} else {

%>
<!-- c:<%=crons.size()%> r:<%=runningprocess.size()%> w:<%=wait.size()%> -->
<table cellspacing="0" cellpadding="0" width="100%" border="0" bgcolor="#FFFFFF">
<tr>
	<td align="right" colspan="2" class="nopadding">
      <div class="content-body" style="padding:4px;">
	  <input type="hidden" name="cl" value="<%=cronlimit%>">
      <select id="command" name="command" onchange="submitFormular('display','chooseoperation');">
          <%
              // command to display what the user want see
              if (command == null || command.equalsIgnoreCase("current")) { %>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.currentjobs.label", locale)%>" value="current"
                  selected><%=getRessource("org.jahia.engines.processDisplay.currentjobs.label", locale)%></option>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.pastjobs.label", locale)%>"
                  value="past"><%=getRessource("org.jahia.engines.processDisplay.pastjobs.label", locale)%></option>
          <% } else { %>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.currentjobs.label", locale)%>"
                  value="current"><%=getRessource("org.jahia.engines.processDisplay.currentjobs.label", locale)%></option>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.pastjobs.label", locale)%>" value="past"
                  selected><%=getRessource("org.jahia.engines.processDisplay.pastjobs.label", locale)%></option>
          <% } %>

      </select>
      <% if (command != null && command.equalsIgnoreCase("past") && past.size() > 10) {
          order="-1";
          String select = "";
      %>
      <select id="size" name="size" onchange="submitFormular('display','chooseoperation');">
          <%
              if (size == null || size.equals("0")) {
                  select = " selected";
          %>
          <option label="10" value="0"<%=select%>>10</option>
          <%
              select = "";
          %>
          <option label="20" value="20"<%=select%>>20</option>
          <option label="50" value="50"<%=select%>>50</option>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%>"
                  value="100"<%=select%>><%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%></option>
          <%
          } else if (size != null && size.equals("20")) {
          %>
          <option label="10" value="0"<%=select%>>10</option>
          <%
              select = " selected";
          %>
          <option label="20" value="20"<%=select%>>20</option>
          <%
              select = "";
          %>
          <option label="50" value="50"<%=select%>>50</option>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%>"
                  value="100"<%=select%>><%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%></option>
          <%
          } else if (size != null && size.equals("50")) {
          %>
          <option label="10" value="0"<%=select%>>10</option>
          <option label="20" value="20"<%=select%>>20</option>
          <%
              select = " selected";
          %>
          <option label="50" value="50"<%=select%>>50</option>
          <%
              select = "";
          %>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%>"
                  value="100"<%=select%>><%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%></option>
          <%
          } else if (size != null && size.equals("100")) {
          %>
          <option label="10" value="0"<%=select%>>10</option>
          <option label="20" value="20"<%=select%>>20</option>
          <option label="50" value="50"<%=select%>>50</option>
          <%
              select = " selected";
          %>
          <option label="<%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%>"
                  value="100"<%=select%>><%=getRessource("org.jahia.engines.processDisplay.alljobs.label", locale)%></option>
          <%
              }
          %>
      </select>
      <%
          }

      %>
	  </div>

<div class="head headtop">
    <div class="object-title">
        <%=getRessource("org.jahia.engines.processDisplay.subtitle", locale)%>
    </div>
</div>


<table width="100%" border="0" cellspacing="1" cellpadding="0" class="evenOddTable">
<tr bgcolor="#eeeeee" valign="top">
    <th nowrap="nowrap"><%=getRessource("org.jahia.engines.processDisplay.operation.label", locale)%></th>

    <th><%=getRessource("org.jahia.engines.processDisplay.action.label", locale)%></th>


    <th><%=getRessource("org.jahia.engines.processDisplay.owner.label", locale)%></th>
    <%
        String pars = JahiaAdministration.composeActionURL(request,response,"search","&sub=display");
        if (command != null) pars += "&command=" + command;
        if (size != null) pars += "&size=" + size;
        if (order != null) {
            if (order.equals("1"))
                pars += "&order=-1";
            else
                pars += "&order=1";
        }
    %>
    <th width="50%" style="vertical-align:middle;">
        
        <%
            if (command != null && command.equalsIgnoreCase("past")) {
        %>
        <a href="<%=pars%>" title="ascendant/descendant" ><img
                src="<%=URL%>images/icons/sort_descending.gif"
                border="0"></a>&nbsp;<span style="position:relative;top:-3px;"><%=getRessource("org.jahia.engines.processDisplay.status.label", locale)%><span>
        <%
            } else {
        %>
            <%=getRessource("org.jahia.engines.processDisplay.status.label", locale)%>
        <%
            }
        %>
        </th>
    
    <th class="lastCol" width="15%"><%=getRessource("org.jahia.engines.processDisplay.operations.label", locale, "Operations")%></th>

</tr>
<%


    if (command == null || command.equalsIgnoreCase("current")) {
        
        if (crons.size() + runningprocess.size() + wait.size() == 0) {
            %><tr><td colspan="5">
                <%= getRessource("net.sf.displaytag.basic.msg.empty_list", locale, "No jobs found") %>&nbsp;&gt;&gt;
                <a href="#past" onclick="submitFormular('display','chooseoperation&command=past'); return false;"><%= getRessource("org.jahia.engines.processDisplay.pastjobs.label", locale, "Past jobs") %></a> 
            </td></tr> <%
        } else {

        // 1st display crons process scheduled if any (for all users)
        logger.debug("displaying the crons jobs" + crons.size());
        if (crons.size() > 0) {
            boolean loopOK = true;//used to protect this fragment from NPE from infos
            for (Iterator it = crons.iterator(); it.hasNext();) {
                String[] infos = new String[20];
                JobDetail jd = (JobDetail) it.next();
                if (jd != null) {
                    try {
                        infos = getJobInfos(jd);
                    } catch (Exception e) {
                        //we catch all
                        logger.error("ERROR on CRONS:",e);
                        loopOK = false;
                    }
                } else {
                    loopOK = false;
                }

                if (loopOK) {
                    String type = getRessource("org.jahia.engines.processDisplay.op." + infos[0] + ".label", locale);
                    String sitekey = "";
                    String user = infos[2];
                    String source = "";
                    if (infos[10] != null && !infos[10].equalsIgnoreCase(""))
                        source = getRessource("org.jahia.engines.processDisplay.fromsite.label", locale) + "&nbsp;<b>" + infos[10] + "</b>";

                    if (infos[11] != null && !infos[11].equalsIgnoreCase(""))
                        sitekey = "&nbsp;" + getRessource("org.jahia.engines.processDisplay.tosite.label", locale) + "&nbsp;<b>" + infos[11] + "</b>";
                    String lastfired = infos[4];//last fired
                    String nextfired = infos[13]; //next fired

                    long f1 = 0;
                    long f2 = 0;
                    try {
                        f1 = Long.parseLong(lastfired);
                    } catch (NumberFormatException e) {
                        f1 = 0;
                    }
                    try {
                        f2 = Long.parseLong(nextfired);
                    } catch (NumberFormatException e) {
                        f2 = 0;
                    }
                    lastfired = printFriendlyRange(f1, locale);
                    nextfired = printFriendlyRange(f2, locale);
                    //String prefix_fired = getRessource("org.jahia.engines.processDisplay.status.running", locale);

%>
<tr valign="top" class="crons">
    <td align="left">Cron job</td>
    <td align="left" width="300px"><%= source %><%= sitekey %></td>

    <td align="left"><%= user %></td>
    <td align="left" valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="0"><tr valign="top">
            <td width="30px">&nbsp;&nbsp;</td>
            <td width="200px" align="left">
                <%=getRessource("org.jahia.engines.processDisplay.lastexecution.label", locale)%>: <%=lastfired%>
                <br><%=getRessource("org.jahia.engines.processDisplay.nextexecution.label", locale)%>: <%=nextfired%>
            </td>


            <td width="30px">&nbsp;&nbsp;</td>
            <td align="left"></td>
        </tr></table>

    </td>
    <td align="left" class="lastCol">&nbsp;</td>
</tr>

<%
            }//end running ok
        }//next row
    } //end display of crons process

    //display my running stuff if any
    if (runningprocess.size() > 0) {
        //logger.debug("displaying running jobs");
        boolean loopOK = true;//used to protect this page from NPE from infos
        for (Iterator it = runningprocess.iterator(); it.hasNext();) {
            String[] infos = new String[20];
            JobDetail jd = (JobDetail) it.next();

            if (jd != null) {
                try {
                    infos = getJobInfos(jd);
                } catch (Exception e) {
                    //we catch all
                    logger.error("ERROR ON running jobs:",e);
                    loopOK = false;
                }
            } else {
                loopOK = false;
            }

            if (loopOK) {
                String type = getRessource("org.jahia.engines.processDisplay.op." + infos[0] + ".label", locale);
                String sitekey = "";
                String user = infos[2];
                String source = getRessource("org.jahia.engines.processDisplay.siteindexation.ofSite.label", locale) + "&nbsp;<b>" + infos[10] + "</b>";
                String fired = infos[4];
                long f = 0;
                try {
                    f = Long.parseLong(fired);
                } catch (NumberFormatException e) {
                    f = 0;
                }
                fired = printFriendlyRange(f, locale);
                String prefix_fired = getRessource("org.jahia.engines.processDisplay.status.running", locale);

%>
<tr valign="top">
    <td align="left"><%= type %></td>
    <td align="left" width="300px"><%= source %><%= sitekey %></td>

    <td align="left"><%= user %></td>
    <td align="left" valign="top">
      <%
        String serverId = jd.getJobDataMap().getString(BackgroundJob.JOB_SERVER);
        String interruptStatus = jd.getJobDataMap().getString(JahiaSiteIndexingJob.INTERRUPT_STATUS);

        String status = "";
        if ( BackgroundJob.STATUS_INTERRUPTED.equals(interruptStatus) ){
            status = getRessource("org.jahia.engines.processDisplay.jobInterrupted.label", locale, "Job interrupted");        
        } else if ( BackgroundJob.STATUS_ABORTED.equals(interruptStatus) ){
            status = getRessource("org.jahia.engines.processDisplay.jobAborted.label", locale, "Job aborted");        
        } else if ( JahiaSiteIndexingJob.INTERRUPT_STATUS_INTERRUPT_REQUESTED.equals(interruptStatus) ){
            status = getRessource("org.jahia.engines.processDisplay.jobInterruptRequested.label", locale, "Job interruption requested");        
        } else if ( JahiaSiteIndexingJob.INTERRUPT_STATUS_ABORT_REQUESTED.equals(interruptStatus) ){
            status = getRessource("org.jahia.engines.processDisplay.jobAbortRequested.label", locale, "Job abort requested");        
        } else { %>
        <img src="<%=request.getContextPath()%>/engines/images/waiting.gif" alt=" "/>&nbsp;
        <% } 
        if (status.length() > 0) {
        %><%= status %>&nbsp;(<% } %>
        <%=prefix_fired%>&nbsp;<%=fired%>
        <% if (status.length() > 0) {
        %>)<% } %>
    </td>
    <td align="left" class="lastCol">
      <%
        if ( !BackgroundJob.STATUS_ABORTED.equals(interruptStatus) && !JahiaSiteIndexingJob.INTERRUPT_STATUS_ABORT_REQUESTED.equals(interruptStatus) ){
      %>
        <a href="javascript:submitFormular('display','abortJob&job=<%=jd.getName()%>&jobGroup=<%=jd.getGroup()%>&serverId=<%=serverId%>&jobSiteKey=<%=jd.getJobDataMap().getString(BackgroundJob.JOB_SITEKEY)%>');"><img src="${pageContext.request.contextPath}/css/images/andromeda/icons/delete.png" alt=" "/>&nbsp;<%=getRessource("org.jahia.engines.processDisplay.operationsAbort.label", locale, "Abort")%></a><br/>
      <% } %>
      <%
        if ( !BackgroundJob.STATUS_INTERRUPTED.equals(interruptStatus) 
          && !JahiaSiteIndexingJob.INTERRUPT_STATUS_INTERRUPT_REQUESTED.equals(interruptStatus)
          && !BackgroundJob.STATUS_ABORTED.equals(interruptStatus) 
          && !JahiaSiteIndexingJob.INTERRUPT_STATUS_ABORT_REQUESTED.equals(interruptStatus) ){
      %>
        <a href="javascript:submitFormular('display','interruptJob&job=<%=jd.getName()%>&jobGroup=<%=jd.getGroup()%>&serverId=<%=serverId%>&jobSiteKey=<%=jd.getJobDataMap().getString(BackgroundJob.JOB_SITEKEY)%>');"><img src="${pageContext.request.contextPath}/css/images/andromeda/icons/media_pause.png" alt=" "/>&nbsp;<%=getRessource("org.jahia.engines.processDisplay.operationsInterrupt.label", locale, "Interrupt")%></a>
      <% } %>
      <%
        if ( BackgroundJob.STATUS_INTERRUPTED.equals(interruptStatus) ){
      %>
        <a href="javascript:submitFormular('display','resumeJob&job=<%=jd.getName()%>&jobGroup=<%=jd.getGroup()%>&serverId=<%=serverId%>&jobSiteKey=<%=jd.getJobDataMap().getString(BackgroundJob.JOB_SITEKEY)%>');"><img src="${pageContext.request.contextPath}/css/images/andromeda/icons/media_play_green.png" alt=" "/>&nbsp;<%=getRessource("org.jahia.engines.processDisplay.operationsResume.label", locale, "Resume")%></a>
      <% } %>
    </td>
</tr>

<%
            }//end running ok
        }//next row

    } //end display of running process
%>
<%

    //display my waiting(pooled process)
    if (wait.size() > 0) {
        //logger.debug("displaying pooled jobs");
        boolean loopOK = true;//used to protect this page from NPE from infos?
        //loop
        for (Iterator it = wait.iterator(); it.hasNext();) {
            String[] infos = new String[20];
            JobDetail jd = (JobDetail) it.next();
            if (jd != null) {
                String jname = jd.getName();

                try {
                    infos = getJobInfos(jd);
                } catch (Exception e) {
                    logger.error("ERROR on waiting jobs:",e);
                    loopOK = false;
                }
            } else {
                loopOK = false;
                logger.error("no waiting jobs");
            }
            if (loopOK) {
                String type = getRessource("org.jahia.engines.processDisplay.op." + infos[0] + ".label", locale);
                String user = infos[2];
                String source = getRessource("org.jahia.engines.processDisplay.siteindexation.ofSite.label", locale) + "&nbsp;<b>" + infos[10] + "</b>";
                String sitekey = "";
                if (infos[11] != null && !infos[11].equalsIgnoreCase(""))
                    sitekey = "&nbsp;" + getRessource("org.jahia.engines.processDisplay.tosite.label", locale) + "&nbsp;<b>" + infos[11] + "</b>";

                String fired = infos[3];
                long f = 0;
                try {
                    f = Long.parseLong(fired);
                } catch (NumberFormatException e) {
                    f = 0;
                }
                fired = printFriendlyRange(f, locale);

%>
<tr valign="top">
    <td align="left"><%= type %></td>
    <td align="left"><%= source %><%= sitekey %></td>
    <td align="left"><%= user %></td>
    <td align="left" valign="top">
        <%=getRessource("org.jahia.engines.processDisplay.status.waiting", locale)%>&nbsp;<%=fired%>
    </td>
    <td align="left" class="lastCol">
        <% String serverId = jd.getJobDataMap().getString(BackgroundJob.JOB_SERVER); %>
        <a href="javascript:submitFormular('display','abortJob&job=<%=jd.getName()%>&jobGroup=<%=jd.getGroup()%>&serverId=<%=serverId%>&jobSiteKey=<%=jd.getJobDataMap().getString(BackgroundJob.JOB_SITEKEY)%>');"><img src="${pageContext.request.contextPath}/css/images/andromeda/icons/delete.png" alt=" "/>&nbsp;<%=getRessource("org.jahia.engines.processDisplay.operationsAbort.label", locale, "Abort")%></a>
    </td>
</tr>
<%
            } //end loopOK
        } // end loop
    } //end processcount
    } // proess count == 0
} else if (command != null && command.equalsIgnoreCase("past")) {
    // here displaying past jobs
    // we need to limit the number of displayed past jobs?
    logger.debug("displaying archived jobs");
    int count = 0;
    int maxDisplayed = 10;//max

    if (size != null && !size.equalsIgnoreCase("0")) {
        maxDisplayed = Integer.parseInt(size);
    }
    String infoasked = request.getParameter("info");
    boolean loopOK = true;//used to protect this page from NPE from infos


    for (Iterator it = past.iterator(); it.hasNext();) {
        String[] infos = new String[20];
        JobDetail jd = (JobDetail) it.next();
        String rowcolor = "#FFFFFF";
        if (count % 2 > 0) rowcolor = "#EEFFFF";
		count++;
         if (count > maxDisplayed) break;//ignored subsequent and looping


        if (jd != null) {

            String jname = jd.getName();


            try {
                infos = getJobInfos(jd);
            } catch (Exception e) {
                logger.error("ERROR on past jobs:",e);
                loopOK = false;
            }

            if (loopOK) {
            //logger.debug("type past job:"+infos[0]);
                boolean failedjob= ((String) jd.getJobDataMap().get(BackgroundJob.JOB_STATUS)).equalsIgnoreCase(BackgroundJob.STATUS_FAILED);
                String type = getRessource("org.jahia.engines.processDisplay.op." + infos[0] + ".label", locale);
                String source = getRessource("org.jahia.engines.processDisplay.siteindexation.ofSite.label", locale) + "&nbsp;<b>" + infos[10] + "</b>";
                String sitekey = "";

                String user = infos[2];

                String fired = infos[4];
                String duration = infos[8];
                int during=0;

                if(infos[8]!=null && !infos[8].equals("")) {
                	during = Integer.parseInt(infos[8]);
                }
				duration=getFriendlyDuration(during,locale);



                long f = 0;
                try {
                    f = Long.parseLong(fired);
                } catch (NumberFormatException e) {
                    f = 0;
                }
                fired = printFriendlyRange(f, locale);
                /*
                String truc1="";
                String truc2="";
                String truc3="";
                if (infos[12] != null && !infos[12].equalsIgnoreCase("")) truc1=infos[12];//type
                if (infos[13] != null && !infos[13].equalsIgnoreCase("")) truc2=infos[13];//comments
                if (infos[14] != null && !infos[14].equalsIgnoreCase("")) truc3=infos[14];//taille
                */

                TreeOperationResult result = (TreeOperationResult) jd.getJobDataMap().get("result");
                if(failedjob) rowcolor="#FFCCCC";
%>
<tr valign="top" style="background-color:<%=rowcolor%>;" onMouseOver="this.style.backgroundColor='#FFFFEE'" onMouseOut="this.style.backgroundColor='<%=rowcolor%>'">
    <td align="left"><%= type %></td>
    <td align="left"><%= source %><%= sitekey %></td>
    <td align="left"><%= user %></td>
    <td align="left" valign="top">
    <%
    if(failedjob) {
   %>
   	<table width="100%" border="0" cellspacing="0" cellpadding="0"><tr valign="top">
            
            <td align="left" width="200px"><%=getRessource("org.jahia.engines.processDisplay.failed.label", locale)%></td>
            <td width="20px">&nbsp;&nbsp;</td>

        </tr></table>
   <% } else { %>
        <table width="100%" border="0" cellspacing="0" cellpadding="0"><tr valign="top">
            
            <td align="left" width="200px">
                <%=getRessource("org.jahia.engines.processDisplay.launched.label", locale)%>&nbsp;<%=fired%></td>
            <td width="20px">&nbsp;&nbsp;</td>
            <td align="left">
                <%=duration%>
            </td>
            <td align="right" valign="bottom">
                <%


                    if (result != null) {
                        String urlinfo;
                        pars = "";

                        if (infoasked != null) pars += ("&info=" + infoasked);

                        int jobstatus = result.getStatus();

                        if (infoasked != null && infoasked.equalsIgnoreCase(jname)) {
                            pars="";
                            if (size != null) pars += ("&size=" + size);
                            if(order != null) pars += ("&order=" + order);
                            if(cronlimit != null) pars += ("&cl=" + cronlimit);
                            urlinfo = "<a href='" + JahiaAdministration.composeActionURL(request, response, "search", "&sub=display&command=past" + pars) + "'><img src='" + request.getContextPath() + "/engines/images/about.gif' width='16' height='16' border='0' alt='info'></a>";
                        } else if(!failedjob) {
                            urlinfo = "";

                            if (size != null) pars += ("&size=" + size);
                            if(order != null) pars += ("&order=" + order);
							if(cronlimit != null) pars += ("&cl=" + cronlimit);
                            if (jobstatus == 0)
                                urlinfo = "<img src='" + request.getContextPath() + "/engines/images/icons/workflow/errors.gif' width='9' height='10' border='0'>";
                            if (jobstatus == 1)
                                urlinfo = "<img src='" + request.getContextPath() + "/engines/images/icons/workflow/accept.gif' width='9' height='10' border='0'>";
                            if (jobstatus == 2)
                                urlinfo = "<img src='" + request.getContextPath() + "/engines/images/icons/workflow/warnings.gif' width='9' height='10' border='0'>";
                            urlinfo += "&nbsp;&nbsp;<a href='" + JahiaAdministration.composeActionURL(request, response, "search", "&sub=display&command=past&info=" + jd.getName() + pars) + "'><img src='" + request.getContextPath() + "/engines/images/about.gif' width='16' height='16' border='0' alt='info'></a>";
                        } else {
                        	urlinfo = getRessource("org.jahia.engines.processDisplay.failed.label", locale);
                        }

                %>
                <%=urlinfo%>
                <% }%>
            </td>
        </tr></table>
        <% } %>
    </td>
    <td align="left" class="lastCol">&nbsp;</td>
</tr>
<% if (infoasked != null && infoasked.equalsIgnoreCase(jname)) {

    int jobstatus = result.getStatus();
%>
<tr><td colspan="3"></td><td bgcolor="#eeeeee"><br>
    <b><%=getRessource("org.jahia.engines.processDisplay.jobresult.label", locale)%>:</b><br>
    status:<%

    switch (jobstatus) {
        case 0:
            // errors
%>
    <img src="<%=request.getContextPath()%>/engines/images/icons/workflow/errors.gif" width="9" height="10"
         border="0"> <%=getRessource("org.jahia.engines.processDisplay.error.message", locale)%>
    <%
            break;
        case 1:
    %>
    <img src="<%=request.getContextPath()%>/engines/images/icons/workflow/accept.gif" width="9" height="10"
         border="0"> <%=getRessource("org.jahia.engines.processDisplay.success.message", locale)%>
    <%
            break;
        case 2:
            //warnings
    %>
    <img src="<%=request.getContextPath()%>/engines/images/icons/workflow/warnings.gif" width="9" height="10"
         border="0"> <%=getRessource("org.jahia.engines.processDisplay.error.message", locale)%>
    <%
          break;
    }
    /* not implemented yet
    if (!result.getErrors().isEmpty() || !result.getWarnings().isEmpty()) {
    %>
      <a href="<%= JahiaAdministration.composeActionURL(request, response, "search", "&sub=displaylog&command=warn&jd=" + jd.getName())%>"><%=getRessource("org.jahia.engines.processDisplay.allmessages.message", locale)%>
      </a>
    <%   
    }
    */
    %>
    <br>
    <br>

</td>
<td align="left" class="lastCol">&nbsp;</td>
</tr>
<%
                }//end of infoasked
            }//end loopOK
        }//data check
    }//end loop

} %>

</table>

</td>
</tr>
</table>
<script language="javascript">
    // to erase del from GET request
    if (window.location.search.indexOf("del=") != -1) {
        window.location = window.location.pathname;
    } else if (window.location.search.indexOf("sub=doindex") != -1) {
    	window.location = window.location.pathname + "?do=search&sub=display";
    }
</script>
<%
    }// end of dispok
    // debugging the scheduler
    //((SchedulerServiceImpl) service).stat();
%>
