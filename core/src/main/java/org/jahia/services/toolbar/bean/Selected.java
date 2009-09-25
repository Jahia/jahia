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
package org.jahia.services.toolbar.bean;

import org.apache.log4j.Logger;
import org.jahia.services.toolbar.resolver.SelectedResolver;

import java.io.Serializable;

/**
 * User: jahia
 * Date: 8 avr. 2008
 * Time: 10:21:27
 */
public class Selected implements Serializable {
    
    private static final transient Logger logger = Logger
            .getLogger(Selected.class);
    
    private boolean value;
    private String classResolver;
    private String inputResolver;

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String getClassResolver() {
        return classResolver;
    }

    public void setClassResolver(String classResolver) {
        this.classResolver = classResolver;
    }

    public String getInputResolver() {
        return inputResolver;
    }

    public void setInputResolver(String inputResolver) {
        this.inputResolver = inputResolver;
    }

    public boolean getRealValue(org.jahia.data.JahiaData jData) {
        if (inputResolver != null) {
            try {
                SelectedResolver resolver = (SelectedResolver) Class.forName(classResolver).newInstance();
                return resolver.isSelected(jData,inputResolver);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return true;
            }
        } else {
            return value;
        }
    }
}
