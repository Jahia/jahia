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
