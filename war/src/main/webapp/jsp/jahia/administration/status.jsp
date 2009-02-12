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

<%@include file="/jsp/jahia/administration/include/header.inc"%>
<%@page import = "org.jahia.bin.*"%>
<%@page import = "java.util.*" %>
<%@page import = "java.text.*" %>
<%@page import = "org.jahia.registries.*" %>
<%@page import = "org.jahia.services.cache.Cache" %>
<%@page import = "org.jahia.services.cache.CacheFactory" %>
<%@ page import="org.jahia.settings.SettingsBean"%>
<%@ page import="org.jahia.params.ProcessingContext"%>
<%@ page import="org.jahia.hibernate.manager.SpringContextSingleton" %>
<%@ page import="org.jahia.hibernate.dao.JahiaSiteDAO" %>
<%@ page import="org.hibernate.stat.Statistics" %>
<%@ page import="org.hibernate.stat.SecondLevelCacheStatistics" %>
<%@ page import="org.hibernate.stat.EntityStatistics" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>

<%
    Long freeMemoryInBytes       =  (Long) request.getAttribute("freeMemoryInBytes");
    long freeMemoryInKBytes      =  freeMemoryInBytes.longValue() >> 10;
    long freeMemoryInMBytes      =  freeMemoryInBytes.longValue() >> 20;
    Long totalMemoryInBytes      =  (Long) request.getAttribute("totalMemoryInBytes");
    long totalMemoryInKBytes     =  totalMemoryInBytes.longValue() >> 10;
    long totalMemoryInMBytes     =  totalMemoryInBytes.longValue() >> 20;
    Long maxMemoryInBytes        =  (Long) request.getAttribute("maxMemoryInBytes");
    long maxMemoryInKBytes       =  maxMemoryInBytes.longValue() >> 10;
    long maxMemoryInMBytes       =  maxMemoryInBytes.longValue() >> 20;
    stretcherToOpen   = 0;
%>


<style type="text/css">



    h2 a:link, h2 a:visited {
      	background-color: #336699;
      	color: #fff;
      	text-shadow: 2px 2px 1px #336699;
      	padding: 4px 8px 0;
      	-moz-outline-style: none;
      	display: block;
      	height: 16px;
      	text-decoration: none;
    }

    #container {
        width: 560px;
        text-align: left;
        margin: 0 auto;
    }

    #header {
        height: 82px;
    }

    #content {
        padding: 0 0 5px 0;
        border-style : dashed;
        border-width : 1px 0px 1px 0px;
    }

    #content div {
        margin: 0;
        padding: 0;
        height: 1%;
    }

    .stretcher {
        background: none !important;
        background: #fff;
    }

    .stretcher p {
        margin : 0px;
        padding : 5px;
    }
    
</style>

<script type="text/javascript" src="<%=URL%>../javascript/moo/prototype.lite.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.pack.js"></script>
<script type="text/javascript">
    //the main function, call to the effect object
    function init(){

        var stretchers = document.getElementsByClassName('stretcher'); //div that stretches
        var toggles = document.getElementsByClassName('stretcher-head'); //h3s where I click on

        //accordion effect
        var myAccordion = new fx.Accordion(
                toggles, stretchers, {opacity: true, duration: 400}
                );

        //hash functions
        var found = false;
        toggles.each(function(h2, i){
            var div = Element.find(h2, 'nextSibling'); //element.find is located in prototype.lite
            if (window.location.href.indexOf(h2.title) > 0) {
                myAccordion.showThisHideOpen(div);
                found = true;
            }
        });
        if (!found) myAccordion.showThisHideOpen(stretchers[0]);
    }
</script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.serverStatus.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
	cellspacing="0">
	<tbody>
		<tr>
			<td style="vertical-align: top;" align="left">
				<%@include file="/jsp/jahia/administration/include/tab_menu.inc"%>
			</td>
		</tr>
		<tr>
			<td style="vertical-align: top;" align="left" height="100%">
			<div class="dex-TabPanelBottom">
			<div class="tabContent">
            <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
			<div id="content" class="fit">
            <div class="head">
                <div class="object-title">
                     <internal:adminResourceBundle
                    resourceName="org.jahia.admin.serverStatus.label"/>
                </div>
            </div>
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"status","&sub=process")%>' method="post">

<div class="stretcher-head head">
    <div class="object-title">
    <a href="#locks" class="sectionLink"><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.title.lockSection.label"/></a>
    </div>                
</div>
<div class="stretcher">
    <table border="0" cellspacing="0" cellpadding="5">
        <tr class="evenLine">
            <td width="100%">
                <strong><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.flushLocks.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                
                <input type="submit" name="flushLocks" value="<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.flush.label"/>">
            </td>
        </tr>
    </table>
</div>

<div class="stretcher-head head">
    <div class="object-title">
     <a href="#caches" class="sectionLink"><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.title.cacheSection.label"/></a>
    </div>                
