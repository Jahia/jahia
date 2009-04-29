/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
