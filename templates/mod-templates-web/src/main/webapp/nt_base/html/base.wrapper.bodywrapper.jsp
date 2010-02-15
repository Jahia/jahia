<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="960.css,01web.css,02mod.css" />


<div id="bodywrapper"><!--start bodywrapper-->
<div id="topheader"><!--start topheader-->

<div class="container container_16">

	<div class="grid_16">
		      <div id="headerPart1"><!--start headerPart1-->
                   <template:area path="header"/>
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
<div id="content"><!--start content-->
    <div class="container container_16">
         ${wrappedContent}
    <div class="clear"></div></div><!--stop content-->
<div class="clear"></div></div>
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