</div>
<div class="stretcher">
    <p>
        <internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.cacheFlushingIntroduction.label"/>
    </p>
    <table border="0" cellspacing="0" cellpadding="5">
        <tr class="evenLine">
            <td width="100%">
                <strong><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.flushAllCaches.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <input type="submit" name="flushAllCaches" value="<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.flush.label"/>">
            </td>
        </tr>
    </table>
    <br>
    <table class="evenOddTable full" border="0" cellspacing="0" cellpadding="5">
        <%
            SortedSet sortedCacheNames = new TreeSet(ServicesRegistry.getInstance().getCacheService().getNames());
            Iterator cacheNameIte = sortedCacheNames.iterator();
            DecimalFormat percentFormat = new DecimalFormat("###.##");
            int cacheCounter = 0;
            String cacheLineClass = "evenLine";
            while (cacheNameIte.hasNext()) {
                if (cacheCounter % 2 == 0) {
                    cacheLineClass = "evenLine";
                } else {
                    cacheLineClass = "oddLine";
                }
                cacheCounter++;
                String curCacheName = (String) cacheNameIte.next();
                Object objectCache = ServicesRegistry.getInstance().getCacheService().getCache (curCacheName);
                if (objectCache instanceof Cache) {
                    Cache curCache = (Cache) objectCache;
                    String resourceKey = "org.jahia.admin.status.ManageStatus.cache." + curCache.getName() + ".description.label";
                    long cacheLimit = curCache.getCacheLimit()/(1024*1024);
                    String efficiencyStr = "0";
                    if (!Double.isNaN(curCache.getCacheEfficiency())) {
                        efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                    }
        %>
        <tr class="<%=cacheLineClass%>">
            <td width="100%">
                <%=curCache.getName()%> : <strong><internal:adminResourceBundle resourceName="<%=resourceKey%>"/></strong>
                <br>
                <%=curCache.size()%>&nbsp;
                <% if(curCache.size() > 1){
                %><internal:adminResourceBundle resourceName="org.jahia.admin.entries.label"/><%
            } else {
            %><internal:adminResourceBundle resourceName="org.jahia.admin.entrie.label"/><%
                } %>
                &nbsp;
                <internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.groupSize.label"/> :
                <%=curCache.getGroupsSize()%>
                &nbsp;
                <internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.groupsKeysTotal.label"/> :
                <%=curCache.getGroupsKeysTotal()%>
                <br>
                <internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.successfulHits.label"/> :
                <%=curCache.getSuccessHits()%> / <%=curCache.getTotalHits()%>&nbsp;
                <internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.totalHits.label"/>,
                <internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.efficiency.label"/> :
                <%=efficiencyStr%> %
            </td>
            <td>
                <input type="submit" name="flush_<%=curCache.getName()%>" value="<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.flush.label"/>">
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
        <thead>
            <tr>
                <th colspan="6"> Hibernate Statistics</th>
            </tr>
        </thead>
        <%
            JahiaSiteDAO dao = (JahiaSiteDAO) SpringContextSingleton.getInstance().getContext().getBean("jahiaSiteDAO");
            Statistics statistics = dao.getHibernateTemplate().getSessionFactory().getStatistics();
            if (statistics.isStatisticsEnabled()) {
        %>
        <tr class="oddLine">
            <td >Number of opened session : <%=statistics.getSessionOpenCount()%>
            </td>
            <td colspan="5">Number of closed session : <%=statistics.getSessionCloseCount()%>
            </td>
        </tr>
        <tr class="evenLine">
            <td>Number of query cache hits : <%=statistics.getQueryCacheHitCount()%>
            </td>
            <td>Number of query cache miss : <%=statistics.getQueryCacheMissCount()%>
            </td>
            <td>Number of query cache put : <%=statistics.getQueryCachePutCount()%>
            </td>
            <td>Number of query execution : <%=statistics.getQueryExecutionCount()%>
            </td>
            <td colspan="2">Max time for query execution in ms : <%=statistics.getQueryExecutionMaxTime()%>
            </td>

        </tr>
        <tr class="oddLine">
            <td colspan="2">Number of second level cache hits : <%=statistics.getSecondLevelCacheHitCount()%>
            </td>
            <td colspan="2">Number of second level cache miss : <%=statistics.getSecondLevelCacheMissCount()%>
            </td>
            <td colspan="2" align="left">Number of second level cache put : <%=statistics.getSecondLevelCachePutCount()%>
            </td>
        </tr>
        <tr class="evenLine">
            <td>Names of second level caches :</td>
            <td>Cache hits</td>
            <td>Cache miss</td>
            <td>Cache put</td>
            <td>Size In Memory</td>
        </tr>
        <% List names = Arrays.asList(statistics.getSecondLevelCacheRegionNames());
            Collections.sort(names);
            for (int i = 0; i < names.size(); i++) {
                String name = (String) names.get(i);
                SecondLevelCacheStatistics cacheStatistics = statistics.getSecondLevelCacheStatistics(name);
        %>
        <tr class="evenLine">
            <td><%=name%>
            </td>
            <td><%=cacheStatistics.getHitCount()%>
            </td>
            <td><%=cacheStatistics.getMissCount()%>
            </td>
            <td><%=cacheStatistics.getPutCount()%>
            </td>
            <td><%=cacheStatistics.getSizeInMemory()%>
            </td>
        </tr>
        <%
            }
        %>
        <tr class="oddLine">
            <td>Number of entity load count : <%=statistics.getEntityLoadCount()%>
            </td>
            <td>Number of entity fetch count : <%=statistics.getEntityFetchCount()%>
            </td>
            <td>Number of entity insert count : <%=statistics.getEntityInsertCount()%>
            </td>
            <td>Number of entity update count : <%=statistics.getEntityUpdateCount()%>
            </td>
            <td>Number of entity delete count : <%=statistics.getEntityDeleteCount()%>
            </td>
        </tr>
        <tr class="evenLine">
            <td>Names of entity</td>
            <td>Loaded</td>
            <td>Fetched</td>
            <td>Inserted</td>
            <td>Updated</td>
            <td>Deleted</td>
        </tr>
        <% List entities = Arrays.asList(statistics.getEntityNames());
            Collections.sort(entities);
            for (int i = 0; i < entities.size(); i++) {
                String name = (String) entities.get(i);
                EntityStatistics entityStatistics = statistics.getEntityStatistics(name);
        %>
        <tr class="evenLine">
            <td>
                <%=name%>
            </td>
            <td><%=entityStatistics.getLoadCount()%>
            </td>
            <td><%=entityStatistics.getFetchCount()%>
            </td>
            <td><%=entityStatistics.getInsertCount()%>
            </td>
            <td><%=entityStatistics.getUpdateCount()%>
            </td>
            <td><%=entityStatistics.getDeleteCount()%>
            </td>
        </tr>
        <%
            }
        %>
        <%
            }
        %>
    </table>
</div>

<!-- BEGIN - ESI server control panel access -->
<%
    ProcessingContext jParams = null;
    if (jData != null) {
        jParams = jData.getProcessingContext();
    }
%>

<div class="stretcher-head head">
    <div class="object-title">
      <a href="#memory" class="sectionLink"><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.title.memorySection.label"/></a>
    </div>                
</div>
<div class="stretcher">
    <table class="evenOddTable full" width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr class="evenLine">
            <td width="100%">
                <strong><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.maxJvmMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=maxMemoryInMBytes%>&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=maxMemoryInKBytes%>&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr class="oddLine">
            <td width="100%">
                <strong><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.totalJvmMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=totalMemoryInMBytes%>&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=totalMemoryInKBytes%>&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr class="evenLine">
            <td width="100%">
                <strong><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.freeMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=freeMemoryInMBytes%>&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=freeMemoryInKBytes%>&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
    </table>
</div>

<div class="stretcher-head head">
    <div class="object-title">
      <a href="#systemInfo" class="sectionLink"><internal:adminResourceBundle resourceName="org.jahia.admin.status.ManageStatus.title.systemInfoSection.label"/></a>
    </div>                
</div>
<div class="stretcher">
    <table  class="evenOddTable full" width="100%" border="0" cellspacing="0" cellpadding="5" style="table-layout: fixed;">
        <% Enumeration propertyNameEnum = System.getProperties().propertyNames();
            int maxWidth = 40;
            int propertyCounter = 0;
            String propertyLineClass = "evenLine";
            while (propertyNameEnum.hasMoreElements()) {
                if (propertyCounter % 2 == 0) {
                    propertyLineClass = "evenLine";
                } else {
                    propertyLineClass = "oddLine";
                }
                propertyCounter++;
                String curPropertyName = (String) propertyNameEnum.nextElement();
                String curPropertyValue = (String) System.getProperty(curPropertyName);
                pageContext.setAttribute("propName", curPropertyName);
                pageContext.setAttribute("propValue", curPropertyValue);
        %>
        <tr class="<%=propertyLineClass%>">
            <td style="width: 40%; overflow: hidden;" title="<c:out value='${propName}'/>">
                <strong><c:out value='${propName}'/></strong>
            </td>
            <td style="width: 60%; overflow: hidden;" title="<c:out value='${propValue}'/>">
                <%
                    while (curPropertyValue.length() > maxWidth) {
                        String curLine = curPropertyValue.substring(0, maxWidth);
                        out.println(curLine);
                        curPropertyValue = curPropertyValue.substring(maxWidth);
                    }
                    out.println(curPropertyValue);
                %>
            </td>
        </tr>
        <% } %>
    </table>
</div>
</form>
</div>
</div>
			</div>
			</td>
		</tr>
	</tbody>
</table>
</div>
  <div id="actionBar">
  	<span class="dex-PushButton"> 
	  <span class="first-child">
      	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
      </span>
     </span> 	      
  </div>

</div>
<script type="text/javascript">
    Element.cleanWhitespace('content');
    init();
</script>

<%@include file="/jsp/jahia/administration/include/footer.inc"%>
