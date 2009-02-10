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

package org.jahia.engines.workflow;
//
//import org.jahia.content.ObjectKey;
//import org.jahia.services.workflow.ExternalWorkflow;
//import org.jahia.params.ProcessingContext;
//import org.jahia.registries.ServicesRegistry;
//
//import java.util.*;
//
///**
// * Created by IntelliJ IDEA.
// * User: toto
// * Date: May 26, 2003
// * Time: 2:40:46 PM
// * To change this template use Options | File Templates.
// */

//public class WorkflowHelper {
//
//    private Map entries;
//    private List allOptions = null;
//    private Set engines;
//    private List orderedKeys;
//
//    public WorkflowHelper(ProcessingContext jParams) {
//        this.entries = new HashMap();
//        this.engines = new HashSet();
//        this.orderedKeys = new ArrayList();
//    }
//
//    public int getSize() {
//        return entries.size();
//    }
//
//    public WorkflowHelperLanguageEntry getLanguageEntry(ObjectKey key, String languageCode) {
//        WorkflowHelperEntry subMap = (WorkflowHelperEntry) entries.get(key);
//        if (subMap == null) {
//            return null;
//        }
//
//        return subMap.getLanguageEntry(languageCode);
//    }
//
//    public WorkflowHelperEntry getEntry(ObjectKey key) {
//        return  (WorkflowHelperEntry) entries.get(key);
//    }
//
//    public Iterator getEntries() {
//        return entries.values().iterator();
//    }
//
//    public boolean isActive(ObjectKey key, String option) {
//        WorkflowHelperEntry entry = getEntry(key);
//        if (entry == null) {
//            return false;
//        }
//        for (Iterator iterator = entry.getLanguages(); iterator.hasNext();) {
//            String lang = (String) iterator.next();
//            if (isActive(key,lang,option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean isActive(ObjectKey key, String languageCode, String option) {
//        WorkflowHelperLanguageEntry entry  = getLanguageEntry(key, languageCode);
//        if (entry != null) {
//            Collection available = entry.getAvailableOptions();
//            for (Iterator iterator = available.iterator(); iterator.hasNext();) {
//                ActionEntry actionEntry = (ActionEntry) iterator.next();
//                if (actionEntry.getKey().equals(option)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    public boolean isActive(ObjectKey key, String languageCode, String option, String additional) {
//        WorkflowHelperLanguageEntry entry  = getLanguageEntry(key, languageCode);
//        if (entry != null) {
//            Collection available = entry.getAvailableOptions();
//            for (Iterator iterator = available.iterator(); iterator.hasNext();) {
//                ActionEntry actionEntry = (ActionEntry) iterator.next();
//                if (actionEntry.getKey().equals(option) && actionEntry.getAdditionalKey().equals(additional)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    public List getAllOptions() {
//        if (this.allOptions == null) {
//            allOptions = new ArrayList();
//            for (Iterator iterator = entries.values().iterator(); iterator.hasNext();) {
//                WorkflowHelperEntry submap = (WorkflowHelperEntry) iterator.next();
//                for (Iterator iterator2 = submap.getLanguageEntries(); iterator2.hasNext();) {
//                    WorkflowHelperLanguageEntry workflowHelperEntry = (WorkflowHelperLanguageEntry) iterator2.next();
//                    Collection options =  workflowHelperEntry.getAvailableOptions();
//                    allOptions.addAll(options);
//                }
//            }
//        }
//        return allOptions;
//    }
//
//    WorkflowHelperEntry addEntry(ObjectKey key, int mode, String workflowName, String processId, ProcessingContext jParams) {
//        ExternalWorkflow workflow = null;
//        if (workflowName != null) {
//            workflow = ServicesRegistry.getInstance().getWorkflowService().getExternalWorkflow(workflowName);
//            addEngine(workflow, jParams);
//        }
//        WorkflowHelperEntry value = new WorkflowHelperEntry(mode, workflowName, processId, workflow);
//        entries.put(key, value);
//        orderedKeys.add(key);
//        return value;
//    }
//
//    void addLanguageItem(ObjectKey key, String languageCode, List options) {
//        WorkflowHelperEntry entry = (WorkflowHelperEntry) entries.get(key);
//        if (entry == null) {
//            return;
//        }
//
//        entry.addLanguageEntry(languageCode, new WorkflowHelperLanguageEntry(options));
//        allOptions = null;
//    }
//
//    private void addEngine(ExternalWorkflow engine, ProcessingContext jParams) {
//        if (!engines.contains(engine)) {
//            engines.add(engine);
//        }
//    }
//
//    public List getOrderedKeys() {
//        return orderedKeys;
//    }
//
//    public void release() {
//        // do nothing
//    }
//
//    public String toString() {
//        return entries.toString();
//    }
//}
//
//
