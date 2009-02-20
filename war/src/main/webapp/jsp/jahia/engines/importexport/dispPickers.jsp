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

<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.content.ContainerDefinitionKey" %>
<%@ page import="org.jahia.content.ContentObject" %>
<%@ page import="org.jahia.data.containers.JahiaContainerDefinition" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.containers.ContentContainer" %>
<%@ page import="org.jahia.services.fields.ContentField" %>
<%@ page import="org.jahia.services.fields.ContentPageField" %>
<%@ page import="org.jahia.services.fields.ContentSmallTextField"%>
<%@page import = "org.jahia.services.pages.ContentPage"%>
<%@ page import="org.jahia.services.pages.JahiaPage"%>
<%@ page import="org.jahia.services.usermanager.JahiaUser"%>
<%@ page import="org.jahia.services.version.EntryLoadRequest"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.*"%>
<%@page import="org.jahia.hibernate.manager.SpringContextSingleton"%>
<%@page import="org.jahia.params.ProcessingContextFactory"%>
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%!

    /**
     * yet another new window to display some infos about Pickers.
     *
     * @author joe pillot
     * @version $Id$
     */

    private static final String bundle_prefix = "org.jahia.engines.importexport.contentpick";
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.engines.engine");


    /**
     *  to clean a list from pseudomodel artifacts
     * @param childs
     * @return a list
     */
    private List getChildFieldsOnly(List childs) {
        List results = new ArrayList();
        if (childs == null || childs.isEmpty()) {
            return results;
        }
        Iterator iterator = childs.iterator();
        ContentObject contentObject = null;
        while (iterator.hasNext()) {
            contentObject = (ContentObject) iterator.next();
            if (contentObject instanceof ContentField) {
                results.add(contentObject);
            }

        }
        return results;
    }

    /**
     * to get (on a best effort) something human readable or very close
     * @param o
     * @return a complete string whith link on pid
     */
    private String getInfoDisplay(Locale loc,ContentObject o,JahiaUser user) {
        String r = "";
        String t = "" + o.getID();
        //int key = o.getID();
        int pageID;
        int siteID = 1;
        boolean isPage = false;
        ContentPage thepage=null;
        boolean isActive=false;
        boolean isReadable=false;




            List li=new ArrayList();
            li.add(loc);
            EntryLoadRequest lr = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,li);


        if (o instanceof ContentContainer) {

            pageID = o.getPageID();
            siteID = o.getSiteID();
            try {
                thepage = ((ContentContainer) o).getPage();
                List l = getChildFieldsOnly(o.getChilds(null, null));

                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    String value = contentField.getValue(null, lr);
                    if (value != null && !value.trim().equals("") && !value.equals("<empty>")) {
                        t = value;
                        if (value.length() > 12) t = value.substring(0, 12) + " (...)";
                        //logger.debug("tkey=" + t);

                        break;
                    }
                }

                // case the content object is text
                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    if (contentField instanceof ContentSmallTextField) {
                        //logger.debug("enter step2:object is smalltextField");
                        String value = contentField.getValue(null, lr);
                        if (value != null && !value.trim().equals("") && !value.equals("<empty>")) {
                            t = value;
                            thepage= contentField.getPage();
                            pageID = contentField.getPageID();
                            siteID = contentField.getSiteID();
                            //logger.debug("text value key=" + t);
                            break;
                        }
                    }
                }

                //looping list of childs to check page type?
                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    if (contentField instanceof ContentPageField) {
                        //logger.debug("enter step3:object is Contentpage field");
                        ContentPage contentPage = ((ContentPageField) contentField).getContentPage(lr);
                        if (contentPage != null) {
                            thepage=contentPage;
	                        t = contentPage.getTitle(lr);
	                        if (thepage.getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT) {
	                            isPage = true;
	                            pageID = contentPage.getID();
	                            siteID = contentPage.getSiteID();
	                            //logger.debug("page value key=" + t);
	                            break;
	                        }
                        }
                    }
                }
                String sitekey=ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteID).getSiteKey();
                String url= Jahia.getContextPath() + Jahia.getServletPath() + "/site/"+sitekey+"/pid/"+pageID;

                //check the page accesibility and rights
                if (thepage.hasActiveEntries()) isActive = true;
                if (thepage.checkReadAccess(user)) isReadable = true;

                //logger.debug("URL---->"+url);
                //if (!isPage) {
                if(isActive && isReadable) {
                    ResourceBundle bundle = ResourceBundle.getBundle("JahiaInternalResources", loc);
                    r = new StringBuffer("<a href=\"").append(url).append("\" target=\"_new\">").append(t).append("</a><br>(pid: ").append(pageID).append(" ").append(getRessource(bundle, "org.jahia.engines.importexport.contentpick.searchsite.onesite.label","")).append(" ").append(sitekey).append(")").toString();
                } else  {
                    r = new StringBuffer("<span class=\"picklink\">").append(t).append("</span><br>(pid: ").append(pageID).append(" - site: ").append(sitekey).append(")").toString() ;
                }
                //} else {
                    //r = "<a href=\"" + Jahia.getContextPath() + Jahia.getServletPath() + "/pid/" + pageID + "\" target=\"_new\">" + t + "</a><br>(pid: " + pageID + " on site: " + sitekey + ")";
                //}

            } catch (JahiaException e) {
                logger.error("error", e);
                return r;
            }

        } else {
            logger.debug("contentobject not contentcontainer");
        }
        return r;
    }

    /**
     * internal method to render bundle resources
     * @return a string empty if resource is non existent
     */
    private String getRessource(ResourceBundle bundle, String label) {

        try {
            return bundle.getString(bundle_prefix + label);
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * internal method to render bundle resources
     * @param bundle
     * @param prefix
     * @param label
     * @return a string (empty if resource non existent)
     */
    private String getRessource(ResourceBundle bundle, String prefix, String label) {

        try {
            return bundle.getString(prefix + label);
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * to format a hit date in a friendly way
     *
     * @param date
     * @param l
     * @return a date string
     */
    public String printFriendlyDate(String date, Locale l, ResourceBundle enginebundle) {
        String s = "NA";
        long d = 0;

        //formatters
        SimpleDateFormat df = new SimpleDateFormat("EEEE dd MMM yyyy - HH:mm:ss", l);
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm", l);
        SimpleDateFormat sdf = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT,l);
        //calendar points
        GregorianCalendar now = new GregorianCalendar(l);
        now.set(Calendar.HOUR_OF_DAY, now.getActualMinimum(Calendar.HOUR_OF_DAY));
        now.set(Calendar.MINUTE, now.getActualMinimum(Calendar.MINUTE));
        now.set(Calendar.SECOND, now.getActualMinimum(Calendar.SECOND));

        GregorianCalendar hier = new GregorianCalendar(l);
        hier.roll(Calendar.DATE, false);
        hier.set(Calendar.HOUR_OF_DAY, hier.getActualMinimum(Calendar.HOUR_OF_DAY));
        hier.set(Calendar.MINUTE, hier.getActualMinimum(Calendar.MINUTE));
        hier.set(Calendar.SECOND, hier.getActualMinimum(Calendar.SECOND));

        //date value of hit
        String val = date;
        if (val != null
                && !"".equals(val.trim())
                && !val.equalsIgnoreCase("<text>")
                && !val.equalsIgnoreCase("<empty>")) {
            try {
                Date theday=sdf.parse(val);
                d = theday.getTime();
            } catch (Exception e) {
                logger.debug("error",e);
                try {
                    d = Long.parseLong(val);
                } catch (NumberFormatException e1) {
                    return date;
                }
            }
            try {

                Calendar c = Calendar.getInstance(l);
                c.setTime(new Date(d));
                if (c.after(now)) {
                    return getRessource(enginebundle, "org.jahia.time.today.prefix", "") + "&nbsp;" + sd.format(new Date(d));
                } else if (c.after(hier) && c.before(now)) {
                    return getRessource(enginebundle, "org.jahia.time.yesterday.prefix", "") + "&nbsp;" + sd.format(new Date(d));
                }

                s = df.format(new Date(d));
            } catch (NumberFormatException e) {
                //defensive code!
                logger.debug(e);

            }

        }
        return s;
    }

    private boolean isPageType(ContentObject o){
        boolean isPage = false;

        if (o instanceof ContentContainer) {

            try {
                List l = getChildFieldsOnly(o.getChilds(null, null));



                //looping list of childs to check page type?
                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    if (contentField instanceof ContentPageField) {
                        ContentPage contentPage = ((ContentPageField) contentField).getContentPage(EntryLoadRequest.STAGED);
                        if (contentPage!= null && contentPage.getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT) {
                            isPage = true;
                            break;
                        }
                    }
                }


            } catch (JahiaException e) {
                logger.error("error", e);

            }
        }
        return isPage;
    }
%>
<%
    //current locale
    final Locale locale=(Locale)request.getSession().getAttribute(ParamBean.SESSION_LOCALE);
    // to bundle html labels
    ResourceBundle enginebundle = ResourceBundle.getBundle("JahiaInternalResources", locale);
    ResourceBundle pickersbundle =  ResourceBundle.getBundle("jahiatemplates.common", locale);

    // jparams
    ParamBean jParams = ((ProcessingContextFactory)SpringContextSingleton.getInstance().getContext().getBean(ProcessingContextFactory.class.getName())).getContext(request, response, pageContext.getServletContext());
    // flag to warn if some param miss or error ocurred
    boolean render = true;

    // to get the id of picked
    String ids = request.getParameter("id");
    int id = 1;
    JahiaUser theUser = (JahiaUser) session.getAttribute(ParamBean.SESSION_USER);
    if (ids == null || ids.equalsIgnoreCase("")) {
        ids = "1";
        render = false;//inconsistent id
    }
    id = Integer.parseInt(ids);


    Set pickers = null;
    Iterator it;
    boolean isPage=false;
    String typeobject="";
    try {
        ContentContainer cc = ContentContainer.getContainer(id); //our picked object
        if (cc != null) {
            pickers = cc.getPickerObjects();
            isPage=isPageType(cc);
            ContainerDefinitionKey k = (ContainerDefinitionKey) cc.getDefinitionKey(null);
            try {
            JahiaContainerDefinition def = (JahiaContainerDefinition) ContentObject.getInstance(k);
            typeobject= def.getName();
        } catch (ClassNotFoundException e) {
            logger.debug(e);
        }
            //l = getChildFieldsOnly(cc.getChilds(null, EntryLoadRequest.CURRENT, null));
        } else {
            render = false;
        }
    } catch (JahiaException e) {
        logger.error("some problems:", e);
        render = false;
    }

%>
<html>
<head><title><%=getRessource(pickersbundle, "pickers.","title")%></title>


    <style type="text/css">
        body {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 12px;
            font-style: normal;
            font-weight: normal;
            margin: 10px;
            color: #838383;
            background-color: #336699;
        }

        #poweredby {
            color: #3E77B0;
        }

        td, th {
            font-size: 12px;
        }

        .picklink {
            text-decoration: none;
            font-weight: bold;
            color: #888888;
        }
        h3 {
        font-size: 16px;

        }

        h3, a {
    color: #398EC3;
    text-decoration: none;
    font-weight: bold;

}

.version {
    float: right;
    text-align: right;
    font-size: 9px;
    vertical-align: bottom;
}

#copyright {
	font-family: Verdana,Arial,Helvetica,sans-serif;
	font-size: 12px;
	color: #838383;

}
    </style>
