<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ include file="../../common/declarations.jspf"%>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div>
<div class="expectedResult">
  <fmt:message key="description.template.languageSwitching.expectedResult"/>
</div>

<h2>Testing all language switching links possibilities</h2>

<h3>horizontal display with flags</h3>
<ui:langBar display="horizontal" linkDisplay="flag"/>


<h3>horizontal display with nameCurrentLocale</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="nameCurrentLocale"/>


<h3>horizontal display with nameInLocale</h3>
<ui:langBar display="horizontal" linkDisplay="nameInLocale"/>


<h3>horizontal display with letter</h3>
<ui:langBar display="horizontal" linkDisplay="letter"/>


<h3>horizontal display with doubleLetter</h3>
<ui:langBar display="horizontal" linkDisplay="doubleLetter"/>


<h3>horizontal display with languageCode</h3>
<ui:langBar display="horizontal" linkDisplay="languageCode"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>horizontal display with flags and state</h3>
<ui:langBar display="horizontal" linkDisplay="flag" />


<h3>horizontal display with nameCurrentLocale and state</h3>
<ui:langBar display="horizontal" linkDisplay="nameCurrentLocale" />


<h3>horizontal display with nameInLocale and state</h3>
<ui:langBar display="horizontal" linkDisplay="nameInLocale" />


<h3>horizontal display with letter and state</h3>
<ui:langBar display="horizontal" linkDisplay="letter" />


<h3>horizontal display with doubleLetter and state</h3>
<ui:langBar display="horizontal" linkDisplay="doubleLetter" />


<h3>horizontal display with languageCode and state</h3>
<ui:langBar display="horizontal" linkDisplay="languageCode"
                                               />


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>vertical display with flags</h3>
<ui:langBar display="vertical" linkDisplay="flag"/>


<h3>vertical display with nameCurrentLocale</h3>
<ui:langBar display="vertical" linkDisplay="nameCurrentLocale"/>


<h3>vertical display with nameInLocale</h3>
<ui:langBar display="vertical" linkDisplay="nameInLocale"/>


<h3>vertical display with letter</h3>
<ui:langBar display="vertical" linkDisplay="letter"/>


<h3>vertical display with doubleLetter</h3>
<ui:langBar display="vertical" linkDisplay="doubleLetter"/>


<h3>vertical display with languageCode</h3>
<ui:langBar display="vertical" linkDisplay="languageCode"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>vertical display with flags and state</h3>
<ui:langBar display="vertical" linkDisplay="flag"
                                               />


<h3>vertical display with nameCurrentLocale and state</h3>
<ui:langBar display="vertical"
                                               linkDisplay="nameCurrentLocale"
                                               />


<h3>vertical display with nameInLocale and state</h3>
<ui:langBar display="vertical" linkDisplay="nameInLocale"
                                               />


<h3>vertical display with letter and state</h3>
<ui:langBar display="vertical" linkDisplay="letter"
                                               />


<h3>vertical display with doubleLetter and state</h3>
<ui:langBar display="vertical" linkDisplay="doubleLetter"
                                               />


<h3>vertical display with languageCode and state</h3>
<ui:langBar display="vertical" linkDisplay="languageCode"
                                               />


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>comboBox display with flags (actually displays the language code)</h3>
<ui:langBar display="comboBox" linkDisplay="flag"/>


<h3>comboBox display with nameCurrentLocale</h3>
<ui:langBar display="comboBox"
                                               linkDisplay="nameCurrentLocale"/>


<h3>comboBox display with nameInLocale</h3>
<ui:langBar display="comboBox" linkDisplay="nameInLocale"/>


<h3>comboBox display with letter</h3>
<ui:langBar display="comboBox" linkDisplay="letter"/>


<h3>comboBox display with doubleLetter</h3>
<ui:langBar display="comboBox" linkDisplay="doubleLetter"/>


<h3>comboBox display with languageCode</h3>
<ui:langBar display="comboBox" linkDisplay="languageCode"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>comboBox display with flags (actually displays the language code) and state</h3>
<ui:langBar display="comboBox" linkDisplay="flag"
                                               />


<h3>comboBox display with nameCurrentLocale and state</h3>
<ui:langBar display="comboBox"
                                               linkDisplay="nameCurrentLocale"
                                               />


<h3>comboBox display with nameInLocale and state</h3>
<ui:langBar display="comboBox" linkDisplay="nameInLocale"
                                               />


<h3>comboBox display with letter and state</h3>
<ui:langBar display="comboBox" linkDisplay="letter"
                                               />


<h3>comboBox display with doubleLetter and state</h3>
<ui:langBar display="comboBox" linkDisplay="doubleLetter"
                                               />


<h3>comboBox display with languageCode and state</h3>
<ui:langBar display="comboBox" linkDisplay="languageCode"
                                               />


<hr/>
<!--------------------------------------------------------------------------------------------->
<h3>horizontal display with flags, states and redirect to home page</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="flag"
                                               
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameCurrentLocale, state and redirect to home page</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="nameCurrentLocale"
                                               
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameInLocale, state and redirect to home page</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="nameInLocale"
                                               
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with letter, state and redirect to home page</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="letter"
                                               
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with doubleLetter, state and redirect to home page</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="doubleLetter"
                                               
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with languageCode, state and redirect to home page</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="languageCode"
                                               
                                               onLanguageSwitch="goToHomePage"/>

<hr/>
<!--------------------------------------------------------------------------------------------->
<h3>horizontal display with flags, states and redirect to home page and order by language code (en, es, fr, it)</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="flag"
                                               
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameCurrentLocale, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="nameCurrentLocale"
                                               
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameInLocale, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="nameInLocale"
                                               
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with letter, state and redirect to home page and order by language code (en, es, fr, it)</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="letter"
                                               
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with doubleLetter, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="doubleLetter"
                                               
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with languageCode, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:langBar display="horizontal"
                                               linkDisplay="languageCode"
                                               
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>

