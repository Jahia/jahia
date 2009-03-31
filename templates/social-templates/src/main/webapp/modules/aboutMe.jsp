<%@ include file="../common/declarations.jspf" %>
<h3>A propos de moi</h3>

                        <div class="aboutMeListItem"><!--start aboutMeListItem -->
                            <div class="aboutMePhoto">
                                <img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/user.png'/>" alt="photo"/>
                            </div>
                            <div class="aboutMeBody"><!--start aboutMeBody -->
                                <h5>Prenom Nom</h5>

                                <p class="aboutMeAge">Age : 35ans</p>

                                <div class="clear"></div>
                                <p class="aboutMeResume">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce
                                    auctor dapibus nibh. Proin viverra arcu eget lorem.Maecenas ligula ligula, tristique
                                    in, venenatis posuere...</p>
                            </div>
                            <!--stop aboutMeBody -->
                            <div class="aboutMeAction">
                                <a class="aboutMeMore" href="#" title="title">More Information</a>
                            </div>
                            <div class="clear"></div>
                        </div>
                        <!--stop aboutMeListItem -->