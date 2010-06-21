package org.jahia.modules.docconverter;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.office.OfficeException;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
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
public class DocumentConverterAction implements Action {
    private String name;

    private DocumentConverterService converterService;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
