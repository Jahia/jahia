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

<%@ include file="../../common/declarations.jspf" %>
<!--start collapsible loginFormTop-->
<div id="peoplebox0" class="collapsible">
    <div class="boxloginFormTop"><!--start box 4 default-->
        <div class="boxloginFormTop-topright"></div>
        <div class="boxloginFormTop-topleft"></div>
        <div class="boxloginFormTop-header">
            <div id="loginFormTop"><!--start loginFormTop-->
                <ui:loginArea>
                    <p><ui:loginUsername labelCssClassName="hide" cssClassName="text" labelKey="username" tabIndex="1"/>
                        <ui:loginPassword labelCssClassName="hide" cssClassName="text" labelKey="password"
                                          tabIndex="2"/>
                        <input class="gobutton png" type="image"
                               src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/loginformtop-button.png'/>"
                               tabindex="3"/></p>

                    <p class="loginFormTopCheckbox"><input type="checkbox" name="remember" id="remember"
                                                           class="loginFormTopInputCheckbox" value="checked"
                                                           tabindex="1"/>
                        <label class="loginFormTopRememberLabel" for="remember"><fmt:message key="rememberMe"/></label></p>
                </ui:loginArea>
            </div>
            <!--stop loginFormTop-->
        </div>
        <div class="box4-bottomright"></div>
        <div class="box4-bottomleft"></div>
        <div class="clear"> </div>
    </div>
    <div class="clear"> </div>
    <!--stop box 4 default-->
</div>
<!--stop collapsible loginFormTop-->