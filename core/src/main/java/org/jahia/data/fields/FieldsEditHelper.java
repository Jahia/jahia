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
