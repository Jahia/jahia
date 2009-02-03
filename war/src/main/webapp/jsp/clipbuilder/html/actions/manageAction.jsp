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

<%@taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-nested" prefix="nested"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@page language="java" import="org.jahia.clipbuilder.html.bean.ClippersManagerBean"%>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.acl.JahiaACLManagerService" %>
<%@ page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.bin.JahiaAdministration" %>
<script language="javascript">
    function go()
    {
        var parentDiv = document.getElementById('clipperList');

        var divs = parentDiv.getElementsByTagName('div');
        for (var i = 0; i < divs.length; i++) {
            // set all disply to none
            divs[i].style.display = 'none';
        }

 //find selected clipper
        box = document.forms[0].clipper;
        if (box != null) {
            divId = box.options[box.selectedIndex].value;
  
   //set corresponding div display to block
            divElement = document.getElementById(divId);
            divElement.style.display = 'block';
        }

    }
</script>
<%
    final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    final JahiaUser user = jData.getProcessingContext().getUser();
    final int currentSiteID = jData.getProcessingContext().getSiteID();
%>
<html:form action="manageAction?do=clipbuilder">

<!-- Title-->
<div class="head">
                <div class="object-title">
                  <bean:message key="manage.title"/>
                </div>
              </div>
<!-- Buttons -->
<table class="principal" align="center" width="100%">
    <tr>
        <td class="topmenubuttons">
            <!-- Add -->
            <html:submit styleClass="fancyButton" property="webClippingAction">
                <bean:message key="manage.button.add"/>
            </html:submit>
            <!-- Load/delete -->
            <logic:notEqual name="<%=ClippersManagerBean.class.toString()%>" property="empty" value="1">
                <html:submit styleClass="fancyButton" property="webClippingAction" disabled="true">
                    <bean:message key="manage.button.delete"/>
                </html:submit>
                <html:submit styleClass="fancyButton" property="webClippingAction" disabled="true">
                    <bean:message key="manage.button.load"/>
                </html:submit>
                <!-- deploy -->
                <html:submit styleClass="fancyButton" property="webClippingAction" disabled="true">
                    <bean:message key="manage.deploy"/>
                </html:submit>
            </logic:notEqual>
            <logic:notEqual name="<%=ClippersManagerBean.class.toString()%>" property="empty" value="0">
                <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
                    <bean:message key="manage.button.delete"/>
                </html:submit>
                <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
                    <bean:message key="manage.button.load"/>
                </html:submit>
                <!-- deploy -->
                <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
                    <bean:message key="manage.deploy"/>
                </html:submit>
            </logic:notEqual>
        </td>
    </tr>
</table>
<!-- Main -->
<logic:present name="deployed" scope="request">
    <table align="center" bgcolor="#006699" cellpadding="0" cellspacing="1" width="100%">
        <tbody>
            <tr>
                <td>
                    <table class="text" align="center" bgcolor="#006699" border="0" width="100%">
                        <tbody>
                            <tr>
                                <td align="center" bgcolor="#dcdcdc">
                                    <font color="#006699">
                                        <b><bean:message key="information"/> </b>
                                    </font>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <ul>
                                        <li class="text">
                                            <bean:message key="clipbuilder.deployement.available"/>
                                        </li>
                                        <li class="text">
                                            <bean:message key="clipbuilder.deployement.permissions"/>
                                            <% if(aclService.getServerActionPermission("admin.components.ManageShareComponents",user,JahiaBaseACL.READ_RIGHTS,currentSiteID)>0){ %>
                                            <a style="font-weight:normal;" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'>
                                                (<internal:adminResourceBundle
                                                    resourceName="org.jahia.admin.manageComponents.label"/>)
                                            </a>
                                            <% } %>
                                        </li>
                                    </ul>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
        </tbody>
    </table>
</logic:present>
<table width="100%">
    <logic:notEqual name="<%=ClippersManagerBean.class.toString()%>" property="empty" value="0">
        <thead>
            <tr>
                <th class="topmenugreen">
                    <bean:message key="manage.listClipper"/>
                </th>
                <th class="topmenugreen">
                    <bean:message key="description.description"/>
                </th>
            </tr>
        </thead>
    </logic:notEqual>
    <tbody>
        <tr>
            <logic:notEqual name="<%=ClippersManagerBean.class.toString()%>" property="empty" value="0">
                <!-- List of all the available clippers -->
                <td>
                    <html:select style="width:100%" onchange="javascript:go();" property="clipper" size="10">
                        <!-- Get all available clippers-->
                        <html:optionsCollection name="<%=ClippersManagerBean.class.toString()%>"
                                                property="clippersBeanList" value="name" label="name"/>
                    </html:select>
                </td>
                <!-- description -->
                <td width="80%" style="vertical-align: top">
                    <div id="clipperList">
                        <logic:iterate name="<%=ClippersManagerBean.class.toString()%>" property="clippersBeanList"
                                       id="currentClipper">
                            <div style="display:none;" id="<bean:write name="currentClipper" property="name"/>">
                                <bean:write name="currentClipper" property="description"/>
                            </div>
                        </logic:iterate>
                    </div>
                </td>
            </logic:notEqual>
            <!--no clipper recorded -->
            <logic:notEqual name="<%=ClippersManagerBean.class.toString()%>" property="empty" value="1">
                <!-- List of all the available clippers -->
                <td>
                    <bean:message key="manage.empty"/>
                </td>
            </logic:notEqual>
        </tr>
    </tbody>
</table>
</html:form>
<script language="javascript">
    go();
</script>
