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
package org.jahia.data.applications;

import org.jahia.content.ObjectKey;

/**
 *
 * User: ktlili
 * Date: 26 janv. 2007
 * Time: 16:26:42
 */
public class EntryPointObjectKey extends ObjectKey {

    private static final long serialVersionUID = 1135214230216807396L;

    public static final String ENTRY_POINT_TYPE = "entrypoint";

    /*TO DO: ckeck value of this IDInType */
    private String IDInType = "100";
    private int idInType;

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method. Please do not use this constructor directly
     */
    public EntryPointObjectKey() {}

    public EntryPointObjectKey(String appID_entryPointName) {
        super(ENTRY_POINT_TYPE, appID_entryPointName);
    }

    public EntryPointObjectKey(String appID_entryPointName, String objectKey) {
        super(ENTRY_POINT_TYPE, appID_entryPointName, objectKey);
    }

    public String getType() {
        return ENTRY_POINT_TYPE;
    }

    public String getIDInType() {
        return this.IDInType;
    }

    public int getIdInType() {
        if (idInType <= 0) {
            try {
                idInType = Integer.parseInt(IDInType);
            } catch (NumberFormatException e) {
                idInType = -1;
            }
        }
        return idInType;
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new EntryPointObjectKey(IDInType, objectKey);
    }

}
