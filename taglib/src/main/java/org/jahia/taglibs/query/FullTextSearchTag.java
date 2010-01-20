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
package org.jahia.taglibs.query;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.FullTextSearch;

import org.apache.log4j.Logger;

/**
 * User: hollis Date: 7 nov. 2007 Time: 15:33:24
 */
@SuppressWarnings("serial")
public class FullTextSearchTag extends ConstraintTag {

    private static Logger logger = Logger.getLogger(FullTextSearchTag.class);

    private FullTextSearch fullTextSearch;

    private String searchExpression;

    private String propertyName;

    @Override
    protected void resetState() {
        fullTextSearch = null;
        searchExpression = null;
        propertyName = null;
        super.resetState();
    }
    
    public Constraint getConstraint() {
        if (fullTextSearch != null) {
            return fullTextSearch;
        }
        if (this.searchExpression == null || this.searchExpression.trim().equals("")) {
            return null;
        }
        try {
            fullTextSearch = this.getQueryFactory().fullTextSearch(getSelectorName(), this.propertyName,
                    this.getQueryFactory().literal(this.getValueFactory().createValue(this.searchExpression)));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return fullTextSearch;
    }

    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

}