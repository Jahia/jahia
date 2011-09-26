function getUrlParam(paramName) {
    var reParam = new RegExp('(?:[\?&]|&amp;)' + paramName + '=([^&]+)', 'i');
    var match = window.location.search.match(reParam);
    return decodeURIComponent((match && match.length > 1) ? match[1] : '');
}

var funcNum = getUrlParam('CKEditorFuncNum');
var base = getUrlParam('base');
var files = getUrlParam('files');
var root = getUrlParam('root');
var type = getUrlParam('type');
if (type == '') {
    type = 'content';
}
var displayablenodeonly = getUrlParam('displayablenodeonly');

$(document).ready(function() {
    var queryString = "nodeTypes=" +
      encodeURIComponent(type == 'pages' ? 'jnt:page' : (type == 'images' ? 'nt:folder,jmix:image,jnt:virtualsite' : (type == 'files' ? 'nt:folder,nt:file,jnt:virtualsite' : 'jnt:content,jnt:page,jnt:virtualsite'))) +
      "&selectableNodeTypes=" + encodeURIComponent(type == 'pages' ? 'jnt:page' : (type == 'images' ? 'jmix:image' : (type == 'files' ? 'nt:file' : 'jnt:content,jnt:page')));
    if(displayablenodeonly!=''){
        queryString = queryString+"&displayablenodeonly="+displayablenodeonly;
    }
    queryString = queryString.length > 0 ? "?" + queryString : "";
    $("#imagepicker-treeItemSelectorTree").treeview($.extend({
		urlBase: base,
		urlExtension: ".tree.json" + queryString,
		urlStartWith: base + root + ".treeRootItem.json" + queryString,
		url: base + root + ".treeRootItem.json" + queryString,
		preview:type == 'images',
		previewPath:files,
		callback: function (uuid, path, title) {
		    window.opener.CKEDITOR.tools.callFunction(funcNum, files + path + (type == 'pages' || displayablenodeonly == 'true' ? '.html' : ''));
		    window.close();
		}
    }, {}));
});