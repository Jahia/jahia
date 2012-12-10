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

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ValidationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ValidationTest.class);

    private final static String TEST_SITE_NAME = "validationTest";
    private final static String SITE_CONTENT_ROOT_NODE = "/sites/" + TEST_SITE_NAME;
    private final static String TEST_NODE_TYPE = "test:validatedNode";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            TestHelper.createSite(TEST_SITE_NAME);
        } catch (Exception e) {
            logger.error("Error setting up ValidationTest environment", e);
        }
    }

    @Before
    public void setUp() {
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
    public void testNotNullConstraint() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
            JCRNodeWrapper testNotNull = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testNotNull", TEST_NODE_TYPE);
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        Assert.assertEquals("/sites/validationTest/testNotNull Field not null: may not be null",
                errors.get(0).getLocalizedMessage());
    }

    @Test
    public void testSizeBetween6And20Constraint() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
            JCRNodeWrapper testSizeBetween6And20 = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testSizeBetween6And20", TEST_NODE_TYPE);
            testSizeBetween6And20.setProperty("test:notNull", "something");
            testSizeBetween6And20.setProperty("test:sizeBetween6And20", "abc");
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        Assert.assertEquals("/sites/validationTest/testSizeBetween6And20 Field with size between 6 and 20: size must be between 6 and 20",
                errors.get(0).getLocalizedMessage());
    }

    @Test
    public void testFieldMatchCustomConstraint() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
            JCRNodeWrapper testFieldMatch = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testFieldMatch", TEST_NODE_TYPE);
            testFieldMatch.setProperty("test:notNull", "something");
            testFieldMatch.setProperty("test:sizeBetween6And20", "abcdefghijklm");
            testFieldMatch.setProperty("test:email", "mail@one.com");
            testFieldMatch.setProperty("test:confirmEmail", "mail@two.com");
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        Assert.assertEquals("/sites/validationTest/testFieldMatch Confirmation email field: Fields don't match",
                errors.get(0).getLocalizedMessage());
    }

    @Test
    public void testEmailConstraint() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
            JCRNodeWrapper testEmail = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testEmail", TEST_NODE_TYPE);
            testEmail.setProperty("test:notNull", "something");
            testEmail.setProperty("test:sizeBetween6And20", "abcdefghijklm");
            testEmail.setProperty("test:email", "wrongmail");
            testEmail.setProperty("test:confirmEmail", "wrongmail");
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        Assert.assertEquals(2, errors.size());
        List<String> messages = Arrays.asList(
                "/sites/validationTest/testEmail Email field: not a well-formed email address",
                "/sites/validationTest/testEmail Confirmation email field: not a well-formed email address"
        );
        for (ConstraintViolationException error: errors) {
            String localizedMessage = error.getLocalizedMessage();
            Assert.assertTrue(messages.contains(localizedMessage));
        }
    }

    @Test
    public void testFutureDateConstraint() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
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
        Assert.assertEquals("/sites/validationTest/testFutureDate Future date field: must be in the future",
                errors.get(0).getLocalizedMessage());
    }

    @Test
    public void testGreaterThan2Constraint() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
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
        Assert.assertEquals("/sites/validationTest/testGreaterThan2 Field with value greater than 2: must be greater than or equal to 3",
                errors.get(0).getLocalizedMessage());
    }

    @Test
    public void testMessageTranslation() throws Exception {
        List<ConstraintViolationException> errors = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
            JCRNodeWrapper testGreaterThan2 = session.getNode(SITE_CONTENT_ROOT_NODE).addNode("testTranslation", TEST_NODE_TYPE);
            testGreaterThan2.setProperty("test:sizeBetween6And20", "abc");
            testGreaterThan2.setProperty("test:email", "badmail.com");
            testGreaterThan2.setProperty("test:confirmEmail", "badmail2.com");
            testGreaterThan2.setProperty("test:futureDate", Calendar.getInstance());
            testGreaterThan2.setProperty("test:greaterThan2", 2);
            LocaleContextHolder.setLocale(Locale.FRENCH, true);
            session.save();
        } catch (CompositeConstraintViolationException e) {
            errors = e.getErrors();
        }
        Assert.assertEquals(7, errors.size());
        List<String> messages = Arrays.asList(
                "/sites/validationTest/testTranslation Champ non nul: ne peut pas être nul",
                "/sites/validationTest/testTranslation Champ dont la taille est comprise entre 6 et 20: la taille doit être entre 6 et 20",
                "/sites/validationTest/testTranslation Champ de confirmation d'e-mail: Les champs ne correspondent pas",
                "/sites/validationTest/testTranslation Champ d'e-mail: Adresse email mal formée",
                "/sites/validationTest/testTranslation Champ de confirmation d'e-mail: Adresse email mal formée",
                "/sites/validationTest/testTranslation Champ de date dans le futur: doit être dans le futur",
                "/sites/validationTest/testTranslation Champ avec valeur strictement supérieure à 2: doit être plus grand que 3"
        );
        for (ConstraintViolationException error: errors) {
            String localizedMessage = error.getLocalizedMessage();
            Assert.assertTrue(messages.contains(localizedMessage));
        }
    }

    @Test
    public void testValidNodeCreation() throws Exception {
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

        Assert.assertTrue(siteNode.hasNode("validNode"));
        validNode.remove();
        session.save();
    }

}