</head>
<%
    if (render) {
%>
<body>
<table cellspacing="0" cellpadding="10" width="100%" border="0" bgcolor="#FFFFFF">
<TR><td width="48px">
<img src="<%=request.getContextPath()%>/jsp/jahia/engines/images/branch_view.gif" width="48" height="48" border="0"></td><td align="left"><h3><%=getRessource(pickersbundle, "pickers.","title")%></h3></td></tr>
<tr><td colspan="2">
<TABLE cellSpacing=1 cellPadding=3 width="100%" border=0>
    <TBODY>


        <TR>
            <TD class="text">
                <fieldset>
                    <legend>
                        <%=getRessource(pickersbundle, "pickers.","table.title")%></legend>
                    <table width="100%" border="0" cellspacing="1" cellpadding="0">
                        <tr bgcolor="#eeeeee" valign="top">
                            <th><%=getRessource(enginebundle, ".results.name.label")%></th>

                            <th><%=getRessource(enginebundle, ".results.publishdate.label")%></th>
                        </tr>
                        <%
                            if (pickers != null) {
                                it = pickers.iterator();
                                int count = 0;
                        %>

                        <!--ID liste des pickers pour le picked id:-->
                        <%=getRessource(pickersbundle, "pickers.","picked.message1")%>:<%=id%>&nbsp;<%=getRessource(pickersbundle, "pickers.","picked.message2")%><br/>
                        <%
                            if(isPage) {
                        %>
                        page
                        <%
                            } else {
                        %>
                        <!--%=getRessource(pickersbundle, "pickers.","picked.type")%-->
                        <%=typeobject%>
                        <%
                            }
                        %>
                        <br>
                        <%
                            while (it.hasNext()) {

                                ContentObject co = (ContentObject) it.next();

                                String info = getInfoDisplay(locale,co,theUser);
                                if(info.equals("")) continue;
                                String author="";
                                String lastcontributor ="";
                                String lastpublishingdate ="";
                                try {
                                    //author = co.getMetadata("createdBy").getValue(null, EntryLoadRequest.CURRENT);
                                    //lastcontributor = co.getMetadata("lastModifiedBy").getValue(null, EntryLoadRequest.CURRENT);
                                   author = co.getMetadataValue("createdBy",jParams,"");
                                   lastcontributor = co.getMetadataValue("lastModifiedBy",jParams,"");
                                   lastpublishingdate= co.getMetadataValue("lastModified",jParams,"");

                                    //logger.debug("summary metadatas: author:"+author+" lastcontrib:"+lastcontributor+" lastpubdate:"+lastpublishingdate);
                                } catch (JahiaException e) {
                                    logger.error("error:",e);
                                }
                                String color = "#FFFFFF";
                                if (count % 2 > 0) color = "#EEFFFF";

                        %>
                        <tr style="background-color:<%=color%>" onMouseOver="this.style.backgroundColor='#FFFFEE'"
                            onMouseOut="this.style.backgroundColor='<%=color%>'">
                            <td><%=info%></td><td><%=printFriendlyDate(lastpublishingdate,jParams.getLocale(), enginebundle)%></td>
                        </tr>
                        <%
                                count++;
                            }
                        } else {

                        %>
                        <tr><td><%=getRessource(pickersbundle, "pickers.","error.message")%></td></tr>
                        <% }
                        %>


                    </table>
                </fieldset>
            </TD></tr>


    </TBODY>
</TABLE>
</td></tr>
<tr><td colspan="2">

<table border="0" width="100%"><tr><td width="48"><img name="logo" src="../../css/images/logo/logo-jahia.gif" border="0" width="45" height="34"></td><td><img src="../images/pix.gif" border="0" width="1" height="10">
<div id="copyright"><%=Jahia.COPYRIGHT%>&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources"
 resourceName="org.jahia.Jahia.copyright.label"/></div><span class="version">Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%></span>
</td></tr></table>
</td></tr>
</table>
</body>
</html>
<%
} else {
%>
<body>
error
</body>
<% } %>