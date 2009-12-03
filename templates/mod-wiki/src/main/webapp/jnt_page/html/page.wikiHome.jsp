<%@ page import="org.jahia.services.render.Resource" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addWrapper name="wikiWrapper"/>
<div id="one"><!--start tab One-->

    <div class="intro wiki">


        <p>
            ---------
            <template:module path="content" autoCreateType="jnt:wikiContent">
                <template:import>
                    <content xmlns:j='http://www.jahia.org/jahia/1.0' xmlns:jcr='http://www.jcp.org/jcr/1.0'
                             jcr:primaryType='jnt:wikiContent' j:nodename='content'
                             jcr:text="My first wiki !"
                            />
                </template:import>
            </template:module>
            ---------

            Un wiki est un <a class="wikidef" href="wikidef.html">logiciel</a> de la famille des systèmes de gestion de
            contenu de site web rendant les pages web modifiables par tous les visiteurs y étant autorisés. Il facilite
            l'écriture collaborative de documents avec un minimum de contraintes. Créé en 2001, Wikipédia est devenu peu
            à peu le plus visité des sites web écrits avec un wiki.
        </p>
        <p>
            Le mot « wiki » vient du redoublement hawaiien <a class="wikidef-new" href="#">wiki wiki</a>, qui signifie «
            rapide ». Les wikis ont été inventés en 1995 par Ward Cunningham pour réaliser la section d'un site sur la
            programmation informatique, qu'il a appelé WikiWikiWeb. Au milieu des années 2000, les wikis ont atteint un
            bon niveau de maturité. Ils sont depuis lors associés à ce qui est dénommé Web 2.0.</p>

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
        <!--stop box -->
        <a name="Fonctionnement_technique" id="Fonctionnement_technique"></a>

        <h3>Fonctionnement technique</h3>

        <p>On appelle « moteur de wiki » le logiciel qui met en œuvre la gestion par wiki.

            Une particularité des wikis par rapport aux autres systèmes de gestion de contenu est que toutes les
            personnes autorisées à modifier le contenu ont les mêmes droits de modification et jouissent d'une liberté
            d'action qui n'est limitée que par la nécessité de ne pas compromettre l'intégrité technique du site :
            chacun peut aussi bien déplacer une simple virgule qu'effacer tout le contenu d'une page. Seules les
            informations générales de navigation servant de modèle à la structure de la page ne sont pas modifiables.
        </p>

        <div class="bottomanchor"><!--start anchor--><a href="#bodywrapper">Page Top </a>

            <div class="clear"></div>
        </div>
        <!--stop anchor-->


        <a name="Identification_des_visiteurs" id="Identification_des_visiteurs"></a>
        <h4>Identification des visiteurs</h4>

        <p>Un wiki n'est pas forcément modifiable par tout le monde ; on peut exiger que les visiteurs s'inscrivent
            avant d'être autorisés à modifier les pages. Dans le cas des wikis qui sont complètement ouverts au public,
            diverses procédures techniques et sociales sont mises en œuvre pour limiter et annuler les modifications
            jugées indésirables.</p>

        <p>Lorsqu'un wiki autorise des visiteurs anonymes à modifier les pages, c'est l'<a
                href="http://fr.wikipedia.org/wiki/Adresse_IP" title="Adresse IP">adresse IP</a> de ces derniers qui les
            identifie ; les utilisateurs inscrits peuvent quant à eux se connecter sous leur nom d'utilisateur.</p>

        <div class="bottomanchor"><!--start anchor--><a href="#bodywrapper">Page Top </a>

            <div class="clear"></div>
        </div>
        <!--stop anchor-->

        <a name="edition" id="edition"></a>
        <h4>Identification des visiteurs</h4>

        <p>On accède à un wiki, en lecture comme en écriture, avec un navigateur web classique. On peut visualiser les
            pages dans deux modes différents : le mode lecture, qui est le mode par défaut, et le mode d'édition, qui
            présente la page sous une forme qui permet de la modifier. En mode d'édition, le texte de la page, affiché
            dans un formulaire web, s'enrichit d'un certain nombre de caractères supplémentaires, suivant les règles
            d'une syntaxe informatique particulière : le wikitexte, qui permet d'indiquer la mise en forme du texte, de
            créer des liens, de disposer des images, etc. Le wikitexte a été conçu pour que les fonctionnalités les plus
            courantes soient faciles à assimiler et taper.

            Quelques wikis proposent, au lieu du wikitexte, une interface d'édition WYSIWYG, citons par exemple
            Confluance et XWiki.
        </p>

        <div class="bottomanchor"><!--start anchor--><a href="#bodywrapper">Page Top </a>

            <div class="clear"></div>
        </div>
        <!--stop anchor-->
    </div>
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

</div>
<!--stop grid_10-->
