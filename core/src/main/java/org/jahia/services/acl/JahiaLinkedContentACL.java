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

///*
// * Copyright 2002-2008 Jahia Ltd
// *
// * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
// * Version 1.0 (the "License"), or (at your option) any later version; you may
// * not use this file except in compliance with the License. You should have
// * received a copy of the License along with this program; if not, you may obtain
// * a copy of the License at
// *
// *  http://www.jahia.org/license/
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */

 package org.jahia.services.acl;
//
//import org.jahia.exceptions.JahiaException;
//import org.jahia.content.ContentObject;
//import org.jahia.hibernate.model.JahiaAcl;
//import org.jahia.hibernate.model.JahiaAclEntry;
//
//import java.util.Map;
//import java.util.HashMap;
//import java.util.Iterator;
//
///**
// * Created by IntelliJ IDEA.
// * Date: 15 déc. 2005 - 17:17:59
// *
// * @author toto
// * @version $Id$
// */

//public class JahiaLinkedContentACL extends JahiaBaseACL {
//    private JahiaBaseACL pickedAcl;
//    private boolean isRoot;
//
//    public JahiaLinkedContentACL() throws JahiaException {
//    }
//
//    public JahiaLinkedContentACL(int aclID, ContentObject object) throws ACLNotFoundException, JahiaException {
//        super(aclID);
//        ContentObject pickedObject = object.getPickedObject();
//        if(pickedObject != null) {
//            this.pickedAcl = pickedObject.getACL();
//            ContentObject root = object;
//            while (true) {
//                ContentObject parent = root.getParent(null, null, null);
//                if (parent == null || parent.getPickedObject() == null) {
//                    break;
//                }
//                root = parent;
//            }
//
//            ContentObject rootPickedObject = root.getPickedObject();
//            if(rootPickedObject != null) {
//                isRoot = pickedAcl.getACL().getId().equals(rootPickedObject.getACL().getACL().getId());
//            }
//        }
//    }
//
//    public JahiaAcl getACL() {
//        return new JahiaLinkedContentAcl(mACL, pickedAcl.getACL());
//    }
//
//    class JahiaLinkedContentAcl extends JahiaAcl {
//        private JahiaAcl pickerAcl;
//        private JahiaAcl pickedAcl;
//
//        public JahiaLinkedContentAcl(JahiaAcl pickerAcl, JahiaAcl pickedAcl) {
//            this.pickerAcl = pickerAcl;
//            this.pickedAcl = pickedAcl;
//
//            setParent(pickerAcl.getParent());
//            setInheritance(pickerAcl.getInheritance());
//            setId(pickerAcl.getId());
//
//            if (pickedAcl.getInheritance().intValue() == 0 && isRoot) {
//                // root picker -> get all recursed acl from the picker object (from root picked to home page)
//                setUserEntries(merge(pickedAcl.getRecursedUserEntries(), pickerAcl.getUserEntries()));
//                setGroupEntries(merge(pickedAcl.getRecursedGroupEntries(), pickerAcl.getGroupEntries()));
//            } else {
//                setInheritance(new Integer(1));
//
//                setUserEntries(mergeWithBrokenInheritance(pickedAcl.getRecursedUserEntries(), pickerAcl.getRecursedUserEntries()));
//                setGroupEntries(mergeWithBrokenInheritance(pickedAcl.getRecursedGroupEntries(), pickerAcl.getRecursedGroupEntries()));
//            }
//        }
//
//        private Map merge(Map pickedEntries, Map pickerEntries) {
//            Map m = new HashMap(pickerEntries);
//            for (Iterator iterator = pickedEntries.keySet().iterator(); iterator.hasNext();) {
//                String n = (String) iterator.next();
//                if (!pickerEntries.containsKey(n)) {
//                    JahiaAclEntry pickedEntry = (JahiaAclEntry) pickedEntries.get(n);
//                    try {
//                        JahiaAclEntry mergeEntry = (JahiaAclEntry) pickedEntry.clone();
//                        mergeEntry.setPermission(JahiaBaseACL.WRITE_RIGHTS, JahiaAclEntry.ACL_NEUTRAL);
//                        mergeEntry.setPermission(JahiaBaseACL.ADMIN_RIGHTS, JahiaAclEntry.ACL_NEUTRAL);
//                        m.put(n,mergeEntry);
//                    } catch (CloneNotSupportedException e1) {
//                    }
//                }
//            }
//            return m;
//        }
//
//        private Map mergeWithBrokenInheritance(Map pickedEntries, Map recursedPickerEntries) {
//            Map m = merge(pickedEntries, new HashMap());
//
//            for (Iterator iterator = recursedPickerEntries.keySet().iterator(); iterator.hasNext();) {
//                String n = (String) iterator.next();
//                if (m.containsKey(n)) {
//                    JahiaAclEntry mergeEntry = (JahiaAclEntry) m.get(n);
////                    if (mergeEntry.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_YES) {
//                        JahiaAclEntry pickerEntry = (JahiaAclEntry) recursedPickerEntries.get(n);
//                        mergeEntry.setPermission(JahiaBaseACL.WRITE_RIGHTS, pickerEntry.getPermission(JahiaBaseACL.WRITE_RIGHTS));
//                        mergeEntry.setPermission(JahiaBaseACL.ADMIN_RIGHTS, pickerEntry.getPermission(JahiaBaseACL.ADMIN_RIGHTS));
////                    }
//                } else {
//                    JahiaAclEntry pickerEntry = (JahiaAclEntry) recursedPickerEntries.get(n);
//                    try {
//                        JahiaAclEntry mergeEntry = (JahiaAclEntry) pickerEntry.clone();
//                        mergeEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_NEUTRAL);
//                        mergeEntry.setPermission(JahiaBaseACL.WRITE_RIGHTS, pickerEntry.getPermission(JahiaBaseACL.WRITE_RIGHTS));
//                        mergeEntry.setPermission(JahiaBaseACL.ADMIN_RIGHTS, pickerEntry.getPermission(JahiaBaseACL.ADMIN_RIGHTS));
//                    } catch (CloneNotSupportedException e) {
//                    }
//                }
//            }
//
//            return m;
//        }
//
//        public JahiaAcl getParent() {
//            if (isRoot) {
//                return pickerAcl.getParent();
//            } else {
//                return new JahiaLinkedContentAcl(pickerAcl, pickedAcl.getParent());
//            }
//        }
//
//        protected String getKey() {
//            return getId().toString() + "/" + pickedAcl.getId().toString();
//        }
//
//    }
//
//
//}
///**
// *$Log $
// */