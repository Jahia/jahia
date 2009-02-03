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
<jsp:useBean id="errormsg" class="java.lang.String" scope="request"/>
<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.errorsave.jahiaGeneratesSomeErrors.label"/>
  </div>
</div>
<div id="pagebody">
  <p class="error">
    <%=errormsg %>
  </p>  
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>