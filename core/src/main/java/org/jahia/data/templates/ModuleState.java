/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.data.templates;

/**
 * Represents the state of a module in its life cycle.
 */
public class ModuleState {
    public enum State {
        INSTALLED, ERROR_WITH_DEFINITIONS, RESOLVED, STARTED, STARTING, STOPPED, STOPPING, UNINSTALLED, UNRESOLVED, UPDATED, WAITING_TO_BE_IMPORTED, SPRING_NOT_STARTED, INCOMPATIBLE_VERSION;

        @Override
        public String toString() {
            switch (this) {
                case UNINSTALLED:
                    return "Uninstalled";
                case UNRESOLVED:
                    return "Unresolved";
                case RESOLVED:
                    return "Resolved";
                case ERROR_WITH_DEFINITIONS:
                    return "Error when parsing definitions";
                case INSTALLED:
                    return "Installed";
                case UPDATED:
                    return "Updated";
                case STOPPED:
                    return "Stopped";
                case STOPPING:
                    return "Stopping";
                case STARTING:
                    return "Starting";
                case STARTED:
                    return "Started";
                case SPRING_NOT_STARTED:
                    return "Spring not started";
                case WAITING_TO_BE_IMPORTED:
                    return "Waiting to be imported by processing server";
                case INCOMPATIBLE_VERSION:
                    return "Incompatible version";
                default:
                    break;
            }
            return super.toString();
        }
    }

    private Object details;

    private State state;

    public Object getDetails() {
        return details;
    }

    public State getState() {
        return state;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return details != null ? new StringBuilder(256).append("state: ").append(state).append("; details: ")
                .append(details).toString() : state.toString();
    }
}
