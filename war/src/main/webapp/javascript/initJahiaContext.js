var contextJsParameters;
var CKEDITOR_BASEPATH;
var scayt_custom_params;
var jASAJ=jASAJ || new Array();

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
    window.addEventListener('DOMContentLoaded', function() {
        var aggregated = window.document.getElementById('jahia-data-aggregatedjs');
        if (aggregated) {
            var aggregatedVal = JSON.parse(aggregated.textContent);
            aggregatedVal.scripts.forEach(function(js) { jASAJ.push(js) })
        }
    });
})();
