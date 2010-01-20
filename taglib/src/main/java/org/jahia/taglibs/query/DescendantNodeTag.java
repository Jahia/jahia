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
import javax.jcr.query.qom.DescendantNode;

/**
 * Tag used to create a DescendantNode Constraint
 *
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 */
@SuppressWarnings("serial")
public class DescendantNodeTag extends ConstraintTag  {

    private DescendantNode descendantNode;
    private String path;

    @Override
    protected void resetState() {
        path = null;
        descendantNode = null;
        super.resetState();
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    public Constraint getConstraint() throws Exception {
        if ( descendantNode != null ){
            return descendantNode;
        }
        if (path.trim().length() == 0){
            return null;
        }
        
        descendantNode = this.getQueryFactory().descendantNode(getSelectorName(), path);

        return descendantNode;
    }

}