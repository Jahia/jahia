/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.render.webflow;

import org.apache.commons.fileupload.FileItem;
import org.jahia.tools.files.FileUpload;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom implementation of the multipart request resolver to add handling of request parameters.
 */
public class MultipartResolver extends CommonsMultipartResolver  {

    @Override
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
