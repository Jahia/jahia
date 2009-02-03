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
limitations under the License.--%>
<%@ page import="org.jahia.bin.JahiaConfigurationWizard" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="JahiaMessageResources" />
<%
    String step = "";
    String steps = JahiaConfigurationWizard.getSteps();
    String op = request.getParameter("sub");
    if (op != null) {
        if (op.equals("add")) {
            step = step + ".1";
        } else if (op.equals("processadd") || op.equals("displaycreateadmin") || op.equals("prepareimport")) {
            step = step + ".1";
        } else if (op.equals("processcreateadmin") || op.equals("processimport")) {
            step = step + ".2";
        } else if (op.equals("processtemplatesetchoice")) {
            if (request.getAttribute("siteTitle")==null) {
                step = step + ".2";
            } else {
                step = step + ".3";
            }
        }
    }
%>

<div id="topTitle">
    <h1>
        Jahia
    </h1>
    <h2 class="edit"><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.jahiaConfigurationWizard.label"/></h2>
    <span class="step">
        <% if (step != null && step.length() > 0) { %>
            <fmt:message key="org.jahia.step.label"/>&nbsp;<%=step%> / <%=steps%>
        <% } else { %>
            &nbsp;
        <% } %>
    </span>
</div>
