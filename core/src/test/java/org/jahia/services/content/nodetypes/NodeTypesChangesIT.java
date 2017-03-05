
/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
        READ_CHANGED_IMPORTED_PROPERTY_VALUE, READ_IMPORTED_CHILD_NODES, CHECK_MIXIN
    }

    @Before
    public void setUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);
        JCRNodeWrapper testNode = session.getNode("/").addNode("nodeTypeChanges", "nt:unstructured");
        testNode.addMixin("mix:referenceable");
        testNodeIdentifier = testNode.getIdentifier();
    }


    /**
     * Test removing a nodetype definition
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("remove nodetype from definition", null), expectedResults);  
    }
    
    /**
     * Test hiding a previous mandatory property
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("hide previous mandatory property", "test_mandatory"), expectedResults);  
    }
    
    /**
     * Test removing properties from a nodetype definition
     */
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

        checkOperations(versionInfo, new ModificationInfo("remove property from nodetype", "test"), expectedResults);
    }
    

    /**
     * Test switching a property definition from non-i18n to i18n
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from non-i18n to i18n", "test"), expectedResults);  
    }

    /**
     * Test switching a property definition from i18n to non-i18n
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from i18n to non-i18n", "test_i18n"), expectedResults);
    }

    /**
     * Test switching a property definition from single to multiple values
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from single to multiple", "test"), expectedResults);
    }       
    
    /**
     * Test switching a property definition from multiple to single values
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from multiple to single", "test_multiple"), expectedResults);
    }
    
    /**
     * Test switching a property definition from type long to string
     */    
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from long to string", "integer"), expectedResults);
    }
    
    /**
     * Test switching a property definition from type string to long
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from string to long", "integerAsString"), expectedResults);
    }
    
    /**
     * Test switching a property definition from type double to decimal
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from double to decimal", "decimalNumber"), expectedResults);
    }     

    /**
     * Test switching a property definition from type long to decimal
     */
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

        checkOperations(versionInfo, new ModificationInfo("switch a property from long to decimal", "integer"), expectedResults);
    }     
    
    /**
     * Test adding a new property definition with mandatory constraint
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("add new property definition with mandatory constraint", null), expectedResults);
    }    
    
    /**
     * Test changing a range constraint
     */    
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
        
        checkOperations(versionInfo, new ModificationInfo("add range constraint to existing property definition", "integer"), expectedResults);
    }        
    
    /**
     * Test moving definitions to a new supertype
     */    
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
        
        checkOperations(versionInfo, new ModificationInfo("move definitions to supertype", "test"), expectedResults);  
    }
    
    /**
     * Test changing the allowed child node type
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("change allowed child node type", "test:bigText", null, null), expectedResults);  
    }
    
    /**
     * Test adding a new yet unrelated allowed child node type
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("add allowed child node type", "test:bigText", null, null), expectedResults);  
    }    
    
    /**
     * Test removing an allowed child node type
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("remove allowed child node type", "test:text", null, null), expectedResults);  
    }       
    
    /**
     * Test changing the allowed child node type using the previous one as supertype of the new one
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("change allowed child node type using previous as supertype", "test:bigText", null, null), expectedResults);  
    }
    
    /**
     * Test changing the allowed child node type to the supertype of the previous allowed node type
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("change allowed child node type using supertype", "test:superText", null, null), expectedResults);  
    }
    
    /**
     * Test changing a node type definition to orderable
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("make allowed child node type orderable", "test:ultimativeText", null, null), expectedResults);  
    }

    /**
     * Test changing the allowed reference nodetype
     */
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
        
        checkOperations(versionInfo, new ModificationInfo("change allowed reference type", "reference"), expectedResults);  
    }    
    
    /**
     * Test adding a new mixin to a nodetype with existing nodes
     */
    @Test
    public void shouldAllowAllOperationsAfterAddingNewMixin() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        addMixinToNodetype();
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
                .put(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, false).put(Operation.READ_IMPORTED_CHILD_NODES, true).put(Operation.CHECK_MIXIN, true).build();
        
        checkOperations(versionInfo, new ModificationInfo("add new mixin to existing nodetype", "test:ultimativeText", "test:mixin"), expectedResults);  
    }       
    
    /**
     * Test adding a new mixin with a default valued property to a nodetype with existing nodes
     */
    @Test
    public void shouldFailMixinPropertyReadOperationAfterAddingNewMixinWithDefaultValuedProperty() throws Exception {
        //given
        VersionInfo versionInfo = init();
        //when
        addMixinWithDefaultValuedPropertyToNodetype();
        //then
        Map<Operation, Boolean> expectedResults = ImmutableMap.<Operation, Boolean> builder().put(Operation.GET_NODE, true)
                .put(Operation.GET_CHILD_NODES, true).put(Operation.EXPORT_NODE, true).put(Operation.COPY_NODE, true)
                .put(Operation.CHECK_COPIED_CHILD_NODES, true).put(Operation.READ_STABLE_PROPERTY_VALUE, true)
                .put(Operation.READ_STRING_PROPERTY_VALUE, true).put(Operation.READ_MULTIPLE_PROPERTY_VALUE, true)
                .put(Operation.READ_I18N_PROPERTY_VALUE, true).put(Operation.READ_DECIMAL_PROPERTY_VALUE, true)
                .put(Operation.READ_INTEGER_PROPERTY_VALUE, true).put(Operation.READ_REFERENCE_PROPERTY_VALUE, true)
                .put(Operation.READ_CHILD_PROPERTIES, false).put(Operation.EDIT_PROPERTY, true).put(Operation.EDIT_CHANGED_PROPERTY, true)
                .put(Operation.REMOVE_PROPERTY, true).put(Operation.REMOVE_CHANGED_PROPERTY, true).put(Operation.ADD_CHILD_NODE, true)
                .put(Operation.RESTORE_VERSION, true).put(Operation.READ_RESTORED_PROPERTY_VALUE, true).put(Operation.REMOVE_NODE, true)
                .put(Operation.REMOVE_CHILD_NODE, true).put(Operation.IMPORT_NODE, true).put(Operation.READ_IMPORTED_PROPERTY_VALUE, true)
                .put(Operation.READ_IMPORTED_CHILD_NODES, true).build();
        
        checkOperations(versionInfo, new ModificationInfo("add new mixin with default valued property to existing nodetype", "test:ultimativeText", "autocreatedProperty", "test"), expectedResults);  
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
    
    private void addMixinToNodetype() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:mixin] mixin\n" +           
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured, test:mixin\n" +           
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
    
    private void addMixinWithDefaultValuedPropertyToNodetype() throws IOException, ParseException, RepositoryException {
        changeDefinition("<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:mixin] mixin\n" +           
                " - autocreatedProperty (string) = 'test'\n" +                
                "[test:superText] > nt:base, mix:versionable\n" +
                " - text (string) primary i18n\n" +
                "[test:text] > test:superText\n" +
                "[test:ultimativeText] > test:text, nt:unstructured, test:mixin\n" +           
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

    private void checkOperations(final VersionInfo versionInfo, final ModificationInfo modificationInfo, Map<Operation, Boolean> expectedResults) {
        final String unchangedProperty = "stable";
        final String nodePath = versionInfo.getTestNodePath();
        final String versionName = versionInfo.getVersionName();
        
        List<Result> resultsForModification = new ArrayList<>();
        // get node
        resultsForModification.addAll(doOperation(Operation.GET_NODE, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                session.getNode(nodePath);
                return Collections.emptyList();
            }
        }));
        
        if (modificationInfo.getChildNodeType() != null) {
            // get child nodes
            resultsForModification.addAll(doOperation(Operation.GET_CHILD_NODES, modificationInfo, nodePath, new CallBack() {
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
        final List<Result> exportNodeResults = doOperation(Operation.EXPORT_NODE, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                HashMap<String, Object> params = new HashMap<>();
                ImportExportBaseService.getInstance().exportNode(session.getNode(nodePath), session.getNode("/"), exportOutputStream, params);
                return Collections.emptyList();                
            }
        });
        resultsForModification.addAll(exportNodeResults);

        // copy node
        resultsForModification.addAll(doOperation(Operation.COPY_NODE, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
                JCRNodeWrapper nread = session.getNode(nodePath);
                if (!nread.copy(session.getNodeByIdentifier(testNodeIdentifier), "test-copy", false)) {
                    throw new RepositoryException("copy was not successful");
                } else if (modificationInfo.getChildNodeType() != null) {
                    results.add(new Result(Operation.COPY_NODE, modificationInfo, true));
                    try {
                        JCRNodeIteratorWrapper it = session.getNodeByIdentifier(testNodeIdentifier).getNode("test-copy").getNodes();
                        int i = 0;
                        while (it.hasNext()) {
                            it.nextNode();
                            i++;
                        }
                        if (i < 3) {
                            results.add(
                                    new Result(Operation.CHECK_COPIED_CHILD_NODES, modificationInfo, false, "Copied child nodes are missing"));
                        } else {
                            results.add(new Result(Operation.CHECK_COPIED_CHILD_NODES, modificationInfo, true));
                        }
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.CHECK_COPIED_CHILD_NODES, modificationInfo, false, ex.toString()));
                        logger.info("unable to perform " + Operation.CHECK_COPIED_CHILD_NODES + " after " +  modificationInfo.getModificationDescription(), ex);
                    }
                }
                return results;
            }
        }));

        // read property
        resultsForModification.addAll(doOperation(Operation.READ_PROPERTIES, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
                JCRNodeWrapper nread = session.getNode(nodePath);
                try {
                    String s = nread.getProperty(unchangedProperty).getValue().getString();

                    results.add(new Result(Operation.READ_STABLE_PROPERTY_VALUE, modificationInfo, "test".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_STABLE_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_STABLE_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex); 
                }
                try {
                    String s = nread.getProperty("test").getValue().getString();

                    results.add(new Result(Operation.READ_STRING_PROPERTY_VALUE, modificationInfo, "test".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_STRING_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_STRING_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
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
                    results.add(new Result(Operation.READ_MULTIPLE_PROPERTY_VALUE, modificationInfo, b));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_MULTIPLE_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_MULTIPLE_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                try {
                    String s = nread.getProperty("test_i18n").getValue().getString();
                    results.add(new Result(Operation.READ_I18N_PROPERTY_VALUE, modificationInfo, "test".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_I18N_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_I18N_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                try {
                    String s = nread.getProperty("decimalNumber").getValue().getString();

                    results.add(new Result(Operation.READ_DECIMAL_PROPERTY_VALUE, modificationInfo, "2.5".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_DECIMAL_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_DECIMAL_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                try {
                    String s = nread.getProperty("integer").getValue().getString();
                    results.add(new Result(Operation.READ_INTEGER_PROPERTY_VALUE, modificationInfo, "10".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_INTEGER_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_INTEGER_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                try {
                    Node node = nread.getProperty("reference").getValue().getNode();
                    results.add(new Result(Operation.READ_REFERENCE_PROPERTY_VALUE, modificationInfo, testNodeIdentifier.equals(node.getIdentifier())));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_REFERENCE_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    logger.info("unable to perform " + Operation.READ_REFERENCE_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                }                
                if (modificationInfo.getChildNodeType() != null) {
                    try {
                        JCRNodeIteratorWrapper it = nread.getNodes();
                        String childNodeProperty = StringUtils.isNotBlank(modificationInfo.getChildNodeProperty()) ? modificationInfo.getChildNodeProperty() : "text";
                        int i = 0;
                        boolean expectedTextFound = true;
                        while (it.hasNext()) {
                            Node childNode = it.nextNode();
                            i++;
                            String expectedValue = StringUtils.isNotBlank(modificationInfo.getChildNodePropertyValue()) ? modificationInfo.getChildNodePropertyValue() : childNode.getName();
                            expectedTextFound = expectedTextFound && expectedValue.equals(childNode.getProperty(childNodeProperty).getValue().getString());
                        }
                        if (i == 3 && expectedTextFound) {
                            results.add(new Result(Operation.READ_CHILD_PROPERTIES, modificationInfo, true));
                        } else {
                            results.add(new Result(Operation.READ_CHILD_PROPERTIES, modificationInfo, false, "Expected text not found in childnodes"));
                        }
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.READ_CHILD_PROPERTIES, modificationInfo, false, ex.toString()));
                        logger.info("unable to perform " + Operation.READ_CHILD_PROPERTIES + " after " +  modificationInfo.getModificationDescription(), ex);
                    }    
                }
                return results;
            }
        }));

        // edit property
        resultsForModification.addAll(doOperation(Operation.EDIT_PROPERTY, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
                try {
                    editProperty(session, unchangedProperty);
                    results.add(new Result(Operation.EDIT_PROPERTY, modificationInfo, true));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.EDIT_PROPERTY, modificationInfo, false, ex.toString()));
                    session.refresh(false);
                    logger.info("unable to perform " + Operation.EDIT_PROPERTY + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                if (StringUtils.isNotEmpty(modificationInfo.getChangedProperty())) {
                    try {
                        editProperty(session, modificationInfo.getChangedProperty());
                        results.add(new Result(Operation.EDIT_CHANGED_PROPERTY, modificationInfo, true));
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.EDIT_CHANGED_PROPERTY, modificationInfo, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.EDIT_CHANGED_PROPERTY + " after " +  modificationInfo.getModificationDescription(), ex);                        
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
        resultsForModification.addAll(doOperation(Operation.REMOVE_PROPERTY, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();
            
                JCRNodeWrapper nread = session.getNode(nodePath);
                try {
                    nread.getProperty(unchangedProperty).remove();
                    session.save();
                    results.add(new Result(Operation.REMOVE_PROPERTY, modificationInfo, true));                    
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.REMOVE_PROPERTY, modificationInfo, false, ex.toString()));
                    session.refresh(false);
                    logger.info("unable to perform " + Operation.REMOVE_PROPERTY + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                if (StringUtils.isNotEmpty(modificationInfo.getChangedProperty())) {
                    try {
                        nread.getProperty(modificationInfo.getChangedProperty()).remove();
                        session.save();
                        results.add(new Result(Operation.REMOVE_CHANGED_PROPERTY, modificationInfo, true));
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.REMOVE_CHANGED_PROPERTY, modificationInfo, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.REMOVE_CHANGED_PROPERTY + " after " +  modificationInfo.getModificationDescription(), ex);
                    }
                }
                return results;
            }
        }));

        if (StringUtils.isNotBlank(modificationInfo.getChildNodeType())) {
            //add child node
            resultsForModification.addAll(doOperation(Operation.ADD_CHILD_NODE, modificationInfo, nodePath, new CallBack() {
                @Override
                public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                    JCRNodeWrapper nread = session.getNode(nodePath);
                    JCRNodeWrapper addedNode = nread.addNode("addedChildNode", modificationInfo.getChildNodeType());
                    addedNode.setProperty("text", "added text");
                    session.save();
                    return Collections.emptyList();
                }
            })); 
            
            if (expectedResults.containsKey(Operation.CHECK_MIXIN)) {
                resultsForModification.addAll(doOperation(Operation.CHECK_MIXIN, modificationInfo, nodePath, new CallBack() {
                    @Override
                    public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                        List<Result> results = new ArrayList<>();
                        JCRNodeWrapper nread = session.getNode(nodePath);
                        JCRNodeIteratorWrapper it = nread.getNodes();
                        boolean expectedMixinFound = true;
                        while (it.hasNext()) {
                            Node childNode = it.nextNode();
                            expectedMixinFound = expectedMixinFound && childNode.isNodeType(modificationInfo.getChildNodeMixin());
                        }                        
                        results.add(new Result(Operation.CHECK_MIXIN, modificationInfo, expectedMixinFound));
                        return Collections.emptyList();
                    }
                }));                 
            }
        }
        
        //restore version
        resultsForModification.addAll(doOperation(Operation.RESTORE_VERSION, modificationInfo, nodePath, new CallBack() {
            @Override
            public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                List<Result> results = new ArrayList<>();                
                session.getWorkspace().getVersionManager().checkout(nodePath);
                session.getWorkspace().getVersionManager().restore(nodePath, versionName, true);
                session.save();
                results.add(new Result(Operation.RESTORE_VERSION, modificationInfo, true));
                JCRNodeWrapper nread = session.getNode(nodePath);
                try {
                    JCRPropertyWrapper property = nread.getProperty(unchangedProperty);
                    String s = property.getDefinition().isMultiple() ? property.getValues()[0].getString()
                            : property.getValue().getString();
                    results.add(new Result(Operation.READ_RESTORED_PROPERTY_VALUE, modificationInfo, "new value".equals(s)));
                } catch (RepositoryException ex) {
                    results.add(new Result(Operation.READ_RESTORED_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                    session.refresh(false);
                    logger.info("unable to perform " + Operation.READ_RESTORED_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                }
                if (StringUtils.isNotEmpty(modificationInfo.getChangedProperty())) {
                    try {
                        JCRPropertyWrapper property = nread.getProperty(modificationInfo.getChangedProperty());
                        if ("reference".equals(modificationInfo.getChangedProperty())) {
                            results.add(new Result(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, modificationInfo, testNodeIdentifier.equals(property.getNode().getIdentifier())));                            
                        } else {
                            String s = property.getDefinition().isMultiple() ? property.getValues()[0].getString()
                                    : property.getValue().getString();
                            results.add(new Result(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, modificationInfo, "new value".equals(s)));
                        }
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.READ_CHANGED_RESTORED_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                    }
                }
                return results;
            }
        }));

        if (modificationInfo.getChildNodeType() != null) {
            // remove child node
            resultsForModification.addAll(doOperation(Operation.REMOVE_CHILD_NODE, modificationInfo, nodePath, new CallBack() {
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
        resultsForModification.addAll(doOperation(Operation.REMOVE_NODE, modificationInfo, nodePath, new CallBack() {
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
            resultsForModification.addAll(doOperation(Operation.IMPORT_NODE, modificationInfo, nodePath, new CallBack() {
                @Override
                public List<Result> execute(final JCRSessionWrapper session) throws Exception {
                    List<Result> results = new ArrayList<>();
                    ByteArrayInputStream stream = new ByteArrayInputStream(exportOutputStream.toString().getBytes(StandardCharsets.UTF_8));
                    JCRNodeWrapper importFolder = session.getNode("/").addNode("nodeTypesImported", "nt:unstructured");
                    session.save();
                    ImportExportBaseService.getInstance().importXML(importFolder.getPath(), stream,
                            DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                    JCRNodeWrapper nread = importFolder.getNode("nodeTypeChanges/test");                    
                    results.add(new Result(Operation.IMPORT_NODE, modificationInfo, true));
                    try {
                        String s = nread.getProperty(unchangedProperty).getValue().getString();
                        results.add(new Result(Operation.READ_IMPORTED_PROPERTY_VALUE, modificationInfo, "test".equals(s)));
                    } catch (RepositoryException ex) {
                        results.add(new Result(Operation.READ_IMPORTED_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                        session.refresh(false);
                        logger.info("unable to perform " + Operation.READ_IMPORTED_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                    }
                    if (StringUtils.isNotEmpty(modificationInfo.getChangedProperty())) {
                        try {
                            if ("reference".equals(modificationInfo.getChangedProperty())) {
                                results.add(new Result(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, modificationInfo,
                                        testNodeIdentifier.equals(nread.getProperty(modificationInfo.getChangedProperty()).getNode().getIdentifier())));
                            } else {
                                String s = nread.getProperty(modificationInfo.getChangedProperty()).getValue().getString();
                                results.add(new Result(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, modificationInfo, "test".equals(s)));
                            }
                        } catch (RepositoryException ex) {
                            results.add(new Result(Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE, modificationInfo, false, ex.toString()));
                            session.refresh(false);
                            logger.info("unable to perform " + Operation.READ_CHANGED_IMPORTED_PROPERTY_VALUE + " after " +  modificationInfo.getModificationDescription(), ex);
                        }
                    }
                    if (modificationInfo.getChildNodeType() != null) {
                        try {
                            JCRNodeIteratorWrapper it = nread.getNodes();
                            int i = 0;
                            while (it.hasNext()) {
                                it.nextNode();
                                i++;
                            }
                            if (i == 3) {
                                results.add(new Result(Operation.READ_IMPORTED_CHILD_NODES, modificationInfo, true));
                            } else {
                                results.add(new Result(Operation.READ_IMPORTED_CHILD_NODES, modificationInfo, false,
                                        "Unexpected number of imported child nodes: " + i));
                            }
                        } catch (RepositoryException ex) {
                            results.add(new Result(Operation.READ_IMPORTED_CHILD_NODES, modificationInfo, false, ex.toString()));
                            session.refresh(false);
                            logger.info("unable to perform " + Operation.READ_IMPORTED_CHILD_NODES + " after " + modificationInfo.getModificationDescription(), ex);
                        }
                    }
                    return results;
                }
            }));
        }

        overallResults.addAll(resultsForModification);
        SoftAssertions softly = new SoftAssertions();
        for (Result result : resultsForModification) {
            softly.assertThat(result.result).describedAs(result.modificationInfo.getModificationDescription() + " - " + result.operation)
                    .isEqualTo(expectedResults.get(result.operation));
        }
        softly.assertAll();
    }

    private List<Result> doOperation(Operation operation, ModificationInfo modificationInfo, String nodePath, CallBack callBack) {
        List<Result> results = new ArrayList<>();
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);
            List<Result> operationResults = callBack.execute(session);
            if (CollectionUtils.isEmpty(operationResults)) {
                results.add(new Result(operation, modificationInfo, true));
            } else {
                results.addAll(operationResults);
            }
        } catch (Exception e) {
            results.add(new Result(operation, modificationInfo, false, e.toString()));
            logger.info("unable to perform " + operation + " after " +  modificationInfo.getModificationDescription(), e);
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
        return results;
    }

    private class Result {
        Operation operation;
        ModificationInfo modificationInfo;
        String detail;
        boolean result;

        public Result(Operation operation, ModificationInfo modificationInfo, boolean result) {
            this.modificationInfo = modificationInfo;
            this.operation = operation;
            this.result = result;
        }

        public Result(Operation operation, ModificationInfo modificationInfo, boolean result, String detail) {
            this.modificationInfo = modificationInfo;
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
            if (mod == null || !mod.equals(r.modificationInfo.getModificationDescription())) {
                // display title
                logger.info("\n=== {} ===", r.modificationInfo.getModificationDescription());

            }
            String detail = StringUtils.isEmpty(r.detail) ? "" : "(" + r.detail + ")";
            logger.info("{}: {} {}", new String[]{r.operation.toString(), Boolean.toString(r.result), detail});
            mod = r.modificationInfo.getModificationDescription();
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
    
    private static class ModificationInfo {

        private String modificationDescription;
        private String changedProperty;

        private String childNodeType;
        private String childNodeMixin;        
        private String childNodeProperty;        
        private String childNodePropertyValue;        

        public ModificationInfo(String modificationDescription, String changedProperty) {
            this.modificationDescription = modificationDescription;
            this.changedProperty = changedProperty;
        }

        public ModificationInfo(String modificationDescription, String childNodeType, String childNodeMixin) {
            this.modificationDescription = modificationDescription;
            this.childNodeType = childNodeType;
            this.childNodeMixin = childNodeMixin;            
        }         
        
        public ModificationInfo(String modificationDescription, String childNodeType, String childNodeProperty, String childNodePropertyValue) {
            this.modificationDescription = modificationDescription;
            this.childNodeType = childNodeType;
            this.childNodeProperty = childNodeProperty;            
            this.childNodePropertyValue = childNodePropertyValue;            
        }        
        
        public String getModificationDescription() {
            return modificationDescription;
        }

        public String getChangedProperty() {
            return changedProperty;
        }
        
        public String getChildNodeType() {
            return childNodeType;
        }
        
        public String getChildNodeMixin() {
            return childNodeMixin;
        }        
        
        public String getChildNodeProperty() {
            return childNodeProperty;
        }
        
        public String getChildNodePropertyValue() {
            return childNodePropertyValue;
        }         
    }
}
