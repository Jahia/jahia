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

