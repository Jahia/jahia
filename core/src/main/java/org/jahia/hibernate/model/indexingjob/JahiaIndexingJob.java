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
 package org.jahia.hibernate.model.indexingjob;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.services.search.IndexableDocument;
import org.jahia.services.search.RemovableDocument;
import org.jahia.services.usermanager.JahiaUser;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 12:53:53
 * To change this template use File | Settings | File Templates.
 *
 * @hibernate.class table="jahia_indexingjobs"
 * @hibernate.discriminator column="classname_indexingjob"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public abstract class JahiaIndexingJob implements Serializable, Cloneable, Comparable {

    public static final String EXCLUSIVE_INDEXING_SERVER_ALL = "all";
    private String id = null;
    private Long date = new Long(0);
    private String className;
    private Integer ruleId = new Integer(-1);
    private Boolean indexImmediately = Boolean.TRUE;
    private Integer scheduledFromTime1 = new Integer(-1);
    private Integer scheduledToTime1 = new Integer(-1);
    private Integer scheduledFromTime2 = new Integer(-1);
    private Integer scheduledToTime2 = new Integer(-1);
    private Integer scheduledFromTime3 = new Integer(-1);
    private Integer scheduledToTime3 = new Integer(-1);
    private String enabledIndexingServers = EXCLUSIVE_INDEXING_SERVER_ALL;
    private Set<JahiaIndexJobServer> processedServers = new HashSet<JahiaIndexJobServer>();

    public JahiaIndexingJob(){
    }

    /**
     * @hibernate.id generator-class="uuid.hex"
     * type="java.lang.String"
     * length="50"
     * column="id_indexingjob"
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * This is the creation time
     *
     * @hibernate.property column="date_indexingjob"
     * @return
     */
    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    /**
     * @hibernate.property column="classname_indexingjob"
     * type="java.lang.String"
     * length="100"
     * insert="false"
     * update="false"
     * @return
     */
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @hibernate.property column="indeximmdty_indexingjob"
     * type="java.lang.Boolean"
     * @return
     */
    public Boolean getIndexImmediately() {
        return indexImmediately;
    }

    public void setIndexImmediately(Boolean indexImmediately) {
        this.indexImmediately = indexImmediately;
    }

    /**
     * @hibernate.property column="ruleId_indexingjob"
     * type="java.lang.Integer"
     * @return
     */
    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * If the indexation for an indexing job is delayed,
     * then this method returns the lower time value of the first
     * allowed indexation time range
     *
     * @hibernate.property column="fromtime1_indexingjob"
     * @return
     */
    public Integer getScheduledFromTime1() {
        return scheduledFromTime1;
    }

    public void setScheduledFromTime1(Integer scheduledFromTime1) {
        this.scheduledFromTime1 = scheduledFromTime1;
    }

    /**
     * If the indexation for an indexing job is delayed,
     * then this method returns the upper time value of the first
     * allowed indexation time range
     *
     * @hibernate.property column="totime1_indexingjob"
     * @return
     */
    public Integer getScheduledToTime1() {
        return scheduledToTime1;
    }

    public void setScheduledToTime1(Integer scheduledToTime1) {
        this.scheduledToTime1 = scheduledToTime1;
    }

    /**
     * If the indexation for an indexing job is delayed,
     * then this method returns the lower time value of the second
     * allowed indexation time range
     *
     * @hibernate.property column="fromtime2_indexingjob"
     * @return
     */
    public Integer getScheduledFromTime2() {
        return scheduledFromTime2;
    }

    public void setScheduledFromTime2(Integer scheduledFromTime2) {
        this.scheduledFromTime2 = scheduledFromTime2;
    }

    /**
     * If the indexation for an indexing job is delayed,
     * then this method returns the upper time value of the second
     * allowed indexation time range
     *
     * @hibernate.property column="totime2_indexingjob"
     * @return
     */
    public Integer getScheduledToTime2() {
        return scheduledToTime2;
    }

    public void setScheduledToTime2(Integer scheduledToTime2) {
        this.scheduledToTime2 = scheduledToTime2;
    }

    /**
     * If the indexation for an indexing job is delayed,
     * then this method returns the lower time value of the third
     * allowed indexation time range
     *
     * @hibernate.property column="fromtime3_indexingjob"
     * @return
     */
    public Integer getScheduledFromTime3() {
        return scheduledFromTime3;
    }

    public void setScheduledFromTime3(Integer scheduledFromTime3) {
        this.scheduledFromTime3 = scheduledFromTime3;
    }

    /**
     * If the indexation for an indexing job is delayed,
     * then this method returns the upper time value of the third
     * allowed indexation time range
     *
     * @hibernate.property column="totime3_indexingjob"
     * @return
     */
    public Integer getScheduledToTime3() {
        return scheduledToTime3;
    }

    public void setScheduledToTime3(Integer scheduledToTime3) {
        this.scheduledToTime3 = scheduledToTime3;
    }

    /**
     * @hibernate.property column="enableserver_indexingjob"
     * type="java.lang.String"
     * length="100"
     * @return
     */
    public String getEnabledIndexingServers() {
        return enabledIndexingServers;
    }

    public void setEnabledIndexingServers(String enabledIndexingServers) {
        this.enabledIndexingServers = enabledIndexingServers;
    }

    /**
     * Set of already processed servers
     *
     * @return
     */
    public Set<JahiaIndexJobServer> getProcessedServers() {
        return processedServers;
    }

    public void setProcessedServers(Set<JahiaIndexJobServer> processedServers) {
        this.processedServers = processedServers;
    }

    public void addProcessedServer(JahiaIndexJobServer jobServer){
        if ( jobServer != null ){
            this.getProcessedServers().remove(jobServer);
            this.getProcessedServers().add(jobServer);
        }
    }

    public void removeIndexJobServer(JahiaIndexJobServer jobServer){
        if ( jobServer != null ){
            this.getProcessedServers().remove(jobServer);
        }
    }

    public abstract Integer getSiteId();

    /**
     * Return true if the job is executable. If false, the job should be discarded
     *
     * @return
     */
    public abstract boolean isValid();

    public abstract void  execute(JahiaUser user);

    /**
     * Each Job is responsible of adding or removing RemovableDocument/IndexableDocument to/from toRemoves/toAdd list
     *
     * @param toRemove List of RemovableDocument instance
     * @param toAdd List of IndexableDocument instance
     * @param user
     */
    public abstract void prepareBatchIndexation(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd, JahiaUser user);

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaIndexingJob castOther = (JahiaIndexingJob) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getId())
                .toHashCode();
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
    public abstract Object clone() throws CloneNotSupportedException;

    public int compareTo(Object o){
        JahiaIndexingJob job = (JahiaIndexingJob)o;
        if (this.getDate().longValue() == job.getDate().longValue()){
            return 0;
        } else if (this.getDate().longValue()>job.getDate().longValue()) {
            return 1;
        }
        return -1;
    }

}
