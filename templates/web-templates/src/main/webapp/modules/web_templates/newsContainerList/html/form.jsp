<form action="${url.base}${currentNode.path}/*" method="post">
    <input type="hidden" name="nodeType" value="web_templates:newsContainer"/>
    <input type="text" name="newsTitle" value="News Title"/><br/>
    <textarea rows="10" cols="80" name="newsDesc">News content</textarea><br/>
    <input type="submit"/>
</form>