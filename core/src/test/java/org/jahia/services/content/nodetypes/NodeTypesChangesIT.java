
/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Files;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.utils.StringOutputStream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * These tests are related to the story https://jira.jahia.org/browse/BACKLOG-6892
 * it creates a nodetype, content from this type then do modification on the nodetype and check if the node is still accessible.
 */
public class NodeTypesChangesIT extends AbstractJUnitTest {


    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTypesChangesIT.class);
    String testNodeIdentifier;
    private static List<Result> overallResults = new ArrayList<>();
    
    public enum Operation {
        GET_NODE, GET_CHILD_NODES, EXPORT_NODE, COPY_NODE, CHECK_COPIED_CHILD_NODES, READ_PROPERTIES, READ_STABLE_PROPERTY_VALUE,
        READ_STRING_PROPERTY_VALUE, READ_MULTIPLE_PROPERTY_VALUE, READ_I18N_PROPERTY_VALUE, READ_DECIMAL_PROPERTY_VALUE,
        READ_INTEGER_PROPERTY_VALUE, READ_REFERENCE_PROPERTY_VALUE, READ_CHILD_PROPERTIES, EDIT_PROPERTY, EDIT_CHANGED_PROPERTY,
        REMOVE_PROPERTY, REMOVE_CHANGED_PROPERTY, ADD_CHILD_NODE, RESTORE_VERSION, READ_RESTORED_PROPERTY_VALUE, READ_CHANGED_RESTORED_PROPERTY_VALUE,
        READ_RESTORE_CHILD_NODES, REMOVE_NODE, REMOVE_CHILD_NODE, IMPORT_NODE, READ_IMPORTED_PROPERTY_VALUE,
        READ_CHANGED_IMPORTED_PROPERTY_VALUE, READ_IMPORTED_CHILD_NODES
    }

    @Before
    public void setUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);
        JCRNodeWrapper testNode = session.getNode("/").addNode("nodeTypeChanges", "nt:unstructured");
        testNode.addMixin("mix:referenceable");
        testNodeIdentifier = testNode.getIdentifier();
    }

    @Test
    public void shouldAllowAllOperationsAfterRemovingNodetype() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        removeNodeType();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "remove nodetype from definition", expectedResults, null, "");  
    }
    
    @Test
    public void shouldAllowAllOperationsAfterHidingPreviousMandatoryProperty() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        hidePreviousMandatoryProperty();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true)
                .put(Operation.READ_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "hide previous mandatory property", expectedResults, "test_mandatory", null);  
    }
    
    @Test
    public void shouldFailOperationsOnlyOnRemovedProperties() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        removeProperties();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, false).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, false)
                .put(Operation.READ_I18N_PROPERTY_VALUE, false).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();

        checkOperations(versionInfo, "remove property from nodetype", expectedResults, "test", null);
    }
    

    @Test
    public void shouldFailReadOperationsOnPropertySwitchedToi18n() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchToi18n();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, false).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();

        checkOperations(versionInfo, "switch a property from non-i18n to i18n", expectedResults, "test", null);  
    }

    @Test
    public void shouldFailReadOperationsOnPropertySwitchedFromi18n() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchFromI18n();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, false).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from i18n to non-i18n", expectedResults, "test_i18n", null);
    }

    @Test
    public void shouldFailWriteOperationsOnPropertySwitchedToMultiple() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchToMultiple();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from single to multiple", expectedResults, "test", null);
    }       
    
    @Test
    public void shouldFailWriteOperationsOnPropertySwitchedFromMultiple() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchFromMultiple();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from multiple to single", expectedResults, "test_multiple", null);
    }
    
    @Test
    public void shouldFailWriteAndRestoredReadOperationsOnPropertySwitchedToString() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchToString();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from long to string", expectedResults, "integer", null);
    }
    
    @Test
    public void shouldFailWriteAndRestoredReadOperationsOnPropertySwitchedFromString() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchFromString();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from string to long", expectedResults, "integerAsString", null);
    }
    
    @Test
    public void shouldFailWriteAndRestoredReadOperationsOnPropertySwitchedDoubleToDecimal() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchDoubleToDecimal();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from double to decimal", expectedResults, "decimalNumber", null);
    }     

    @Test
    public void shouldFailWriteAndRestoredReadOperationsOnPropertySwitchedLongToDecimal() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        switchLongToDecimal();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, false).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, false).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).build();        

        checkOperations(versionInfo, "switch a property from long to decimal", expectedResults, "integer", null);
    }     
    
    
    @Test
    public void shouldFailImportAndWriteOperationsIfValueForNewMandatoryPropertyIsMissing() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        addPropertyWithMandatoryConstraint();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, false).put(Operation.REMOVE_PROPERTY, false).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, false)
                .build();
        
        checkOperations(versionInfo, "add new property definition with mandatory constraint", expectedResults, null, null);
    }    
    
    @Test
    public void shouldFailImportAndRestoredReadOperationsIfConstraintIsNoLongerValid() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        addRangeConstraintToNumericProperty();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true).put(Operation.REMOVE_PROPERTY, true)
                .put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false)
                .put(Operation.REMOVE_NODE, true).put(Operation.IMPORT_NODE, false).build();
        
        checkOperations(versionInfo, "add range constraint to existing property definition", expectedResults, "integer", null);
    }        
    
    @Test
    public void shouldAllowAllOperationsAfterMovingDefinitionsToSupertype() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        moveDefinitionsToSupertype();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.RESTORE_VERSION, true)
                .put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.REMOVE_NODE, true).put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true)
                .put(Operation.READ_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "move definitions to supertype", expectedResults, "test", null);  
    }
    
    @Test
    public void shouldFailChildNodeWriteOperationsAfterChangingToUnrelatedAllowedNodeType() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        changeToUnrelatedAllowedChildNodeType();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, false).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_IMPORTED_CHILD_NODES, false).build();
        
        checkOperations(versionInfo, "change allowed child node type", expectedResults, "test", "test:bigText");  
    }
    
    @Test
    public void shouldFailChildNodeWriteOperationsAfterAddingUnrelatedAllowedNodeType() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        addUnrelatedAllowedChildNodeType();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, false).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_IMPORTED_CHILD_NODES, false).build();
        
        checkOperations(versionInfo, "add allowed child node type", expectedResults, "test", "test:bigText");  
    }    
    
    @Test
    public void shouldAllowAllOperationsAfterRemovingAllowedNodeType() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        removeAllowedChildNodeType();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "remove allowed child node type", expectedResults, "test", "test:text");  
    }       
    
    @Test
    public void shouldFailChildNodeWriteOperationsAfterChangingToAllowedNodeTypeUsingPreviousAsSupertype() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        changeAllowedChildNodeTypeUsingPreviousAsSupertype();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, false).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_IMPORTED_CHILD_NODES, false).build();
        
        checkOperations(versionInfo, "change allowed child node type using previous as supertype", expectedResults, "test", "test:bigText");  
    }
    
    @Test
    public void shouldAllowAllOperationsAfterChangingToAllowedNodeTypeUsingSupertype() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        changeAllowedChildNodeTypeUsingSupertype();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "change allowed child node type using supertype", expectedResults, "test", "test:superText");  
    }
    
    @Test
    public void shouldAllowAllOperationsAfterMakingAllowedNodeTypeOrderable() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        makeChildNodeTypeOrderable();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, true).put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "make allowed child node type orderable", expectedResults, "test", "test:ultimativeText");  
    }

    @Test
    public void shouldFailRestoreReferenceOperationsAfterChangingReferenceType() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        changeReferenceType();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, true).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, false).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, "change allowed reference type", expectedResults, "reference", null);  
    }    
    
    private VersionInfo init() throws IOException, ParseException, RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);        
        // create definition file
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +        
                "[test:ultimativeText] > test:text, nt:unstructured\n" +                
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +                
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - integerAsString (string)\n" +                
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)";
        
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // create node from nodetype
        JCRNodeWrapper testNode = session.getNodeByIdentifier(testNodeIdentifier);
        JCRNodeWrapper node = testNode.addNode("test", "test:test");
        node.setProperty("test", "test");
        node.setProperty("test_multiple",new String[]{"test", "test", "test"});
        node.setProperty("test_i18n", "test");
        node.setProperty("test_mandatory", "test");        
        node.setProperty("stable", "test");        
        node.setProperty("decimalNumber", (double)2.5);
        node.setProperty("integer", 10);
        node.setProperty("integerAsString", "10");        
        node.setProperty("reference", testNode);        
        session.save();
        
        JCRNodeWrapper subNode = node.addNode("text1", "test:ultimativeText");
        subNode.setProperty("text", "text1");
        session.save();
        subNode = node.addNode("text2", "test:ultimativeText");
        subNode.setProperty("text", "text2");
        session.save();
        subNode = node.addNode("text3", "test:ultimativeText");
        subNode.setProperty("text", "text3");
        session.save();        
        
        // create a version
        Version v = session.getWorkspace().getVersionManager().checkin(node.getPath());
        session.getWorkspace().getVersionManager().checkout(node.getPath());
        node.setProperty("test", "new value");
        node.setProperty("test_multiple",new String[]{"new value", "new value", "new value"});
        node.setProperty("test_i18n", "new value");
        node.setProperty("test_mandatory", "new value");
        node.setProperty("stable", "new value");       
        node.setProperty("decimalNumber", (double)12.5);
        node.setProperty("integer", 20);        
        node.setProperty("integerAsString", "20");
        node.setProperty("reference", subNode);        
        session.save();
        v = session.getWorkspace().getVersionManager().checkin(node.getPath());
        String versionName = v.getName();
        session.getWorkspace().getVersionManager().checkout(node.getPath());

        // restore origin value
        node.setProperty("test", "test");
        node.setProperty("test_multiple",new String[]{"test", "test", "test"});
        node.setProperty("test_i18n", "test");
        node.setProperty("stable", "test");        
        node.setProperty("test_mandatory", "test");
        node.setProperty("decimalNumber", (double)2.5);
        node.setProperty("integer", 10);        
        node.setProperty("integerAsString", "10");
        node.setProperty("reference", testNode);
        session.save();

        // cleanup
        FileUtils.deleteQuietly(file);
        return new VersionInfo(node.getPath(), versionName);
    }


    private void removeProperties() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" + 
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +   
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void removeNodeType() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n");
    }
    
    private void hidePreviousMandatoryProperty() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +       
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +              
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +        
                " - test_mandatory (string) hidden" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void switchToi18n() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +       
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string) i18n\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void switchToMultiple() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +       
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string) multiple\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +              
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }
    
    private void switchToString() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (string)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }
    
    private void switchDoubleToDecimal() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (decimal)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }
    
    private void switchLongToDecimal() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (decimal)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }    
    
    private void switchFromString() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - integerAsString (long)\n" +                
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void switchFromMultiple() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string)\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void switchFromI18n() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string)\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void addPropertyWithMandatoryConstraint() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string) mandatory\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - newTestMandatory (string) mandatory\n" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }
    
    private void addRangeConstraintToNumericProperty() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long) < '[100,500]'\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }

    private void moveDefinitionsToSupertype() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:supertype] > nt:base, mix:versionable\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)" +
                "[test:test] > test:supertype\n" +
                " - stable (string)\n");
    }
    
    private void changeToUnrelatedAllowedChildNodeType() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:bigText] > test:superText\n" +
                " - text (string, richtext) i18n\n" +                
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:bigText)");
    }
    
    private void addUnrelatedAllowedChildNodeType() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:bigText] > test:ultimativeText\n" +
                " - text (string, richtext) i18n\n" +                
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText,test:bigText)");
    }    
    
    private void removeAllowedChildNodeType() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text)");
    }       
    
    private void changeAllowedChildNodeTypeUsingPreviousAsSupertype() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:bigText] > test:ultimativeText\n" +
                " - text (string, richtext) i18n\n" +                
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:bigText,test:ultimativeText)");
    }    
    
    private void changeAllowedChildNodeTypeUsingSupertype() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:bigText] > test:text\n" +
                " - text (string, richtext) i18n\n" +                
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:superText)");
    }        
    
    private void makeChildNodeTypeOrderable() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text orderable\n" +                
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (reference) < nt:unstructured\n" +
                " + * (test:text,test:ultimativeText)");
    }       
    
    private void changeReferenceType() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured\n" +           
                "[test:test] > nt:base, mix:versionable\n" +
                " - stable (string)\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test_mandatory (string) mandatory" +                
                " - decimalNumber (double)\n" +
                " - integer (long)\n" +
                " - reference (weakreference) < test:ultimativeText\n" +
                " + * (test:text,test:ultimativeText)");
    }        
    
    private void changeDefinition(String definition) throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();

        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void checkOperations(final VersionInfo versionInfo, final String modification, Map<Operation, Boolean> expectedResults, final String changedProperty, final String childNodesType) {
        final String unchangedProperty = "stable";
        final String nodePath = versionInfo.getTestNodePath();
        final String versionName = versionInfo.getVersionName();
        
        List<Result> resultsForModification = new ArrayList<>();
        // get node
        resultsForModification.addAll(doOperation(Operation.GET_NODE, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                session.getNode(nodePath);
                return Collections.emptyList();
            }
        }));
        
        if (childNodesType != null) {
            // get child nodes
            resultsForModification.addAll(doOperation(Operation.GET_CHILD_NODES, modification, nodePath, new CallBack() {
                @Override
                public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                    JCRNodeIteratorWrapper it = session.getNode(nodePath).getNodes();
                    int i = 0;
                    while (it.hasNext()) {
                        it.nextNode();
                        i++;
                    }
                    if (i < 3) {
                        throw new ItemNotFoundException("Child node is missing");
                    }
                    return Collections.emptyList();
                }
            }));
        }
        
        //export
        final OutputStream exportOutputStream = new StringOutputStream();
        final List<Result> exportNodeResults = doOperation(Operation.EXPORT_NODE, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                HashMap<String, Object> params = new HashMap<>();
                ImportExportBaseService.getInstance().exportNode(session.getNode(nodePath), session.getNode("/"), exportOutputStream, params);
                return Collections.emptyList();                
            }
        });
        resultsForModification.addAll(exportNodeResults);

        // copy node
        resultsForModification.addAll(doOperation(Operation.COPY_NODE, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
                JCRNodeWrapper nread = session.getNode(nodePath);
                if (!nread.copy(session.getNodeByIdentifier(testNodeIdentifier), "test-copy", false)) {
                    throw new RepositoryException("copy was not successful");
                } else if (childNodesType != null) {
                    results.add(new Result(Operation.COPY_NODE, modification, true));
                    try {
                        JCRNodeIteratorWrapper it = session.getNodeByIdentifier(testNodeIdentifier).getNode("test-copy").getNodes();
                        int i = 0;
                        while (it.hasNext()) {
                            it.nextNode();
                            i++;
                        }
                        if (i < 3) {
                            results.add(
                                    new Result(Operation.CHECK_COPIED_CHILD_NODES, modification, false, "Copied child nodes are missing"));
                        } else {
                            results.add(new Result(Operation.CHECK_COPIED_CHILD_NODES, modification, true));
                        }
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.CHECK_COPIED_CHILD_NODES, modification, false, ex.toString()));
                        logger.info("unable to perform " + Operation.CHECK_COPIED_CHILD_NODES + " after " +  modification, ex);
                    }
                }
                return results;
            }
        }));

        // read property
        resultsForModification.addAll(doOperation(Operation.READ_PROPERTIES, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
                JCRNodeWrapper nread = session.getNode(nodePath);
                try {
                    String s = nread.getProperty(unchangedProperty).getValue().getString();

                    results.add(new Result(Operation.READ_STABLE_PROPERTY_VALUE, modification, "test".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_STABLE_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_STABLE_PROPERTY_VALUE + " after " +  modification, ex); 
                }
                try {
                    String s = nread.getProperty("test").getValue().getString();

                    results.add(new Result(Operation.READ_STRING_PROPERTY_VALUE, modification, "test".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_STRING_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_STRING_PROPERTY_VALUE + " after " +  modification, ex);
                }
                try {
                    boolean isMultiple = nread.getProperty("test_multiple").getDefinition().isMultiple();
                    boolean b = true;
                    if (isMultiple) {
                        for (Value v : nread.getProperty("test_multiple").getValues()) {
                            b &= "test".equals(v.getString());
                        }
                    } else {
                        String s = nread.getProperty("test").getValue().getString();
                        b = s.equals("test");
                    }
                    results.add(new Result(Operation.READ_MULTIPLE_PROPERTY_VALUE, modification, b));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_MULTIPLE_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_MULTIPLE_PROPERTY_VALUE + " after " +  modification, ex);
                }
                try {
                    String s = nread.getProperty("test_i18n").getValue().getString();
                    results.add(new Result(Operation.READ_I18N_PROPERTY_VALUE, modification, "test".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_I18N_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_I18N_PROPERTY_VALUE + " after " +  modification, ex);
                }
                try {
                    String s = nread.getProperty("decimalNumber").getValue().getString();

                    results.add(new Result(Operation.READ_DECIMAL_PROPERTY_VALUE, modification, "2.5".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_DECIMAL_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_DECIMAL_PROPERTY_VALUE + " after " +  modification, ex);
                }
                try {
                    String s = nread.getProperty("integer").getValue().getString();
                    results.add(new Result(Operation.READ_INTEGER_PROPERTY_VALUE, modification, "10".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_INTEGER_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_INTEGER_PROPERTY_VALUE + " after " +  modification, ex);
                }
                try {
                    Node node = nread.getProperty("reference").getValue().getNode();
                    results.add(new Result(Operation.READ_REFERENCE_PROPERTY_VALUE, modification, testNodeIdentifier.equals(node.getIdentifier())));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_REFERENCE_PROPERTY_VALUE, modification, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_REFERENCE_PROPERTY_VALUE + " after " +  modification, ex);
                }                
                if (childNodesType != null) {
                    try {
                        JCRNodeIteratorWrapper it = nread.getNodes();
                        int i = 0;
                        boolean expectedTextFound = true;
                        while (it.hasNext()) {
                            Node childNode = it.nextNode();
                            i++;
                            expectedTextFound = expectedTextFound && childNode.getName().equals(childNode.getProperty("text").getValue().getString());
                        }
                        if (i == 3 && expectedTextFound) {
                            results.add(new Result(Operation.READ_CHILD_PROPERTIES, modification, true));
                        } else {
                            results.add(new Result(Operation.READ_CHILD_PROPERTIES, modification, false, "Expected text not found in childnodes"));
                        }
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.READ_CHILD_PROPERTIES, modification, false, ex.toString()));
                        logger.info("unable to perform " + Operation.READ_CHILD_PROPERTIES + " after " +  modification, ex);
                    }    
                }
                return results;
            }
        }));

        // edit property
        resultsForModification.addAll(doOperation(Operation.EDIT_PROPERTY, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
                try {
                    editProperty(session, unchangedProperty);
                    results.add(new Result(Operation.EDIT_PROPERTY, modification, true));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.EDIT_PROPERTY, modification, false, ex.toString()));
                    session.refresh(false);
                    logger.info("unable to perform " + Operation.EDIT_PROPERTY + " after " +  modification, ex);
                }
                if (StringUtils.isNotEmpty(changedProperty)) {
                    try {
                        editProperty(session, changedProperty);
                        results.add(new Result(Operation.EDIT_CHANGED_PROPERTY, modification, true));
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.EDIT_CHANGED_PROPERTY, modification, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.EDIT_CHANGED_PROPERTY + " after " +  modification, ex);                        
                    }
                }
                return results;
            }
            private void editProperty(final JCRSessionWrapper session, final String propertyToTest) throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                boolean isMultiple = nread.hasProperty(propertyToTest) ? nread.getProperty(propertyToTest).getDefinition().isMultiple()
                        : false;
                if (isMultiple) {
                    nread.setProperty(propertyToTest, new String[] {"100"});
                } else if ("reference".equals(propertyToTest)) {
                    nread.setProperty(propertyToTest, nread.getNodes().iterator().next());                    
                } else  {
                    nread.setProperty(propertyToTest, "100");
                }
                session.save();
            }
        }));

        // remove property
        resultsForModification.addAll(doOperation(Operation.REMOVE_PROPERTY, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
            
                JCRNodeWrapper nread = session.getNode(nodePath);
                try {
                    nread.getProperty(unchangedProperty).remove();
                    session.save();
                    results.add(new Result(Operation.REMOVE_PROPERTY, modification, true));                    
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.REMOVE_PROPERTY, modification, false, ex.toString()));
                    session.refresh(false);
                    logger.info("unable to perform " + Operation.REMOVE_PROPERTY + " after " +  modification, ex);
                }
                if (StringUtils.isNotEmpty(changedProperty)) {
                    try {
                        nread.getProperty(changedProperty).remove();
                        session.save();
                        results.add(new Result(Operation.REMOVE_CHANGED_PROPERTY, modification, true));
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.REMOVE_CHANGED_PROPERTY, modification, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.REMOVE_CHANGED_PROPERTY + " after " +  modification, ex);
                    }
                }
                return results;
            }
        }));

        if (StringUtils.isNotBlank(childNodesType)) {
            //add child node
            // remove child node
            resultsForModification.addAll(doOperation(Operation.ADD_CHILD_NODE, modification, nodePath, new CallBack() {
                @Override
                public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                    JCRNodeWrapper nread = session.getNode(nodePath);
                    JCRNodeWrapper addedNode = nread.addNode("addedChildNode", childNodesType);
                    addedNode.setProperty("text", "added text");
                    session.save();
                    return Collections.emptyList();
                }
            }));            
        }
        
        //restore version
        resultsForModification.addAll(doOperation(Operation.RESTORE_VERSION, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();                
                session.getWorkspace().getVersionManager().checkout(nodePath);
                session.getWorkspace().getVersionManager().restore(nodePath, versionName, true);
                session.save();
                results.add(new Result(Operation.RESTORE_VERSION, modification, true));
                JCRNodeWrapper nread = session.getNode(nodePath);
                try {
                    JCRPropertyWrapper property = nread.getProperty(unchangedProperty);
                    String s = property.getDefinition().isMultiple() ? property.getValues()[0].getString()
                            : property.getValue().getString();
                    results.add(new Result(Operation.READ_RESTORED_PROPERTY_VALUE, modification, "new value".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_RESTORED_PROPERTY_VALUE, modification, false, ex.toString()));
                    session.refresh(false);
                    logger.info("unable to perform " + Operation.READ_RESTORED_PROPERTY_VALUE + " after " +  modification, ex);
                }
                if (StringUtils.isNotEmpty(changedProperty)) {
                    try {
                        JCRPropertyWrapper property = nread.getProperty(changedProperty);
                        if ("reference".equals(changedProperty)) {
                            results.add(new Result(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, modification, testNodeIdentifier.equals(property.getNode().getIdentifier())));                            
                        } else {
                            String s = property.getDefinition().isMultiple() ? property.getValues()[0].getString()
                                    : property.getValue().getString();
                            results.add(new Result(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, modification, "new value".equals(s)));
                        }
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, modification, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE + " after " +  modification, ex);
                    }
                }
                return results;
            }
        }));

        if (childNodesType != null) {
            // remove child node
            resultsForModification.addAll(doOperation(Operation.REMOVE_CHILD_NODE, modification, nodePath, new CallBack() {
                @Override
                public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                    session.getWorkspace().getVersionManager().checkout(nodePath);
                    JCRNodeWrapper nread = session.getNode(nodePath).getNodes().iterator().next();
                    nread.remove();
                    session.save();
                    return Collections.emptyList();
                }
            }));
        }        
        
        // remove node
        resultsForModification.addAll(doOperation(Operation.REMOVE_NODE, modification, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                nread.remove();
                session.save();
                return Collections.emptyList();
            }
        }));
        
        // import node
        if (exportNodeResults.get(0).result) {
            resultsForModification.addAll(doOperation(Operation.IMPORT_NODE, modification, nodePath, new CallBack() {
                @Override
                public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                    List<Result> results = new ArrayList<>();
                    ByteArrayInputStream stream = new ByteArrayInputStream(exportOutputStream.toString().getBytes(StandardCharsets.UTF_8));
                    JCRNodeWrapper importFolder = session.getNode("/").addNode("nodeTypesImported", "nt:unstructured");
                    session.save();
                    ImportExportBaseService.getInstance().importXML(importFolder.getPath(), stream,
                            DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                    JCRNodeWrapper nread = importFolder.getNode("nodeTypeChanges/test");                    
                    results.add(new Result(Operation.IMPORT_NODE, modification, true));
                    try {
                        String s = nread.getProperty(unchangedProperty).getValue().getString();
                        results.add(new Result(Operation.READ_IMPORTED_PROPERTY_VALUE, modification, "test".equals(s)));
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.READ_IMPORTED_PROPERTY_VALUE, modification, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.READ_IMPORTED_PROPERTY_VALUE + " after " +  modification, ex);
                    }
                    if (StringUtils.isNotEmpty(changedProperty)) {
                        try {
                            if ("reference".equals(changedProperty)) {
                                results.add(new Result(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, modification,
                                        testNodeIdentifier.equals(nread.getProperty(changedProperty).getNode().getIdentifier())));
                            } else {
                                String s = nread.getProperty(changedProperty).getValue().getString();
                                results.add(new Result(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, modification, "test".equals(s)));
                            }
                        } catch (RepositoryException ex) {
                            results.add(new Result(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, modification, false, ex.toString()));
                            session.refresh(false);
                            logger.info("unable to perform " + Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE + " after " +  modification, ex);
                        }
                    }
                    if (childNodesType != null) {
                        try {
                            JCRNodeIteratorWrapper it = nread.getNodes();
                            int i = 0;
                            while (it.hasNext()) {
                                it.nextNode();
                                i++;
                            }
                            if (i == 3) {
                                results.add(new Result(Operation.READ_IMPORTED_CHILD_NODES, modification, true));
                            } else {
                                results.add(new Result(Operation.READ_IMPORTED_CHILD_NODES, modification, false,
                                        "Unexpected number of imported child nodes: " + i));
                            }
                        } catch (RepositoryException ex) {
                            results.add(new Result(Operation.READ_IMPORTED_CHILD_NODES, modification, false, ex.toString()));
                            session.refresh(false);
                            logger.info("unable to perform " + Operation.READ_IMPORTED_CHILD_NODES + " after " + modification, ex);
                        }
                    }
                    return results;
                }
            }));
        }

        overallResults.addAll(resultsForModification);
        SoftAssertions softly = new SoftAssertions();
        for (Result result : resultsForModification) {
            softly.assertThat(result.result).describedAs(result.modification + " - " + result.operation)
                    .isEqualTo(expectedResults.get(result.operation));
        }
        softly.assertAll();
    }

    private List<Result> doOperation(Operation operation, String modification, String nodePath, CallBack callBack) {
        List<Result> results = new ArrayList<>();
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);
            List<Result> operationResults = callBack.execute(session);
            if (CollectionUtils.isEmpty(operationResults)) {
                results.add(new Result(operation, modification, true));
            } else {
                results.addAll(operationResults);
            }
        } catch (Exception e) {
            results.add(new Result(operation, modification, false, e.toString()));
            logger.info("unable to perform " + operation + " after " +  modification, e);
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
        return results;
    }

    private class Result {
        Operation operation;
        String modification, detail;
        boolean result;

        public Result(Operation operation, String modification, boolean result) {
            this.modification = modification;
            this.operation = operation;
            this.result = result;
        }

        public Result(Operation operation, String modification, boolean result, String detail) {
            this.modification = modification;
            this.operation = operation;
            this.result = result;
            this.detail = detail;
        }
    }

    private interface CallBack {
        List<Result> execute(JCRSessionWrapper session) throws Exception;
    }
   
    @AfterClass
    public static void showTable() throws Exception {
        String mod = null;
        for (Result r : overallResults) {
            if (mod == null || !mod.equals(r.modification)) {
                // display title
                logger.info("\n=== {} ===", r.modification);

            }
            String detail = StringUtils.isEmpty(r.detail) ? "" : "(" + r.detail + ")";
            logger.info("{}: {} {}", new String[]{r.operation.toString(), Boolean.toString(r.result), detail});
            mod = r.modification;
        }
    }
    
    private static class VersionInfo {

        private String testNodePath;
        private String versionName;

        public VersionInfo(String testNodePath, String versionName) {
            this.testNodePath = testNodePath;
            this.versionName = versionName;
        }

        public String getTestNodePath() {
            return testNodePath;
        }

        public String getVersionName() {
            return versionName;
        }
    }
}
