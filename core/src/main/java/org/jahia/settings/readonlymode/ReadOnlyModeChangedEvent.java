/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.settings.readonlymode;

import org.springframework.context.ApplicationEvent;

/**
 * This event is broadcasted to notify corresponding Spring service about the change of the read-only mode.
 *
 * @author Sergiy Shyrkov
 */
public class ReadOnlyModeChangedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -9011075112888785206L;

    private boolean readOnlyModeIsOn;

    /**
     * Initializes an instance of this class.
     *
     * @param source the event source object
     * @param readOnlyModeIsOn <code>true</code> in case the read-only mode is enabled; <code>false</code> if it is disabled
     *
     */
    public ReadOnlyModeChangedEvent(Object source, boolean readOnlyModeIsOn) {
        super(source);
        this.readOnlyModeIsOn = readOnlyModeIsOn;
    }

    /**
     * Returns <code>true</code> in case the read-only mode is enabled; <code>false</code> if it is disabled.
     *
     * @return <code>true</code> in case the read-only mode is enabled; <code>false</code> if it is disabled
     */
    public boolean isReadOnlyModeIsOn() {
        return readOnlyModeIsOn;
    }

}
