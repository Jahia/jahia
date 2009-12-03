<div id="content"><!--start content-->
    <div class="container container_16"> <!--start container_16-->
        <div class='grid_3'><!--start grid_3-->
            <div class="box">
                <div class="boxgrey boxpadding16 boxmarginbottom16">

                    <div class="box-inner">
                        <div class="box-inner-border"><!--start box -->
                            <h3 class="boxtitleh3">Rechercher sur le Wiki</h3>

                            <div id="wikisearch">
                                <form method="get" action="#">
                                    <fieldset>
                                        <p class="field">
                                            <input name="search" type="text" class="search" tabindex="4"
                                                   value="Search..."/>

                                        </p>

                                        <div class="divButton">
                                            <a class="aButton" href="#"><span>Rechercher</span></a>

                                            <div class="clear"></div>
                                        </div>
                                    </fieldset>
                                </form>
                                <ul class="list4">
                                    <li>
                                        <a href="#">Recherche avancée</a>
                                    </li>

                                </ul>
                            </div>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop box -->
            <div class="box">
                <div class="boxgrey boxpadding16 boxmarginbottom16">
                    <div class="box-inner">
                        <div class="box-inner-border"><!--start box -->

                            <h3 class="boxtitleh3">Liste 4 </h3>


                            <ul class="list4">

                                <li><a href="#">Titre de mon lien</a></li>
                                <li><a href="#">Titre de mon lien</a></li>
                                <li><a href="#">Titre de mon lien</a></li>
                                <li><a href="#">Titre de mon lien</a></li>
                                <li><a href="#">Titre de mon lien</a></li>
                                <li><a href="#">Titre de mon lien</a></li>


                            </ul>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop box -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->


        <div class='grid_10'><!--start grid_10-->

            <h2>Wiki Definition</h2>

            <div class="idTabsContainer"><!--start idTabsContainer-->
                <ul class="idTabs">
                    <li><a class="on selected" href="${url.base}${currentNode.path}.wikiHome.html"><span>Article</span></a></li>
                    <li><a class="off" href="${url.base}${currentNode.path}.wikiContribute.html"><span>Contribute</span></a></li>
                    <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.wikiHistory.html"><span>source Historique </span></a></li>
                </ul>
            </div>
            <div class="tabContainer"><!--start tabContainer-->
                ${wrappedContent}
            </div>
            <!--stop tabContainer-->
        </div>
        <!--stop grid_10-->
        <div class='grid_3'><!--start grid_3-->
            <img src="${url.currentModule}/images/jahia-apps-wiki.png" alt="jahia-apps-wiki"/>
            <h4 class="title titlegrey">Navigation</h4>

            <ul id="menusimple">
                <li class="menutop"><a href="#">Accueil</a></li>
                <li class="menutop"><a href="#">Portails thématiques</a></li>
                <li class="menutop"><a href="#">Index alphabétique</a></li>
            </ul>
            <div class="box">
                <div class="boxgrey boxpadding16 boxmarginbottom16">
                    <div class="box-inner">
                        <div class="box-inner-border"><!--start box -->
                            <h3 class="boxtitleh3">Langues</h3>

                            <ul class="list4">
                                <li><a href="#">Français</a></li>
                                <li><a href="#">Anglais</a></li>
                                <li><a href="#">Portugais</a></li>
                                <li><a href="#">Italien</a></li>
                                <li><a href="#">Chinois</a></li>
                                <li><a href="#">Indien</a></li>
                            </ul>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop box -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->

        <div class='clear'></div>
    </div>
    <!--stop container_16-->

    <div class="clear"></div>
</div>
<!--stop content-->
