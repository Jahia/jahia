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
            document.getElementById(id).innerHTML = http.responseText;
        }
    }
    http.send(null);
}
