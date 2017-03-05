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
