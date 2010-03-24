package org.jahia.services.content;

import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import java.util.Calendar;
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

    private Version version;
    private Calendar checkinDate = null;
    private String comment;
    private int depth;

    public VersionInfo(Version version, Calendar checkinDate, String comment, int depth) {
        this.version = version;
        this.checkinDate = checkinDate;
        this.comment = comment;
        this.depth = depth;
    }

    public Version getVersion() {
        return version;
    }

    public String getComment() {
        return comment;
    }

    public int getDepth() {
        return depth;
    }

    public Calendar getCheckinDate() {
        return checkinDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionInfo)) return false;

        VersionInfo that = (VersionInfo) o;

        if (checkinDate != null) {
            return checkinDate.equals(that.checkinDate);
        } else {
            return version.equals(that.version);
        }
    }

    @Override
    public int hashCode() {
        if (checkinDate != null) {
            return checkinDate.hashCode();
        } else {
            return version.hashCode();
        }
    }

    public int compareTo(Object o) {
        if (this == o) return 0;

        VersionInfo that = (VersionInfo) o;
        if (this.equals(that)) {
            return 0;
        }

        if (checkinDate != null) {
            return this.checkinDate.compareTo(that.checkinDate);
        } else {
            try {
                return this.version.getCreated().compareTo(that.version.getCreated());
            } catch (RepositoryException re) {
                logger.error("Error while comparing versions ", re);
                return 0;
            }
        }
    }
}
