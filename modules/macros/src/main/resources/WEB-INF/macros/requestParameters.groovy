if (renderContext.getRequest().getQueryString() != null) {
print "?" + URLEncoder.encode(renderContext.getRequest().getQueryString(), "UTF-8")
}