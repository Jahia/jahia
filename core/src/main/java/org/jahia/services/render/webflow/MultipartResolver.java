package org.jahia.services.render.webflow;

import org.apache.commons.fileupload.FileItem;
import org.jahia.tools.files.FileUpload;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartResolver extends CommonsMultipartResolver  {

    protected MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
        FileUpload fileUpload = (FileUpload) request.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
        List<FileItem> fileItems = new ArrayList<FileItem>(fileUpload.getFileItems().values());

        MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
        Map<String, String> multipartParameterContentTypes = new HashMap<String, String>();

        for (FileItem fileItem : fileItems) {
            CommonsMultipartFile file = new CommonsMultipartFile(fileItem);
            multipartFiles.add(fileItem.getFieldName(), file);
        }

        Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
        for (String param : fileUpload.getParameterNames()) {
            if (!request.getQueryString().contains("&"+param+"=") && !request.getQueryString().startsWith(param+"=")) {
                multipartParameters.put(param, fileUpload.getParameterValues(param));
                multipartParameterContentTypes.put(param, fileUpload.getParameterContentType(param));
            }
        }

        return new MultipartParsingResult(multipartFiles, multipartParameters, multipartParameterContentTypes);
    }

}
