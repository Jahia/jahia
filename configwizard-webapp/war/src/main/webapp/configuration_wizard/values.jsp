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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@page import="org.jahia.utils.WebAppPathResolver"%>
<%@ include file="header.inc" %>

<jsp:useBean id="input" class="java.lang.String" scope="request"/>
<jsp:useBean id="selectedLocale" class="java.lang.String" scope="request"/>

<%!
    public final String validate(String data) {
        int index;
        if (data.length() == 0) {
            data = "notSpecified";
        } else if ((index = data.indexOf('?')) > 0) {
            final StringBuffer buff = new StringBuffer();
            buff.append(data.substring(0, index));
            buff.append("?<br/>");
            buff.append(data.substring(index + 1));
            return buff.toString();
        }
        return data;
    }
%>

<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.confirmYourValues.label"/>
    </div>
</div>
<div id="pagebody">
<table summary="<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.superUserSettings.label"/>">
<caption><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.superUserSettings.label"/>:</caption>
<tr>
    <th id="t1"><span>Parameter</span></th>
    <th id="t2"><span>Value</span></th>
</tr>
<tr>
    <td headers="t1" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.userName.label"/>
    </td>
    <td headers="t2" class="t4">
        <%=(String) values.get("root_user")%>
    </td>
</tr>
<tr>
    <td headers="t1" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.passWord.label"/>
    </td>
    <td headers="t2" class="t4">**************</td>
