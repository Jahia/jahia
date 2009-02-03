<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-nested" prefix="nested"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@page import="org.jahia.clipbuilder.html.struts.Util.*;"%>
<table align="center" class="principal" width="100%">
  <html:form action="saveClipper?do=clipbuilder" method="post">
    <!-- Header -->
    <table align="center" class="principal" width="100%">
      <!-- Title -->
      <tr>
        <td>
          <bean:message key="preview.title"/>
        </td>
      </tr>
    </table>
    <!-- Buttons -->
    <table class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <logic:equal name="previewForm" property="from" value="wizard">
            <!-- Done -->
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="button.done"/>
            </html:submit>
          </logic:equal>
          <logic:present name="testClipperForm">
            <!-- Previous -->
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="button.previous"/>
            </html:submit>
          </logic:present>
        </td>
      </tr>
    </table>
    <table  width="100%">
       <iframe id="clip_builder" src="<%=response.encodeURL(request.getContextPath()+"/administration/webBrowser?do=clipbuilder")%>" width="100%" height="375" frameborder="1" scrolling="auto"></iframe>
    </table>
  </html:form>
  <!-- HTML page -->
</table>
