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
package org.jahia.test.services.validation;

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.test.TestHelper;
import org.jahia.utils.i18n.JahiaLocaleContextHolder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

public class ValidationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ValidationTest.class);

    private static final String TEST_SITE_NAME = "validationTest";
    private static final String SITE_CONTENT_ROOT_NODE = "/sites/" + TEST_SITE_NAME;
    private static final String TEST_NODE_TYPE = "test:validatedNode";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            TestHelper.createSite(TEST_SITE_NAME);
        } catch (Exception e) {
            fail("Error setting up ValidationTest environment", e);
        }
    }


    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TEST_SITE_NAME);
        } catch (Exception e) {
            logger.error("Error tearing down ValidationTest environment", e);
        }
    }

    @Test
    public void testNotNullConstraint() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
            session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testNotNull", TEST_NODE_TYPE);
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        assertThat(errors).extracting(Exception::getLocalizedMessage)
                .containsExactly("/sites/validationTest/testNotNull Field not null: must not be null");
    }

    @Test
    public void testSizeBetween6And20Constraint() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
            JCRNodeWrapper testSizeBetween6And20 = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testSizeBetween6And20", TEST_NODE_TYPE);
            testSizeBetween6And20.setProperty("test:notNull", "something");
            testSizeBetween6And20.setProperty("test:sizeBetween6And20", "abc");
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        assertThat(errors).extracting(Exception::getLocalizedMessage).containsExactly(
                "/sites/validationTest/testSizeBetween6And20 Field with size between 6 and 20: size must be between 6 and 20");
    }

    @Test
    public void testFieldMatchCustomConstraint() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
            JCRNodeWrapper testFieldMatch = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testFieldMatch", TEST_NODE_TYPE);
            testFieldMatch.setProperty("test:notNull", "something");
            testFieldMatch.setProperty("test:sizeBetween6And20", "abcdefghijklm");
            testFieldMatch.setProperty("test:email", "mail@one.com");
            testFieldMatch.setProperty("test:confirmEmail", "mail@two.com");
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        assertThat(errors).extracting(Exception::getLocalizedMessage)
                .containsExactly("/sites/validationTest/testFieldMatch Confirmation email field: Fields don't match");
    }

    @Test
    public void testEmailConstraint() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
            JCRNodeWrapper testEmail = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testEmail", TEST_NODE_TYPE);
            testEmail.setProperty("test:notNull", "something");
            testEmail.setProperty("test:sizeBetween6And20", "abcdefghijklm");
            testEmail.setProperty("test:email", "wrongmail");
            testEmail.setProperty("test:confirmEmail", "wrongmail");
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        assertThat(errors).hasSize(2).extracting(Exception::getLocalizedMessage).containsExactlyInAnyOrder(
                "/sites/validationTest/testEmail Email field: must be a well-formed email address",
                "/sites/validationTest/testEmail Confirmation email field: must be a well-formed email address");
    }

    @Test
    public void testFutureDateConstraint() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
            JCRNodeWrapper testFutureDate = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testFutureDate", TEST_NODE_TYPE);
            testFutureDate.setProperty("test:notNull", "something");
            testFutureDate.setProperty("test:sizeBetween6And20", "abcdefghijklm");
            testFutureDate.setProperty("test:email", "good@mail.com");
            testFutureDate.setProperty("test:confirmEmail", "good@mail.com");
            testFutureDate.setProperty("test:futureDate", Calendar.getInstance());
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        assertThat(errors).extracting(Exception::getLocalizedMessage)
                .containsExactly("/sites/validationTest/testFutureDate Future date field: must be a future date");
    }

    @Test
    public void testGreaterThan2Constraint() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
            JCRNodeWrapper testGreaterThan2 = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testGreaterThan2", TEST_NODE_TYPE);
            testGreaterThan2.setProperty("test:notNull", "something");
            testGreaterThan2.setProperty("test:sizeBetween6And20", "abcdefghijklm");
            testGreaterThan2.setProperty("test:email", "good@mail.com");
            testGreaterThan2.setProperty("test:confirmEmail", "good@mail.com");
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(date.getTimeInMillis() + 3600000);
            testGreaterThan2.setProperty("test:futureDate", date);
            testGreaterThan2.setProperty("test:greaterThan2", 2);
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        assertThat(errors).extracting(Exception::getLocalizedMessage)
        .containsExactly("/sites/validationTest/testGreaterThan2 Field with value greater than 2: must be greater than or equal to 3");
    }

    @Test
    public void testMessageTranslation() throws RepositoryException {
        List<ConstraintViolationException> errors = null;
        Locale previousLocale = JahiaLocaleContextHolder.getLocale();
        try {
            try {
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
                JCRNodeWrapper testGreaterThan2 = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testTranslation", TEST_NODE_TYPE);
                testGreaterThan2.setProperty("test:sizeBetween6And20", "abc");
                testGreaterThan2.setProperty("test:email", "badmail.com");
                testGreaterThan2.setProperty("test:confirmEmail", "badmail2.com");
                testGreaterThan2.setProperty("test:futureDate", Calendar.getInstance());
                testGreaterThan2.setProperty("test:greaterThan2", 2);
                JahiaLocaleContextHolder.setLocale(Locale.FRENCH);
                session.save();
            } catch (CompositeConstraintViolationException e) {
                errors = e.getErrors();
            }
            assertThat(errors).hasSize(7).extracting(Exception::getLocalizedMessage).containsExactlyInAnyOrder(
                    "/sites/validationTest/testTranslation Champ non nul: ne doit pas être nul",
                    "/sites/validationTest/testTranslation Champ dont la taille est comprise entre 6 et 20: la taille doit être comprise entre 6 et 20",
                    "/sites/validationTest/testTranslation Champ de confirmation d'e-mail: Les champs ne correspondent pas",
                    "/sites/validationTest/testTranslation Champ d'e-mail: doit être une adresse électronique syntaxiquement correcte",
                    "/sites/validationTest/testTranslation Champ de confirmation d'e-mail: doit être une adresse électronique syntaxiquement correcte",
                    "/sites/validationTest/testTranslation Champ de date dans le futur: doit être une date dans le futur",
                    "/sites/validationTest/testTranslation Champ avec valeur strictement supérieure à 2: doit être supérieur ou égal à 3"
            );
        } finally {
            JahiaLocaleContextHolder.setLocale(previousLocale);
        }
    }

    @Test
    public void testValidNodeCreation() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode(SITE_CONTENT_ROOT_NODE);
        JCRNodeWrapper validNode = siteNode.addNode("validNode", TEST_NODE_TYPE);
        validNode.setProperty("test:notNull", "something");
        validNode.setProperty("test:sizeBetween6And20", "abcdefghijklm");
        validNode.setProperty("test:email", "good@mail.com");
        validNode.setProperty("test:confirmEmail", "good@mail.com");
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(date.getTimeInMillis() + 3600000);
        validNode.setProperty("test:futureDate", date);
        validNode.setProperty("test:greaterThan2", 3);
        session.save();

        assertThat(siteNode.hasNode("validNode")).isTrue();
        validNode.remove();
        session.save();
    }

}
