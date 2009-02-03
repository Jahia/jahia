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

<%@page import="java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<jsp:useBean id="url" class="java.lang.String" scope="request" />
<% // http files path. %>
<jsp:useBean id="input" class="java.lang.String" scope="request" />
<% // inputs size. %>
<jsp:useBean id="values" class="java.util.HashMap" scope="request" />
<% // Map containing values. %>
<jsp:useBean id="operation" class="java.lang.String" scope="request" />
<% // The selected operation. %>
<%
Boolean isLynx = (Boolean) request.getAttribute("isLynx"); // Linx.
Boolean indexExists = (Boolean) request.getAttribute("indexExists"); // Does the index exist or not. %>
<% if ( !indexExists.booleanValue() ) { %>
<p class="errorBold">
    <internal:adminResourceBundle resourceName="org.jahia.admin.search.ManageSearch.indexNotExist.label" />
</p>
<% } else { %>
<jsp:include page="processing.jsp" flush="true" />
<% } %>
<div class="head headtop">
    <div class="object-title">
        <internal:adminResourceBundle resourceName="org.jahia.admin.search.ManageSearch.availableOperation.label" />
    </div>
</div>
<ul style="list-style-type: none">
    <li>
        <input type="radio" name="operation" value="doindex"<% if (operation.equals("doindex")) { %> checked<%} %>><internal:adminResourceBundle resourceName="org.jahia.admin.search.ManageSearch.reIndexAndOptimize.label"/>.
    </li>
</ul>