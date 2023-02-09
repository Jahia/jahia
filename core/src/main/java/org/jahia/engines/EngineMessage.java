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
 package org.jahia.engines;

import java.io.Serializable;


/**
 * <p>Title: EngineMessage bean object. </p> <p>Description: Inspired by Struts ActionMessage,
 * but more JavaBean compliant so that it can work with JSTL and better with Jahia's
 * localization classes.</p> <p>Copyright: Copyright (c) 2002</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class EngineMessage implements Serializable {

    private static final long serialVersionUID = 8836639382833159715L;

    /**
     * <p>The message key for this message.</p>
     */
    private String key = null;

    /**
     * <p>The replacement values for this mesasge.</p>
     */
    private Object[] values = null;

    /**
     * <p>Indicates whether the key is taken to be as a  bundle key [true] or
     * literal value [false].</p>
     */
    private boolean resource = true;

    public EngineMessage() {
        super();
    }


    public EngineMessage(String key) {
    	this();
        this.key = key;
    }

    public EngineMessage(String key, boolean resource) {
        this(key);
        this.resource = resource;
    }

    public EngineMessage(String key, Object... values) {
        this(key);
        this.values = values;
    }

    public EngineMessage(String key, String... values) {
        this(key);
        this.values = values;
    }

    /**
     * <p>Get the message key for this message.</p>
     *
     * @return The message key for this message.
     */
    public String getKey() {
        return (this.key);
    }

    /**
     * <p>Get the replacement values for this message.</p>
     *
     * @return The replacement values for this message.
     */
    public Object[] getValues() {
        return (this.values);
    }

    /**
     * <p>Indicate whether the key is taken to be as a  bundle key [true] or
     * literal value [false].</p>
     *
     * @return <code>true</code> if the key is a bundle key;
     *         <code>false</code> otherwise.
     */
    public boolean isResource() {
        return (this.resource);
    }

    /**
     * <p>Returns a String in the format: key[value1, value2, etc].</p>
     *
     * @return String representation of this message
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.key);
        buff.append("[");

        if (this.values != null) {
            for (int i = 0; i < this.values.length; i++) {
                buff.append(this.values[i]);

                // don't append comma to last entry
                if (i < (this.values.length - 1)) {
                    buff.append(", ");
                }
            }
        }

        buff.append("]");

        return buff.toString();
    }
}
