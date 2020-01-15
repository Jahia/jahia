var contextJsParameters;
var CKEDITOR_BASEPATH;
var scayt_custom_params;
(function() {
    var ctx = window.document.getElementById('jahia-data-ctx');
    if (ctx) {
        contextJsParameters=JSON.parse(ctx.textContent);
    }
    var ck = window.document.getElementById('jahia-data-ck');
    if (ck) {
        var ckVal = JSON.parse(ck.textContent);
        CKEDITOR_BASEPATH=ckVal.path;
        scayt_custom_params=new Array();
        if (ckVal.lng) {
            scayt_custom_params['sLang']=ckVal.lng;
        }
    }
})();
