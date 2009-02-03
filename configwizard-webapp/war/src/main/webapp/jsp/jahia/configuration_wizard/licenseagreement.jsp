<%--
Copyright 2002-2008 Jahia Ltd
Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at
http://www.jahia.org/license/
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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