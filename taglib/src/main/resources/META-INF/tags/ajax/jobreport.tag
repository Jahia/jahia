<%--
Copyright 2002-2006 Jahia Ltd

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

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ attribute name="name" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="groupName" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="jobreport" jahiaType="jobreport" name="<%=name%>" groupName="<%=groupName%>">

</template:gwtJahiaModule>

