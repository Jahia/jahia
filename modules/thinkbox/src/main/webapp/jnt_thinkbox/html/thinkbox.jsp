(CJN_SERVER) Remplacer http://localhost:8080 par la valeur dynamique
<br />
(CJN_USER_TB) Remplacer 'cms/render/default/en/sites/testnote/page.html' par la valeur dynamique du noeud du user
<br />
<br />
javascript:(function(){CJN_SERVER='http://localhost:8080';CJN_USER_TB='cms/render/default/en/sites/testnote/page.html';try{var%20x=document.createElement('SCRIPT');x.type='text/javascript';x.src=CJN_SERVER+'/javascript/jahiaclip.js?'+(new%20Date().getTime()/100000);document.getElementsByTagName('head')[0].appendChild(x);}catch(e){alert(e);}})();
