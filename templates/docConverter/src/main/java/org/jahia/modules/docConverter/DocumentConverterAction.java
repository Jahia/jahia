package org.jahia.modules.docConverter;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: fabrice
 * Date: Apr 20, 2010
 * Time: 11:14:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentConverterAction implements Action {
    private transient static Logger logger = Logger.getLogger(DocumentConverterAction.class);
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

        if (!converterService.isEnabled()) {
                
        }
        // Get parameters + file
        final ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();
        final FileUpload fu = paramBean.getFileUpload();
        DiskFileItem inputFile = fu.getFileItems().get("fileField");
        List<String> mimeTypeList = parameters.get("mimeType");

        String returnedMimeType = mimeTypeList != null ? mimeTypeList.get(0):null;

        // Convert
        boolean conversionSucceeded = true;
        String failureMessage = null;
        File convertedFile = null;
        try {
            convertedFile = converterService.convert(inputFile.getStoreLocation(),
                                inputFile.getContentType(), returnedMimeType);
        } catch(IOException ioe) {
            conversionSucceeded = false;
            failureMessage = ioe.getMessage();
        }

        if (convertedFile == null) {
            conversionSucceeded = false;
        }


        // Create a conversion node and the file node if all succeeded
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRNodeWrapper conversionsNode = session.getNode("/shared/conversions");
        conversionsNode.checkout();
        JCRNodeWrapper conversionNode = conversionsNode.addNode(String.valueOf(System.currentTimeMillis()), "jnt:convertedFile");

        String originFileName = inputFile.getName();
        String originMimeType = inputFile.getContentType();
        String convertedFileName = FilenameUtils.getBaseName(inputFile.getName()) + "." + converterService.getExtension(returnedMimeType);
        String convertedFileUUID = null;
        if (conversionSucceeded) {
            JCRNodeWrapper convertedFilesNode = session.getNode("/users/" + renderContext.getUser().getName() + "/files");
            FileInputStream iStream = new FileInputStream(convertedFile);

            JCRNodeWrapper convertedFileNode = convertedFilesNode.uploadFile(convertedFileName, iStream, returnedMimeType);
            convertedFileUUID = convertedFileNode.getIdentifier();
            conversionNode.setProperty("convertedDocFile", convertedFileUUID);
            iStream.close();
        } else {
           conversionNode.setProperty("conversionFailureMessage", failureMessage);
        }

        conversionNode.setProperty("originDocName", originFileName);
        conversionNode.setProperty("originDocFormat", originMimeType);
        conversionNode.setProperty("convertedDocName", convertedFileName);
        conversionNode.setProperty("convertedDocFormat", returnedMimeType);
        conversionNode.setProperty("conversionSucceeded", conversionSucceeded);

        session.save();

        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }


    

  /**
     * @param converterService the converterService to set
     */
    public void setConverterService(DocumentConverterService converterService) {
        this.converterService = converterService;
    }

}
