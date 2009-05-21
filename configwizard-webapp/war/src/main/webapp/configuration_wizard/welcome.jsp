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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="header.inc" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<jsp:useBean id="javavendor" class="java.lang.String" scope="request"/>
<jsp:useBean id="javaversion" class="java.lang.String" scope="request"/>
<jsp:useBean id="os" class="java.lang.String" scope="request"/>
<jsp:useBean id="osVersion" class="java.lang.String" scope="request"/>
<jsp:useBean id="osArch" class="java.lang.String" scope="request"/>
<jsp:useBean id="server" class="java.lang.String" scope="request"/>
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.welcomeTo.label"/>&nbsp;<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.jahiaConfigurationWizard.label"/>
    </div>
</div>
<div id="pagebody">
    <% if (Boolean.parseBoolean((String)values.get("jahiaconfigured"))) { %>
    <font size="3" color="red">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.jahiaAlreayInstalled"/>
    </font>
    <% }%>
    <table cellspacing="0" cellpadding="2" border="0" summary="<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.detectedIntro.label"/>">
    <caption>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.detectedIntro.label"/>
    </caption>
    <tr>
        <th id="t1"><span>System parameter</span></th>
        <th id="t2"><span>Value</span></th>
    </tr>
    <tr>
        <td class="t2">
                <span><fmt:message
                        key="org.jahia.bin.JahiaConfigurationWizard.welcome.servletContainer.label"/></span>
        </td>
        <td>
            <span><%=server%></span>
        </td>
    </tr>
    <tr>
        <td class="t2">
            <span><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.javaVendor.label"/></span>
        </td>
        <td>
            <span><%=javavendor%></span>
        </td>
    </tr>
    <tr>
        <td class="t2">
            <span><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.javaVersion.label"/></span>
        </td>
        <td>
            <span><%=javaversion%></span>
        </td>
    </tr>
    <tr>
        <td class="t2">
            <span><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.operatingSystem.label"/></span>
        </td>
        <td>
                <span><%=os%>
                    <fmt:message
                            key="org.jahia.bin.JahiaConfigurationWizard.welcome.versionInitial.label"/>.<%=osVersion%>
                    (<%=osArch%>)
                </span>
        </td>
    </tr>
    </table>

    <%
        List availableBundleLocales = (List) request.getAttribute("availableBundleLocales");
        if (availableBundleLocales.size() > 0) { %>
    <h4>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.selectInstallationLanguage.label"/>
    </h4>
    <select name="newLocale" onchange="setWaitingCursor(); submitFormular('welcome', 'back')">
        <%
            Locale usedLocale = session.getAttribute(JahiaConfigurationWizard.SESSION_LOCALE) != null ?
                    (Locale)session.getAttribute(JahiaConfigurationWizard.SESSION_LOCALE) : request.getLocale();
            for (Locale curLocale : (List<Locale>)availableBundleLocales) {
                final String localeDisplayName;
                localeDisplayName = StringUtils.capitalize(curLocale.getDisplayName(curLocale));
                if (curLocale.equals(usedLocale)) {
        %>
        <option selected="selected" value="<%=curLocale.toString()%>"><%=localeDisplayName%></option>
        <%      } else { %>
        <option value="<%=curLocale.toString()%>"><%=localeDisplayName%></option>
        <%      }
        }
        %>
    </select>
    <% } %>
    <br/>
    <br/>
    <p>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.welcome.smoothAndSimpleConfiguration.label"/>.
    </p>
    <br/>

</div>

<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>