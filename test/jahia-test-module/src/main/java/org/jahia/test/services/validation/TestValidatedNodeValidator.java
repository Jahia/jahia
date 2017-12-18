/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.validation;

import org.hibernate.validator.constraints.Email;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Calendar;

@FieldMatch.List({
        @FieldMatch(first = "test_email", second = "test_confirmEmail", propertyName = "test:confirmEmail")
})
public class TestValidatedNodeValidator implements JCRNodeValidator {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TestValidatedNodeValidator.class);
    private JCRNodeWrapper node;

    public TestValidatedNodeValidator(JCRNodeWrapper node) {
        this.node = node;
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

    @Email
    public String getTest_confirmEmail() {
        return node.getPropertyAsString("test:confirmEmail");
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
