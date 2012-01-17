/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
        StringBuffer buff = new StringBuffer();

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