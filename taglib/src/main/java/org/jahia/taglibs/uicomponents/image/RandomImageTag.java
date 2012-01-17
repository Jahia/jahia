/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
                StringBuffer js = new StringBuffer();
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

        path = "";
        fileTypes = "jpg";
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
            if (filena != null && filena.indexOf(".") > 0) {
                String ext = filena.substring(filena.indexOf(".") + 1, filena.length());
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
        StringBuffer output = new StringBuffer();

        output.append("function getRandom").append(this.hashCode()).append("(imgarray) { ");
        output.append("var randomNum = Math.floor((imgarray.length)*(Math.random())); ");
        output.append(" return imgarray[randomNum]; } ");
        out.print(output.toString());
    }
}
