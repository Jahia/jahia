function replace(id, url) {
    var http = false;
    if(navigator.appName == "Microsoft Internet Explorer") {
        http = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        http = new XMLHttpRequest();
    }
    http.open("GET", url, true);
    http.onreadystatechange=function() {
        if(http.readyState == 4) {
            result = http.responseText;
            result = result.replace('<div id="' + id + '">', '<div id="replaced' + id + '">');
            document.getElementById(id).innerHTML = result;
        }
    }
    http.send(null);
}
