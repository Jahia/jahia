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
 package org.jahia.hibernate.model;

import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.timebasedpublishing.RangeRetentionRule;
import org.jahia.exceptions.JahiaException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 12:53:53
 * To change this template use File | Settings | File Templates.
 * @hibernate.joined-subclass name="RangeRetentionRule" table="jahia_retrule_range" extends="org.jahia.hibernate.model.JahiaRetentionRule"
 * @hibernate.joined-subclass-key column="id_retrule_range"
 */
public class JahiaRangeRetentionRule extends JahiaRetentionRule {

    private Long validFromDate = new Long(0);
    
    private Long validToDate = new Long(0);

    private Boolean notifiedValidFromDate = Boolean.FALSE;

    private Boolean notifiedValidToDate = Boolean.FALSE;

    public JahiaRangeRetentionRule(){
        super();
    }

    /**
     * @hibernate.property column="validfrom_retrule_range"
     * @return
     */
    public Long getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(Long validFromDate) {
        this.validFromDate = validFromDate;
    }
    
    /**
     * @hibernate.property column="validto_retrule_range"
     * @return
     */
    public Long getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(Long validToDate) {
        this.validToDate = validToDate;
    }

    /**
     * @hibernate.property column="notiffromd_retrule_range"
     * @return
     */
    public Boolean getNotifiedValidFromDate() {
        return notifiedValidFromDate;
    }

    public void setNotifiedValidFromDate(Boolean notifiedValidFromDate) {
        this.notifiedValidFromDate = notifiedValidFromDate;
    }

    /**
     * @hibernate.property column="notiftod_retrule_range"
     * @return
     */
    public Boolean getNotifiedValidToDate() {
        return notifiedValidToDate;
    }

    public void setNotifiedValidToDate(Boolean notifiedValidToDate) {
        this.notifiedValidToDate = notifiedValidToDate;
    }

    public RetentionRule getRetentionRule() throws ClassNotFoundException, JahiaException {
        RangeRetentionRule rule = new RangeRetentionRule();
        rule.setEnabled(this.getEnabled());
        rule.setId(this.getId());
        rule.setValidFromDate(this.getValidFromDate());
        rule.setValidToDate(this.getValidToDate());
        rule.setInherited(this.getInherited());
        rule.setComment(this.getComment());
        rule.setTitle(this.getTitle());
        rule.setShared(this.getInherited());
        rule.setNotifiedValidFromDate(this.getNotifiedValidFromDate());
        rule.setNotifiedValidToDate(this.getNotifiedValidToDate());
        rule.setRetentionRuleDef(this.getRetentionRuleDef().toRetentionRuleDef());
        rule.loadSettings(this.getSettings());
        return rule;
    }

    public String toString(){
        return new StringBuffer(getClass().getName())
                .append("id="+getId())
                .toString();
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
    public Object clone() throws CloneNotSupportedException{
        JahiaRangeRetentionRule rule = new JahiaRangeRetentionRule();
        rule.setEnabled(this.getEnabled());
        rule.setId(this.getId());
        rule.setRetentionRuleDef(this.getRetentionRuleDef());
        rule.setValidFromDate(this.getValidFromDate());
        rule.setValidToDate(this.getValidToDate());
        rule.setComment(this.getComment());
        rule.setTitle(this.getTitle());
        rule.setShared(this.getShared());
        rule.setNotifiedValidFromDate(this.getNotifiedValidFromDate());
        rule.setNotifiedValidToDate(this.getNotifiedValidToDate());
        return rule;
    }
}
