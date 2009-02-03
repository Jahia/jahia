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
<%@ page import="org.jahia.bin.Jahia" %>
<%@ include file="header.inc" %>
<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.error.label"/>: <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.errorconfigured.jahiaAlreadyInstalledAndConfigured.label"/>&nbsp;!
  </div>
</div>
<div id="pagebody">
  <p>
    <fmt:message key="org.jahia.pleaseFollowTheLink.label"/>.
  </p>
  <p>
    <a href="<%=Jahia.getContextPath()%><%=Jahia.getServletPath()%>" title="<fmt:message key="org.jahia.goToJahia.label"/>"><fmt:message key="org.jahia.goToJahia.label"/></a>
  </p>
</div>
</div>
<%@ include file="footer.inc" %>