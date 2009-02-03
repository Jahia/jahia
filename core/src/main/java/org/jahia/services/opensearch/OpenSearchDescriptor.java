/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.opensearch;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.templates.components.opensearch.client.model.Image;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.Namespace;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.OpenSearchDescriptorConstants;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.URLTemplate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 09:35:53
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchDescriptor implements OpenSearchDescriptorConstants {

    private static final org.jdom.Namespace OPEN_SEARCH_NS =
            org.jdom.Namespace.getNamespace(Namespace.OPEN_SEARCH_NS.getPrefix(),Namespace.OPEN_SEARCH_NS.getURI());

    private Map namespaces = new HashMap();
    private String docPath;
    private Element descriptor;
    private String shortName;
    private String description;
    private List images;
    private List urls;
    private org.jdom.Namespace defaultNamespace = org.jdom.Namespace.NO_NAMESPACE;

    public OpenSearchDescriptor(String docPath) throws Exception {
        this.docPath = docPath;
        namespaces.put(Namespace.OPEN_SEARCH_NS.getPrefix(),Namespace.OPEN_SEARCH_NS);
        loadDescriptor(null);
    }

    public OpenSearchDescriptor(InputStream inputStream) throws Exception {
        namespaces.put(Namespace.OPEN_SEARCH_NS.getPrefix(),Namespace.OPEN_SEARCH_NS);
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

    public List<Image> getImages(){
        return this.images;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public Map<String,Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map namespaces) {
        this.namespaces = namespaces;
    }

    private void loadImages(){
        this.images = new ArrayList();
        List imagesEl = this.descriptor.getChildren(IMAGE,defaultNamespace);
        Iterator it = imagesEl.iterator();
        Element el = null;
        Image image = null;
        String uri = null;
        String height = null;
        String width = null;
        String type = null;
        while (it.hasNext()){
            el = (Element)it.next();
            uri = getTextValue(el.getTextTrim(),"");
            type = getTextValue(el.getAttributeValue(Image.TYPE_ATTRIBUTE),"");
            height = getTextValue(el.getAttributeValue(Image.HEIGHT_ATTRIBUTE),"");
            width = getTextValue(el.getAttributeValue(Image.WIDTH_ATTRIBUTE),"");
            image = new Image(uri, type, width, height);
            this.images.add(image);
        }
    }

    private void loadURLs(){
        this.urls = new ArrayList();
        List imagesEl = this.descriptor.getChildren(URL,defaultNamespace);
        Iterator it = imagesEl.iterator();
        Element el = null;
        String template = null;
        String type = null;
        String indexOffset = null;
        String pageOffset = null;
        int indexOffsetInt = 0;
        int pageOffsetInt = 0;
        URLTemplate URLTmpl = null;
        URL url = null;
        List parameters = null;
        while (it.hasNext()){
            el = (Element)it.next();
            addAdditionalNamespaces(el.getAdditionalNamespaces());
            /*
            template = getTextValue(el.getAttributeValue(org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL.TEMPLATE_ATTRIBUTE,defaultNamespace),
                    "");
            */
            template = "";
            Element templateEl = el.getChild(TEMPLATE,defaultNamespace);
            if (templateEl != null){
                template = templateEl.getText();
            }
            type = getTextValue(el.getAttributeValue(org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL.TYPE_ATTRIBUE,defaultNamespace),"");
            indexOffset = getTextValue(el.getAttributeValue(org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL.INDEX_OFFSET_ATTRIBUTE,defaultNamespace),"");
            try {
                indexOffsetInt = Integer.parseInt(indexOffset);
            } catch ( Throwable t ){
            }
            pageOffset = getTextValue(el.getAttributeValue(org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL.PAGE_OFFSET_ATTRIBUTE,defaultNamespace),"");
            try {
                pageOffsetInt = Integer.parseInt(pageOffset);
            } catch ( Throwable t ){
            }
            type = getTextValue(el.getAttributeValue(org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL.TYPE_ATTRIBUE),"");
            URLTmpl = new URLTemplate(template,namespaces);
            // @todo parse additional parameters
            parameters = new ArrayList();
            url = new URL(URLTmpl,type,indexOffsetInt,pageOffsetInt,parameters);
            this.urls.add(url);
        }
    }

    /**
     *
     * @param additionalNamespaces
     */
    private void addAdditionalNamespaces(List additionalNamespaces){
        Iterator it = additionalNamespaces.iterator();
        org.jdom.Namespace jdNS = null;
        Namespace namespace = null;
        while (it.hasNext()){
            jdNS = (org.jdom.Namespace)it.next();
            namespace = new Namespace(jdNS.getPrefix(),jdNS.getURI());
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