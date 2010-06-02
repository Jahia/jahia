function replace(id, url, callback) {
    var http = false;
    if(navigator.appName == "Microsoft Internet Explorer") {
        http = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        http = new XMLHttpRequest();
    }
    http.open("GET", url, true);
    http.onreadystatechange=function() {
        var result;
        if (http.readyState == 4) {
            result = http.responseText;
            $("#" + id).html(result);
            eval(callback);
        }
    };
    http.send(null);
}

function jreplace(id,url,params,callback) {
    $.get(url,params,function(data){
        $("#"+id).html(data);
        eval(callback);
    });
}