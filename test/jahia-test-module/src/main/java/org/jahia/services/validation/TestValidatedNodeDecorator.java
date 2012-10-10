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

package org.jahia.services.validation;

import org.hibernate.validator.constraints.Email;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Calendar;

public class TestValidatedNodeDecorator extends JCRNodeDecorator {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TestValidatedNodeDecorator.class);

    public TestValidatedNodeDecorator(JCRNodeWrapper node) {
        super(node);
    }

    @NotNull
    public String getTest_notNull() {
        return node.getPropertyAsString("test:notNull");
    }

    @Size(min = 6, max = 20)
    public String getTest_sizeBetween6And20() {
        return node.getPropertyAsString("test:sizeBetween6And20");
    }

    @Email
    public String getTest_email() {
        return node.getPropertyAsString("test:email");
    }

    @Future
    public Calendar getTest_futureDate() {
        try {
            JCRPropertyWrapper property = node.getProperty("test:futureDate");
            if (property != null) {
                return property.getDate();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    @Min(3)
    public Long getTest_greaterThan2() {
        try {
            JCRPropertyWrapper property = node.getProperty("test:greaterThan2");
            if (property != null) {
                return Long.valueOf(property.getLong());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

}
