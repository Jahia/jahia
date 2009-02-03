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

<%@ include file="../../common/declarations.jspf"%>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div>
<div class="expectedResult">
  <fmt:message key="description.template.languageSwitching.expectedResult"/>
</div>

<h2>Testing all language switching links possibilities</h2>

<h3>horizontal display with flags</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="flag"/>


<h3>horizontal display with nameCurrentLocale</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="nameCurrentLocale"/>


<h3>horizontal display with nameInLocale</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="nameInLocale"/>


<h3>horizontal display with letter</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="letter"/>


<h3>horizontal display with doubleLetter</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="doubleLetter"/>


<h3>horizontal display with languageCode</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="languageCode"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>horizontal display with flags and state</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="flag"
                                               displayLanguageState="true"/>


<h3>horizontal display with nameCurrentLocale and state</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="nameCurrentLocale"
                                               displayLanguageState="true"/>


<h3>horizontal display with nameInLocale and state</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="nameInLocale"
                                               displayLanguageState="true"/>


<h3>horizontal display with letter and state</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="letter"
                                               displayLanguageState="true"/>


<h3>horizontal display with doubleLetter and state</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="doubleLetter"
                                               displayLanguageState="true"/>


<h3>horizontal display with languageCode and state</h3>
<ui:languageSwitchingLinks display="horizontal" linkDisplay="languageCode"
                                               displayLanguageState="true"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>vertical display with flags</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="flag"/>


<h3>vertical display with nameCurrentLocale</h3>
<ui:languageSwitchingLinks display="vertical"
                                               linkDisplay="nameCurrentLocale"/>


<h3>vertical display with nameInLocale</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="nameInLocale"/>


<h3>vertical display with letter</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="letter"/>


<h3>vertical display with doubleLetter</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="doubleLetter"/>


<h3>vertical display with languageCode</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="languageCode"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>vertical display with flags and state</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="flag"
                                               displayLanguageState="true"/>


<h3>vertical display with nameCurrentLocale and state</h3>
<ui:languageSwitchingLinks display="vertical"
                                               linkDisplay="nameCurrentLocale"
                                               displayLanguageState="true"/>


<h3>vertical display with nameInLocale and state</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="nameInLocale"
                                               displayLanguageState="true"/>


<h3>vertical display with letter and state</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="letter"
                                               displayLanguageState="true"/>


<h3>vertical display with doubleLetter and state</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="doubleLetter"
                                               displayLanguageState="true"/>


<h3>vertical display with languageCode and state</h3>
<ui:languageSwitchingLinks display="vertical" linkDisplay="languageCode"
                                               displayLanguageState="true"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>comboBox display with flags (actually displays the language code)</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="flag"/>


<h3>comboBox display with nameCurrentLocale</h3>
<ui:languageSwitchingLinks display="comboBox"
                                               linkDisplay="nameCurrentLocale"/>


<h3>comboBox display with nameInLocale</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="nameInLocale"/>


<h3>comboBox display with letter</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="letter"/>


<h3>comboBox display with doubleLetter</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="doubleLetter"/>


<h3>comboBox display with languageCode</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="languageCode"/>


<hr/>
<!--------------------------------------------------------------------------------------------->

<h3>comboBox display with flags (actually displays the language code) and state</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="flag"
                                               displayLanguageState="true"/>


<h3>comboBox display with nameCurrentLocale and state</h3>
<ui:languageSwitchingLinks display="comboBox"
                                               linkDisplay="nameCurrentLocale"
                                               displayLanguageState="true"/>


<h3>comboBox display with nameInLocale and state</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="nameInLocale"
                                               displayLanguageState="true"/>


<h3>comboBox display with letter and state</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="letter"
                                               displayLanguageState="true"/>


<h3>comboBox display with doubleLetter and state</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="doubleLetter"
                                               displayLanguageState="true"/>


<h3>comboBox display with languageCode and state</h3>
<ui:languageSwitchingLinks display="comboBox" linkDisplay="languageCode"
                                               displayLanguageState="true"/>


<hr/>
<!--------------------------------------------------------------------------------------------->
<h3>horizontal display with flags, states and redirect to home page</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="flag"
                                               displayLanguageState="true"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameCurrentLocale, state and redirect to home page</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="nameCurrentLocale"
                                               displayLanguageState="true"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameInLocale, state and redirect to home page</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="nameInLocale"
                                               displayLanguageState="true"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with letter, state and redirect to home page</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="letter"
                                               displayLanguageState="true"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with doubleLetter, state and redirect to home page</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="doubleLetter"
                                               displayLanguageState="true"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with languageCode, state and redirect to home page</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="languageCode"
                                               displayLanguageState="true"
                                               onLanguageSwitch="goToHomePage"/>

<hr/>
<!--------------------------------------------------------------------------------------------->
<h3>horizontal display with flags, states and redirect to home page and order by language code (en, es, fr, it)</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="flag"
                                               displayLanguageState="true"
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameCurrentLocale, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="nameCurrentLocale"
                                               displayLanguageState="true"
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with nameInLocale, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="nameInLocale"
                                               displayLanguageState="true"
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with letter, state and redirect to home page and order by language code (en, es, fr, it)</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="letter"
                                               displayLanguageState="true"
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with doubleLetter, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="doubleLetter"
                                               displayLanguageState="true"
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>


<h3>horizontal display with languageCode, state and redirect to home page and order by language code (en, es, fr,
    it)</h3>
<ui:languageSwitchingLinks display="horizontal"
                                               linkDisplay="languageCode"
                                               displayLanguageState="true"
                                               order="en, es, fr, it"
                                               onLanguageSwitch="goToHomePage"/>

