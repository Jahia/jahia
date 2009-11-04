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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<template:addResources type="css" resources="960.css,01web.css" nodetype="jnt:page"/>----
<div id="bodywrapper"><!--start bodywrapper-->
    <div id="topheader"><!--start topheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div class="logotop"><a href="#"><img class="logotop" src="img/logo-top.png" alt="logo"/></a></div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop topheader-->
    <div id="bottomheader"><!--start bottomheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div id="search-bar"><!--start search-bar-->
                    <form class="active" method="get" action="#">
                        <div class="form-container">
                            <input type="text" value="Start Searching ..." name="term" class="text-input"/>
                            <input type="submit" value="Search" class="submit"/>
                        </div>
                    </form>
                </div>
                <!--stop search-bar-->
                <div id="breadcrumbs"><!--start breadcrumbs-->
                    <a class="first" href="#">Home</a> /
                    <a href="#">Rubrique</a> /
                    <a href="#">Sous-rubrique</a> /
                    <span>Page courante</span>
                </div>
                <!--stop breadcrumbs-->
                <h1>Template 1 </h1>
                <div class="clear"></div>
                <div id="navigation"><!--start navigation-->
                    <div id="shortcuts">
                        <h3><a title="Shortcuts" href="#">Shortcuts</a></h3>
                    </div>
                    <div id="navbar">
                        <ul class="main-nav">
                            <li class="home"><a href="/">Home</a></li>
                            <li class="submenu"><a href="Template1.html">Templates</a>

                                <div class="box-inner">
                                    <ul class="box-inner-border">
                                        <li><a href="Template1.html">Template1</a></li>
                                        <li><a href="Template2.html">Template2</a></li>
                                        <li><a href="Template3.html">Template3</a></li>
                                        <li><a href="Template4.html">Template4</a></li>
                                        <li><a href="Template5.html">Template5</a></li>
                                        <li><a href="Template6.html">Template6</a></li>
                                    </ul>
                                </div>
                            </li>
                            <li class="submenu"><a href="centre-page-fond.html">Exemples</a>

                                <div class="box-inner">
                                    <ul class="box-inner-border">
                                        <li><a href="centre-page-fond.html">Centre page avec fond</a></li>
                                        <li><a href="centre-page-simple.html">Centre page avec simple</a></li>
                                        <li><a href="box-contenu-exemples.html">Box de contenus exemples</a></li>
                                        <li><a href="box-listes-exemples.html">Box de listes exemples</a></li>
                                    </ul>
                                </div>
                            </li>
                            <li class="submenu"><a href="#">Menu Niveau 1</a>

                                <div class="box-inner">
                                    <ul class="box-inner-border">
                                        <li><a href="#">Menu Niveau 2</a></li>
                                        <li><a href="#">Menu Niveau 2</a></li>
                                        <li><a href="#">Menu Niveau 2</a></li>
                                        <li><a href="#">Menu Niveau 2</a></li>
                                        <li><a href="#">Menu Niveau 2</a></li>
                                        <li><a href="#">Menu Niveau 2</a></li>
                                    </ul>
                                </div>
                            </li>
                            <li class="nosubmenu"><a href="#">Menu Niveau 1</a></li>
                            <li class="nosubmenu"><a href="Menu Niveau 1">Menu Niveau 1</a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </div>
                <!--stop navigation-->
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop bottomheader-->
    <div id="content"><!--start content-->
        <div class="container container_16"><!--start container_16-->
            ${wrappedContent}
            <div class='clear'></div>
        </div>
        <!--stop container_16-->
        <div class="clear"></div>
    </div>
    <!--stop content-->

    <div id="topfooter"><!--start topfooter-->
        <div class="container container_16">

            <div class="container container_16"> <!--start container_16-->
                <div class='grid_10'><!--start grid_10-->
                    <h3>1er Texte Riche Footer</h3>

                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum convallis ante vel turpis
                        accumsan dapibus.
                        Integer iaculis lectus et nibh cursus bibendum. Mauris faucibus sapien eget magna ultricies
                        gravida in vel enim. Mauris scelerisque erat sit amet nisi fermentum a dictum tortor
                        ullamcorper. Phasellus blandit, urna sed congue malesuada, dui diam tincidunt ligula, id
                        facilisis massa ligula vitae orci. Aenean dignissim mattis dui, in mattis magna iaculis id.
                        Nulla in porttitor lectus. Nunc condimen</p>
                </div>
                <!--stop grid_10-->
                <div class='grid_6'><!--start grid_6-->
                    <h3>2eme Texte Riche Footer</h3>

                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum convallis ante vel turpis
                        accumsan dapibus.
                        Integer iaculis lectus et nibh cursus bibendum. Mauris faucibus sapien eget magna ultricies
                        gravida in vel enim. Mauris scelerisque erat sit amet nisi fermentum a dictum tortor
                        ullamcorper. </p>
                </div>
                <!--stop grid_6-->
                <div class='clear'></div>
            </div>
            <!--stop container_16-->

            <div class='clear'></div>

        </div>

        <div class="clear"></div>
    </div>
    <!--stop topfooter-->

    <div id="bottomfooter"><!--start bottomfooter-->
        <div class="container container_16">

            <div class="grid_16">
                <p class="copyright">
                    <span>COPYRIGHT (C) 2009 <a href="#">Jahia intranet</a></span>|
                    <span><a href="/legal/website">TERMS OF USAGE</a></span>|
                    <span><a href="/support">SUPPORT/HELP</a></span>
                </p>

                <p class="footerText">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum convallis ante
                    vel turpis accumsan dapibus. Integer iaculis lectus et nibh cursus bibendum. Mauris faucibus sapien
                    eget magna ultricies gravida in vel enim. Mauris scelerisque erat sit amet nisi fermentum a dictum
                    tortor ullamcorper. Phasellus blandit, urna sed congue malesuada, dui diam tincidunt ligula, id
                    facilisis massa ligula vitae orci. Aenean dignissim mattis dui, in mattis magna iaculis id. Nulla in
                    porttitor lectus. </p>
            </div>

            <div class='clear'></div>

        </div>

        <div class="clear"></div>
    </div>
    <!--stop bottomfooter-->


    <div class="clear"></div>
</div>
<!--stop bodywrapper-->