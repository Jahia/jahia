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
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclEntryPK;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.usermanager.JahiaDBGroup;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import java.util.*;

/**
 * JahiaAclDAO Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12/27/2004</pre>
 */
public class JahiaAclDAOTest extends TestCase {
// ------------------------------ FIELDS ------------------------------

    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());
    private JahiaAclDAO dao = null;

// -------------------------- STATIC METHODS --------------------------

    public static Test suite() {
        return new TestSuite(JahiaAclDAOTest.class);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaAclDAOTest() {
        String[] paths = {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaAclDAO) ctx.getBean("jahiaAclDAO");
        assertNotNull(dao);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFindAclById() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        JahiaAclEntry entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, new Integer(ACLInfo.GROUP_TYPE_ENTRY),"guest:1"),0,0);
        Map groupEntries = new HashMap(1);
        groupEntries.put(entry.getComp_id().getTarget(),entry);
        acl.setGroupEntries(groupEntries);
        dao.saveAcl(acl);
        assertTrue(acl.getId()!=null);
        Integer id = acl.getId();
        acl =null;
        assertNull(acl);
        acl = dao.findAclById(id);
        final Set jahiaAclEntries = acl.getGroupEntries().keySet();
        assertFalse(jahiaAclEntries.isEmpty());
        final Iterator iterator = jahiaAclEntries.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            entry = (JahiaAclEntry) acl.getGroupEntries().get(key);
            log.debug(entry.toString());
            assertTrue(entry.getComp_id().getId().getId().equals(acl.getId()));
        }
    }

    public void testGetAllAcl() throws Exception {
        long start = System.currentTimeMillis();
        List acls = dao.getAcls();
        assertFalse("Acls are empty",acls.isEmpty());
        log.debug("Acls size = "+acls.size() + " in "+(System.currentTimeMillis()-start)+" ms");
        start = System.currentTimeMillis();
        acls = dao.getAcls();
        assertFalse("Acls are empty",acls.isEmpty());
        log.debug("Acls size = "+acls.size() + " in "+(System.currentTimeMillis()-start)+" ms");
    }

    public void testRemoveAcl() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        JahiaAclEntry entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, new Integer(ACLInfo.USER_TYPE_ENTRY),"guest:1"),0,0);
        Map userEntries = new HashMap(1);
        userEntries.put(entry.getComp_id().getTarget(),entry);
        acl.setUserEntries(userEntries);
        dao.saveAcl(acl);
        assertTrue(acl.getId()!=null);
        assertTrue(acl.getUserEntries().size()>0);
        dao.removeAcl(acl.getId());
        try {
            dao.findAclById(acl.getId());
            fail("object found in database");
        } catch(ObjectRetrievalFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected exception: " + e.getMessage());
            }
            assertTrue(e != null);
        }
    }

    public void testSaveAcl() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        dao.saveAcl(acl);
        assertTrue(acl.getId()!=null);
    }

    public void testSaveWithParentAcl() throws Exception {
        JahiaAcl aclParent = new JahiaAcl();
        aclParent.setParent(null);
        aclParent.setInheritance(new Integer(ACLInfo.INHERITANCE));
        dao.saveAcl(aclParent);
        assertTrue(aclParent.getId()!=null);
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(aclParent);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        dao.saveAcl(acl);
        assertTrue(acl.getId()!=null);
        Integer id = acl.getId();
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(id);
        assertNotNull(acl.getParent());
        JahiaAcl subChild = new JahiaAcl();
        subChild.setParent(acl);
        subChild.setInheritance(new Integer(ACLInfo.INHERITANCE));
        dao.saveAcl(subChild);
        assertTrue(subChild.getId()!=null);
        Integer subChildId = subChild.getId();
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(subChildId);
        assertNotNull(acl.getParent().getParent());

    }

    public void testUpdateAcl() throws Exception {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(null);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        JahiaAclEntry entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, new Integer(ACLInfo.USER_TYPE_ENTRY),"guest:1"),0,0);
        Map userEntries = new HashMap(1);
        userEntries.put(entry.getComp_id().getTarget(),entry);
        acl.setUserEntries(userEntries);
        entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, new Integer(ACLInfo.GROUP_TYPE_ENTRY),"guest:1"),0,0);
        Map groupEntries = new HashMap(1);
        groupEntries.put(entry.getComp_id().getTarget(),entry);
        acl.setGroupEntries(groupEntries);
        acl.setGroupEntry(new JahiaDBGroup(1,"test","test:1",1,null,null), new JahiaAclEntry(new JahiaAclEntryPK(acl,new Integer(ACLInfo.GROUP_TYPE_ENTRY), "test:1"),0,0 ));
        dao.saveAcl(acl);
        assertTrue(acl.getId()!=null);
        Integer id = acl.getId();
        entry = new JahiaAclEntry(new JahiaAclEntryPK(acl, new Integer(ACLInfo.USER_TYPE_ENTRY),"cedric:1"),0,0);
        acl.getUserEntries().put(entry.getComp_id().getTarget(),entry);
        ((JahiaAclEntry)acl.getUserEntries().get("guest:1")).setEntryState(1);
        dao.updateAcl(acl);
        acl = null;
        assertNull(acl);
        acl = dao.findAclById(id);
        assertNotNull(acl);
        assertNotNull(acl.getUserEntries());
        assertTrue(acl.getUserEntries().size()==2);
        assertTrue(((JahiaAclEntry)acl.getUserEntries().get("guest:1")).getEntryState()==1);
        assertNotNull(acl.getGroupEntries());
        assertTrue(acl.getGroupEntries().size()==2);
        assertTrue(((JahiaAclEntry)acl.getGroupEntries().get("test:1")).getEntryState()==0);
    }

    public void testSchemaExport(){
        Map beansOfType = ctx.getBeansOfType(LocalSessionFactoryBean.class, true, true);
        LocalSessionFactoryBean sessionFactoryBean = (LocalSessionFactoryBean) beansOfType.values().iterator().next();
        Configuration configuration = sessionFactoryBean.getConfiguration();
        SchemaExport schemaExport = new SchemaExport(configuration);
        schemaExport.setOutputFile("c:/temp/hibernateExport.sql");
        schemaExport.setDelimiter(";");
        schemaExport.create(true,false);
    }
}

