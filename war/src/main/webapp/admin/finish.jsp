<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page import="org.jahia.bin.*"%>
<%
    String processMessage                = (String) request.getAttribute("processMessage");
%>

<%@include file="/admin/include/header.inc"%>

<tr>
    <td align="left" class="text" colspan="2"><h3><fmt:message key="org.jahia.bin.JahiaAdministration.confirmation.label"/></h3></td>
</tr>
</table>
&nbsp;<br>&nbsp;<br>

<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <font class="text"><%=processMessage%></font>
        &nbsp;<br>&nbsp;<br>
        <%if(!isLynx){%>
         <div class="buttonList" style="padding-top: 8px; padding-bottom: 8px">
          <div class="button" title="<fmt:message key='org.jahia.admin.users.ManageUsers.applyModif.label'/>">
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>" ><fmt:message key="org.jahia.admin.ok.label"/></a>
          </div>

        </div>
        <%}else{%>
            &nbsp;&nbsp;<a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.bin.JahiaAdministration.ok.label"/> >></a>
        <%}%>

    </td>

</tr>


</table>

<%@include file="/admin/include/footer.inc"%>