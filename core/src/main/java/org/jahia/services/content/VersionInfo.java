package org.jahia.services.content;

import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
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

    private static transient Logger logger = Logger.getLogger(VersionInfo.class);

    public static final long VERSION_EQUALITY_TIME_RANGE = 5*60*1000; // 5 mins equality test range

    private Version version;
    private long revisionNumber = -1;
    private String comment;
    private int depth;

    public VersionInfo(Version version, long revisionNumber, String comment, int depth) {
        this.version = version;
        this.revisionNumber = revisionNumber;
        this.comment = comment;
        this.depth = depth;
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

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionInfo)) return false;

        VersionInfo that = (VersionInfo) o;

        try {
            if ((revisionNumber != -1) && (that.revisionNumber != -1)) {
                if (Math.abs(version.getCreated().getTimeInMillis() - that.version.getCreated().getTimeInMillis()) <= VERSION_EQUALITY_TIME_RANGE) {
                    if (revisionNumber == that.revisionNumber) return true;
                }
            }
            // no revision number available, let's test version names simply.
            return version.getName().equals(that.version.getName());
        } catch (RepositoryException re) {
            logger.error("Repository error while calculating equality, returning false !", re);
            return false;
        }

    }

    @Override
    public int hashCode() {
        if (revisionNumber !=-1) {
            return (int) (revisionNumber ^ (revisionNumber >>> 32));
        } else {
            try {
                return version.getName().hashCode();
            } catch (RepositoryException re) {
                logger.error("Repository error while calculating hashcode for version, returning -1 hashcode", re);
                return -1;
            }
        }
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
        if ((revisionNumber != -1) && (that.revisionNumber != -1)) {
            return new Long(revisionNumber).compareTo(new Long(that.getRevisionNumber()));
        } else {
            try {
                return this.version.getCreated().compareTo(that.version.getCreated());
            } catch (RepositoryException re) {
                logger.error("Repository exception while comparing version creation times, returning equality !!!", re);
                return 0;
            }
        }
    }
}
