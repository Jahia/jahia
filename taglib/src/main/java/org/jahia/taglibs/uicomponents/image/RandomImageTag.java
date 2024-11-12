/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.uicomponents.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;


/**
 * Class RandomImageTag : shows random image from the specified folder
 *
 * @author Werner Assek
 * @jsp:tag name="random-image" body-content="empty"
 * description="shows a random image from spcified folder
 * <p><attriInfo>
 * </attriInfo>"
 */
public class RandomImageTag extends AbstractJCRTag {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(RandomImageTag.class);

    private String path = "";
    private String fileTypes = "jpg";


    public int doStartTag() throws JspException {

        try {
            Object pageCPath = pageContext.getAttribute(getPath());
            if (pageCPath != null)
                setPath(pageCPath.toString());
        } catch (Exception e) {
            throw new JspException("this tag is designed to be used in a JahiaContext only", e);
        }

        List<JCRNodeWrapper> imagelist = Collections.emptyList();
        try {
            imagelist = getSortedImages(path, false);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SKIP_BODY;
        }

        List<JCRNodeWrapper> resultImgs = filterFileTypes(imagelist);
        List<String> pathes = new ArrayList<String>();
        for (int i = 0; i < resultImgs.size(); i++) {
            JCRNodeWrapper acc = resultImgs.get(i);
            pathes.add(acc.getUrl());
        }

        if (pathes.size() > 0) ;
        {
            try {
                pageContext.getOut().println("<img id=\"img" + this.hashCode() + "\" src=\"" + pathes.get(0) + "\" />");
                pageContext.getOut().println("<script language=\"JavaScript\"><!--\n");
                createJSRandomMethod(pageContext.getOut());
                StringBuilder js = new StringBuilder();
                js.append("\nimgarray").append(this.hashCode()).append(" = new Array(");
                for (int i = 0; i < pathes.size(); i++) {
                    if (i > 0)
                        js.append(", ");
                    js.append("'").append(pathes.get(i)).append("'");// imgarray hascode = new Array(
                }
                js.append("); ");
                js.append("\ndocument.getElementById('img").append(this.hashCode())
                        .append("').src=getRandom").append(this.hashCode()).append("(imgarray")
                        .append(this.hashCode()).append("); ");
                js.append("//--> </script>");
                pageContext.getOut().print(js.toString());

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return SKIP_BODY;
    }


    public int doEndTag() {

        resetState();
        return EVAL_PAGE;
    }

    private List<JCRNodeWrapper> getSortedImages(String webdavpath, boolean thumbdisp) throws PathNotFoundException, RepositoryException {


        JCRNodeWrapper node = getJCRSession().getNode(webdavpath);

        List<JCRNodeWrapper> images = new ArrayList<JCRNodeWrapper>();
        for (NodeIterator iterator = node.getNodes(); iterator.hasNext();) {
            JCRNodeWrapper thefile = (JCRNodeWrapper) iterator.nextNode();
            //we don't list binaries files not image and not valid (with access denied)
            if (thefile.getName().toLowerCase().endsWith("jpg") || thefile.getName().toLowerCase().endsWith("gif")
                    || thefile.getName().toLowerCase().endsWith("png") || thefile.getName().toLowerCase().endsWith("bmp")) {
                images.add(thefile);
            }
        }
        return images;

    }

    private List<JCRNodeWrapper> filterFileTypes(List<JCRNodeWrapper> fileList) {
        List<JCRNodeWrapper> result = new ArrayList<JCRNodeWrapper>();


        for (int i = 0; i < fileList.size(); i++) {
            JCRNodeWrapper acc = (JCRNodeWrapper) fileList.get(i);

            String filena = acc.getName();
            if (filena != null && filena.lastIndexOf(".") > 0) {
                String ext = filena.substring(filena.lastIndexOf(".") + 1, filena.length());
                if (fileTypes.toLowerCase().indexOf(ext.toLowerCase()) >= 0)
                    result.add(acc);
            }

        }
        return result;

    }

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getFileTypes() {
        return fileTypes;
    }


    public void setFileTypes(String fileTypes) {
        this.fileTypes = fileTypes;
    }

    private void createJSRandomMethod(JspWriter out) throws IOException {
        StringBuilder output = new StringBuilder();

        output.append("function getRandom").append(this.hashCode()).append("(imgarray) { ");
        output.append("var randomNum = Math.floor((imgarray.length)*(Math.random())); ");
        output.append(" return imgarray[randomNum]; } ");
        out.print(output.toString());
    }

    @Override
    protected void resetState() {
        path = "";
        fileTypes = "jpg";
        super.resetState();
    }

}
