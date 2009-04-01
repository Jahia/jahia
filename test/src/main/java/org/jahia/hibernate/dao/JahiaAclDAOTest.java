/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.hibernate.dao;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclEntryPK;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.usermanager.JahiaDBGroup;
import org.jahia.test.TestHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JahiaAclDAO Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12/27/2004</pre>
 */
public class JahiaAclDAOTest extends AbstractTransactionalSpringContextTests {
// ------------------------------ FIELDS ------------------------------

    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());
    private JahiaAclDAO dao = null;

// -------------------------- STATIC METHODS --------------------------

    public static Test suite() {
        final TestSuite testSuite = new TestSuite();
        testSuite.addTest(new JahiaAclDAOTest("testSaveAcl"));
        testSuite.addTest(new JahiaAclDAOTest("testSaveWithParentAcl"));
        testSuite.addTest(new JahiaAclDAOTest("testUpdateAcl"));
        testSuite.addTest(new JahiaAclDAOTest("testRemoveAcl"));
        testSuite.addTest(new JahiaAclDAOTest("testFindAclById"));
        testSuite.addTest(new JahiaAclDAOTest("testGetAllAcl"));
        return testSuite;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaAclDAOTest(String s) {
        super(s);
        ctx = SpringContextSingleton.getInstance().getContext();
        applicationContext = (ConfigurableApplicationContext) ctx;
        setTransactionManager((PlatformTransactionManager) ctx.getBean("transactionManager"));
    }
// -------------------------- OTHER METHODS --------------------------

    /*public void setUp() throws Exception {
        super.setUp();

        assertNotNull(dao);
    }

    public void tearDown() throws Exception {
        TestHelper.cleanDatabase();
        super.tearDown();
    }*/

    /**
     * This implementation creates a transaction before test execution.
     * <p/>
     * Override {@link #onSetUpBeforeTransaction()} and/or
     * {@link #onSetUpInTransaction()} to add custom set-up behavior for
     * transactional execution. Alternatively, override this method for general
     * set-up behavior, calling <code>super.onSetUp()</code> as part of your
     * method implementation.
     *
     * @throws Exception simply let any exception propagate
     * @see #onTearDown()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        dao = (JahiaAclDAO) ctx.getBean("jahiaAclDAO");
    }

    public void testFindAclById() throws Exception {
        JahiaAcl acl = createAcl("guest:1");
        JahiaAclEntry entry;
        Integer id = acl.getId();
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(id);
        final Set<String> jahiaAclEntries = acl.getGroupEntries().keySet();
        assertFalse(jahiaAclEntries.isEmpty());
        for (String jahiaAclEntry : jahiaAclEntries) {
            entry = acl.getGroupEntries().get(jahiaAclEntry);
            log.debug(entry.toString());
            assertTrue(entry.getComp_id().getId().getId().equals(acl.getId()));
        }
    }

    private JahiaAcl createAcl(String aclEntries) {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(ACLInfo.INHERITANCE);
        JahiaAclEntry entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, ACLInfo.GROUP_TYPE_ENTRY, aclEntries), 0, 0);
        Map<String, JahiaAclEntry> groupEntries = new HashMap<String, JahiaAclEntry>(1);
        groupEntries.put(entry.getComp_id().getTarget(), entry);
        acl.setGroupEntries(groupEntries);
        dao.saveAcl(acl);
        assertTrue(acl.getId() != null);
        return acl;
    }

    public void testGetAllAcl() throws Exception {
        int size = dao.getAcls().size();
        createAcl("guest:1");
        createAcl("username:1");
        createAcl("test:1");
        List acls = dao.getAcls();
        assertFalse("Acls are empty", acls.isEmpty());
        assertEquals("Acls Size must equals 3",acls.size(),size+3);
    }

    public void testRemoveAcl() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(ACLInfo.INHERITANCE);
        JahiaAclEntry entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, ACLInfo.USER_TYPE_ENTRY, "guest:1"), 0, 0);
        Map<String, JahiaAclEntry> userEntries = new HashMap<String, JahiaAclEntry>(1);
        userEntries.put(entry.getComp_id().getTarget(), entry);
        acl.setUserEntries(userEntries);
        dao.saveAcl(acl);
        assertTrue(acl.getId() != null);
        assertTrue(acl.getUserEntries().size() > 0);
        dao.removeAcl(acl.getId());
        try {
            dao.findAclById(acl.getId());
            fail("object found in database");
        } catch (ObjectRetrievalFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected exception: " + e.getMessage());
            }
            assertTrue(e != null);
        }
    }

    public void testSaveAcl() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(ACLInfo.INHERITANCE);
        dao.saveAcl(acl);
        assertTrue(acl.getId() != null);
    }

    public void testSaveWithParentAcl() throws Exception {
        JahiaAcl aclParent = new JahiaAcl();
        aclParent.setParent(null);
        aclParent.setInheritance(ACLInfo.INHERITANCE);
        dao.saveAcl(aclParent);
        assertTrue(aclParent.getId() != null);
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(aclParent);
        acl.setInheritance(ACLInfo.INHERITANCE);
        dao.saveAcl(acl);
        assertTrue(acl.getId() != null);
        Integer id = acl.getId();
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(id);
        assertNotNull(acl.getParent());
        JahiaAcl subChild = new JahiaAcl();
        subChild.setParent(acl);
        subChild.setInheritance(ACLInfo.INHERITANCE);
        dao.saveAcl(subChild);
        assertTrue(subChild.getId() != null);
        Integer subChildId = subChild.getId();
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(subChildId);
        assertNotNull(acl.getParent().getParent());

    }

    public void testUpdateAcl() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(ACLInfo.INHERITANCE);
        JahiaAclEntry entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, ACLInfo.USER_TYPE_ENTRY, "guest:1"), 0, 0);
        Map<String, JahiaAclEntry> userEntries = new HashMap<String, JahiaAclEntry>(1);
        userEntries.put(entry.getComp_id().getTarget(), entry);
        acl.setUserEntries(userEntries);
        entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, ACLInfo.GROUP_TYPE_ENTRY, "guest:1"), 0, 0);
        Map<String, JahiaAclEntry> groupEntries = new HashMap<String, JahiaAclEntry>(1);
        groupEntries.put(entry.getComp_id().getTarget(), entry);
        acl.setGroupEntries(groupEntries);
        acl.setGroupEntry(new JahiaDBGroup(1, "test", "test:1", 1, null, null), new JahiaAclEntry(new JahiaAclEntryPK(acl, ACLInfo.GROUP_TYPE_ENTRY, "test:1"), 0, 0));
        dao.saveAcl(acl);
        assertTrue(acl.getId() != null);
        Integer id = acl.getId();
        entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, ACLInfo.USER_TYPE_ENTRY, "username:1"), 0, 0);
        acl.getUserEntries().put(entry.getComp_id().getTarget(), entry);
        acl.getUserEntries().get("guest:1").setEntryState(1);
        dao.updateAcl(acl);
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(id);
        assertNotNull(acl);
        assertNotNull(acl.getUserEntries());
        assertTrue(acl.getUserEntries().size() == 2);
        assertTrue(acl.getUserEntries().get("guest:1").getEntryState() == 1);
        assertNotNull(acl.getGroupEntries());
        assertTrue(acl.getGroupEntries().size() == 2);
        assertTrue(acl.getGroupEntries().get("test:1").getEntryState() == 0);
    }
}

