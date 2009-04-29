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
package org.jahia.services.lock;

import org.jahia.content.*;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.bin.Jahia;

import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.List;

/**
 * <p>Title: Jahia locking system implementation.</p>
 * <p>Description:
 * Implement a unique lock key system. A lock key is defined by three parameters :
 * - An action : Kind of operation which is perfomed on a resource.
 * - A name : which is the lock name given by the engine names or something else.
 * - An identifier : Given by a content object, a site, ...
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2003)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 *
 * @author unascribed
 * @version 1.0
 */
public class LockKey implements Serializable {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LockKey.class);

    static final long serialVersionUID = 1701832103044285757L;
    
    public static final String UPDATE_ACTION = "Update";
    public static final String ADD_ACTION = "Add";
    public static final String DELETE_ACTION = "Delete";
    public static final String MODIFY_ACTION = "Modify";
    public static final String WORKFLOW_ACTION = "Workflow";
    public static final String IMPORT_ACTION = "Import";
    public static final String EXPORT_ACTION = "Export";
    public static final String LIVEEXPORT_ACTION = "LiveExport";

    private static final String WAITING_FOR_APPROVAL_LOCKNAME = "WaitingForApproval";
    private static final String MARK_FOR_DELETE_LOCKNAME = "MarkForDelete";
    public static final String RESTORE_LIVE_CONTENT_ACTION = "RestoreLiveContent";

    public static final String UPDATE_CONTAINERLIST_TYPE = UPDATE_ACTION + "_" +
            ContentContainerListKey.CONTAINERLIST_TYPE;
    public static final String DELETE_CONTAINERLIST_TYPE = DELETE_ACTION + "_" +
            ContentContainerListKey.CONTAINERLIST_TYPE;
    public static final String ADD_CONTAINER_TYPE = ADD_ACTION + "_" +
            ContentContainerListKey.CONTAINERLIST_TYPE;
    public static final String DELETE_CONTAINER_TYPE = DELETE_ACTION + "_" +
            ContentContainerKey.CONTAINER_TYPE;
    public static final String UPDATE_CONTAINER_TYPE = UPDATE_ACTION + "_" +
            ContentContainerKey.CONTAINER_TYPE;

    public static final String LIVEEXPORT_PAGE_TYPE = LIVEEXPORT_ACTION + "_" +
            ContentPageKey.PAGE_TYPE;
    public static final String EXPORT_CONTAINER_TYPE = EXPORT_ACTION + "_" +
            ContentContainerKey.CONTAINER_TYPE;

    public static final String UPDATE_FIELD_TYPE = UPDATE_ACTION + "_" +
            ContentFieldKey.FIELD_TYPE;
    public static final String UPDATE_PAGE_TYPE = UPDATE_ACTION + "_" +
            ContentPageKey.PAGE_TYPE;
    public static final String WORKFLOW_TYPE = WORKFLOW_ACTION + "_" +
            ContentPageKey.PAGE_TYPE;
    public static final String WAITING_FOR_APPROVAL_TYPE = MODIFY_ACTION + "_" +
            WAITING_FOR_APPROVAL_LOCKNAME;
    public static final String MARK_FOR_DELETE_TYPE = MODIFY_ACTION + "_" +
            MARK_FOR_DELETE_LOCKNAME;
    public static final String RESTORE_LIVE_CONTAINER_TYPE = RESTORE_LIVE_CONTENT_ACTION + "_" +
            ContentContainerKey.CONTAINER_TYPE;

    private String name;
    private String action;
    private int id;

    private LockKey(final String name, final int id, final String action) {
        this.name = name;
        this.id = id;
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return action + "_" + name;
    }

    public ObjectKey getObjectKey() {
        if ("SITE".equals(name)) {
            return null;
        }
        final StringBuffer buff = new StringBuffer();
        buff.append(name).append(ObjectKey.KEY_SEPARATOR).append(id);
        final String objectKey = buff.toString();
        try {
            return ObjectKey.getInstance(objectKey);
        } catch (ClassNotFoundException cnfe) {
            // Displaying stack trace is not necessary because we effectively
            // try to get a content object from the LockKey; other objects
            // or critical section can be locked.
            logger.error("Object key '" + objectKey + "' not found !");
            return null;
        }
    }

    public ContentObject getContentObject() {
        ObjectKey k = getObjectKey();
        if (k != null) {
            try {
                return (ContentObject) ContentObject.getInstance(k);
            } catch (Exception e) {
                logger.error("Object key '" + k + "' not found !");
            }
        }
        return null;
    }

    /**
     * @param pageID actually not used to compose the LockKey, reason why this method is deprecated.
     * @deprecated Please use composeLockKey(final String lockType, final int id) instead
     */
    public static LockKey composeLockKey(final String lockType,
                                         final int id,
                                         final int pageID) {
        final int index = lockType.indexOf("_");
        return new LockKey(lockType.substring(index + 1), id, lockType.substring(0, index));
    }

    /**
     *
     */
    public static LockKey composeLockKey(final String lockType,
                                         final int id) {
        final int index = lockType.indexOf("_");
        return new LockKey(lockType.substring(index + 1), id, lockType.substring(0, index));
    }

    public static LockKey composeLockKey(final ObjectKey key, final String action) {
        return new LockKey(key.getType(), key.getIdInType(), action);
    }

    public static LockKey composeLockKey(final String lockKeyStr) {
        if (lockKeyStr == null || lockKeyStr.length() == 0) return null;
        try {
            final StringTokenizer lockKeyToken = new StringTokenizer(lockKeyStr, "_");
            final String action = lockKeyToken.nextToken();
            if (!lockKeyToken.hasMoreTokens()) return null;
            final String name = lockKeyToken.nextToken();
            if (!lockKeyToken.hasMoreTokens()) return null;
            final int id = Integer.parseInt(lockKeyToken.nextToken());
            return new LockKey(name, id, action);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean equals(final Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final LockKey lockKey = (LockKey) obj;
            return lockKey.getAction().equals(this.action) &&
                    lockKey.getName().equals(this.name) &&
                    lockKey.getId() == this.id;
        }
        return false;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public int getPageID() {
        ContentObject contentObject = getContentObject();
        if (contentObject != null) {
            return contentObject.getPageID();
        }
        return -1;
    }

    // Serialization methods
    private void readObject(final java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        name = (String) stream.readObject();
        id = stream.readInt();
        action = (String) stream.readObject();
    }

    private void writeObject(final java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(name);
        stream.writeInt(id);
        stream.writeObject(action);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append(action).append("_").
                append(name).append("_").
                append(id);
        return buff.toString();
    }

    public static String getFriendlyMessage(final LockKey blockingLockKey,
                                            final ProcessingContext jParams) {
        final StringBuffer buff = new StringBuffer();
        final List<Map<String, Serializable>> infos = ServicesRegistry.getInstance().getLockService().getInfo(blockingLockKey);
        if (LockKey.WAITING_FOR_APPROVAL_TYPE.equals(blockingLockKey.getType())) {
            return JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.waitingForApproval.label",
                    jParams.getLocale());
        } else if (LockKey.MARK_FOR_DELETE_TYPE.equals(blockingLockKey.getType())) {
            return JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.markForDelete.label",
                    jParams.getLocale());
        }
        if (infos == null || infos.size() == 0) return "N/A";
        final Map<String, Serializable> lockInfo = infos.get(0);
        if (blockingLockKey.getType().startsWith(LockKey.WORKFLOW_ACTION)) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.lockedByWorkflow.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (LockKey.ADD_CONTAINER_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.containerTocontentContainer.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (LockKey.UPDATE_CONTAINERLIST_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.updateList.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (LockKey.UPDATE_PAGE_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.objectParentPageProperties.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (LockKey.UPDATE_CONTAINER_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.isEditionMode.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (LockKey.DELETE_CONTAINER_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.willBeDeleted.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (LockKey.UPDATE_FIELD_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.isEditionMode.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));

        } else if (blockingLockKey.getType().startsWith(LockKey.IMPORT_ACTION)) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.copy.label",
                    jParams.getLocale()));

        } else if (blockingLockKey.getType().startsWith(LockKey.EXPORT_ACTION)) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.copy.label",
                    jParams.getLocale()));

        } else if (blockingLockKey.getType().startsWith(LockKey.LIVEEXPORT_ACTION)) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.copy.label",
                    jParams.getLocale()));

        } else if (LockKey.RESTORE_LIVE_CONTAINER_TYPE.equals(blockingLockKey.getType())) {
            buff.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.restorelivecontainer.RestoreLiveContainer_Engine.restoreReadOnly.label",
                    jParams.getLocale()));
            buff.append(" ");
            buff.append(((JahiaUser) lockInfo.get(LockRegistry.OWNER)).getUsername());
            buff.append(". ");
            final long timeRemaining = (Long) lockInfo.get(LockRegistry.TIME_REMAINING);
            buff.append(getTimeString(timeRemaining, jParams));
        }
        return buff.toString();
    }

    private static String getTimeString(final long timeRemaining, final ProcessingContext jParams) {
        final StringBuffer buff = new StringBuffer();
        buff.append("<img border='0' src='");
        buff.append(Jahia.getContextPath());
        buff.append("/engines/images/stopwatch.gif");
        buff.append("' title=' ");
        final String remaining = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.lock.remainingTime.label",
                    jParams.getLocale());
        buff.append(remaining);
        buff.append(": ");
        final long h1 = timeRemaining / 3600000;
        final long m1 = (timeRemaining - h1 * 3600000) / 60000;
        final long s1 = (timeRemaining - h1 * 3600000 - m1 * 60000) / 1000;
        buff.append(h1);
        buff.append(" h ");
        buff.append(m1);
        buff.append(" m ");
        buff.append(s1);
        buff.append(" s ");
        buff.append("' ");
        buff.append("alt='");
        buff.append(remaining);
        buff.append("' /> ");
        return buff.toString();
    }
}
