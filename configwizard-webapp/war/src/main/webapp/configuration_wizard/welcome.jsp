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