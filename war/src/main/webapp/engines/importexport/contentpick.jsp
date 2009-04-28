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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page import="org.jahia.content.ContentObject,
                 org.jahia.content.ObjectKey,
                 org.jahia.data.fields.JahiaField,
                 org.jahia.data.search.JahiaSearchHit" %>
<%@ page import="org.jahia.data.search.JahiaSearchResult" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.jahia.gui.HTMLToolBox" %>
<%@ page import="org.jahia.services.containers.ContentContainer" %>
<%@ page import="org.jahia.services.fields.ContentField" %>
<%@ page import="org.jahia.services.pages.JahiaPageService" %>
<%@ page import="org.jahia.services.pages.PageProperty" %>
<%@ page import="org.jahia.services.sites.JahiaSite" %>
<%@ page import="org.jahia.services.sites.JahiaSitesService" %>
<%@ page import="java.lang.Exception" %>
<%@ page import="java.lang.Integer" %>
<%@ page import="java.lang.Long" %>
<%@ page import="java.lang.NumberFormatException" %>
<%@ page import="java.lang.String" %>
<%@ page import="java.lang.StringBuffer" %>
<%@ page import="java.lang.System" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.services.pages.JahiaPage" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.registries.EnginesRegistry" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.version.EntryLoadRequest" %>
<%@ page import="org.jahia.utils.TextHtml" %>
<%@ page import="org.jahia.engines.importexport.ManageContentPicker" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%!


    // declarations
    private static final String bundle_prefix = "org.jahia.time";
    private static JahiaSitesService siteService = ServicesRegistry.getInstance().getJahiaSitesService();
    private static JahiaPageService pgService = ServicesRegistry.getInstance().getJahiaPageService();
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("jsp.jahia.engines.engine");

    /**
     * extract a description teaser if the description input is more than max words.
     * (remove previously all word & html weird tags) else return the description unchanged or tags cleaned
     * @param description a description string
     * @param max max words
     * @param cleantag clean boolean
     * @return a description string (cleaned or not) or a mini description. an empty result means use initial description unchanged
     */
    private String extractDescriptionTeaser(String description, int max, boolean cleantag) {
        if(description==null || description.equals("")) return "";
        String pattern = "<.*?>";
        String result = description.replaceAll(pattern, " ");//removing tags
        pattern = "\\s+";
        result = result.replaceAll(pattern, " ").trim(); //removing extraspace
        StringTokenizer tok = new StringTokenizer(result, " ");
        int wordcount = tok.countTokens();
        if (wordcount > max) {
            //create the minidescription
            String minidescription = "";
            int c = 0;
            while (tok.hasMoreTokens() && c < max + 1) {
                String el = tok.nextToken();
                minidescription = minidescription + el + " ";
                c++;
            }
            return minidescription;
        } else {
            //return the description cleaned
            if (cleantag) return result;
        }
        return "";//this return means
    }

    /**
     * to format a hit date in a friendly way
     * @param hit the hit object
     * @param datekey the key
     * @param l locale
     * @return a date string
     */
    public String printFriendlyDate(JahiaSearchHit hit, String datekey, Locale l) {
        String s = "NA";
        long d;

        //formatters
        SimpleDateFormat df = new SimpleDateFormat("EEEE dd MMM yyyy - HH:mm:ss", l);
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm", l);

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
        String val = hit.getParsedObject().getValue(datekey);
        if (val != null
                && !"".equals(val.trim())
                && !val.equalsIgnoreCase("<text>")
                && !val.equalsIgnoreCase("<empty>")) {
            try {
                d = Long.parseLong(val);
                Calendar c = Calendar.getInstance(l);
                c.setTime(new Date(d));
                if (c.after(now)) {
                    long diftime = System.currentTimeMillis() - d;
                    long difmin = diftime / 60000;
                    if (difmin < 60) {
                        String pref = getRessource(ResourceBundle.getBundle("JahiaInternalResources", l), ".today.rangeprefix");
                        return pref + " " + difmin + " minutes";
                    } else {
                        String pref = getRessource(ResourceBundle.getBundle("JahiaInternalResources", l), ".today.prefix");
                        return pref + " " + sd.format(new Date(d));
                    }
                } else if (c.after(hier) && c.before(now)) {
                    String pref = getRessource(ResourceBundle.getBundle("JahiaInternalResources", l), ".yesterday.prefix");
                    return pref + " " + sd.format(new Date(d));
                }

                s = df.format(new Date(d));
            } catch (NumberFormatException e) {
                //defensive code!
                logger.debug(e);

            }

        }
        return s;
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
     * to get the urlkey if available
     * @param pid
     * @return
     */
    private String getUrlKey(int pid) {
        Iterator<PageProperty> propval = null;
        String ukey = "";
        try {
            propval = pgService.getPageProperties(pid).values().iterator();
        } catch (JahiaException e) {
            return "";
        }

        while (propval.hasNext()) {
            PageProperty prop = (PageProperty) propval.next();

            if (prop.getName().equals(PageProperty.PAGE_URL_KEY_PROPNAME))
                ukey = prop.getValue();
        }

        return ukey;
    }

    private String getPagePath(ContentPage page, ProcessingContext jParams, EntryLoadRequest elr,int pathsize) {
        Iterator<ContentPage> pages;
        StringBuffer bf = new StringBuffer();
        int cpath=0;
        try {
            pages = page.getContentPagePath(elr, jParams.getOperationMode(), jParams.getUser());
            while (pages.hasNext()) {
                ContentPage thePage = (ContentPage) pages.next();
                String _label = thePage.getTitle(elr);
        if(_label==null) _label="nd";

                cpath +=_label.length();
                if(pathsize==0 || cpath<pathsize-3){
                bf.append("<a href=\"").append(thePage.getUrl(jParams)).append("\" target=\"_new\">").append(_label).append("</a>");
                bf.append(" / ");
                cpath ++;
                } else {
                    bf.append("...");
                    break;
                }
            }

        } catch (JahiaException e) {
            return "";
        }
        return bf.toString();
    }
%>
<%
/**
 * @version $Id$
 */

Map<String, Object> engineMap = (Map<String, Object>) request.getAttribute("org.jahia.engines.EngineHashMap");
JahiaSearchResult sr = (JahiaSearchResult) engineMap.get("searchResults");
String theScreen = (String) engineMap.get("screen");
//final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
final String URL = request.getContextPath();
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
if (elh != null) {
    jParams.setCurrentLocale(elh.getCurrentLocale());
}

// to bundle html labels
ResourceBundle bundle = ResourceBundle.getBundle("JahiaInternalResources", jParams.getLocale());
String bundle_prefix = "org.jahia.engines.importexport.contentpick";

// type of object
String objectType = (String) engineMap.get("objecttype");

//parameter to order the results (def:score)
String orderBy = (String) engineMap.get("orderby");
logger.debug("enginemap orderby=" + orderBy);
if (orderBy == null || orderBy.equalsIgnoreCase("")) {
    logger.debug("set order to def title");
    orderBy = "title";
}
// # of virtual sites
int sitecount = 0;
try {
    sitecount = siteService.getNbSites();
} catch (JahiaException e) {
    logger.error(e);
}
// server path
String serverpath = "http://" + request.getServerName() + ":" + request.getServerPort();
String searchsite = (String) engineMap.get("searchSite");

//size  & order of results displayed
String size = (String) engineMap.get("size");
String asc = (String) engineMap.get("asc");
if (asc == null) asc = "1";//def asc=1, asc=0 if desc
String sortIcon = URL + "/engines/images/icons/sort_" + ("1".equals(asc) ? "ascending" : "descending") + ".gif";

// newsearch
String newsearch = request.getParameter("newsearch");
StringBuffer sb;//create the main stringbuffer
StringBuffer sb2;//create the secondary stringbuffer(used to multisites)

//concat mode
String concatmode = (String) engineMap.get("concat");
if (concatmode == null) concatmode = "or";

//smode
String smode = (String) engineMap.get("smode");
if (smode == null) smode = "or";

//main condition
String cond1 = (String) engineMap.get("smode");
if (cond1 == null) cond1 = "or";

// specific code to render link to call select_page engine
Map<String, Object> selectPageURLParams = new HashMap<String, Object>();
selectPageURLParams.put(SelectPage_Engine.OPERATION, SelectPage_Engine.LINK_OPERATION);
selectPageURLParams.put(SelectPage_Engine.PARENT_PAGE_ID, new Integer(0));
selectPageURLParams.put(SelectPage_Engine.PAGE_ID, new Integer(0));
int homepageid = 0;
if (!searchsite.equalsIgnoreCase("all")) {
    homepageid = ServicesRegistry.getInstance().getJahiaSitesService().getSite(Integer.parseInt(searchsite)).getHomePageID();
    logger.debug("homepageid=" + homepageid + " on site:" + searchsite);
}
selectPageURLParams.put(SelectPage_Engine.HOMEPAGE_ID, new Integer(homepageid));
String selectPageURL = ((SelectPage_Engine) EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine")).renderLink(jParams, selectPageURLParams);

String query2 = (String) engineMap.get("query2");
String query21 = (String) engineMap.get("query21");
String query22 = (String) engineMap.get("query22");
String query23 = (String) engineMap.get("query23");
String query24 = (String) engineMap.get("query24");

String query3 = (String) engineMap.get("query3");
String query31 = (String) engineMap.get("query31");
String query32 = (String) engineMap.get("query32");
String query33 = (String) engineMap.get("query33");
String query34 = (String) engineMap.get("query34");

String query4 = (String) engineMap.get("query4");
String query41 = (String) engineMap.get("query41");
String query42 = (String) engineMap.get("query42");
String query43 = (String) engineMap.get("query43");
String query44 = (String) engineMap.get("query44");

String smode2 = (String) engineMap.get("smode2");
String smode3 = (String) engineMap.get("smode3");
String smode4 = (String) engineMap.get("smode4");

if (query2 == null) query2 = "";
if (query21 == null) query21 = "";
if (query22 == null) query22 = "";
if (query23 == null) query23 = "";
if (query24 == null) query24 = "";

if (query3 == null) query3 = "";
if (query31 == null) query31 = "";
if (query32 == null) query32 = "";
if (query33 == null) query33 = "";
if (query34 == null) query34 = "";

if (query4 == null) query4 = "";
if (query41 == null) query41 = "";
if (query42 == null) query42 = "";
if (query43 == null) query43 = "";
if (query44 == null) query44 = "";

if (smode2 == null) smode2 = "";
if (smode3 == null) smode3 = "";
if (smode4 == null) smode4 = "";

// to create the additional queries html
sb = new StringBuffer();
boolean displayConcat=false;
int paramqueries=1;
for (int z = 2; z < 5; z++) {
    sb.append("<div id=\"sc");
    sb.append(z);

    sb.append("\" class=\"switchcontent");
    //visibility modules
    if (z == 2 && ("true".equals(request.getParameter("visibility_sc2")) || !query21.equalsIgnoreCase("") || !query22.equalsIgnoreCase("") || !query23.equalsIgnoreCase("") || !query24.equalsIgnoreCase(""))) {
      sb.append(1); //visibility
      displayConcat=true;
      paramqueries++;
    }
    if (z == 3 && ("true".equals(request.getParameter("visibility_sc3")) || !query31.equalsIgnoreCase("") || !query32.equalsIgnoreCase("") || !query33.equalsIgnoreCase("") || !query34.equalsIgnoreCase(""))) {
      sb.append(1); //visibility
      displayConcat=true;
      paramqueries++;
    }
    if (z == 4 && ("true".equals(request.getParameter("visibility_sc4")) || !query41.equalsIgnoreCase("") || !query42.equalsIgnoreCase("") || !query43.equalsIgnoreCase("") || !query44.equalsIgnoreCase(""))) {
      sb.append(1); //visibility
      displayConcat=true;
      paramqueries++;
    }
    sb.append("\"><input type=\"hidden\" name=\"visibility_sc").append(z).append("\" id=\"visibility_sc").append(z).append("\" value=\"").append(request.getParameter("visibility_sc" + z)).append("\"/>");
    sb.append("<table class=\"formTable\" cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
    sb.append("<tr>");
    sb.append("  <th width=\"150\">");
    sb.append(bundle.getString(bundle_prefix + ".prefix.author.label"));//which
    sb.append("  </th>");
    sb.append("  <td>");

    //condition modules

    sb.append("<select name=\"condition");
    sb.append(z);
    sb.append("\" onchange=\"goSearch(this.name)\">");
    sb.append("<option value=\"createdby\" ");
    if (z == 2 && smode2.equalsIgnoreCase("createdby")) sb.append("selected=\"selected\"");
    if (z == 3 && smode3.equalsIgnoreCase("createdby")) sb.append("selected=\"selected\"");
    if (z == 4 && smode4.equalsIgnoreCase("createdby")) sb.append("selected=\"selected\"");
    sb.append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.author.label"));
    sb.append("</option>");
    sb.append("<option value=\"keywords\" ");
    if (z == 2 && smode2.equalsIgnoreCase("keywords")) sb.append("selected=\"selected\"");
    if (z == 3 && smode3.equalsIgnoreCase("keywords")) sb.append("selected=\"selected\"");
    if (z == 4 && smode4.equalsIgnoreCase("keywords")) sb.append("selected=\"selected\"");
    sb.append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.keywords.label"));
    sb.append("</option>");
    sb.append("<option value=\"pageid\" ");
    if (z == 2 && smode2.equalsIgnoreCase("pageid")) sb.append("selected=\"selected\"");
    if (z == 3 && smode3.equalsIgnoreCase("pageid")) sb.append("selected=\"selected\"");
    if (z == 4 && smode4.equalsIgnoreCase("pageid")) sb.append("selected=\"selected\"");
    sb.append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.pageid.label"));
    sb.append("</option>");
    sb.append("<option value=\"date\" ");
    if (z == 2 && smode2.equalsIgnoreCase("date")) sb.append("selected=\"selected\"");
    if (z == 3 && smode3.equalsIgnoreCase("date")) sb.append("selected=\"selected\"");
    if (z == 4 && smode4.equalsIgnoreCase("date")) sb.append("selected=\"selected\"");
    sb.append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.date.label"));
    sb.append("</option>");
    sb.append("</select>");
    sb.append("  </td>");
    sb.append("  <td width=\"40\">");
    if (z != 4) {
        sb.append("<a href=\"javascript:expandcontent('sc");
        sb.append((z + 1));
        sb.append("')\" title=\"").append(bundle.getString(bundle_prefix + ".criteria.add.label")).append("\"><img src=\"").append(URL).append("/engines/images/adding.png\" width=\"16\"height=\"16\" border=\"0\"></a>");
    }
    sb.append("<a href=\"javascript:contractcontent('sc");
    sb.append(z);
    sb.append("')\" title=\"").append(bundle.getString(bundle_prefix + ".criteria.remove.label")).append("\"><img src=\"").append(URL).append("/engines/images/deleting.png\" width=\"16\" height=\"16\" border=\"0\"></a>");
    sb.append("  </td>");
    sb.append("</tr>");
    sb.append("</table>");

    // author submodule
    sb.append("<span id=\"input");
    sb.append(z);
    sb.append(1);
    sb.append("\" class=\"contvis");
    if ((z == 2 && smode2.equalsIgnoreCase("createdby"))
            || (z == 3 && smode3.equalsIgnoreCase("createdby"))
            || (z == 4 && smode4.equalsIgnoreCase("createdby"))
            ) sb.append("1\">");
    else
        sb.append("\">");
    sb.append("<table class=\"formTable\" cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
    sb.append("<tr>");
    sb.append("  <th width=\"150\">");
    sb.append(bundle.getString(bundle_prefix + ".postfix.author.label"));// select type author
    sb.append("  </th>");
    sb.append("  <td colspan=\"2\">");
    sb.append("&nbsp;<select name=\"condition");
    sb.append(z);
    sb.append(1);
    sb.append("\" onchange=\"goSearch(this.name)\">");
    sb.append("<option value=\"createdby\" >");
    sb.append(bundle.getString(bundle_prefix + ".condition.creator.label"));
    sb.append("</option>");
    sb.append("<option value=\"contributor\" >");
    sb.append(bundle.getString(bundle_prefix + ".condition.contributor.label"));
    sb.append("</option>");
    sb.append("<option value=\"all\" >");
    sb.append(bundle.getString(bundle_prefix + ".condition.both.label"));

    sb.append("</option>");
    sb.append("</select>");
    sb.append("<INPUT size=20 value=\"");
    if (z == 2) sb.append(query21);
    if (z == 3) sb.append(query31);
    if (z == 4) sb.append(query41);
    sb.append("\" name=\"query");
    sb.append(z);
    sb.append(1);
    sb.append("\" onkeypress=\"submitenter(this,event)\"/>");
    sb.append("  </td>");
    sb.append("</tr>");
    sb.append("</table>");
    sb.append("</span>");


    sb.append("<span id=\"input");
    sb.append(z);
    sb.append(2);
    sb.append("\" class=\"contvis");
    if ((z == 2 && smode2.equalsIgnoreCase("keywords"))
            || (z == 3 && smode3.equalsIgnoreCase("keywords"))
            || (z == 4 && smode4.equalsIgnoreCase("keywords"))
            ) sb.append("1\">");
    else
        sb.append("\">");
    sb.append("<table class=\"formTable\" cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
    sb.append("<tr>");
    sb.append("  <th width=\"150\">");
    sb.append(bundle.getString(bundle_prefix + ".postfix.keywords.label"));//keywords submodule
    sb.append("  </th>");
    sb.append("  <td colspan=\"2\">");
    sb.append("&nbsp;<INPUT size=30 value=\"");
    if (z == 2) sb.append(query22);
    if (z == 3) sb.append(query32);
    if (z == 4) sb.append(query42);
    sb.append("\" name=\"query");
    sb.append(z);
    sb.append(2);
    sb.append("\" onkeypress=\"submitenter(this,event)\"/>");
    sb.append("  </td>");
    sb.append("</tr>");
    sb.append("</table>");
    sb.append("</span>");

    //page id submodule
    sb.append("<span id=\"input");
    sb.append(z);
    sb.append(3);
    sb.append("\" class=\"contvis");
    if ((z == 2 && smode2.equalsIgnoreCase("pageid"))
            || (z == 3 && smode3.equalsIgnoreCase("pageid"))
            || (z == 4 && smode4.equalsIgnoreCase("pageid"))
            ) sb.append("1\">");
    else
        sb.append("\">");
    sb.append("<table class=\"formTable\" cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
    sb.append("<tr>");
    sb.append("  <th width=\"150\">");
    sb.append(bundle.getString(bundle_prefix + ".postfix.author.label"));//keywords submodule
    sb.append("  </th>");
    sb.append("  <td colspan=\"2\">");

    sb.append("&nbsp;<INPUT size=20 value=\"");
    if (z == 2) sb.append(query23);
    if (z == 3) sb.append(query33);
    if (z == 4) sb.append(query43);
    sb.append("\" name=\"query");
    sb.append(z);
    sb.append(3);
    sb.append("\" onkeypress=\"submitenter(this,event)\"/>");
    sb.append("  </td>");
    sb.append("</tr>");
    sb.append("</table>");
    sb.append("</span>");

    //date submodule
    sb.append("<span id=\"input");
    sb.append(z);
    sb.append(4);
    sb.append("\" class=\"contvis");
    if ((z == 2 && smode2.equalsIgnoreCase("date"))
            || (z == 3 && smode3.equalsIgnoreCase("date"))
            || (z == 4 && smode4.equalsIgnoreCase("date"))
            ) sb.append("1\">");
    else
        sb.append("\">");
    sb.append("<table class=\"formTable\" cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
    sb.append("<tr>");
    sb.append("  <th width=\"150\">");
    // select type date
    sb.append(bundle.getString(bundle_prefix + ".postfix.author.label"));
    sb.append("  </th>");
    sb.append("  <td colspan=\"2\">");

    sb.append("&nbsp;<select name=\"condition");
    sb.append(z);
    sb.append(4);
    sb.append("\">");
    sb.append("<option value=\"pub\" selected=\"selected\">");//default:publication date
    sb.append(bundle.getString(bundle_prefix + ".results.publishdate.label"));
    sb.append("</option>");
    sb.append("<option value=\"mod\" >");
    sb.append(bundle.getString(bundle_prefix + ".condition.contributiondate.label"));
    sb.append("</option>");
    sb.append("<option value=\"cre\" >");
    sb.append(bundle.getString(bundle_prefix + ".results.creationdate.label"));
    sb.append("</option>");
    sb.append("</select>");
    // select date range
    sb.append("<select name=\"condition");
    sb.append(z);
    sb.append(4);
    sb.append(4);
    String selectedDate = request.getParameter("condition" + z + "44");
    sb.append("\" onchange=\"goSearch(this.name)\">");
    sb.append("<option value=\"-\"").append("-".equals(selectedDate) ? " selected=\"selected\"" : "").append(">");//default:vide
    sb.append(bundle.getString(bundle_prefix + ".condition.daterange.message.label"));
    sb.append("</option>");
    sb.append("<option value=\"week\"").append("week".equals(selectedDate) ? " selected=\"selected\"" : "").append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.daterange.week.label"));
    sb.append("</option>");
    sb.append("<option value=\"month\"").append("month".equals(selectedDate) ? " selected=\"selected\"" : "").append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.daterange.month.label"));
    sb.append("</option>");
    sb.append("<option value=\"months\"").append("months".equals(selectedDate) ? " selected=\"selected\"" : "").append(">");
    sb.append(bundle.getString(bundle_prefix + ".condition.daterange.months.label"));
    sb.append("</option>");
    sb.append("</select>");
    sb.append("  </td>");
    sb.append("</tr>");
    sb.append("</table>");
    sb.append("</span>");

    // the browse link
    sb.append("<span id=\"sitemap");
    sb.append(z);
    sb.append("\" class=\"contvis\">");
    sb.append("<table class=\"formTable\" cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
    sb.append("<tr>");
    sb.append("  <th width=\"150\" colspan=\"3\">");
    sb.append("<a href=\"javascript:callSelectPageLink(");
    sb.append(z);
    sb.append(")\">");
    sb.append(bundle.getString(bundle_prefix + ".condition.sitemapview.label"));
    sb.append("</a>");
    sb.append("  </th>");
    sb.append("</tr>");
    sb.append("</table>");
    sb.append("</span>");


    sb.append("</div>");

}
%>
<utility:setBundle basename="JahiaInternalResources"/>
<!-- Begin contenpick.jsp -->
<!-- css specific for contentpicker -->
<style type="text/css">
    .switchcontent {
        display: none;
    }
    .switchcontent1 {
        display: block;
    }

    /* invisible */
    .contvis {
        display: none;
        width: 100%;
    }
    /* visible */
    .contvis1 {
        display: block;
        width: 100%;
    }
    /* add & remove criteria*/
    .command {
        float: right;
        display: block;
        text-align: right;
    }
</style>
<script type="text/javascript">
  var expand = <%=paramqueries%>;//number of queries non-empty
  var myWin = null;
  function displayPickers(id) {
      if (myWin) myWin.close();
      myWin = window.open("<%=URL%>/engines/importexport/dispPickers.jsp?id=" + id, "jwin", "scrollbars=yes,width=600,height=400");
  }

  //used to call the pagelink select
  function callSelectPageLink(callback_cond) {
      var url = "<%=selectPageURL%>&cond=" + callback_cond;
      OpenJahiaScrollableWindow(url, 'selectPage_<%=HTMLToolBox.cleanSessionID(jParams.getSessionID())%>', 950, 720);
  }

  //callback function to grab the pageid selected
  function handleActionChanges(param) {
    var substr = param.substring(param.indexOf("pageSelected=") + 13)
    var end = substr.lastIndexOf("&");
    var cond;
    var pselected;
    //alert(param)
    if (end != -1) {
        pselected = substr.substring(0, end);

    } else {
        pselected = substr.substring(0);
    }
    var substr1 = param.substring(param.indexOf("callback=") + 9)
    var end1 = substr1.lastIndexOf("&");
    if (end1 != -1) {
        cond = substr1.substring(0, end1);
    } else {
        cond = substr1.substring(0);
    }
    if (cond == 2) {
        if (document.getElementById("sc2").style.display == "none") expandcontent("sc2");
        document.getElementById("input23").style.display = "block";

        document.mainForm.query23.value = pselected;
    }
    if (cond == 3) {
        if (document.getElementById("sc3").style.display == "none") expandcontent("sc3");
        document.getElementById("input33").style.display = "block";
        document.mainForm.query33.value = pselected;
    }
    if (cond == 4) {
        if (document.getElementById("sc4").style.display == "none") expandcontent("sc4");
        document.getElementById("input43").style.display = "block";
        document.mainForm.query43.value = pselected;
    }
    //alert(cond);
    //displayWaiting();
    //@("contentPick");
    //alert(pselected)
  }
  // sorting
  function orderby(param) {
    document.mainForm.orderby.value = param;
    document.mainForm.newsearch.value = "0";
    //alert(document.mainForm.orderby.value);
    handleActionChange("contentPick");
  }
  // ascending order
  function ascorder(param) {
    //alert(param);
    document.mainForm.newsearch.value = "0";
    if (param == "1") {

        document.mainForm.asc.value = "0";
    } else {
        document.mainForm.asc.value = "1";
    }
    handleActionChange("contentPick");
  }
  //submit
  function submitenter(myfield, e)
  {
    var characterCode;
    if (e && e.which) { // (NN4+)
        e = e
        characterCode = e.which //character code is contained in NN4's which property
    } else {
        e = event
        characterCode = e.keyCode //IE's keyCode property
    }

    if (characterCode == 13) { //(enter key)

        goSearch("go");
        return false
    } else return true
  }

  // cosmetic search query functions
  function expandcontent(cid) {
    //alert(document.getElementById(cid).style.display);
    if (document.getElementById(cid).style.display != "block") {
      document.getElementById(cid).style.display = "block";
      document.getElementById("visibility_" + cid).value = "true";
      expand++;
    } else {
      document.getElementById(cid).style.display = "none";
      document.getElementById("visibility_" + cid).value = "false";
      expand--;
      if (expand == 1) document.getElementById("sc0").style.display = "none";
    }

    if (expand > 1) document.getElementById("sc0").style.display = "block";
  }


  function contractcontent(cid) {
    document.getElementById(cid).style.display = "none";
    document.getElementById("visibility_" + cid).value = "false";
    expand--;
    if (expand == 1) document.getElementById("sc0").style.display = "none";
    if (cid == "sc2") {
      document.mainForm.query21.value = "";
      document.mainForm.query22.value = "";
      document.mainForm.query23.value = "";
      document.mainForm.query24.value = "";
    }
    if (cid == "sc3") {
      document.mainForm.query31.value = "";
      document.mainForm.query32.value = "";
      document.mainForm.query33.value = "";
      document.mainForm.query34.value = "";
    }
    if (cid == "sc4") {
      document.mainForm.query41.value = "";
      document.mainForm.query42.value = "";
      document.mainForm.query43.value = "";
      document.mainForm.query44.value = "";
    }
  }
  // search function to preprocess queries
  //to check the emptyness of queries
  function checkQueries() {
    if (document.getElementById("query").value == "") return false;
    return true;
  }

  //display waiting icon
  function displayWaiting() {
    document.getElementById("waiting").src = "<%=URL%>/engines/images/waiting.gif";
  }

  //searching
  function goSearch(name) {
    //alert("name="+name);

    if (name == "go") {
      document.mainForm.orderby.value = "";
      document.mainForm.newsearch.value = "1";
      displayWaiting();
      handleActionChange("contentPick");
      return;
    }
    if (name == "concat") {
      if (checkQueries() && expand > 1) {
          displayWaiting();
          handleActionChange("contentPick");
      }
      return;
    }
    if (name == "searchSite") {
      //document.mainForm.newsearch.value = "1";
      displayWaiting();
      handleActionChange("contentPick");
      return;
    }
    if (name == "condition1" && checkQueries()) {
      document.mainForm.orderby.value = "";
      document.mainForm.newsearch.value = "1";

      displayWaiting();
      handleActionChange("contentPick");
    }
    // to get the page selected id (call to selectedpage engine)
    if (name == "condition2") {
      if (document.mainForm.condition2.selectedIndex == 0) {
        //author
        document.getElementById('input21').style.display = "block";
        document.getElementById('input22').style.display = "none";
        document.mainForm.query22.value="";
        document.getElementById('input23').style.display = "none";
        document.mainForm.query23.value="";
        document.getElementById('input24').style.display = "none";
        document.mainForm.condition244.selectedIndex=0;
        document.getElementById('sitemap2').style.display = "none";
      } else if (document.mainForm.condition2.selectedIndex == 1) {
        //keywords
        document.getElementById('input22').style.display = "block";
        document.getElementById('input21').style.display = "none";
        document.mainForm.query21.value="";
        document.getElementById('input23').style.display = "none";
        document.mainForm.query23.value="";
        document.getElementById('input24').style.display = "none";
        document.mainForm.condition244.selectedIndex=0;
        document.getElementById('sitemap2').style.display = "none";
      } else if (document.mainForm.condition2.selectedIndex == 2) {
        //pageid
        document.getElementById('input23').style.display = "block";
        document.getElementById('input21').style.display = "none";
        document.mainForm.query21.value="";
        document.getElementById('input22').style.display = "none";
        document.mainForm.query22.value="";
        document.getElementById('input24').style.display = "none";
        document.mainForm.condition244.selectedIndex=0;
        document.getElementById('sitemap2').style.display = "block";
      } else if (document.mainForm.condition2.selectedIndex == 3) {
        //date range
        document.getElementById('input24').style.display = "block";
        document.getElementById('input21').style.display = "none";
        document.mainForm.query21.value="";
        document.getElementById('input22').style.display = "none";
        document.mainForm.query23.value="";
        document.getElementById('input23').style.display = "none";

        document.getElementById('sitemap2').style.display = "none";
      }
    }

    if (name == "condition3") {
      if (document.mainForm.condition3.selectedIndex == 0) {
        //author
        document.getElementById('input31').style.display = "block";
        document.getElementById('input32').style.display = "none";
        document.mainForm.query32.value="";
        document.getElementById('input33').style.display = "none";
        document.mainForm.query33.value="";
        document.getElementById('input34').style.display = "none";
        document.mainForm.condition344.selectedIndex=0;
        document.getElementById('sitemap3').style.display = "none";
      } else if (document.mainForm.condition3.selectedIndex == 1) {
        //keywords
        document.getElementById('input32').style.display = "block";
        document.getElementById('input31').style.display = "none";
        document.mainForm.query31.value="";
        document.getElementById('input33').style.display = "none";
        document.mainForm.query33.value="";
        document.getElementById('input34').style.display = "none";
        document.mainForm.condition344.selectedIndex=0;
        document.getElementById('sitemap3').style.display = "none";
      } else if (document.mainForm.condition3.selectedIndex == 2) {
        //pageid
        document.getElementById('input33').style.display = "block";
        document.getElementById('input31').style.display = "none";
        document.mainForm.query31.value="";
        document.getElementById('input32').style.display = "none";
        document.mainForm.query32.value="";
        document.getElementById('input34').style.display = "none";
        document.mainForm.condition344.selectedIndex=0;

        document.getElementById('sitemap3').style.display = "block";
      } else if (document.mainForm.condition3.selectedIndex == 3) {
        //date range
        document.getElementById('input34').style.display = "block";
        document.getElementById('input31').style.display = "none";
        document.mainForm.query31.value="";
        document.getElementById('input32').style.display = "none";
        document.mainForm.query32.value="";
        document.getElementById('input33').style.display = "none";
        document.mainForm.query33.value="";
        document.getElementById('sitemap3').style.display = "none";
      }
    }

    if (name == "condition4") {

      if (document.mainForm.condition4.selectedIndex == 0) {
        //author
        document.getElementById('input41').style.display = "block";
        document.getElementById('input42').style.display = "none";
        document.mainForm.query42.value="";
        document.getElementById('input43').style.display = "none";
        document.mainForm.query43.value="";
        document.getElementById('input44').style.display = "none";
        document.mainForm.condition444.selectedIndex=0;
        document.getElementById('sitemap4').style.display = "none";
      } else if (document.mainForm.condition4.selectedIndex == 1) {
        //keywords
        document.getElementById('input42').style.display = "block";
        document.getElementById('input41').style.display = "none";
        document.mainForm.query41.value="";
        document.getElementById('input43').style.display = "none";
        document.mainForm.query43.value="";
        document.getElementById('input44').style.display = "none";
        document.mainForm.condition444.selectedIndex=0;
        document.getElementById('sitemap4').style.display = "none";
      } else if (document.mainForm.condition4.selectedIndex == 2) {
        //pageid
        document.getElementById('input43').style.display = "block";
        document.getElementById('input41').style.display = "none";
        document.mainForm.query41.value="";
        document.getElementById('input42').style.display = "none";
        document.mainForm.query42.value="";
        document.getElementById('input44').style.display = "none";
        document.mainForm.condition444.selectedIndex=0;

        document.getElementById('sitemap4').style.display = "block";
      } else if (document.mainForm.condition4.selectedIndex == 3) {
        //date range
        document.getElementById('input44').style.display = "block";
        document.getElementById('input41').style.display = "none";
        document.mainForm.query41.value="";
        document.getElementById('input42').style.display = "none";
        document.mainForm.query42.value="";
        document.getElementById('input43').style.display = "none";
        document.mainForm.query43.value="";
        document.getElementById('sitemap4').style.display = "none";
      }
    }
  }

  function showDescription(id){
      if(document.getElementById('infoline_'+id).style.display=="block")
          document.getElementById('infoline_'+id).style.display="none";
      else
          document.getElementById('infoline_'+id).style.display="block";
  }
  //preloading
  waitingimage = new Image();
  waitingimage.src = "<%=URL%>/engines/images/waiting.gif";

</script>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../tools.inc" %>
    <div id="content" class="fit w2">
      <%--
        <table border="0">
          <tr>
            <td>DEBUG<br/></td>
          </tr>
          <tr>
            <td>
              sort by <%=orderBy%><br/>
              screen <%=theScreen%><br/>
              path<%=serverpath%><br/>
              sitecount<%=sitecount%><br/>
              size<%=size%><br/>
              asc<%=asc%><br/>
              newsearch<%=newsearch%><br/>
              concatenation mode:<%=concatmode%><br/>
              (scope) searchsite:<%=searchsite%><br/>
            </td>
          </tr>
          <tr>
            <td>mainquery<%=engineMap.get("searchString")%>:<%=engineMap.get("query1")%><br/></td>
          </tr>
          <tr>
            <td>ADDITIONAL PARAMS<br/></td>
          </tr>
          <tr>
            <td>
              condition1<%=cond1%><br/>
              smode0<%=smode%><br/>
              smode2<%=smode2%><br/>
              smode3<%=smode3%><br/>
              smode4<%=smode4%><br/>
              query2:<%=query2%>/<%=query21%>/<%=query22%>/<%=query23%>/<%=query24%><br/>
              query3:<%=query3%>/<%=query31%>/<%=query32%>/<%=query33%>/<%=query34%><br/>
              query4:<%=query4%>/<%=query41%>/<%=query42%>/<%=query43%>/<%=query44%><br/>
            </td>
          </tr>
        </table>
      --%>

      <!-- order param -->
      <input type="hidden" name="orderby" value="<%=orderBy%>"/>
      <input type="hidden" name="asc" value="<%=asc%>"/>
      <!-- lucene query param -->
      <input type="hidden" name="bigquery" value="<%=engineMap.get("searchString")%>"/>
      <!-- page selected param unused-->
      <input type="hidden" name="pageselected" value=""/>
      <!-- new search param -->
      <input type="hidden" name="newsearch" value=""/>

      <div class="head">
        <table cellpadding="0" cellspacing="0" border="0" width="100%" class="object-title">
          <tr>
            <th width="100%">    
             <fmt:message key="org.jahia.engines.importexport.contentpick.welcome.label1"/>&nbsp;<%=objectType%>&nbsp;<fmt:message key="org.jahia.engines.importexport.contentpick.welcome.label2"/>
            </th>

            <td>
              <img id="waiting" src="<%=URL%>/engines/images/pix.gif" width="16" height="16" border="0" align="absmiddle">
            </td>
          </tr>
        </table>
      </div>


      <!-- main query box -->
      <div id="sc1" class="switchcontent1">
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <%
          //int count = 0;
          Iterator<JahiaSite> siteEnum;
          try {
            siteEnum = siteService.getSites();
            JahiaSite jahiaSite;
            %>
            <tr>
              <th width="150">
                <%=bundle.getString(bundle_prefix + ".searchsite.onesite.label")%>&nbsp;
              </th>
              <td>
                <%
                if (sitecount > 1) {
                  // looping on all sites
                  %>
                  <select name="searchSite" onchange="goSearch('go')">
                    <%
                    sb2 = new StringBuffer();
                    while (siteEnum.hasNext()) {
                      sb2.append("<option value=\"");
                      jahiaSite = (JahiaSite) siteEnum.next();
                      sb2.append(jahiaSite.getID());
                      sb2.append("\" ");
                      if (searchsite.equalsIgnoreCase(String.valueOf(jahiaSite.getID()))) sb2.append("selected=\"selected\" ");
                      sb2.append(">");
                      sb2.append(jahiaSite.getSiteKey());
                      sb2.append("</option>");
                      //count++;
                    }

                    sb2.append("<option value=\"all\" ");
                    if (searchsite.equalsIgnoreCase("all")) sb2.append("selected=\"selected\"");
                    sb2.append(" >");
                    sb2.append(bundle.getString(bundle_prefix + ".searchsite.all.label"));
                    sb2.append("</option>");
                    %>
                    <%=sb2.toString()%>
                  </select>
                <%} else {
                  // one site only
                  jahiaSite = (JahiaSite) siteEnum.next();
                  %>
                  <b><%=jahiaSite.getSiteKey() %></b>
                  <input type="hidden" name="searchSite" value="<%=jahiaSite.getID() %>"/>
                <% }%>
              </td>
            </tr>
          <%} catch (JahiaException e) {
            logger.error(e);
          }%>
        </table>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th width="150">
                <%=bundle.getString(bundle_prefix + ".welcome.label2")%>&nbsp;
              </th>
            <td>
              <!-- concat box -->
              <div id="sc0" class="switchcontent" style="background-color: #ffffff;<% if(displayConcat){%> display: none;<%}%>">
                <select name="concat" id="cond0" ><!--onchange="goSearch(this.name)"-->
                  <option value="or" <% if (concatmode.equalsIgnoreCase("or")) {%> selected="selected"<%}%>><fmt:message key="org.jahia.engines.importexport.contentpick.condition0.or.label"/></option>
                  <option value="and" <% if (concatmode.equalsIgnoreCase("and")) {%>selected="selected"<%}%>><fmt:message key="org.jahia.engines.importexport.contentpick.condition0.and.label"/></option>
                </select>&nbsp;&nbsp;
                <fmt:message key="org.jahia.engines.importexport.contentpick.prefix.and.label"/>&nbsp;&nbsp;
              </div>
              <!-- end concat box -->
              <select name="condition1" id="cond1" onchange="goSearch(this.name)">
                <option value="or" <% if (cond1.equalsIgnoreCase("or")) {%>selected="selected"<%}%>><fmt:message key="org.jahia.engines.importexport.contentpick.condition1.or.label"/></option>
                <option value="and" <% if (cond1.equalsIgnoreCase("and")) {%>selected="selected"<%}%>><fmt:message key="org.jahia.engines.importexport.contentpick.condition1.and.label"/></option>
                <option value="exact" <% if (cond1.equalsIgnoreCase("exact")) {%>selected="selected"<%}%>><fmt:message key="org.jahia.engines.importexport.contentpick.condition1.exact.label"/></option>
              </select>
              <!-- main query -->
              <input id="query" name="query" size=30 value="<%=engineMap.get("query1")%>" onkeypress="submitenter(this,event)"/>
            </td>
            <td width="40">
              <a href="javascript:expandcontent('sc2')" title="<%=bundle.getString(bundle_prefix+".criteria.add.label")%>">
                <img src="<%=URL%>/engines/images/adding.png" width="16" height="16" border="0" alt="<%=bundle.getString(bundle_prefix+".criteria.add.label")%>"></a>&nbsp;&nbsp;&nbsp;
            </td>
          </tr>
        </table>
      </div>

      <%=sb.toString()%>
      <div class="content-body">
        <div id="operationMenu">
            <center>
                <span class="dex-PushButton">
                    <span class="first-child">
                      <a class="ico-search" href="javascript:goSearch('go')" title="<%=bundle.getString(bundle_prefix+".search.label")%>" style="text-decoration:none"><%=bundle.getString(bundle_prefix + ".search.label")%></a>
                    </span>
              </span>
          </center>
        </div>
      </div>


      <%if (sr != null) {
        int totalHits = sr.getHitCount();
        %>
        <% if (totalHits > 1) {String select = ""; %>
          <div class="content-body">
            <div id="operationMenu">
              <select id="size" name="size" onchange="handleActionChange('contentPick');">
                <%
                if (size == null) size = "10";
                String[] vals = {"10", "50", "100"};
                String[] labels = {"lastten", "lastfifty", "lasthundred"};
                for (int i = 0; i < 3; i++) {
                  if (size != null && size.equalsIgnoreCase(vals[i])) select = "selected";
                  else select = "";%>
                  <option label="<%=bundle.getString(bundle_prefix +".results."+ labels[i]+".label")%>" value="<%=vals[i]%>" <%=select%>>
                    <%=bundle.getString(bundle_prefix + ".results." + labels[i] + ".label")%>
                  </option>
                <%}%>
              </select>
            </div>
          </div>
        <%}%>
        <%if (totalHits != 0) {%>
          <table width="100%" class="evenOddTable" border="0" cellpadding="5" cellspacing="0">
            <thead>
              <tr valign="top">
                <th>
                  <% if (orderBy.equalsIgnoreCase("title")) { %>
                    <%=bundle.getString(bundle_prefix + ".results.name.label")%>
                    <a href="javascript:ascorder('<%=asc%>');"><img src="<%= sortIcon %>" border="0"></a>
                  <% } else { %>
                    <a href="javascript:orderby('title');"><%=bundle.getString(bundle_prefix + ".results.name.label")%></a>
                  <% } %>
                </th>
                <th>
                  <%=bundle.getString(bundle_prefix + ".results.infos.label")%>
                </th>
                <th>
                  <% if (orderBy.equalsIgnoreCase("createdby")) { %>
                    <%=bundle.getString(bundle_prefix + ".results.author.label")%>
                    <a href="javascript:ascorder('<%=asc%>');"><img src="<%= sortIcon %>" border="0"></a>
                  <% } else { %>
                    <a href="javascript:orderby('createdby');"><%=bundle.getString(bundle_prefix + ".results.author.label")%></a>
                  <% } %>
                </th>
                <th>
                  <% if (orderBy.equalsIgnoreCase("lastmodifiedby")) { %>
                    <%=bundle.getString(bundle_prefix + ".results.contributor.label")%>
                    <a href="javascript:ascorder('<%=asc%>');"><img src="<%= sortIcon %>" border="0"></a>
                  <% } else { %>
                    <a href="javascript:orderby('lastmodifiedby');"><%=bundle.getString(bundle_prefix + ".results.contributor.label")%></a>
                  <% } %>
                </th>
                <th>
                  <%=bundle.getString(bundle_prefix + ".results.usage.label")%>
                </th>
                <th>
                  <% if (orderBy.equalsIgnoreCase("created")) { %>
                    <%=bundle.getString(bundle_prefix + ".results.creationdate.label")%>
                    <a href="javascript:ascorder('<%=asc%>');"><img src="<%= sortIcon %>" border="0"></a>
                  <% } else { %>
                    <a href="javascript:orderby('created');"><%=bundle.getString(bundle_prefix + ".results.creationdate.label")%></a>
                  <% } %>
                </th>
                <th>
                  <% if (orderBy.equalsIgnoreCase("lastpublishingdate")) { %>
                    <%=bundle.getString(bundle_prefix + ".results.publishdate.label")%>
                    <a href="javascript:ascorder('<%=asc%>');"><img src="<%= sortIcon %>" border="0"></a>
                  <% } else { %>
                    <a href="javascript:orderby('lastpublishingdate');"><%=bundle.getString(bundle_prefix + ".results.publishdate.label")%></a>
                  <% } %>
                </th>
                <th>
                  <center><%=bundle.getString(bundle_prefix + ".results.copy.label")%></center>
                </th>
                <th>
                  <center><%=bundle.getString(bundle_prefix + ".results.copylinked.label")%></center>
                </th>
                <!--<th class="text"><center><%=bundle.getString(bundle_prefix + ".results.copylinked.label")%> on change</center></th>-->
              </tr>
            </thead>
            <%
            sb = new StringBuffer();
            //EntryLoadRequest elr = elh.getCurrentEntryLoadRequest();
            for (int i = 0; i < sr.results().size(); i++) {
              JahiaSearchHit thisHit = (JahiaSearchHit) sr.results().get(i);
              String url = /*serverpath +*/ thisHit.getURL();
              /*
              if (thisHit.getTeaser() != null && !thisHit.getTeaser().equalsIgnoreCase("")) {
                  url = serverpath + thisHit.getURL();
              }
              */
              ObjectKey key = thisHit.getSearchHitObjectKey();
              ContentObject co = null;

              String sitekey = null;
              int pickersUsage = 0;
              ContentContainer thiscontainer = null;
              String keywords = "";
              String description = "";
              String minidescription = "";
              boolean hasLongDescription = false;
              try {
                //try: a more defensive code
                JahiaSitesService siteservice = ServicesRegistry.getInstance().getJahiaSitesService();
                thiscontainer = ContentContainer.getContainer(Integer.parseInt(thisHit.getId()));
                int siteId = thiscontainer.getSiteID();
                if (key == null) key = thiscontainer.getObjectKey();
                //debug
                List<ContentField> metas = thiscontainer.getMetadatas();
                Iterator<ContentField> ia = metas.iterator();
                while (ia.hasNext()) {
                    ContentField cfield = (ContentField) ia.next();
                    logger.debug(cfield.getDisplayName(jParams) + ":" + cfield.getValue(jParams));
                }
                sitekey = siteservice.getSite(siteId).getSiteKey();
                pickersUsage = thiscontainer.getPickerObjects().size();

                //got metadata description and keywords
                if (thiscontainer != null && thiscontainer.getMetadata("description", true) != null) {
                  description = thiscontainer.getMetadata("description", true).getValue(jParams, elh.getPreviousEntryLoadRequest());
                  if(description==null){
                    description="";
                    logger.debug("description is null!");
                  }
                }
                keywords = thiscontainer.getMetadataValue("keywords", jParams, "").trim();

                minidescription = extractDescriptionTeaser(description,10,true);
                StringTokenizer st=new StringTokenizer(minidescription," ");
                if(!minidescription.equals("") && st.countTokens()==11) hasLongDescription = true;

                //debug stuff
                if (logger.isDebugEnabled()) {
                    JahiaField dfield1 = null;
                    if (thiscontainer.getMetadata("description", true) != null) {
                        dfield1 = thiscontainer.getMetadata("description", true).getJahiaField(elh.getPreviousEntryLoadRequest());
                    }
                    logger.debug("D:" + dfield1);
                    JahiaField dfield2 = thiscontainer.getMetadataAsJahiaField("keywords", jParams, true);
                    logger.debug("K:" + dfield2);
                }

              } catch (JahiaException e) {
                logger.error("error", e);
                continue;
              }
              String cdate = printFriendlyDate(thisHit, "created", request.getLocale());
              String pdate = printFriendlyDate(thisHit, "lastpublishingdate", request.getLocale());
              //object info
              String pageId = "" + thisHit.getPageId();
              String pagepath=getPagePath( (ContentPage) ContentPage.getChildInstance(pageId),jParams,elh.getPreviousEntryLoadRequest(),0);
              String pagetitle= ManageContentPicker.getTitle(thisHit);
              //out.println("pagetitle="+pagetitle+"xxx");
              pagetitle=pagetitle.replaceAll("<!--","");
              pagetitle=pagetitle.replaceAll("-->","");
              // check colum limits and truncate
              String[] words=pagetitle.split(" ");
              int offset=0;
              int maxWord=12;//max word length,please change this as you want
              boolean toCut=false;
              for(int j=0;j<words.length;j++){

              if(words[j].length()>maxWord) {
                toCut=true;
                break;
              }
              offset=offset+words[j].length()+1;
              }
              if(toCut){
                //out.print("offset:"+offset);
                if(offset==0){
                  pagetitle=pagetitle.substring(offset,maxWord-4)+"...";
                }else{
                  pagetitle=pagetitle.substring(0,offset-1)+"...";
                }
              }

              String theUkey= getUrlKey(thisHit.getPageId());
              String siteLocalisation="";
              if (!theUkey.equals(""))
                  siteLocalisation = "urlkey:" + theUkey + " Site:" + sitekey;
              else
                  siteLocalisation = "PID:" + pageId + " Site:" + sitekey;
              //check if object is a text contentfield or a contentpage
              /*
              if (thisHit.getType() == JahiaSearchHitInterface.PAGE_TYPE) {
                  siteLocalisation = "PID:" + thisHit.getPageId() + " Site:" + sitekey;
              }
              */
              //page id container
              //String pageId=""+thisHit.getPage().getID();

              String color = "#FFFFFF";
              if (i % 2 > 0) color = "#EEFFFF";
              sb.append("<tr id=\"");
              sb.append(i).append("_1\" style=\"background-color:");
              sb.append(color);
              sb.append("\" onMouseOver=\"this.style.backgroundColor='#FFFFEE';document.getElementById('").append(i).append("_2').style.backgroundColor='#FFFFEE'\" onMouseOut=\"this.style.backgroundColor='");
              sb.append(color);
              sb.append("';document.getElementById('").append(i).append("_2').style.backgroundColor='");
              sb.append(color);
              sb.append("'\">");
              sb.append("<td rowspan=\"2\" valign=\"top\">");
              //sb.append("ukey=").append(theUkey).append(" url=").append(url);
              sb.append("<a href=\"");
              sb.append(url);
              sb.append("\" ");

              if (!theUkey.equals(""))
                  sb.append("title=\"urlkey:").append(theUkey);
              else
                  sb.append("title=\"pid:").append(pageId);


              sb.append(" in site:");
              sb.append(sitekey);
              sb.append(" (score: ").append(thisHit.getScore()).append(")");
              sb.append("\" target=\"_new\"><b>");
              sb.append(pagetitle);
              sb.append("</b></a><br><font size=\"1\"><i>(");
              sb.append(siteLocalisation);
              sb.append(")</i></font></td>");
              sb.append("<td colspan=\"8\">").append(pagepath).append("</td>");
              sb.append("</tr>");
              sb.append("<tr id=\"");
              sb.append(i).append("_2\" style=\"background-color:");
              sb.append(color);
              sb.append("\" onMouseOver=\"this.style.backgroundColor='#FFFFEE';document.getElementById('").append(i).append("_1').style.backgroundColor='#FFFFEE'\" onMouseOut=\"this.style.backgroundColor='");
              sb.append(color);
              sb.append("';document.getElementById('").append(i).append("_1').style.backgroundColor='");
              sb.append(color);
              sb.append("'\">");

              if (hasLongDescription) {
                  //sb.append("<td style=\"text-align:left;\" onMouseOver=\"document.getElementById('infoline_").append(i).append("').style.display='block';\" onMouseOut=\"document.getElementById('infoline_").append(i).append("').style.display='none';\">");
                  sb.append("<td style=\"text-align:left;\">");
                  sb.append("<b>").append(bundle.getString(bundle_prefix + ".results.description.label")).append("</b>:");
                  sb.append(minidescription);
                  sb.append("<a href=\"javascript:showDescription(").append(i).append(")\">...</a>");
              } else {
                  sb.append("<td style=\"text-align:left;\">");
                  if (!description.trim().equals("") && !description.trim().equals("<text>")){

                      sb.append("<b>").append(bundle.getString(bundle_prefix + ".results.description.label")).append("</b>:");
                      sb.append(description);
                  } else {
                      logger.debug("NO description");
                  }
              }
              sb.append("<br/>");
              if (!keywords.equals(""))
                  sb.append("<b>").append(bundle.getString(bundle_prefix + ".results.keywords.label")).append("</b>:");
              sb.append(keywords);
              sb.append("</td><td class=\"text\">");
              String creator = thisHit.getParsedObject().getValue("createdby");
              sb.append(creator != null && creator.length() > 0 ? creator : "&nbsp");
              sb.append("</td><td class=\"text\">");
              String lastContributor = thisHit.getParsedObject().getValue("lastmodifiedby");
              sb.append(lastContributor != null && lastContributor.length() > 0 ? lastContributor : "&nbsp");
              sb.append("</td><td class=\"text\"><center>");
              if (pickersUsage > 0) {
                  sb.append("<a href=\"javascript:displayPickers('");
                  sb.append(thisHit.getId());
                  sb.append("',600,400)\">");
                  sb.append(pickersUsage);
                  sb.append("</a>");
              } else
                  sb.append("0");


              sb.append("</center></td><td class=\"text\">");
              sb.append(cdate);
              sb.append("</td><td class=\"text\">");
              sb.append(pdate);
              sb.append("</td><td class=\"text\"><center><input type=\"radio\" name=\"contentPickOp\" value=\"copy_");
              sb.append(thisHit.getId());
              sb.append("\"/></center></td><td class=\"text\"><center><input type=\"radio\" name=\"contentPickOp\" value=\"actlink_");
              sb.append(thisHit.getId());

              sb.append("\"/></center></td></tr>");
              sb.append("<tr><td>&nbsp;</td>");
              sb.append("<td colspan=\"8\">");
              sb.append("<div id=\"infoline_").append(i).append("\" style=\"background-color:#eee;display:none;\">");
              sb.append(description).append("<br/>");
              sb.append(keywords);
              sb.append("</div>");
              sb.append("&nbsp;</td></tr>");
            }//end loop
            %>
          <%=sb.toString()%>
          </table>
        <%} else {%>
          <div class="content-body padded"><center><strong><%=bundle.getString(bundle_prefix + ".noresults.label")%></strong></center></div>
        <%}%>
      <%} else {
        if(newsearch==null){%>
          <div class="content-body padded"><center><strong><%=bundle.getString(bundle_prefix + ".resultmessage.label")%></strong></center></div>
        <% } else {%>
        <div class="content-body padded"><center><strong><%=bundle.getString(bundle_prefix + ".noresults.label")%></strong></center></div>
        <%}
      }%>
      <script type="text/javascript">
          document.mainForm.query.focus();
      </script>
    </div>
  </div>
</div>
<!-- End contenpick.jsp -->