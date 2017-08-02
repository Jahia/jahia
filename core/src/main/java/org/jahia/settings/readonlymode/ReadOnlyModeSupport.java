/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

/**
 * The interface is implemented by services, which support the read-only mode change, i.e. are capable of putting themselves into a
 * read-only mode and back.
 * 
 * @author Sergiy Shyrkov
 */
public interface ReadOnlyModeSupport {

    /**
     * Called by a DX dedicated controller when the read-only mode switch is requested.
     * 
     * @param readOnlyModeIsOn <code>true</code> in case the read-only mode should be enabled; <code>false</code> otherwise
     * @param timeout an amount of milliseconds to wait till the service is forced to react on a mode switch; zero-value means the service
     *            is forced to react on the mode switch immediately; a negative value (say, <code>-1</code>) indicates that the service can
     *            perform mode-switch gracefully, i.e. wait indefinitely until it could perform the mode switch.
     */
    void onReadOnlyModeChanged(boolean readOnlyModeIsOn, long timeout);

    /**
     * Returns the priority of this service, i.e. the higher the priority the earlier the service will be notified of the read only mode
     * changes.
     * 
     * @return the priority of the service, which determines the order of notification about read-only mode changes (the higher the value
     *         is, the earlier the service will be notified)
     */
    int getReadOnlyModePriority();
}
