<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="converter.css,files-icons.css"/>

<div class="container container_16"><!--start container_16-->
	<div class='grid_6'><!--start grid_6-->
<c:if test="${!renderContext.loggedIn}">

<div class="boxconverter">
                <div class="boxconvertergrey boxconverterpadding16 boxconvertermarginbottom16">

                    <div class="boxconverter-inner">
                        <div class="boxconverter-inner-border"><!--start boxconverter -->

				  <div class="Form formLoginConverter">
                  <form method="post" action="index.html">
                  <h3 class="boxconvertertitleh3">Accés Document converter</h3>
				<fieldset><legend>Login spécifique Document converter</legend>
                <p><label for="convertertitle" class="left">Login :</label>
                <input type="text" name="convertertitle" id="convertertitle" class="field" value="" tabindex="25" /></p>
                <p><label for="loginConverterPassword" class="left">Password :</label>
                <input type="text" name="loginConverterPassword" id="loginConverterPassword" class="field" value="" tabindex="25" /></p>
                <div class="formMarginLeft"><input type="submit" name="submit" id="submit" class="button" value="Accés" tabindex="28" />
                </div>
              </fieldset>
              </form>
              </div>
                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxconverter -->

      <div class='clear'></div></div><!--stop grid_6-->

    <div class='grid_10'><!--start grid_10-->


<div class='clear'></div></div><!--stop grid_10-->




<div class='clear'></div></div><!--stop container_16-->

</c:if>

<c:if test="${renderContext.loggedIn}">

    <div class="container container_16"> <!--start container_16-->



	<div class='grid_16'><!--start grid_16-->
    <h4 class="boxconverter-title2">Uploader un document pour transformation</h4>
<div class="boxconverter"><!--start boxconverter -->
	<div class="boxconvertergrey boxconverterpadding10 boxconvertermarginbottom16">
		<div class="boxconverter-inner">
			<div class="boxconverter-inner-border">
           <div class="Form formconverterupload"><!--start formconverterupload-->
                        <form action="${url.base}${currentNode.path}.convert.do" method="post" enctype="multipart/form-data">
						<input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}" />
                        <p>
                        <label for="uploadfile" class="left">Upload a document : </label>
                            <input id="uploadfile" name="fileField" tabindex="1" type="file" />
                        </p>
                        <p>
                        <label for="uploadfile" class="left">Transform into : </label>
                        <select name="mimeType">
                        <option value="application/pdf">Pdf</option>
                        <option value="application/msword">Word</option>
                        <option value="text/plain">Text</option>
                        </select>
                        <input type="submit" id="submit" class="button" value="Convert" tabindex="4" />
                        </p>
              </form>

			</div>
            </div>
		</div>
	</div>
</div><!--stop boxconverter -->


<div class="boxconverter "><!--start boxconverter -->
            <div class="boxconverterpadding16 boxconvertermarginbottom16">
                <div class="boxconverter-inner">
                  <div class="boxconverter-inner-border">
                    <div class="floatright">

                    </div>
                    <div class="imagefloatleft">
					  <div class="itemImage itemImageLeft"><span class="icon6464 doc6464"></span></div>
					  <div class="itemImageConverterArrow itemImageLeft"><a href="#"><img alt="" src="${url.currentModule}/img/convert.png"/></a></div>
					  <div class="itemImage itemImageLeft"><span class="icon6464 pdf6464"></span></div>
					</div>
                    <h3 >Document original : Le nom de mon document.Doc</h3>
                    <h3 >Document transforme : <a href="#"><img alt="" src="${url.currentModule}/img/download.png"/> Le nom de mon document.PDF</a></h3>
                    <span class="clearMaringPadding converterdate">Date de Transformation : 10/02/2010</span>
	  <span class="clearMaringPadding converterauthor"><a href="#">Par Regis Mora</a></span>
                    <!--stop boxconverter -->
                    <div class="clear"></div>
                  </div>
			</div>
		</div>
</div><!--stop boxconverter -->

<div class="boxconverter">
            <div class=" boxconverterred boxconverterpadding16 boxconvertermarginbottom16">
                <div class="boxconverter-inner">
                    <div class="boxconverter-inner-border">
                    <h3 class="boxconvertertitleh3 clearMaringPadding">Erreur :</h3>
                    <p class="clearMaringPadding">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean scelerisque lorem sed metus vehicula non venenatis eros blandit. Donec molestie vestibulum nunc, ac molestie augue semper a. Quisque ut pharetra sem. Ut vitae urna ipsum.</p>
                      <div class="clear"></div>
                  </div>
			</div>
		</div>
