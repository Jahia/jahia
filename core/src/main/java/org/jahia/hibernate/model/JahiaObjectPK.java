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

 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.content.ObjectKey;
import org.jahia.content.ObjectKeyInterface;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 26 juil. 2005
 * Time: 16:48:12
 * To change this template use File | Settings | File Templates.
 */
public class JahiaObjectPK extends CachedPK implements Serializable,Cloneable, ObjectKeyInterface {

    private Integer id;
    private String type;

    public JahiaObjectPK(String type, Integer id){
        this.type = type;
        this.id = id;
    }

    public JahiaObjectPK(){
    }

    /**
     * @hibernate.property column="id_jahia_obj"
     * @return
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        updated();
        this.id = id;
    }

    /**
     * @hibernate.property column="type_jahia_obj" length="22"
     * @return
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        updated();
        this.type = type;
    }

    public String getKey() {
        if ( this.getType() == null || this.getId() == null ){
            return null;
        }
        StringBuffer buf = new StringBuffer(50);
        buf.append(this.getType());
        buf.append(KEY_SEPARATOR);
        buf.append(this.getIDInType());
        return buf.toString();
    }

    public String getIDInType(){
        return String.valueOf(this.getId());
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public org.jahia.content.ObjectKey toObjectKey()
            throws java.lang.ClassNotFoundException
    {
        String key = this.getKey();
        if ( key == null ){
            return null;
        }
        return ObjectKey.getInstance(this.getKey());
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("id="+getId())
                .append("type="+getType())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaObjectPK castOther = (JahiaObjectPK) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .append(this.getType(), castOther.getType())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .append(getType())
                .toHashCode();
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object <tt>x</tt>, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be <tt>true</tt>, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be <tt>true</tt>, this is not an absolute requirement.
     * <p/>
     * By convention, the returned object should be obtained by calling
     * <tt>super.clone</tt>.  If a class and all of its superclasses (except
     * <tt>Object</tt>) obey this convention, it will be the case that
     * <tt>x.clone().getClass() == x.getClass()</tt>.
     * <p/>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by <tt>super.clone</tt> before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by <tt>super.clone</tt>
     * need to be modified.
     * <p/>
     * The method <tt>clone</tt> for class <tt>Object</tt> performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface <tt>Cloneable</tt>, then a
     * <tt>CloneNotSupportedException</tt> is thrown. Note that all arrays
     * are considered to implement the interface <tt>Cloneable</tt>.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p/>
     * The class <tt>Object</tt> does not itself implement the interface
     * <tt>Cloneable</tt>, so calling the <tt>clone</tt> method on an object
     * whose class is <tt>Object</tt> will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     *
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the <code>Cloneable</code> interface. Subclasses
     *                                    that override the <code>clone</code> method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    protected Object clone() throws CloneNotSupportedException {
        JahiaObjectPK pk = new JahiaObjectPK(this.getType(),this.getId());
        return pk;
    }

}