</tr>
<tr>
    <td headers="t1" class="t3">
        <fmt:message key="org.jahia.firstName.label"/>
    </td>
    <td headers="t2" class="t4">
        <% if (validate((String) values.get("root_firstname")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("root_firstname")%>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t1" class="t3">
        <fmt:message key="org.jahia.lastName.label"/>
    </td>
    <td headers="t2" class="t4">
        <% if (validate((String) values.get("root_lastname")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("root_lastname")%>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t1" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.emailAdress.label"/>
    </td>
    <td headers="t2" class="t4">
        <% if (validate((String) values.get("root_mail")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("root_mail")%>
        <% } %>
    </td>
</tr>
</table>
<table summary="<fmt:message key="org.jahia.serverSettings.label"/>">
<caption><fmt:message key="org.jahia.serverSettings.label"/>:</caption>
<tr>
    <th id="t3"><span>Parameter</span></th>
    <th id="t4"><span>Value</span></th>
</tr>
<tr>
    <td headers="t3" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.serverHome.label"/>
    </td>
    <td headers="t4" class="t4">
        <%=(String) values.get("server_home")%>
    </td>
</tr>
<tr>
    <td headers="t3" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.hostURL.label"/>
    </td>
    <td headers="t4" class="t4">
        <% if (validate((String) values.get("server_url")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("server_url")%>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t3" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.jahiaFilesPath.label"/>
    </td>
    <td headers="t4" class="t4">
        <%  WebAppPathResolver webAppPathResolver = new WebAppPathResolver();
            webAppPathResolver.setServletContext(pageContext.getServletContext());
            if (validate((String) values.get("server_jahiafiles")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <div style="overflow: hidden" title='<%=JahiaTools.convertContexted((String) values.get("server_jahiafiles"), webAppPathResolver)%>'><%=JahiaTools.convertContexted((String) values.get("server_jahiafiles"), webAppPathResolver)%></div>
        <% } %>
    </td>
</tr>
</table>
<table summary="<fmt:message key="org.jahia.databaseSettings.label"/>">
<caption><fmt:message key="org.jahia.databaseSettings.label"/>:</caption>
<tr>
    <th id="t5"><span>Parameter</span></th>
    <th id="t6"><span>Value</span></th>
</tr>
<tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.driver.label"/>
    </td>
    <td headers="t6" class="t4">
        <%=(String) values.get("database_driver")%>
    </td>
</tr>
<tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.databaseURL.label"/>
    </td>
    <td headers="t6" class="t4">
        <div style="overflow: hidden" title='<%=values.get("database_url")%>'><%=values.get("database_url")%></div>
    </td>
</tr>
<tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.userName.label"/>
    </td>
    <td headers="t6" class="t4">
        <%=(String) values.get("database_user")%>
    </td>
</tr>
<tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.passWord.label"/>
    </td>
    <td headers="t6" class="t4">**************</td>
</tr>
<tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.storeFilesInDB.label"/>
    </td>
    <td headers="t6" class="t4">
        <% if (Boolean.parseBoolean((String)values.get("storeFilesInDB"))) { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.true.label"/>
        <% } else { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.false.label"/>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.storeBigTextInDB.label"/>
    </td>
    <td headers="t6" class="t4">
        <% if (Boolean.parseBoolean((String)values.get("storeBigTextInDB"))) { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.true.label"/>
        <% } else { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.false.label"/>
        <% } %>
    </td>
</tr>
<!--tr>
    <td headers="t5" class="t3">
        <fmt:message key="org.jahia.useExistingDb.label"/>
    </td>
    <td headers="t6" class="t4">
        <% if (Boolean.parseBoolean((String)values.get("useExistingDb"))) { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.true.label"/>
        <% } else { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.false.label"/>
        <% } %>
    </td>
</tr-->
</table>
<table summary="<fmt:message key="org.jahia.utfCompliance.label"/>">
<caption><fmt:message key="org.jahia.utfCompliance.label"/>:</caption>
<tr>
    <th id="t7"><span>Parameter</span></th>
    <th id="t8"><span>Value</span></th>
</tr>
<tr>
    <td headers="t7" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.serverUTFCompliant.label"/>
    </td>
    <td headers="t8" class="t4">
        <% final String utf8Encoding = (String) values.get("utf8Encoding");
            if (utf8Encoding.equals("true")) { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.true.label"/>
        <% } else { %>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.false.label"/>
        <% } %>
    </td>
</tr>
</table>
<table summary="<fmt:message key="org.jahia.mailSettings.label"/>">
<caption><fmt:message key="org.jahia.mailSettings.label"/>:</caption>
<tr>
    <th id="t9"><span>Parameter</span></th>
    <th id="t10"><span>Value</span></th>
</tr>
<tr>
    <td headers="t9" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.server.label"/>
    </td>
    <td headers="t10" class="t4">
        <% if (validate((String) values.get("mail_server")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("mail_server")%>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t9" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.recipient.label"/>
    </td>
    <td headers="t10" class="t4">
        <% if (validate((String) values.get("mail_recipient")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("mail_recipient")%>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t9" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.formAddress.label"/>
    </td>
    <td headers="t10" class="t4">
        <% if (validate((String) values.get("mail_from")).equals("notSpecified")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("mail_from")%>
        <% } %>
    </td>
</tr>
<tr>
    <td headers="t9" class="t3">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.eventNotification.label"/>
    </td>
    <td headers="t10" class="t4">
        <% if (validate((String) values.get("mail_parano")).equals("Disabled")) { %>
        ---&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.disabled.label"/>&nbsp;---
        <% } else { %>
        <%=(String) values.get("mail_parano")%>
        <% } %>
    </td>
</tr>
</table>

<%--<table summary="<fmt:message key="org.jahia.defaultSiteSettings.label"/>">--%>
<%--<caption><fmt:message key="org.jahia.defaultSiteSettings.label"/></caption>--%>
<%--<tr>--%>
<%--<th id="t11"><span>Parameter</span></th>--%>
<%--<th id="t12"><span>Value</span></th>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteKey.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<%=(String) values.get("sitekey")%>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteTitle.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<%=(String) values.get("sitetitle")%>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3"--%>
<%--><fmt:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteServerName.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<%=(String) values.get("siteservername")%>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteLanguage.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<%=((Locale)values.get("sitelocale")).getDisplayName(new Locale(selectedLocale)) %>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.username.label"/>--%>
<%--<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.siteAdmin.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<%=(String) values.get("adminUsername")%>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.password.label"/>--%>
<%--<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.siteAdmin.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">**************</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.firstName.label"/>--%>
<%--<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.siteAdmin.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<% if (validate((String) values.get("adminFirstName")).equals("notSpecified")) { %>--%>
<%-----&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;-----%>
<%--<% } else { %>--%>
<%--<%=(String) values.get("adminFirstName")%>--%>
<%--<% } %>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.lastName.label"/>--%>
<%--<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.siteAdmin.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<% if (validate((String) values.get("adminLastName")).equals("notSpecified")) { %>--%>
<%-----&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;-----%>
<%--<% } else { %>--%>
<%--<%=(String) values.get("adminLastName")%>--%>
<%--<% } %>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:adminResourceBundle resourceName="org.jahia.admin.eMail.label"/>--%>
<%--<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.siteAdmin.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<% if (validate((String) values.get("adminEmail")).equals("notSpecified")) { %>--%>
<%-----&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;-----%>
<%--<% } else { %>--%>
<%--<%=(String) values.get("adminEmail")%>--%>
<%--<% } %>--%>
<%--</td>--%>
<%--</tr>--%>
<%--<tr>--%>
<%--<td headers="t9" class="t3">--%>
<%--<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.templates.prePackagedTemplates.label"/>--%>
<%--</td>--%>
<%--<td headers="t10" class="t4">--%>
<%--<% if (validate((String) values.get("templates")).equals("notSpecified")) { %>--%>
<%-----&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.notSpecified.label"/>&nbsp;-----%>
<%--<% } else { %>--%>
<%--<%=(String) values.get("templates")%>--%>
<%--<% } %>--%>
<%--</td>--%>
<%--</tr>--%>
<%--</table>--%>

<h5><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.values.confirmSaveConfiguration.label"/>&nbsp;?</h5>
<br/>
<br/>

</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>