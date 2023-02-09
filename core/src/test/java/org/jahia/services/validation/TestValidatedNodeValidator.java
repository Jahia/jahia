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
package org.jahia.services.validation;

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
