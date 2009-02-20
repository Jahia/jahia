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

<%@page import   = "java.util.*" %>
<%@page import="org.jahia.bin.*"%>


<%

    //String      myVar            = (String)      request.getAttribute("myVar");

%>

<%@include file="/jsp/jahia/administration/include/header.inc"%>

<tr>
    <td align="right" class="text" colspan="2"><b>::&nbsp;&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.sitemap.SiteMap_Engines.siteMap.label"/>&nbsp;&nbsp;--&nbsp;&nbsp;::</b>&nbsp;&nbsp;&nbsp;&nbsp;</td>
</tr>
</table>
<br>&nbsp;<br>
<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
    <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"processsitemap","")%>' method="post">
    <input type="hidden" name="myname" value="toto">
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <table border="0">
        <tr>
            <td>
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;<br>
                <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap width="145" valign="top"><font class="text"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b></font></td>
                    <td valign="top">
                        <font class="text">
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a><br>
                        </font>
                    </td>
                </tr>
                </table>
            </td>
        </tr>
        </table>
    </td>
</tr>
   </form>
<tr>
    <td colspan="2" align="right">
        <br>&nbsp;<br>
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td align="left">
                    <font class="text2"><%=jahiaDisplayMessage%></font>
                </td>
                <td>&nbsp;&nbsp;&nbsp;</td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td colspan="2">&nbsp;</td>
</tr>
</table>
<%@include file="/jsp/jahia/administration/include/footer.inc"%>