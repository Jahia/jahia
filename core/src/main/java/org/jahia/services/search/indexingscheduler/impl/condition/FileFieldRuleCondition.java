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

package org.jahia.services.search.indexingscheduler.impl.condition;

import java.util.ArrayList;
import java.util.List;

import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRFileContent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.search.indexingscheduler.FileFieldRuleEvaluationContext;
import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;

/**
 * A Rule that matches agains File extension of File Field
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 15:05:48
 * To change this template use File | Settings | File Templates.
 */
public class FileFieldRuleCondition implements RuleCondition {

    public static final String ALL_FILES        = ".*";
    public static final String PDF_EXT          = ".pdf";
    public static final String WORD_EXT         = ".doc";
    public static final String EXCEL_EXT        = ".xls";
    public static final String POWERPOINT_EXT   = ".ppt";

    private List<String> fileExtensions = new ArrayList<String>();

    public FileFieldRuleCondition() {
    }

    public FileFieldRuleCondition(List<String> fileExtentions) {
        this();
        this.fileExtensions = fileExtentions;
    }

    /**
     * This condition accepts only a FileFieldRuleEvaluationContext paramater
     * If it is not the case, an exception will be thrown
     *
     * @param ctx
     * @return
     * @throws JahiaException
     */
    public boolean evaluate(RuleEvaluationContext ctx) throws JahiaException {

        if (!(ctx instanceof FileFieldRuleEvaluationContext)){
            throw new JahiaException("The RuleEvaluationContext is not a FileFieldRuleEvalationContext as expected",
                    "The RuleEvaluationContext is not a FileFieldRuleEvalationContext as expected",
                    JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY);
        }

        FileFieldRuleEvaluationContext fileFieldCtx = (FileFieldRuleEvaluationContext)ctx;

        if (fileExtensions == null || fileExtensions.isEmpty()){
            return false;
        }
        if (fileFieldCtx.getField()==null || fileFieldCtx.getField().getObject()==null){
            return false;
        }
        JahiaFileField fField = (JahiaFileField)fileFieldCtx.getField().getObject();

        String realName = fField.getRealName();
        if ( realName == null ){
            return false;
        }
        int pos = realName.toLowerCase().lastIndexOf(".");
        if (pos == -1){
            return false;
        }
        String ext = realName.substring(pos).toLowerCase();
        if (!fileExtensions.contains(ext)){
            return false;
        }
        JCRNodeWrapper file = JCRStoreService.getInstance()
                .getFileNode(fField.getRealName (), null);
        if (file.isValid () && !file.isCollection ()) {
            String contentType = fField.getType ();
            if (contentType != null && !file.getPath().equals("#")) {
            JCRFileContent fileContent = file.getFileContent();
            if (fileContent != null && fileContent.getExtractedText() == null)
                return true;
            }
        }
        return false;
    }

    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

}
