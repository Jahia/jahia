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
 package org.jahia.services.search;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 f�vr. 2005
 * Time: 18:19:31
 * To change this template use File | Settings | File Templates.
 */
public class CacheableDocumentProxy implements java.lang.reflect.InvocationHandler {

    private IndexableDocument obj;
    private Date date;
    private Date serverDate;

    public Date getServerDate() {
        return serverDate;
    }

    public void setServerDate(Date serverDate) {
        this.serverDate = serverDate;
    }

    public static Object newInstance(Object obj) {
    return java.lang.reflect.Proxy.newProxyInstance(
        obj.getClass().getClassLoader(),
        obj.getClass().getInterfaces(),
        new CacheableDocumentProxy(obj));
    }

    private CacheableDocumentProxy(Object obj) {
        this.obj = (IndexableDocument)obj;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
    throws Throwable
    {
        Object result;                  
        try {
            result = m.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " +
                           e.getMessage());
        } finally {
        }
        return result;
    }

    public IndexableDocument getDoc() {
        return (IndexableDocument)obj;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCacheKey(){
        StringBuffer buff = new StringBuffer();
        if ( this.obj != null ){
            buff.append(this.obj.getKeyFieldName());
            buff.append("_");
            buff.append(this.obj.getKey());
            buff.append("_");
            if ( obj instanceof RemovableDocument ) {
                buff.append("toremove");
            } else {
                buff.append("toadd");
            }
        }
        if ( this.date != null ){
            buff.append("_");
            buff.append(this.date.getTime());
        }
        if ( this.serverDate != null ){
            buff.append("_");
            buff.append(this.serverDate.getTime());
        }

        return buff.toString();
    }
}
