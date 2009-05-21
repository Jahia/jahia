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
package org.jahia.services.lock;

import org.jahia.services.usermanager.JahiaUser;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Title: Jahia locking system implementation.</p>
 * <p>Description:
 * This class implements the lock object with an expiration time. A lock belongs
 * always to a context meaning that a owner (JahiaUser) and a identifier (session
 * ID for example) should be provided.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2003)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
public class Lock implements LockDefinitions, Serializable {

    /**
     * Default constructor.
     *
     * @param owner The lock owner defined by a JahiaUser object.
     * @param id The lock id defined by a contextual string such as http session id.
     * @param timeout The period in second during which the lock is valid. For a
     * non expiration time lock use the constant NO_EXPIRATION_TIME.
     */
    public Lock(JahiaUser owner, String id, int timeout) {
        this(owner, id, timeout,
                (timeout > NO_EXPIRATION_TIME) ? (System.currentTimeMillis() + timeout*1000) : -1,
                false);
    }

    /**
     * Persistence constructor
     *
     * @param owner The lock owner defined by a JahiaUser object.
     * @param id The lock id defined by a contextual string such as http session id.
     * @param timeout The period in second during which the lock is valid. For a
     * non expiration time lock use the constant NO_EXPIRATION_TIME.
     * @param expirationDate
     * @param stolen initial value to indicate if this lock has been stolen.
     */
    public Lock(JahiaUser owner, String id, int timeout, long expirationDate, boolean stolen) {
        this.expirationDate = expirationDate;
        this.timeout = timeout * 1000;
        this.owner = owner;
        this.id = id;
        this.stealed = stolen;
    }

    /**
     * Get the system time and return if lock is already valid or not.
     *
     * @return True if lock did not expired, false otherwise.
     */
    public boolean hasExpired() {
        if (this.timeout <= NO_EXPIRATION_TIME) {
            return false;
        }
        Date date = new Date();
        return date.getTime() > this.expirationDate;
    }

    /**
     * Get the lock time remaining.
     *
     * @return The lock time remaining.
     */
    public long getTimeRemaining() {
        if (this.timeout <= NO_EXPIRATION_TIME) {
            return NO_EXPIRATION_TIME;
        }
        Date date = new Date();
        return this.expirationDate - date.getTime();
    }

    /**
     * Reset the lock time out to the new timeout parameter.
     *
     * @param timeout The timeout in second.
     */
    public void resetTimeout(int timeout) {
        if (this.timeout <= NO_EXPIRATION_TIME) {
            return;
        }
        Date date = new Date();
        this.timeout = timeout * 1000;
        this.expirationDate = date.getTime() + this.timeout;
    }

    /**
     * Time out getter.
     *
     * @return The time out in second.
     */
    public long getTimeout() {
        return this.timeout;
    }

    /**
     * Lock owner (JahiaUser) getter.
     *
     * @return The lock owner.
     */
    public JahiaUser getOwner() {
        return this.owner;
    }

    /**
     * Owner (JahiaUser) getter. Change the lock owner.
     *
     * @param owner The new lock owner.
     */
    public void setOwner(JahiaUser owner) {
        this.owner = owner;
    }

    /**
     * Lock identifier getter.
     *
     * @return The lock ID.
     */
    public String getID() {
        return this.id;
    }

    /**
     * Lock ID setter. Change the lock ID.
     *
     * @param id The lock ID.
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * If a Jahia user has sufficiant rights (admin for example) he can steal a
     * lock. In this case this getter returned if the lock was stolen or not.
     *
     * @return True if the lock was stolen, false otherwise.
     */
    public boolean isStealed() {
        return stealed;
    }

    /**
     * If a Jahia user has sufficiant rights (admin for example) he can steal a
     * lock. In this case this setter can be used to mark the lock as stolen.
     *
     * @param stealed Should be true for stolen lock.
     */
    public void setStealed(boolean stealed) {
        this.stealed = stealed;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    private long expirationDate;
    private long timeout;
    private JahiaUser owner;
    private String id;
    private boolean stealed;

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("Lock: ");
        buff.append(id);
        return buff.toString();
    }
}