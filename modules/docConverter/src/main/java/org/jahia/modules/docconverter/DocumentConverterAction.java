/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.docconverter;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.office.OfficeException;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Action;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.transform.DocumentConverterService;
import org.jahia.tools.files.FileUpload;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Document conversion action.
 * User: fabrice
 * Date: Apr 20, 2010
 * Time: 11:14:20 AM
 */
public class DocumentConverterAction extends Action {

    private DocumentConverterService converterService;

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        if (converterService.isEnabled()) {
        // Get parameters + file
        final FileUpload fu = (FileUpload) req.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
        DiskFileItem inputFile = fu.getFileItems().get("fileField");
        List<String> mimeTypeList = parameters.get("mimeType");

        String returnedMimeType = mimeTypeList != null ? mimeTypeList.get(0) : null;

        // Convert
        boolean conversionSucceeded = true;
        String failureMessage = null;
        File convertedFile = null;
        try {
            convertedFile = converterService.convert(inputFile.getStoreLocation(), inputFile.getContentType(),
                                                     returnedMimeType);
        } catch (IOException ioe) {
            conversionSucceeded = false;
            failureMessage = ioe.getMessage();
        } catch (OfficeException ioe) {
            conversionSucceeded = false;
            failureMessage = ioe.getMessage();
        }

        if (convertedFile == null) {
            conversionSucceeded = false;
        }


        // Create a conversion node and the file node if all succeeded
        JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
        String originFileName = inputFile.getName();
        String originMimeType = inputFile.getContentType();
        String convertedFileName = FilenameUtils.getBaseName(inputFile.getName()) + "." + converterService.getExtension(
                returnedMimeType);
        JCRNodeWrapper convertedFilesNode = session.getNode("/users/" + renderContext.getUser().getName() + "/files");
        JCRNodeWrapper convertedFileNode;
        if (conversionSucceeded) {
            FileInputStream iStream = new FileInputStream(convertedFile);
            convertedFileNode = convertedFilesNode.uploadFile(convertedFileName, iStream, returnedMimeType);
            convertedFileNode.addMixin("jmix:convertedFile");
            iStream.close();
        } else {
            convertedFileNode = convertedFilesNode.uploadFile(convertedFileName, inputFile.getInputStream(), inputFile.getContentType());
            convertedFileNode.addMixin("jmix:convertedFile");
            convertedFileNode.setProperty("conversionFailedMessage", failureMessage);
        }

        convertedFileNode.setProperty("originDocName", originFileName);
        convertedFileNode.setProperty("originDocFormat", originMimeType);
        convertedFileNode.setProperty("convertedDocName", convertedFileName);
        convertedFileNode.setProperty("convertedDocFormat", returnedMimeType);
        convertedFileNode.setProperty("conversionSucceeded", conversionSucceeded);

        session.save();
        }
        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }


    /**
     * @param converterService the converterService to set
     */
    public void setConverterService(DocumentConverterService converterService) {
        this.converterService = converterService;
    }

}
