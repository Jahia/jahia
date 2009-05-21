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
<%@ page language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.services.categories.Category" %>
<%@ page import="org.jahia.utils.GUITreeTools" %>
<%@ page import="javax.swing.*" %>
<%@ page import="javax.swing.tree.DefaultMutableTreeNode" %>
<%@ page import="javax.swing.tree.TreePath" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>

<%
    final JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
%>

<!-- FIXME : The following javascript file path are hardcoded. -->
<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/selectbox.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/checkbox.js"></script>
<!--#HELP# <script language="javascript" src="${pageContext.request.contextPath}/javascript/help.js"></script> -->

<script type="text/javascript">
// Overide the previous check function
function check() {
    return true;
}

function sendForm(method, params) {
    document.mainForm.screen.value = "categories";
    var sep = "?";
    if (document.mainForm.action.indexOf("?") > 0) {
        sep = "&";
    }
    document.mainForm.action += sep + params;
    teleportCaptainFlam(document.mainForm);
}
</script>
<!-- -->

