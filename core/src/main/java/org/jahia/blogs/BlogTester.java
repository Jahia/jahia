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
