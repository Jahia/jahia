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
<%@page import="org.jahia.clipbuilder.html.*"%>
<!-- Main -->
<table align="center" class="backcolor2 principal" width="100%">
  <html:form action="configure?do=clipbuilder" method="post">
    <!-- Header -->
    <table align="center" class="principal" width="100%">
      <!-- Title -->
      <tr>
        <td>
          <bean:message key="configuration.title"/>
        </td>
        <!-- Help -->
        <td style="text-align:right">
          <a href="javascript:popitup('<%=response.encodeURL(request.getContextPath()+"/jsp/clipbuilder/html/help/configureHelp.jsp")%>')">
            <bean:message key="wizard.help"/>
          </a>
        </td>
      </tr>
    </table>
    <!-- Buttons -->
    <table align="center" class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="configuration.button.saveDefault"/>
          </html:submit>
        <%if (SessionManager.getClipperBean(request) != null) {        %>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="configuration.button.saveCurrentClipper"/>
          </html:submit>
        <%}        %>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="configuration.button.reset"/>
          </html:submit>
        </td>
      </tr>
    </table>
    <!-- Error-->
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
    <!-- Main -->
    <table width="100%">
      <!-- Connection -->
      <tr>
        <td>
          <table width="100%">
            <!-- title-->
            <tr>
              <td class="topmenugreen">
                <bean:message key="connection.title"/>
              </td>
            </tr>
            <!-- Proxy -->
            <tr>
              <td>
                <table width="100%">
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="connection.proxy"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:text name="configureForm" property="proxy"/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <!-- Browser -->
      <tr>
        <td>
          <table width="100%">
            <tr>
              <td class="topmenugreen">
                <bean:message key="configuration.browser.title"/>
              </td>
            </tr>
            <!-- Type/javascript/ssl -->
            <tr>
              <td>
                <table width="100%">
                  <!-- Javascript code -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="configuration.browser.javascript.code"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:select property="browserJavascriptCode" size="1">
                        <html:option value="2">
                          <bean:message key="configuration.browser.javascript.code.nothing"/>
                        </html:option>
                        <html:option value="0">
                          <bean:message key="configuration.browser.javascript.code.remove"/>
                        </html:option>
                        <html:option value="1">
                          <bean:message key="configuration.browser.javascript.code.refactor"/>
                        </html:option>
                      </html:select>
                    </td>
                  </tr>
                  <!-- Javascript Event -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="configuration.browser.javascript.event"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:select property="browserJavascriptEvent" size="1">
                        <html:option value="2">
                          <bean:message key="configuration.browser.javascript.event.nothing"/>
                        </html:option>
                        <html:option value="0">
                          <bean:message key="configuration.browser.javascript.event.remove"/>
                        </html:option>
                        <html:option value="1">
                          <bean:message key="configuration.browser.javascript.event.refactor"/>
                        </html:option>
                      </html:select>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <!-- Web Client -->
      <tr>
        <td>
          <table width="100%">
            <tr>
              <td class="topmenugreen">
                <bean:message key="client.title"/>
              </td>
            </tr>
            <!-- Type/javascript/ssl -->
            <tr>
              <td>
                <table width="100%">
                  <!-- Type -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="client.type"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:select property="client" size="1">
                        <html:option value="1">
                          <bean:message key="client.type.httpClient"/>
                        </html:option>
                        <html:option value="0">
                          <bean:message key="client.type.htmlunit"/>
                        </html:option>
                      </html:select>
                    </td>
                  </tr>
                  <!-- Javascript -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="client.javascript.enable"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:checkbox property="enableJavascript"/>
                    </td>
                  </tr>
                  <!-- SSL -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="client.ssl.enable"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:checkbox property="enableSSL"/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <!-- Html Document -->
      <tr>
        <td>
          <table width="100%">
            <!-- Title  -->
            <tr>
              <td class="topmenugreen">
                <bean:message key="htmlDocument.title"/>
              </td>
            </tr>
            <tr>
              <td>
                <table width="100%">
                  <!-- Type -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="htmlDocument.type"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <!-- See HTMLDocument class for value-->
                      <html:select property="htmlDocument" size="1">
                        <html:option value="1">
                          <bean:message key="htmlDocument.type.htmlparser"/>
                        </html:option>
                        <html:option value="0">
                          <bean:message key="htmlDocument.type.dom"/>
                        </html:option>
                      </html:select>
                    </td>
                  </tr>
                  <!-- Css -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="htmlDocument.css.enable"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:checkbox property="enableCSS"/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <!-- Portlet -->
      <tr>
        <td>
          <table width="100%">
            <!-- Title  -->
            <tr>
              <td class="topmenugreen">
                <bean:message key="portlet.config.title"/>
              </td>
            </tr>
            <tr>
              <td>
                <table width="100%">
                  <!-- Cache -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="portlet.cacheExpiration"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:text name="configureForm" property="portletCacheExpiration"/>
                    </td>
                  </tr>
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="portlet.cacheContext"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:select property="portletCacheContext" size="1">
                        <html:option value="0">
                          <bean:message key="portlet.cacheContext.Portal"/>
                        </html:option>
                        <html:option value="1">
                          <bean:message key="portlet.cacheContext.User"/>
                        </html:option>
                      </html:select>
                    </td>
                  </tr>
                  <!-- SSL -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="portlet.config.ssl"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <html:checkbox property="portletEnableSSL"/>
                    </td>
                  </tr>
                  <!-- Continual Clipping -->
                  <tr>
                    <td width="33%" class="leftlevel2 waInput">
                      <bean:message key="portlet.config.clipbuilder"/>
                    </td>
                    <td class="leftlevel2 waInput">
                      <!-- See HTMLDocument class for value-->
                      <html:select property="portletContinualClipping" size="1">
                        <html:option value="4">
                          <bean:message key="portlet.config.clipbuilder.popup.passif"/>
                        </html:option>
                        <html:option value="3">
                          <bean:message key="portlet.config.clipbuilder.popup.actif"/>
                        </html:option>
                        <html:option value="1">
                          <bean:message key="portlet.config.clipbuilder.iframe.actif"/>
                        </html:option>
                        <html:option value="2">
                          <bean:message key="portlet.config.clipbuilder.iframe.passif"/>
                        </html:option>
                      </html:select>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </html:form>
</table>
