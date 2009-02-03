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

package org.jahia.blogs;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import org.apache.log4j.Logger;

/**
 * Simple tester class for testing the XML-RPC methods and the ping servlet.
 *
 * @author Xavier Lawrence
 */
public class BlogTester {
    
    public static final String SERVER = "http://localhost:8080/jahia/blogs";
    public static final String TB_PING_SERVLET = 
            "http://localhost:8080/jahia/trackbacks";
    
    static Logger log = Logger.getLogger(BlogTester.class);
    
    /** Creates a new instance of BlogTester */
    public BlogTester() {
    }
    
    /**
     *
     */
    private static Object execute(XmlRpcClient xmlrpc, String methodName,
            List params) throws XmlRpcException, IOException {
        return xmlrpc.execute(methodName, new Vector(params));
    }
    
    /**
     *
     */
    private static List toList(String[] params) {
        List result = new ArrayList(params.length);
        
        for (int i=0; i<params.length; i++) {
            result.add(params[i]);
        }    
        return result;
    }
    
    /**
     *
     */
    private static String sendPing(String postID) throws IOException {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(TB_PING_SERVLET);
        
        post.setRequestHeader("Content-type", 
                "application/x-www-form-urlencoded; charset=utf-8");
        
        post.addParameter("entryID", postID);
        post.addParameter("url", "http://test.com/12");
        post.addParameter("title", "Le titre");
        post.addParameter("excerpt", "Un court r�sum�");
        post.addParameter("blog_name", "Le nom du blog");
        
        int status = client.executeMethod(post);
        String body = post.getResponseBodyAsString();
        
        StringBuffer buff = new StringBuffer();
        buff.append("StatusCode: ");
        buff.append(status);
        buff.append("; ");
        buff.append(body);
        
        return buff.toString();
    }
    
    /**
     *
     */
    public static void main(String[] args) throws Exception {
        
        String postID = "27";
        
        if (args == null || args.length == 0) {
            log.info("Need to specify a postID to ping as argument");
            log.info("Using postID "+postID);
        } else {
            postID = args[0];
        }
        
        XmlRpcClient xmlrpc = new XmlRpcClient(SERVER);
        
        String[] params = {postID};
        String name = "mt.getTrackbackPings";
        
        log.info("Sending ping...");
        String result = sendPing(postID);
        log.info("Result of ping: "+result);

        log.info("Executing "+name);
        result = execute(xmlrpc, name, toList(params)).toString();
        log.info("Result of "+name+": "+result);
    }
}