</div>
<div class="boxconverter">
            <div class=" boxconvertergreen boxconverterpadding16 boxconvertermarginbottom16">
                <div class="boxconverter-inner">
                    <div class="boxconverter-inner-border">

                    <h3 class="boxconvertertitleh3 clearMaringPadding">Transformation Valide :</h3>
                    <p class="clearMaringPadding">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean scelerisque lorem sed metus vehicula non venenatis eros blandit. Donec molestie vestibulum nunc, ac molestie augue semper a. Quisque ut pharetra sem. Ut vitae urna ipsum.</p>
                      <div class="clear"></div>
                  </div>
			</div>
		</div>
</div>
<h3>Icones disponibles :</h3>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 file6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 video6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 pptx6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 exe6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 doc6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 html6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 img6464"></span></div>
</div>
<div class="clear"></div><br/><br/>

<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 sound6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 xls6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 zip6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 txt6464"></span></div>
</div>
<div class="imagefloatleft">
    <div class="itemImage itemImageLeft"><span class="icon6464 pdf6464"></span></div>
</div>
<div class="clear"></div><br/><br/>



    <h4 class="boxconverter-title2">Rapport des transformations</h4>

    <div class="boxconverter">
            <div class="boxconvertergrey boxconverterpadding16 boxconvertermarginbottom16">
                <div class="boxconverter-inner">
                    <div class="boxconverter-inner-border"><!--start boxconverter -->

<table width="100%" class="table tableConverterRapport " summary="Rapport de mes transformations">
    <caption class=" hidden">
    Edition Mes taches en cours (table)
    </caption>
    <colgroup>
        <col span="1" width="10%" class="col1"/>
        <col span="1" width="30%" class="col2"/>
        <col span="1" width="10" class="col3"/>
        <col span="1" width="10%" class="col4"/>
        <col span="1" width="15%" class="col5"/>
        <col span="1" width="15%" class="col5"/>
        <col span="1" width="10%" class="col5"/>
    </colgroup>
    <thead>
        <tr>
            <th class="center" id="Statut" scope="col">Statut</th>
            <th id="TitleOriginal" scope="col">Nom Document Original<a title="sort down" href="#"> <img src="${url.currentModule}/img/sort-arrow-down.png" alt="down"/></a></th>
            <th class="center" id="OriginalDoc" scope="col">Format Original</th>
            <th class="center" id="TranformDoc" scope="col">Format Transforme</th>
            <th class="center" id="Date" scope="col">Date de convertion <a title="sort down" href="#"> <img src="${url.currentModule}/img/sort-arrow-up.png" alt="up"/></a></th>
            <th class="center" id="User" scope="col">Utilisateur<a title="sort down" href="#"> <img src="${url.currentModule}/img/sort-arrow-up.png" alt="up"/></a></th>
            <th class="center" id="Download" scope="col">Download</th>
        </tr>
    </thead>

    <tbody>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="odd">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/valide.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
        <tr class="even">
            <td class="center" headers="Statut"><img alt="" src="${url.currentModule}/img/error.png" /></td>
            <td headers="TitleOriginal"><a href="#">Titre de mon document</a></td>
            <td class="center" headers="OriginalDoc">.Word</td>
            <td class="center" headers="TranformDoc">.PDF</td>
            <td class="center" headers="Date">20 Nov 2009</td>
            <td class="center" headers="User">Regis Mora</td>
            <td class="center" headers="Download"><a href="#"><img alt="" src="${url.currentModule}/img/download.png"/></a></td>
        </tr>
    </tbody>
</table>
<div class="pagination"><!--start pagination-->
                  <div class="paginationPosition"> <span>Page 2 of 2 - 450 results</span> - Show
                    <select name="paginationShow" id="paginationShow">
                      <option>20</option>
                      <option>50</option>
                      <option>100</option>
                    </select>
                  </div>
                  <div class="paginationNavigation"> <a href="#" class="previousLink">Previous</a> <span><a href="#" class="paginationPageUrl">1</a></span> <span><a href="#" class="paginationPageUrl">2</a></span> <span><a href="#" class="paginationPageUrl">3</a></span> <span><a href="#" class="paginationPageUrl">4</a></span> <span><a href="#" class="paginationPageUrl">5</a></span> <span class="currentPage">6</span> <a href="#" class="nextLink">Next</a> </div>
               <div class="clear"></div>
</div><!--stop pagination-->
<div class="clear"></div>

                  </div>
			</div>
		</div>
</div><!--stop boxconverter -->

<div class='clear'></div></div><!--stop grid_16-->



	<div class='clear'></div>
</div>

</c:if>