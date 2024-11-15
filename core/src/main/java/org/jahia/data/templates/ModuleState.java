/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
        INSTALLED, ERROR_WITH_DEFINITIONS, ERROR_WITH_RULES, RESOLVED, STARTED, STARTING, STOPPING, UPDATED, WAITING_TO_BE_IMPORTED, SPRING_STARTING, SPRING_NOT_STARTED, INCOMPATIBLE_VERSION;

        @Override
        public String toString() {
            switch (this) {
                case RESOLVED:
                    return "Resolved";
                case ERROR_WITH_DEFINITIONS:
                    return "Error when parsing definitions";
                case ERROR_WITH_RULES:
                    return "Error when compiling rules";
                case INSTALLED:
                    return "Installed";
                case UPDATED:
                    return "Updated";
                case STOPPING:
                    return "Stopping";
                case STARTING:
                    return "Starting";
                case STARTED:
                    return "Started";
                case SPRING_STARTING:
                    return "Spring starting";
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
