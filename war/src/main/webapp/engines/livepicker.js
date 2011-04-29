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
    type = 'files';
}
$(document).ready(function() {
    var queryString = "nodeTypes=" +
      encodeURIComponent(type == 'pages' ? 'jnt:page' : (type == 'images' ? 'nt:folder,jmix:image,jnt:virtualsite' : 'nt:folder,nt:file,jnt:virtualsite')) +
      "&selectableNodeTypes=" + encodeURIComponent(type == 'pages' ? 'jnt:page' : (type == 'images' ? 'jmix:image' : 'nt:file'));
    queryString = queryString.length > 0 ? "?" + queryString : "";
    $("#imagepicker-treeItemSelectorTree").treeview($.extend({
		urlBase: base,
		urlExtension: ".tree.json" + queryString,
		urlStartWith: base + root + ".treeRootItem.json" + queryString,
		url: base + root + ".treeRootItem.json" + queryString,
		preview:type == 'images',
		previewPath:files,
		callback: function (uuid, path, title) {
		    window.opener.CKEDITOR.tools.callFunction(funcNum, files + path + (type == 'pages' ? '.html' : ''));
		    window.close();
		}
    }, {}));
});