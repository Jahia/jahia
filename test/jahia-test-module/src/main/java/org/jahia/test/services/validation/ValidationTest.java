/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

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
                .containsExactly("/sites/validationTest/testNotNull Field not null: may not be null");
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
                "/sites/validationTest/testEmail Email field: not a well-formed email address",
                "/sites/validationTest/testEmail Confirmation email field: not a well-formed email address");
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
                .containsExactly("/sites/validationTest/testFutureDate Future date field: must be in the future");
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
        LocaleContext localeContext = null;
        try {
            try {
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,Locale.ENGLISH);
                JCRNodeWrapper testGreaterThan2 = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testTranslation", TEST_NODE_TYPE);
                testGreaterThan2.setProperty("test:sizeBetween6And20", "abc");
                testGreaterThan2.setProperty("test:email", "badmail.com");
                testGreaterThan2.setProperty("test:confirmEmail", "badmail2.com");
                testGreaterThan2.setProperty("test:futureDate", Calendar.getInstance());
                testGreaterThan2.setProperty("test:greaterThan2", 2);
                localeContext = LocaleContextHolder.getLocaleContext();
                LocaleContextHolder.setLocale(Locale.FRENCH, true);
                session.save();
            } catch (CompositeConstraintViolationException e) {
                errors = e.getErrors();
            }
            assertThat(errors).hasSize(7).extracting(Exception::getLocalizedMessage).containsExactlyInAnyOrder(
                    "/sites/validationTest/testTranslation Champ non nul: ne peut pas être nul",
                    "/sites/validationTest/testTranslation Champ dont la taille est comprise entre 6 et 20: la taille doit être entre 6 et 20",
                    "/sites/validationTest/testTranslation Champ de confirmation d'e-mail: Les champs ne correspondent pas",
                    "/sites/validationTest/testTranslation Champ d'e-mail: Adresse email mal formée",
                    "/sites/validationTest/testTranslation Champ de confirmation d'e-mail: Adresse email mal formée",
                    "/sites/validationTest/testTranslation Champ de date dans le futur: doit être dans le futur",
                    "/sites/validationTest/testTranslation Champ avec valeur strictement supérieure à 2: doit être plus grand que 3"
            );
        } finally {
            if (localeContext != null) {
                LocaleContextHolder.setLocaleContext(localeContext);
            }
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
