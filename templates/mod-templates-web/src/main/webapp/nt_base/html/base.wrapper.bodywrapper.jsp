<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="960.css,01web.css" />


<div id="bodywrapper"><!--start bodywrapper-->
<div id="topheader"><!--start topheader-->

<div class="container container_16">

	<div class="grid_16">
		      <div id="headerPart1"><!--start headerPart1-->
				<div id="languages"><!--start languages-->
					<ul>
                        <li><a href="#">FR</a></li>
                        <li><a href="#">UK</a></li>
                        <li> <a href="#">DE</a></li>
					</ul>
				</div><!--stop languages-->
				<div id="topshortcuts"><!--start topshortcuts-->
                    <ul>
                    <li class="topshortcuts-login"><a href="javascript:;" onclick="ShowHideLayer(0);"><span>Login</span></a></li>
                    <li class="topshortcuts-mysettings"><a href="#"><span>My settings</span></a></li>
                    <li class="topshortcuts-print"><a href="#" onclick="javascript:window.print()"><span>Imprimer cette page</span></a></li>
                    <li class="topshortcuts-typoincrease"><a href="javascript:ts('body',1)"><span>Agrandir</span></a></li>
                    <li class="topshortcuts-typoreduce"><a  href="javascript:ts('body',-1)"><span>Réduire</span></a></li>
                    <li class="topshortcuts-home"><a href="index.html"><span>Home</span></a></li>
                    <li class="topshortcuts-contact"><a href="contact.html"><span>Contact</span></a></li>
                    <li class="topshortcuts-sitemap"><a href="sitemap.html"><span>Sitemap</span></a></li>
                    </ul>
				</div><!--stop topshortcuts-->
                   <div class="clear"></div><div id="box0" class="collapsible"><!--start collapsible loginFormTop-->
                	<div class="boxloginFormTop"><!--start box 4 default-->
								<div class="boxloginFormTop-topright"></div>
                                <div class="boxloginFormTop-topleft"></div>
                                <div class="boxloginFormTop-header">
                                 <div id="loginFormTop"><!--start loginFormTop-->
							<form action="" method="post">
							<p><label class="hide">Login: </label>
							<input class="text" type="text" name="userLogin" value="Login" tabindex="1"/>
                            <label class="hide">Password: </label>
                            <input class="text" type="password" name="userPwd"  value="Password" tabindex="2"/>
                            <input class="gobutton png" type="image" src="img/loginformtop-button.png" tabindex="3"/></p>
                            <p class="loginFormTopCheckbox"><input type="checkbox" name="remember" id="remember" class="loginFormTopInputCheckbox" value="checked" tabindex="1" />
                    <label class="loginFormTopRememberLabel" for="remember" >Remember me</label></p>
					</form>
						</div><!--stop loginFormTop-->
                        </div>
                                <div class="box4-bottomright"></div>
                                <div class="box4-bottomleft"></div>
                            <div class="clear"></div></div>
                              <div class="clear"></div>
							  <!--stop box 4 default-->



                </div><!--stop collapsible loginFormTop-->
				<div class="clear"></div>
			</div>

	</div>
</div>

<div class="clear"></div></div><!--stop topheader-->
<div id="page"><!--start page-->
<div id="bottomheader"><!--start bottomheader-->

<div class="container container_16">


    	<h1 class="hide">Nom du site</h1>
        <div class="logotop"><template:area path="logo"/></div>

</div>
<div class="container container_16">
    <div id="navigationN1"><!--start navigationN1-->
        <template:area path="topMenu"/><!--Include MENU-->
    </div><!--stop navigationN1-->
</div>
<div class="clear"></div></div>
<!--stop bottomheader-->
<div class="container container_16"></div>

<div id="content"><!--start content-->

     ${wrappedContent}

</div><!--stop content-->
<div id="footer"><!--start footer-->
<div id="footerPart3"><!--start footerPart3-->
<div class="container container_16">
<div class='grid_2'><!--start grid_2-->
    <template:area path="logoFooter"/>
</div><!--stop grid_2-->
<div class='grid_14'><!--start grid_14-->
<template:area path="footer"/>
      </div><!--stop grid_12-->


	<div class='clear'></div>
</div>

<div class="clear"></div></div>
<!--stop footerPart3-->
<div class="clear"></div></div><!--stop footer-->

<div class="clear"></div></div><!--stop page-->

<div class="clear"></div></div><!--stop bodywrapper-->
