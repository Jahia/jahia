/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
        if(comment!=null) {
            return comment.equals(that.comment);
        }
        if (checkinDate != null) {
            return checkinDate.equals(that.checkinDate);
        } else {
            return version.equals(that.version);
        }
    }

    @Override
    public int hashCode() {
        if(comment!=null) {
            return comment.hashCode();
        }
        if (checkinDate != null) {
            return checkinDate.hashCode();
        } else {
            return 31*version.hashCode();
        }
    }

    public int compareTo(Object o) {
        if (this == o) return 0;

        VersionInfo that = (VersionInfo) o;
        if (this.equals(that)) {
            return 0;
        }
        if (comment != null && that.comment != null) {
            return this.comment.compareTo(that.comment);
        }
        else if (checkinDate != null && that.checkinDate != null) {
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
