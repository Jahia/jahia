<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><%
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, post-check=0, pre-check=0");
%>
<%@ page import="org.apache.log4j.Logger,
                 org.jahia.bin.Jahia,
                 org.jahia.content.NodeOperationResult,
                 org.jahia.content.TreeOperationResult,
                 org.jahia.engines.EngineMessage,
                 org.jahia.exceptions.JahiaException,
                 org.jahia.params.ParamBean,
                 org.jahia.registries.ServicesRegistry,
                 org.jahia.services.scheduler.SchedulerService,
                 org.jahia.services.usermanager.JahiaUser,
                 org.quartz.JobDetail,
                 java.text.MessageFormat,
                 java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%!
    /**
     * jsp pages to display logs
     * $Id: $
     */
    private static final Logger logger = Logger.getLogger("jsp.jahia.engines");
    private static SchedulerService service = ServicesRegistry.getInstance().getSchedulerService();




    /**
     * internal method to render bundle resources
     * @return a string empty if resource is non existent
     * @param label the keylabel
     * @param l the locale
     */
    private String getRessource(String label, Locale l) {
        try {
            return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
        } catch (Exception e) {
            try {
                return ResourceBundle.getBundle("JahiaMessageResources", l).getString(label);
            } catch (Exception e1) {
                return "";
            }
        }
    }
%>
<%
    JahiaUser theUser = (JahiaUser) session.getAttribute(ParamBean.SESSION_USER);
    Locale locale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE);
    if (locale == null) locale = request.getLocale();//fail-over locale
    boolean dispOK = true;//error flag
    String errorMessage = "initialization error!";
    if (theUser == null || theUser.getUsername().equals("guest")) {
        dispOK = false;
        errorMessage = "please this page is reserved to logged users!";
    }

    // passed jobs
    if (theUser == null) {
        dispOK = false;
        errorMessage = "please this page is reserved to logged users!";
    }
    List alls = null;
    try {
        alls = service.getAllJobsDetails();
    } catch (JahiaException e) {
        logger.debug("error", e);
        errorMessage = "getAllJobsDetails unavailable!";
        dispOK = false;
    }

    if (!dispOK) {
        // error display
%>
<html>
<head><title><%=getRessource("org.jahia.engines.processDisplay.title", locale)%>
</title>
</head>

<body>
<%=errorMessage%><br>
<script language="javascript">
    //document.close();//closing now!
</script>
</body>
</html>
<%


} else {
    // logs display
%>
<html>
<head><title><%=getRessource("org.jahia.engines.processDisplay.title", locale)%>
</title>
<link type="text/css" rel="stylesheet" href="styles.css"/>
</head>

<body>

<table cellspacing="0" cellpadding="15" width="100%" border="0" bgcolor="#FFFFFF">
    <tr valign="bottom">
        <td width="48px">
            <a href="javascript:history.back()" alt="back" title="back"><img src="<%=request.getContextPath()%>/engines/images/gauge.gif" width="48" height="48" border="0"></a></td>
        <td align="left"><h3>
            <%=getRessource("org.jahia.engines.processDisplay.label", locale)%>
        </h3></td>
        <td align="right"><div id="buttons">
                    <div class="button">
                        <a href="javascript:history.back();"
                           title="<%=getRessource("org.jahia.window.close", locale)%>"><%=getRessource("org.jahia.engines.processDisplay.back", locale)%></a>
                    </div>
                </div></td>
    </tr>

    <tr>
        <td colspan="3">
<%
    if (dispOK) {
        //looping to get all jobs
        JobDetail jd = null;
        dispOK = false;
        // check for job requested
        for (Iterator iterator = alls.iterator(); iterator.hasNext();) {
            jd = (JobDetail) iterator.next();
            if (jd.getName().equals(request.getParameter("jd"))) {
                dispOK = true;
                break;
            }
        }
        if (!dispOK) {
        errorMessage = "job not found!";
        }
        if (dispOK) {

            TreeOperationResult result = (TreeOperationResult) jd.getJobDataMap().get("result");
            
                if(result.getWarnings().size()>0){
                    %><br/><h2>Warnings</h2><%
                }
                // warn messages
                for (Object aWarning : result.getWarnings()) {
                    NodeOperationResult thenode = (NodeOperationResult)aWarning;
                    EngineMessage msg = thenode.getMsg();
                    String keyValue = msg != null ? getRessource(msg.getKey(), locale) : null;
                    if (keyValue != null) {
                        MessageFormat msgFormat = new MessageFormat(keyValue);
                        msgFormat.setLocale(locale);
                        %><%= msgFormat.format(msg.getValues()) %><br/><%
                    } else if (thenode.getComment() != null) {
                        %><%= thenode.getComment() %><br/><%
                    }
                }

                if(result.getErrors().size()>0){
                    %><br/><h2>Errors</h2><%
                }
                // error messages
                for (Object anError : result.getErrors()) {
                    NodeOperationResult thenode = (NodeOperationResult)anError;                    
                    EngineMessage msg = thenode.getMsg();
                    String keyValue = msg != null ? getRessource(msg.getKey(), locale) : null;
                    if (keyValue != null) {
                        MessageFormat msgFormat = new MessageFormat(keyValue);
                        msgFormat.setLocale(locale);
                        %><%= msgFormat.format(msg.getValues()) %><br/><%
                    } else if (thenode.getComment() != null) {
                        %><%= thenode.getComment() %><br/><%
                    }
                    }
                }
    }
%>
        </td>
    </tr>
    <tr>
        <td colspan="3">

            <table border="0" width="100%">
                <tr>
                    <td width="48"><img name="logo"
                                        src="<%=request.getContextPath()%>/css/images/logo/logo-jahia.gif"
                                        border="0"
                                        width="45" height="34"></td>
                    <td><img
                            src="<%=request.getContextPath()%>/engines/images/pix.gif" border="0" width="1"
                            height="10">

                        <div id="copyright"><%=Jahia.COPYRIGHT%>&nbsp;
                            <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                    resourceName="org.jahia.Jahia.copyright.label"/>
                        </div><span
                            class="version">Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=
                    Jahia.getBuildNumber()%></span>

                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<div id="poweredby">
    <span>Powered by Jahia</span>
</div>
<script language="javascript">
    // to erase del from GET request
    if (window.location.search.indexOf("del=") != -1) {
        window.location = window.location.pathname;
    }
</script>
</body>
</html>
<% } %>
