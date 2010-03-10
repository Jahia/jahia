package org.jahia.services.content;

import javax.jcr.version.Version;
import java.util.Date;

/**
 * Contains version info for end-users.
 *
 * @author loom
 *         Date: Mar 10, 2010
 *         Time: 10:08:08 AM
 */
public class VersionInfo implements Comparable {
    private Version version;
    private long revisionNumber;
    private String comment;
    private boolean subNode;
    private Date date;

    public VersionInfo(Version version, long revisionNumber, String comment, boolean subNode, Date date) {
        this.version = version;
        this.revisionNumber = revisionNumber;
        this.comment = comment;
        this.subNode = subNode;
        this.date = date;
    }

    public Version getVersion() {
        return version;
    }

    public long getRevisionNumber() {
        return revisionNumber;
    }

    public String getComment() {
        return comment;
    }

    public boolean isSubNode() {
        return subNode;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionInfo)) return false;

        VersionInfo that = (VersionInfo) o;

        if (revisionNumber != that.revisionNumber) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (revisionNumber ^ (revisionNumber >>> 32));
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     * <p/>
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     * <p/>
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     * <p/>
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     * <p/>
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     * <p/>
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo(Object o) {
        VersionInfo that = (VersionInfo) o;
        return new Long(revisionNumber).compareTo(new Long(that.getRevisionNumber()));
    }
}
