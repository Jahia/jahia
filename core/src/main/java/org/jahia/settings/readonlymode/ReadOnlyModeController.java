/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.settings.readonlymode;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jahia.services.SpringContextSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The read-only mode controller service that is responsible for performing mode switch and ensuring an appropriate event is broadcasted and
 * the related services are "notified".
 *
 * @author Sergiy Shyrkov
 */
public final class ReadOnlyModeController implements Serializable {

    /**
     * Read only mode status.
     */
    public enum ReadOnlyModeStatus {

        /**
         * Read only mode is switched off
         */
        OFF,

        /**
         * Read only mode is switched on
         */
        ON,

        /**
         * The application is in progress switching read only mode on
         */
        PENDING_ON,

        /**
         * The application is in progress switching read only mode off
         */
        PENDING_OFF,

        /**
         * The application is partially in read only mode on but an exception happened
         */
        PARTIAL_ON,

        /**
         * The application is partially in read only mode off but an exception happened
         */
        PARTIAL_OFF
    }

    private static final Comparator<ReadOnlyModeCapable> SERVICES_COMPARATOR_BY_PRIORITY =
            Comparator.comparingInt(ReadOnlyModeCapable::getReadOnlyModePriority);

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ReadOnlyModeController INSTANCE = new ReadOnlyModeController();
    }

    private static final long serialVersionUID = 5240686816879033535L;
    private static final Logger logger = LoggerFactory.getLogger(ReadOnlyModeController.class);

    /**
     * Handles the case, when a service encounters read-only mode violation, i.e. a data or state modification is requested.
     *
     * @param message the detailed error message from the service
     * @throws ReadOnlyModeException as a result of violation of read-only mode
     */
    public static void readOnlyModeViolated(String message) throws ReadOnlyModeException {
        throw new ReadOnlyModeException(message);
    }

    public static ReadOnlyModeController getInstance() {
        return Holder.INSTANCE;
    }

    private final transient Collection<ReadOnlyModeSwitchListener> switchListeners = new CopyOnWriteArrayList<>();
    private final transient Collection<ReadOnlyModeStatusSupplier> statusSuppliers = new CopyOnWriteArrayList<>();
    private transient volatile ReadOnlyModeStatus readOnlyStatus = ReadOnlyModeStatus.OFF;

    /**
     * Performs the switch of all DX services into read-only mode or back.
     *
     * @param enable <code>true</code> if the read-only mode should be enabled; <code>false</code> if it should be disabled
     * @throws IllegalStateException if switching to the specified mode is not permitted
     */
    public synchronized void switchReadOnlyMode(boolean enable) {
        final ReadOnlyModeStatus targetStatus = enable ? ReadOnlyModeStatus.ON : ReadOnlyModeStatus.OFF;
        logger.info("Received request to switch read only mode to {}", targetStatus);

        if (targetStatus != readOnlyStatus) { // don't do anything on this node if current status is already the target one
            if (!isStatusUpdateAllowed(enable)) {
                throw new IllegalStateException("The read-only mode state is " + readOnlyStatus + ", unable to switch to " + targetStatus);
            }

            // switch to pending
            readOnlyStatus = enable ? ReadOnlyModeStatus.PENDING_ON : ReadOnlyModeStatus.PENDING_OFF;

            List<ReadOnlyModeCapable> services = new LinkedList<>(SpringContextSingleton.getBeansOfType(ReadOnlyModeCapable.class).values());
            Collections.sort(services, enable ? SERVICES_COMPARATOR_BY_PRIORITY.reversed() : SERVICES_COMPARATOR_BY_PRIORITY);

            logger.info("Switching read only status of {} services", services.size());
            for (ReadOnlyModeCapable service : services) {
                try {
                    service.switchReadOnlyMode(enable);
                } catch (RuntimeException e) {
                    readOnlyStatus = enable ? ReadOnlyModeStatus.PARTIAL_ON : ReadOnlyModeStatus.PARTIAL_OFF;
                    logger.error("Error switching read only status of the service " + service, e);
                    throw e;
                }
            }

            // switch to final state
            readOnlyStatus = targetStatus;
        }

        // notify listeners
        notifyReadOnlyModeSwitched(enable);

        logger.info("Finished read-only mode switch. Now the read-only mode is {}", readOnlyStatus);
    }

    public ReadOnlyModeStatus getReadOnlyStatus() {
        return readOnlyStatus;
    }

    /**
     * Returns a list of read-only statuses from multiple origins
     *
     * <p>The returned list will always contain at least one element
     * which is the read-only status of the current instance.
     * Additional statuses can be provided by {@link ReadOnlyModeStatusSupplier}.
     *
     * @return a list of {@link ReadOnlyModeStatusInfo}
     * @see #addStatusSupplier(ReadOnlyModeStatusSupplier)
     * @see #removeStatusSupplier(ReadOnlyModeStatusSupplier)
     * @since 7.3.2.1
     */
    public List<ReadOnlyModeStatusInfo> getReadOnlyStatuses() {
        List<ReadOnlyModeStatusInfo> statuses = new ArrayList<>();
        statuses.add(new ReadOnlyModeStatusInfo("local", getReadOnlyStatus()));

        for (ReadOnlyModeStatusSupplier supplier : statusSuppliers) {
            statuses.addAll(supplier.getStatuses());
        }

        return statuses;
    }

    /**
     * Registers a {@link ReadOnlyModeSwitchListener} with this instance.
     *
     * <p>{@link ReadOnlyModeSwitchListener#onReadOnlyModeSwitched(boolean)} will be
     * fire once read only mode switch has completed.
     *
     * @param listener the listener to register
     * @since 7.3.2.1
     */
    public void addSwitchListener(ReadOnlyModeSwitchListener listener) {
        Objects.requireNonNull(listener);
        switchListeners.add(listener);
    }

    /**
     * Unregisters a {@link ReadOnlyModeSwitchListener} from this instance.
     *
     * @param listener the listener to unregister
     * @since 7.3.2.1
     */
    public void removeSwitchListener(ReadOnlyModeSwitchListener listener) {
        Objects.requireNonNull(listener);
        switchListeners.remove(listener);
    }

    /**
     * Registers a {@link ReadOnlyModeStatusSupplier} with this instance.
     *
     * @param supplier the supplier to register
     * @since 7.3.2.1
     */
    public void addStatusSupplier(ReadOnlyModeStatusSupplier supplier) {
        Objects.requireNonNull(supplier);
        statusSuppliers.add(supplier);
    }

    /**
     * Unregisters a {@link ReadOnlyModeStatusSupplier} from this instance.
     *
     * @param supplier the supplier to unregister
     * @since 7.3.2.1
     */
    public void removeStatusSupplier(ReadOnlyModeStatusSupplier supplier) {
        Objects.requireNonNull(supplier);
        statusSuppliers.remove(supplier);
    }

    private void notifyReadOnlyModeSwitched(boolean enabled) {
        for (ReadOnlyModeSwitchListener listener : switchListeners) {
            listener.onReadOnlyModeSwitched(enabled);
        }
    }

    /*
     * Checks if the read-only mode status change is allowed in the current state.
     * 
     * @param switchModeTo target read-only mode status, we are checking the switch into
     * @return <code>true</code> if the current state allows the requested change; <code>false</code> - otherwise
     */
    private boolean isStatusUpdateAllowed(boolean switchModeTo) {
        return (switchModeTo ? readOnlyStatus == ReadOnlyModeStatus.OFF : readOnlyStatus == ReadOnlyModeStatus.ON)
                || readOnlyStatus == ReadOnlyModeStatus.PARTIAL_ON || readOnlyStatus == ReadOnlyModeStatus.PARTIAL_OFF;
    }

}