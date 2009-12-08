<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addWrapper name="wrapper.wiki"/>
<div id="one"><!--start tab One-->

    <div class="intro wiki">
        <template:module node="${currentNode}" forcedTemplate="syntax"/>

     <%--
        <div class="box"><!--start box -->
            <div class="boxshadow boxpadding16 boxmarginbottom16">
                <div class="box-inner">

                    <div class="box-inner-border">
                        <h3 class="boxtitleh3"><a href="#">Sommaire</a></h3>
                        <ul class="sumary wiki">
                            <li class="toclevel-1 tocsection-1"><a href="#Fonctionnement_technique"><span
                                    class="tocnumber">1</span> <span class="toctext">Fonctionnement technique</span></a>
                                <ul>
                                    <li class="toclevel-2 tocsection-2"><a href="#Identification_des_visiteurs"><span
                                            class="tocnumber">1.1</span> <span class="toctext">Identification des visiteurs</span></a>
                                    </li>
                                    <li class="toclevel-2 tocsection-3"><a href="#edition"><span
                                            class="tocnumber">1.2</span> <span class="toctext">Édition</span></a></li>

                                    <li class="toclevel-2 tocsection-4"><a href="#Liens_et_cr.C3.A9ation_de_pages"><span
                                            class="tocnumber">1.3</span> <span class="toctext">Liens et création de pages</span></a>
                                    </li>
                                    <li class="toclevel-2 tocsection-5"><a href="#Suivi_des_modifications"><span
                                            class="tocnumber">1.4</span> <span
                                            class="toctext">Suivi des modifications</span></a></li>
                                </ul>
                            </li>
                            <li class="toclevel-1 tocsection-6"><a href="#Aspects_sociaux_du_wiki"><span
                                    class="tocnumber">2</span> <span class="toctext">Aspects sociaux du wiki</span></a>
                                <ul>
                                    <li class="toclevel-2 tocsection-7"><a href="#Histoire_de_la_wikisph.C3.A8re"><span
                                            class="tocnumber">2.1</span> <span
                                            class="toctext">Histoire de la wikisphère</span></a></li>

                                    <li class="toclevel-2 tocsection-8"><a href="#Fonctionnement_humain"><span
                                            class="tocnumber">2.2</span> <span
                                            class="toctext">Fonctionnement humain</span></a></li>
                                    <li class="toclevel-2 tocsection-9"><a href="#Exemple_avec_Wikip.C3.A9dia"><span
                                            class="tocnumber">2.3</span> <span
                                            class="toctext">Exemple avec Wikipédia</span></a></li>
                                </ul>
                            </li>
                            <li class="toclevel-1 tocsection-10"><a href="#Notes_et_r.C3.A9f.C3.A9rences"><span
                                    class="tocnumber">3</span> <span class="toctext">Notes et références</span></a></li>
                            <li class="toclevel-1 tocsection-11"><a href="#Voir_aussi"><span class="tocnumber">4</span>
                                <span class="toctext">Voir aussi</span></a>

                                <ul>
                                    <li class="toclevel-2 tocsection-12"><a href="#Articles_connexes"><span
                                            class="tocnumber">4.1</span> <span class="toctext">Articles connexes</span></a>
                                    </li>
                                    <li class="toclevel-2 tocsection-13"><a href="#Liens_externes"><span
                                            class="tocnumber">4.2</span> <span class="toctext">Liens externes</span></a>
                                    </li>
                                    <li class="toclevel-2 tocsection-14"><a href="#Bibliographie"><span
                                            class="tocnumber">4.3</span> <span class="toctext">Bibliographie</span></a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                        <div class="clear"></div>

                    </div>
                </div>
            </div>
        </div>
        --%>
        <div class="bottomanchor"><!--start anchor--><a href="wikiPage.jsp#bodywrapper">Page Top </a>

            <div class="clear"></div>
        </div>
        <!--stop anchor-->
    </div>
<%--
    <div class="box">
        <div class="boxshadow boxgrey boxpadding16 boxmarginbottom16">
            <div class="box-inner">

                <div class="box-inner-border"><!--start box -->
                    <h3>Voir aussi</h3>
                    <h4>Articles connexes</h4>
                    <ul class="list4">

                        <li><a href="#">Liste de logiciels wiki</a><a href="#"></a></li>
                        <li><a href="#">WikiWikiWeb</a>, le premier wiki</li>
                        <li><a href="#">Wiki d'entreprise</a></li>
                        <li><a href="#">Wiki territorial</a></li>

                        <li><a href="#">Web social</a></li>
                    </ul>
                    <h4>Liens externes</h4>
                    <ul class="list4">
                        <li><span class="lang">(fr)</span> <a class="external" href="#">Catégorie wiki</a> de l’annuaire
                            <a href="#">dmoz</a></li>
                        <li><span class="lang">(fr)</span> <a class="external" href="#">la page Wiki dans Framasoft</a>notices
                        </li>

                        <li><span class="lang">(fr)</span> <a class="external" href="#">« De la supercherie wiki »</a>,
                            critique du principe wiki en dix points
                        </li>
                        <li><span class="lang">(fr)</span> <a class="external" href="#">« Les wikis dans
                            l'infrastructure Gouvernementale »</a>, une vision d'avenir (vidéo)
                        </li>
                        <li><span class="lang">(fr)</span> <a class="external" href="#">Sept cours sur Wikipédia</a>en
                            tant que système de gestion des connaissances, en licence <a
                                    href="http://fr.wikipedia.org/wiki/GFDL" title="GFDL">GFDL</a></li>
                        <li><span class="lang">(fr)</span><a class="external" href="#"> WikiMatrix</a>comparateur de
                            wikis
                        </li>

                    </ul>
                    <h4>Bibliographie</h4>
                    <ul class="list4">

                        <li>Jérôme Delacroix, Les wikis. Espaces de l'intelligence collective, M2 Éditions, Paris, 2005.
                            (fr)
                        </li>
                    </ul>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->
 --%>
</div>
<!--stop grid_10-->
