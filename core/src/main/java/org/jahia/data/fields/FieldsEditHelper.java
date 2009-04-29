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
package org.jahia.data.fields;

import java.util.*;

import org.jahia.data.containers.ContainerEditView;
import org.jahia.data.containers.ContainerFacadeInterface;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import java.io.Serializable;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface FieldsEditHelper extends Serializable {

    public JahiaField getSelectedField();

    public int getSelectedFieldId();

    public void setSelectedFieldId(int fieldId);

    public int getLastFieldId();

    public void setLastFieldId(int fieldId);

    public Map<Integer, List<String>> getUpdatedFields();

    public void addUpdatedField(int fieldId, String languageCode);

    public boolean containsUpdatedField(int fieldId, String languageCode);

    public Map<Integer, String> getFieldForms();

    public void setFieldForms(Map<Integer, String> fieldForms);

    public void addFieldForm(int fieldId, String fieldForm);

    public String getFieldForm(int fieldId);

    public void setContainerEditView(ContainerEditView ctnEditView);

    public ContainerEditView getContainerEditView();

    public JahiaField getField(int fieldId);

    public JahiaField getField(String fieldName);

    public void setStayOnSameField(boolean stayOnSameField);

    public boolean getStayOnSameField();

    public void processRequest(ProcessingContext jParams, int lastFieldId);

    public void setCtnListFieldAcls(Map<Integer, Integer> ctnListFieldAcls);

    public Map<Integer, Integer> getCtnListFieldAcls();

    public void setVisibleFields(Set<Integer> visibleFields);

    public Set<Integer> getVisibleFields();

    /**
     * Process current fields, return false on error
     *
     * @param engineName                  String
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param elh                         EngineLanguageHelper
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @param mode                        int
     * @return boolean
     * @throws JahiaException
     */
    public boolean processCurrentFields(String engineName,
                                        ContainerFacadeInterface jahiaContentContainerFacade,
                                        EngineLanguageHelper elh,
                                        ProcessingContext jParams, Map<String, Object> engineMap, int mode)
            throws JahiaException;

    /**
     * Process last field, return false on error
     *
     * @param engineName                  String
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param elh                         EngineLanguageHelper
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @param mode                        int
     * @return boolean
     * @throws JahiaException
     */
    public boolean processLastFields(String engineName,
                                     ContainerFacadeInterface jahiaContentContainerFacade,
                                     EngineLanguageHelper elh,
                                     ProcessingContext jParams,
                                     Map<String, Object> engineMap,
                                     int mode)
            throws JahiaException;

    /**
     * Check Fields validation
     *
     * @param engineName                  String
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param elh                         EngineLanguageHelper
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @return EngineValidationHelper
     * @throws JahiaException
     */
    public abstract EngineValidationHelper validate(String engineName,
                                                    ContainerFacadeInterface jahiaContentContainerFacade,
                                                    EngineLanguageHelper elh,
                                                    ProcessingContext jParams,
                                                    Map<String, Object> engineMap)
            throws JahiaException;

    public abstract boolean areValuesTheSameInAllActiveLanguages(final JahiaField theField,
                                                                 final ContainerFacadeInterface jahiaContentContainerFacade)
            throws JahiaException;

}
