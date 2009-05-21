/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.query.dasl;

import org.apache.webdav.lib.search.BasicSearchBuilder;
import org.apache.webdav.lib.search.SearchException;
import org.apache.webdav.lib.search.SearchRequest;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 16:03:25
 * To change this template use File | Settings | File Templates.
 */
public class DASLSearchBuilder extends BasicSearchBuilder {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (DASLSearchBuilder.class);

    private static final String BASIC_SEARCH = "basicsearch";
    private static final String ORDER_BY = "orderby";
    private static final String ORDER = "order";
    private static final String PROP = "prop";
    private static final String SCORE = "score";
    private static final String DESCENDING = "descending";

    protected List<Namespace> namespaces = new ArrayList<Namespace>();

    public DASLSearchBuilder(){
        super();
    }

    public String build(SearchRequest search, Map variables, List scopes)
        throws SearchException
    {
        String query = super.build(search, variables, scopes);
        try
        {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(query));
            Element root = document.getRootElement();
            if (root != null)
            {
                Iterator<Namespace> iterator = this.namespaces.iterator();
                Namespace ns = null;
                while ( iterator.hasNext() ){
                    ns = iterator.next();
                    root.addNamespace(ns.getPrefix(),ns.getURI());
                }
                addSort(root.element((QName.get (BASIC_SEARCH,Constants.DAV_PREFIX,Constants.DAV))));
            }
            StringWriter out = new StringWriter(1024);
            OutputFormat outputFormat = OutputFormat.createCompactFormat();
            outputFormat.setPadText(false);
            outputFormat.setTrimText(false);
            XMLWriter writer = new XMLWriter(outputFormat);
            writer.setWriter(out);
            writer.write(document);
            query = out.toString();
        }
        catch (Exception t){
            logger.debug("Error building query",t);
            throw new SearchException("Error building query");
        }
        return query;
    }

    protected void addSort(Element el){
        if ( el == null ){
            return;
        }
        Element orderByEl = el.addElement((QName.get (ORDER_BY,Constants.DAV_PREFIX,Constants.DAV)));
        Element orderEl = orderByEl.addElement((QName.get (ORDER,Constants.DAV_PREFIX,Constants.DAV)));
        Element propEl = orderEl.addElement((QName.get (PROP,Constants.DAV_PREFIX,Constants.DAV)));
        propEl.addElement((QName.get (SCORE,Constants.DAV_PREFIX,Constants.DAV)));
        orderEl.addElement((QName.get (DESCENDING,Constants.DAV_PREFIX,Constants.DAV)));
    }

    public void addNamespace(Namespace ns){
        if ( ns == null ){
            return;
        }
        this.namespaces.add(ns);
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

}
