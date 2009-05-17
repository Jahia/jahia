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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ include file="common/declarations.jspf" %>
<style type="text/css">
<!--
DIV#errors { color : #B42C29; }
DIV#errors li { color : #B42C29; }
-->
</style>
<%!
    public String getSingleValue(HttpServletRequest req, String attrName) {
        String[] values = (String[]) req.getAttribute(attrName);
        if (values == null) {
            return "";
        }
        if (values.length == 0) {
            return "";
        }
        return values[0];
    }
%>
<template:template>
    <template:templateHead>
        <%@ include file="common/template-head.jspf" %>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody>
        <div id="header">
            <div id="utilities">
                <div class="content">
                    <a name="pagetop"></a>
                    <span class="breadcrumbs"><fmt:message key='youAreHere'/>:</span>
                    <ui:currentPagePath cssClassName="breadcrumbs"/>
                    <ui:languageSwitchingLinks display="horizontal" linkDisplay="flag" displayLanguageState="true"/>
                </div>
            </div>
        </div>
        <div id="pagecontent">
            <div class="content1cols">
                <div class="padding">
                    <div id="columnB">
                        <h2><fmt:message key="newUserRegistration.success.title"/></h2>
                        <div><fmt:message key="newUserRegistration.success.intro"/>"</div>
                        <div>
                            <a href="${requestScope.currentPage.url}" title="<fmt:message key='backToPreviousPage'/>"><fmt:message key='backToPreviousPage'/></a>
                        </div>                        
                    </div>
                    <br class="clear"/>
                </div>
            </div>
            <!-- end of content1cols section -->
        </div>
        <!-- end of pagecontent section-->

        <div id="footer">
            <template:include page="common/footer.jsp"/>
        </div>
    </template:templateBody>
</template:template>