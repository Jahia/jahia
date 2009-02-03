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
  <html:form action="browse?do=clipbuilder" method="post">
    <!-- Header -->
    <table align="center" class="principal" width="100%">
      <!-- Title -->
      <tr>
        <td>
            <bean:message key="browse.title"/>
        </td>
        <!-- Help -->
        <td style="text-align:right" >
          <a href="javascript:popitup('<%=response.encodeURL(request.getContextPath()+"/jsp/clipbuilder/html/help/browseHelp.jsp")%>')">
            <bean:message key="wizard.help"/>
          </a>
        </td>
      </tr>
    </table>
    <!-- Buttons -->
    <table class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <!-- Init-->
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="browse.resetRecord"/>
          </html:submit>
          <logic:equal name="recording" property="statut" value="0">
            <!-- Not recording-->
            <!-- Start -->
            <logic:present name="selectPartForm">
              <html:submit styleClass="fancyButton" property="webClippingAction" disabled="true">
                <bean:message key="browse.startRecord"/>
              </html:submit>
            </logic:present>
            <logic:notPresent name="selectPartForm">
              <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
                <bean:message key="browse.startRecord"/>
              </html:submit>
            </logic:notPresent>
            <!-- Remove last -->
            <html:submit styleClass="fancyButton" property="webClippingAction" disabled="true">
              <bean:message key="browse.removeLast"/>
            </html:submit>
            <!-- Replay: this feature has been removed -->
            <!-- Next -->
            <html:submit styleClass="fancyButton" property="webClippingAction" disabled="true">
              <bean:message key="button.next"/>
            </html:submit>
          </logic:equal>
          <logic:equal name="recording" property="statut" value="1">
            <!-- Recording-->
            <!-- Stop -->
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="browse.stopRecord"/>
            </html:submit>
            <!-- Remove Last -->
            <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
              <bean:message key="browse.removeLast"/>
            </html:submit>
            <!-- Next -->
            <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
              <bean:message key="button.next"/>
            </html:submit>
          </logic:equal>
        </td>
      </tr>
    </table>
    <!-- Errors-->
    <logic:messagesPresent>
      <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
        <tbody>
          <tr>
            <td>
              <table align="center" bgcolor="#ffffff" border="0" width="100%">
                <tbody>
                  <tr>
                    <td align="center" bgcolor="#dcdcdc">
                      <font color="#cc0000">
                        <bean:message key="error.warning"/>
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
    <!-- Information -->
    <table border="0" width="100%" cellpadding="5" cellspacing="0" class="clipInputTable">
      <thead>
        <tr style="background-color: #cccccc">
          <th colspan="2">
            <bean:message key="information"/>
          </th>
        </tr>
      </thead>
      <tbody>
            <!-- Browse Information -->
              <tr>
                <!-- Automatic record-->
                <th>
                  <bean:message key="browse.beginRecord.tilte"/>
                </th>
                <td>
                  <bean:message key="browse.beginRecord.description"/>
                </td>
              </tr>
              <tr>
                <!-- Add manually-->
                <th>
                  <bean:message key="browse.addUrl"/>
                </th>
                <td>
                  <html:text property="sourceUrl" size="25"/>
                  <html:submit styleClass="fancyButton" property="webClippingAction" disabled="false">
                    <bean:message key="browse.button.addUrl"/>
                  </html:submit>
                </td>
              </tr>
      </tbody>
    </table>
    <iframe id="clip_builder" src="<%=response.encodeURL(request.getContextPath()+"/administration/webBrowser?do=clipbuilder")%>" width="100%" height="375" frameborder="1" scrolling="auto"></iframe>
  </html:form>
<!-- HTML page -->
