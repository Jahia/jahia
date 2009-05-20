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
<jsp:useBean id="selectedJahiaEdition" class="java.lang.String" scope="request"/>
<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.licenseAgreement.label"/>
  </div>
</div>
<div id="pagebody">
  <%@ include file="error.inc" %>
  <p>
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.licenseagreement.licenseIntro.label"/>
  </p>
    <br />
  <fieldset>
    <%-- p>
      <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.licenseagreement.openSourceLicenseIntro.label"/>
    </p --%>
    <textarea rows="30" cols="40" style="width:100%" readonly="readonly">
      <%@include file="license.txt" %>
    </textarea>
      <br />
    <input name="acceptLicense" type="checkbox" id="acceptOpenSourceLicense" value="true" />
    <label for="acceptOpenSourceLicense">
      <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.licenseagreement.iAgree.label"/>
    </label>
      <br />
        <br />
  </fieldset>
  <br/>
  <br />
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>