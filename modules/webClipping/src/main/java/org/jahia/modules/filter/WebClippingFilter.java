package org.jahia.modules.filter;


import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.jahia.modules.Rewriter.WebClippingRewriter;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import ucar.nc2.util.net.EasySSLProtocolSocketFactory;

import javax.servlet.ServletException;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 24 déc. 2010
 * Time: 16:10:23
 * To change this template use File | Settings | File Templates.
 */
public class WebClippingFilter extends AbstractFilter {
    static private final Logger log = Logger.getLogger(WebClippingFilter.class);
    private final String MAP_SITE_URL_PARAMS = "siteURLParameters";
    // private EhCacheProvider cacheProviders;
    // private boolean cacheable;
    // private Cache urlsCache;

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (!renderContext.isEditMode()) {
            String url;
            if (renderContext.getRequest().getParameter("jahia_url_web_clipping") != null && renderContext.getRequest().getParameter("jahia_url_web_clipping").length() > 0) {
                //todo encode this url, for users can't tape directly url.
                url = renderContext.getRequest().getParameter("jahia_url_web_clipping");
            } else {
                url = resource.getNode().getPropertyAsString("url");
            }
            String original_method = getOriginalMethod(renderContext);
            Map map = new HashMap();
            map.put("URL_PARAMS", renderContext.getRequest().getParameterMap());
            return doGetOrPost(url, renderContext, resource, chain, original_method, map);
            /*/cache content, the response is cache if properties of the currentNode have change since the last version in cache
            //every url are cache and they have a timeToLive in the cache equal to the property "cacheDelay".
            final Element element = urlsCache.get(url + resource.getNode().getIdentifier());
            final Element elementDate = urlsCache.get("lastModificationDate" + resource.getNode().getIdentifier());
            String propertieLastModified = null;
            if (elementDate != null) {
                propertieLastModified = elementDate.getObjectValue().toString();
            }
            if (element != null && element.getValue() != null && propertieLastModified.equals(resource.getNode().getPropertyAsString("jcr:lastModified"))) {
                //the content is already in cache, then return it
                return element.getObjectValue().toString();
            } else {
                //get response content and cache it
                // this.cacheable = true;
                */
                /*   String response = (url, renderContext, resource, chain);
                  if (Integer.valueOf(resource.getNode().getPropertyAsString("cacheDelay")) != 0 && cacheable) {
                      Element elementToPut = new Element(url + resource.getNode().getIdentifier(), response);
                      //cache lastmodified date of the node, for know if properties have changes.
                      Element propertieToPut = new Element("lastModificationDate" + resource.getNode().getIdentifier(), resource.getNode().getPropertyAsString("jcr:lastModified"));
                      elementToPut.setTimeToLive(Integer.valueOf(resource.getNode().getPropertyAsString("cacheDelay")));
                      propertieToPut.setTimeToLive(Integer.valueOf(resource.getNode().getPropertyAsString("cacheDelay")));
                      urlsCache.put(elementToPut);
                      urlsCache.put(propertieToPut);
                  }
                  return response;
              }  */
        } else {
            return "WebClip module is only available in live";
        }
    }

    private String rewriteBody(String body, String url, String charset, Resource resource, RenderContext context) throws IOException {
        OutputDocument document;
        document = new WebClippingRewriter(url).rewriteBody(body, resource, context);
        return document.toString();
    }

    private String getOriginalMethod(RenderContext renderRequest) {
        String original_method = renderRequest.getRequest().getParameter("original_method");
        if (original_method != null) {
            if (!"GET".equalsIgnoreCase(original_method)) {
                original_method = "POST";
            } else {
                original_method = "GET";
            }
        } else {
            original_method = "GET";
        }
        return original_method;
    }

    protected String doGetOrPost(String urlToClip, RenderContext renderContext, Resource resource, RenderChain chain, String original_method, Map map) throws ServletException, IOException {
        try {
            // get the content of the url and rewrite it
            if (original_method.equals("POST")) {
                return getURLContentWithPostMethod(urlToClip, renderContext, resource, chain, map);
            } else {
                return getURLContentWithGetMethod(urlToClip, renderContext, resource, chain, map);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String getURLContentWithGetMethod(String urlToClip, RenderContext renderContext, Resource resource, RenderChain chain, Map map) throws IOException {
        String path = urlToClip;
        Map parameters = (Map) map.get("URL_PARAMS");
        // Get the httpClient
        HttpClient httpClient = new HttpClient();
        Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
        httpClient.getParams().setContentCharset("UTF-8");
        //
        // Add parameters
        if (parameters != null) {
            StringBuffer params = new StringBuffer(4096);
            Iterator iterator = parameters.entrySet().iterator();
            int index = 0;
            String characterEncoding = httpClient.getParams().getContentCharset();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (!entry.getKey().toString().equals("original_method") && !entry.getKey().toString().equals("jahia_url_web_clipping")) {
                    // Is not a jahia params so pass it to the url
                    if (!parameters.isEmpty()) {
                        final Object value = entry.getValue();
                        if (value instanceof String[]) {
                            String[] strings = (String[]) value;
                            StringBuffer buffer = new StringBuffer(4096);
                            for (int i = 0; i < strings.length; i++) {
                                String string = strings[i];
                                buffer.append((i != 0) ? "," : "").append(string);
                            }
                            params.append(index == 0 ? "?" : "&").append(entry.getKey().toString()).append("=").append(URLEncoder.encode(buffer.toString(), characterEncoding));
                            index++;
                        } else {
                            params.append(index == 0 ? "?" : "&").append(entry.getKey().toString()).append("=").append(URLEncoder.encode(value.toString(), characterEncoding));
                            index++;
                        }
                    }
                }
            }
            path = path + params.toString();
        }
        // Rebuild Path by encoding the path
        URL targetURL = new URL(path);
        String[] pathInfo = targetURL.getPath().split("/");
        StringBuffer pathBuffer;
        if (pathInfo.length > 0) {
            pathBuffer = new StringBuffer(URLEncoder.encode(pathInfo[0], "UTF-8"));
            for (int i = 1; i < pathInfo.length; i++) {
                String s = pathInfo[i];
                String[] s2 = s.split(";");
                pathBuffer.append("/").append(URLEncoder.encode(s2[0], "UTF-8"));
                if (s2.length > 1) { // there is a jsessionid so let's add it again without encoding
                    pathBuffer.append(";").append(s2[1]);
                }
            }
        } else {
            pathBuffer = new StringBuffer("");
        }
        path = targetURL.getProtocol() + "://" + targetURL.getHost() + (targetURL.getPort() == -1 ? "" : ":" + targetURL.getPort()) + pathBuffer.toString() + (targetURL.getQuery() != null ? "?" + targetURL.getQuery() : "");
        // Create a get method for accessing the url.
        HttpMethodBase httpMethod = new GetMethod(path);
        // Set a default retry handler (see httpclient doc).
        httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        String contentCharset = httpClient.getParams().getContentCharset();
        // Get the response of the url in a string.
        httpClient.getParams().setContentCharset(contentCharset);
        return getResponse(path, renderContext, resource, chain, httpMethod, httpClient);
    }

    private String getURLContentWithPostMethod(String urlToClip, RenderContext renderContext, Resource resource, RenderChain chain, Map map) {
        String path = urlToClip;
        Map parameters = (Map) map.get("URL_PARAMS");
        // Get the httpClient
        HttpClient httpClient = new HttpClient();
        Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
        httpClient.getParams().setContentCharset("UTF-8");
        // Create a post method for accessing the url.
        PostMethod postMethod = new PostMethod(path);
        // Set a default retry handler (see httpclient doc).
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        if (parameters != null) {
            Iterator iterator = parameters.entrySet().iterator();
            StringBuffer buffer = new StringBuffer(4096);
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (!entry.getKey().toString().equals("original_method") && !entry.getKey().toString().equals("jahia_url_web_clipping")) {
                    final Object value = entry.getValue();
                    if (value instanceof String[]) {
                        buffer.setLength(0);
                        String[] strings = (String[]) entry.getValue();
                        for (int i = 0; i < strings.length; i++) {
                            String string = strings[i];
                            buffer.append((i != 0) ? "," : "").append(string);
                        }
                        postMethod.addParameter(entry.getKey().toString(), buffer.toString());
                    } else {
                        postMethod.addParameter(entry.getKey().toString(), value.toString());
                    }
                }
            }
        }
        String contentCharset = httpClient.getParams().getContentCharset();
        httpClient.getParams().setContentCharset(contentCharset);
        return getResponse(path, renderContext, resource, chain, postMethod, httpClient);
    }

    private String getResponse(String urlToClip, RenderContext renderContext, Resource resource, RenderChain chain, HttpMethodBase httpMethod, HttpClient httpClient) {
        try {
            httpMethod.getParams().setParameter("http.connection.timeout", resource.getNode().getPropertyAsString("connectionTimeout"));
            httpMethod.getParams().setParameter("http.protocol.expect-continue", Boolean.valueOf(resource.getNode().getPropertyAsString("expectContinue")));
            httpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);

            int statusCode = httpClient.executeMethod(httpMethod);

            if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                    || statusCode == HttpStatus.SC_SEE_OTHER || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
                if (log.isDebugEnabled()) {
                    log.debug("We follow a redirection ");
                }
                String redirectLocation;
                Header locationHeader = httpMethod.getResponseHeader("location");
                if (locationHeader != null) {
                    redirectLocation = locationHeader.getValue();
                    if (!redirectLocation.startsWith("http")) {
                        URL siteURL = new URL(urlToClip);
                        String tmpURL = siteURL.getProtocol() + "://" + siteURL.getHost() + ((siteURL.getPort() > 0) ? ":" + siteURL.getPort() : "") + "/" + redirectLocation;
                        httpMethod = new GetMethod(tmpURL);
                        // Set a default retry handler (see httpclient doc).
                        httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
                        httpMethod.getParams().setParameter("http.connection.timeout", resource.getNode().getPropertyAsString("connectionTimeout"));
                        httpMethod.getParams().setParameter("http.protocol.expect-continue", resource.getNode().getPropertyAsString("expectContinue"));
                        httpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
                    } else {
                        httpMethod = new GetMethod(redirectLocation);
                    }
                }
            }
            if (statusCode != HttpStatus.SC_OK) {
                //this.cacheable = false;
                StringBuffer buffer = new StringBuffer("<html>\n<body>");
                buffer.append('\n' + "Error getting ").append(urlToClip).append(" failed with error code ").append(statusCode);
                buffer.append("\n</body>\n</html>");
                return buffer.toString();
            }

            String[] type = httpMethod.getResponseHeader("Content-Type").getValue().split(";");
            String contentCharset = "UTF-8";
            if (type.length == 2) {
                contentCharset = type[1].split("=")[1];
            }
            InputStream inputStream = new BufferedInputStream(httpMethod.getResponseBodyAsStream());
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100 * 1024);
                byte[] buffer = new byte[100 * 1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();
                final byte[] responseBodyAsBytes = outputStream.toByteArray();
                String responseBody = new String(responseBodyAsBytes, "US-ASCII");
                Source source = new Source(responseBody);
                List list = source.findAllStartTags(Tag.META);
                for (Object aList : list) {
                    StartTag startTag = (StartTag) aList;
                    Attributes attributes = startTag.getAttributes();
                    final Attribute attribute = attributes.get("http-equiv");
                    if (attribute != null && attribute.getValue().equalsIgnoreCase("content-type")) {
                        type = attributes.get("content").getValue().split(";");
                        if (type.length == 2) {
                            contentCharset = type[1].split("=")[1];
                        }
                    }
                }
                final String s = contentCharset.toUpperCase();
                return rewriteBody(new String(responseBodyAsBytes, s), urlToClip, s, resource, renderContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //this.cacheable = false;
            StringBuffer buffer = new StringBuffer("<html>\n<body>");
            buffer.append('\n' + "Error getting ").append(urlToClip).append(" failed with error : ").append(e.toString());
            buffer.append("\n</body>\n</html>");
            return buffer.toString();
        }
        return null;
    }

    /* public void setCacheProviders(EhCacheProvider cacheProviders) {
      this.cacheProviders = cacheProviders;
  }

 public void afterPropertiesSet() throws Exception {
      /CacheManager cacheManager = cacheProviders.getCacheManager();
      if (!cacheManager.cacheExists("WebClipModuleCache")) {
          cacheManager.addCache("WebClipModuleCache");
      }
      urlsCache = cacheManager.getCache("WebClipModuleCache");
  }  */
}
