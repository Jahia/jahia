<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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

