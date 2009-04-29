/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.opensearch;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.opensearch.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 09:35:53
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchDescriptor implements GWTJahiaOpenSearchDescriptorConstants {

    private Map<String, GWTJahiaNamespace> namespaces = new HashMap<String, GWTJahiaNamespace>();
    private String docPath;
    private Element descriptor;
    private String shortName;
    private String description;
    private List<GWTJahiaImage> images;
    private List<GWTJahiaURL> urls;
    private org.jdom.Namespace defaultNamespace = org.jdom.Namespace.NO_NAMESPACE;

    public OpenSearchDescriptor(String docPath) throws Exception {
        this.docPath = docPath;
        namespaces.put(GWTJahiaNamespace.OPEN_SEARCH_NS.getPrefix(), GWTJahiaNamespace.OPEN_SEARCH_NS);
        loadDescriptor(null);
    }

    public OpenSearchDescriptor(InputStream inputStream) throws Exception {
        namespaces.put(GWTJahiaNamespace.OPEN_SEARCH_NS.getPrefix(), GWTJahiaNamespace.OPEN_SEARCH_NS);
        loadDescriptor(inputStream);
    }

    private void loadDescriptor(InputStream inputStream) throws Exception {
        SAXBuilder sxb = new SAXBuilder();
        Document doc = null;
        sxb.setValidation(false);
        if (inputStream == null){
            doc = sxb.build(new File(docPath));
        } else {
            doc = sxb.build(inputStream);
        }
        this.descriptor = doc.getRootElement();
        defaultNamespace = this.descriptor.getNamespace();
        this.shortName = getTextValue(this.descriptor.getChildText(SHORT_NAME,defaultNamespace),"no name");
        this.description = getTextValue(this.descriptor.getChildText(DESCRIPTION,defaultNamespace),"");
        loadImages();
        loadURLs();
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public Element getDescriptor() {
        return descriptor;
    }

    public String getShortName(){
        return this.shortName;
    }

    public String getDescription(){
        return this.description;
    }

    public List<GWTJahiaImage> getImages(){
        return this.images;
    }

    public List<GWTJahiaURL> getUrls() {
        return urls;
    }

    public Map<String, GWTJahiaNamespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, GWTJahiaNamespace> namespaces) {
        this.namespaces = namespaces;
    }

    private void loadImages(){
        this.images = new ArrayList<GWTJahiaImage>();
        List<?> imagesEl = this.descriptor.getChildren(IMAGE,defaultNamespace);
        for (Iterator<?> it = imagesEl.iterator(); it.hasNext(); ){
            Element el = (Element)it.next();
            String uri = getTextValue(el.getTextTrim(),"");
            String type = getTextValue(el.getAttributeValue(GWTJahiaImage.TYPE_ATTRIBUTE),"");
            String height = getTextValue(el.getAttributeValue(GWTJahiaImage.HEIGHT_ATTRIBUTE),"");
            String width = getTextValue(el.getAttributeValue(GWTJahiaImage.WIDTH_ATTRIBUTE),"");
            GWTJahiaImage image = new GWTJahiaImage(uri, type, width, height);
            this.images.add(image);
        }
    }

    private void loadURLs(){
        this.urls = new ArrayList<GWTJahiaURL>();
        List<?> imagesEl = this.descriptor.getChildren(URL,defaultNamespace);
        int indexOffsetInt = 0;
        int pageOffsetInt = 0;
        for (Iterator<?> it = imagesEl.iterator(); it.hasNext();){
            Element el = (Element)it.next();
            addAdditionalNamespaces(el.getAdditionalNamespaces());
            /*
            template = getTextValue(el.getAttributeValue(org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL.TEMPLATE_ATTRIBUTE,defaultNamespace),
                    "");
            */
            String template = "";
            Element templateEl = el.getChild(TEMPLATE,defaultNamespace);
            if (templateEl != null){
                template = templateEl.getText();
            }
            String type = getTextValue(el.getAttributeValue(GWTJahiaURL.TYPE_ATTRIBUE,defaultNamespace),"");
            String indexOffset = getTextValue(el.getAttributeValue(GWTJahiaURL.INDEX_OFFSET_ATTRIBUTE,defaultNamespace),"");
            try {
                indexOffsetInt = Integer.parseInt(indexOffset);
            } catch ( Throwable t ){
            }
            String pageOffset = getTextValue(el.getAttributeValue(GWTJahiaURL.PAGE_OFFSET_ATTRIBUTE,defaultNamespace),"");
            try {
                pageOffsetInt = Integer.parseInt(pageOffset);
            } catch ( Throwable t ){
            }
            type = getTextValue(el.getAttributeValue(GWTJahiaURL.TYPE_ATTRIBUE),"");
            GWTJahiaURLTemplate URLTmpl = new GWTJahiaURLTemplate(template,namespaces);
            // @todo parse additional parameters
            List<GWTJahiaParameter> parameters = new ArrayList<GWTJahiaParameter>();
            GWTJahiaURL url = new GWTJahiaURL(URLTmpl,type,indexOffsetInt,pageOffsetInt,parameters);
            this.urls.add(url);
        }
    }

    /**
     *
     * @param additionalNamespaces
     */
    private void addAdditionalNamespaces(List<Namespace> additionalNamespaces){
        for (Namespace jdNS : additionalNamespaces){
            GWTJahiaNamespace namespace = new GWTJahiaNamespace(jdNS.getPrefix(),jdNS.getURI());
            namespaces.put(namespace.getPrefix(),namespace);
        }
    }

    private String getTextValue(String value, String defaultValue){
        String result = value;
        if ( result == null ){
            return defaultValue;
        }
        return result;
    }
}