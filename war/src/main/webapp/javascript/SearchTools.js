

function SearchTools() {
}

SearchTools.showHitDetails = 
function (frameHtmlElementId,labelHtmlElementId,onMsg,offMsg){
  var htmlEl = document.getElementById(labelHtmlElementId);
  if ( htmlEl ){
    if ( !htmlEl.labelMsg ){
      htmlEl.labelMsg = "on";
    }
    if ( htmlEl.labelMsg == "on" ){
      htmlEl.innerHTML = offMsg;
      htmlEl.labelMsg = "off";
    } else {
      htmlEl.innerHTML = onMsg;
      htmlEl.labelMsg = "on";
    }
  }
  var frameHtmlEl = document.getElementById(frameHtmlElementId);
  if ( frameHtmlEl ){
    if ( htmlEl.labelMsg == "off" ){
      frameHtmlEl.style.display = "block";
    } else {
      frameHtmlEl.style.display = "none";
    }
  }
}

SearchTools.resetSearchRefine =
function (formName){
  var searchForm = document.forms[formName];
  searchForm.elements["searchRefineAttribute"].value = 'reset';
  searchForm.submit();
}
