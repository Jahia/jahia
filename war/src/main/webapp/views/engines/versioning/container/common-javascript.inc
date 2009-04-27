<script type="text/javascript" src="<%=URL%>../javascript/zimbra/versioning/ContainerVersionCompare.js"></script>
<script language="javascript" type="text/javascript">
<!--

document.onkeydown = keyDown;
function keyDown() {
    if (document.all) {
        var ieKey = event.keyCode;
        if (ieKey == 13) { document.retryForm.submit(); }
        if (ieKey == 87 && event.ctrlKey) { window.close(); }
    }
}

function handleLanguageChange(lang) {
    document.mainForm.method.value = "POST";
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.engine_lang.value = lang;
    var engineView = '<%=engineView%>';
    var method = "";
    if ( engineView == "revisionsList" ){
      method = "showRevisionsList"; 
    } else if ( engineView == "confirmRestore" ){
      method = "showConfirmRestore"; 
    } else if ( engineView == "containersList" ){
      method = "showContainersList"; 
    }  
    sendForm(method);
}   


function sendFormSave(){
    window.close();
}

function sendFormClose(){
    sendForm('close', '');
}

function sendFormCancel(){
    sendForm('cancel', '');
}    

function sendForm(method, params) {
	if (typeof workInProgressOverlay != 'undefined') { workInProgressOverlay.launch(); }
    document.mainForm.method.value = method;
    document.mainForm.action = "<%=actionURL%>";
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.engineview.value = "<%=engineView%>";
    if (params != null && params != "") {
        params += "&method=" + method;
    } else {
        params = "&method=" + method;
    }

    if (params != null && params != "") {
        if (document.mainForm.action.indexOf("?") > 0) {
            if (params.charAt(0) == "&") {
                document.mainForm.action += params;
            } else {
                document.mainForm.action += "&" + params;
            }
        } else {
            if (params.charAt(0) == "&") {
                document.mainForm.action += "?" + params.substring(1);
            } else {
                document.mainForm.action += "?" + params;
            }
        }
    }
    flagSubmit();
    document.mainForm.submit();
}

function releaseLock(lockType, pid, refreshParentWindow) {
  return;
}

//-->
</script>