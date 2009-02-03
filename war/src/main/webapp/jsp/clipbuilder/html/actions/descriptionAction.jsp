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
<html:form action="beginExtractWizard?do=clipbuilder" method="post">
    <!-- Header -->
    <table class="header" width="100%">
      <tr>
        <!-- title -->
        <td>
            <bean:message key="description.title"/>
        </td>
        <!-- config -->
        <td>
          <html:link action="/configure?do=clipbuilder&webClippingAction=initDescription">
            <bean:message key="description.button.configure"/>
          </html:link>
        </td>
        <!-- Help -->
        <td style="text-align: right">
          <a href="javascript:popitup('<%=response.encodeURL(request.getContextPath()+"/jsp/clipbuilder/html/help/descriptionHelp.jsp")%>')">
            <bean:message key="wizard.help"/>
          </a>
        </td>
      </tr>
    </table>
    <!-- Buttons -->
    <table class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <logic:present name="browseForm">
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="button.description.update"/>
            </html:submit>
          </logic:present>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="button.reset"/>
          </html:submit>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="button.next"/>
          </html:submit>
        </td>
      </tr>
    </table>
    <!-- Error-->
    <logic:messagesPresent>
      <table bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
        <tbody>
          <tr>
            <td>
              <table align="center" bgcolor="#ffffff" border="0" width="100%">
                <tbody>
                  <tr>
                    <td align="center" bgcolor="#dcdcdc">
                      <font color="#cc0000">
                        <bean:message key="error.error"/>
                      </font>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <UL>
                        <html:messages id="error">
                          <LI> <bean:write name="error"/> </LI>
                        </html:messages>
                      </UL>
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
    </logic:messagesPresent>
    <!-- Main purpose of the page-->
    <table class="clipInputTable" cellpadding="5" cellspacing="0" border="0" style="width:100%">
      <tr>
        <th style="width: 150px">
          <bean:message key="description.name"/>
        </th>
        <td>
          <html:text property="webClippingName" size="35"/>
        </td>
      </tr>
      <!-- Description -->
      <tr>
        <th>
          <bean:message key="description.description"/>
        </th>
        <td>
          <html:textarea property="webClippingDescription" rows="7" cols="20"/>
        </td>
      </tr>
      <!-- Url -->
      <tr>
        <th>
          <bean:message key="description.url"/>
        </th>
        <td>
          <logic:present name="browseForm">
            <bean:write name="descriptionClipperForm" property="webClippingTargetUrl"/>
          </logic:present>
          <logic:notPresent name="browseForm">
            <html:text property="webClippingTargetUrl" size="35"/>
          </logic:notPresent>
        </td>
      </tr>
    </table>
    <!-- Buttons -->
    <table class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <logic:present name="browseForm">
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="button.description.update"/>
            </html:submit>
          </logic:present>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="button.reset"/>
          </html:submit>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="button.next"/>
          </html:submit>
        </td>
      </tr>
    </table>
</html:form>
