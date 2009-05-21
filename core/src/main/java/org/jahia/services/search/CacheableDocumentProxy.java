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
