package org.jahia.services.content.impl.external.modules;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.jahia.api.Constants;
import org.jahia.services.content.impl.external.ExternalData;
import org.jahia.services.content.impl.external.vfs.VFSDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 10/25/12
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModulesDataSource extends VFSDataSource implements ServletContextAware,InitializingBean {
    private ServletContext servletContext;

    @Override
    public List<String> getSupportedNodeTypes() {
        return Arrays.asList(Constants.JAHIANT_NODETYPEFOLDER, Constants.JAHIANT_TEMPLATETYPEFOLDER,
                Constants.JAHIANT_CSSFOLDER, Constants.JAHIANT_CSSFILE, Constants.JAHIANT_JAVASCRIPTFOLDER,
                Constants.JAHIANT_JAVASCRIPTFILE, Constants.JAHIANT_VIEWFILE, Constants.JAHIANT_DEFINITIONFILE,
                Constants.JAHIANT_RESOURCEBUNDLEFOLDER);
    }

    @Override
    public void saveItem(ExternalData data) {
        if (data.getPath().endsWith(Constants.JCR_CONTENT)) {
            OutputStream outputStream = null;
            try {
                outputStream = getFile(data.getPath().substring(0, data.getPath().indexOf("/" + Constants.JCR_CONTENT))).getContent().getOutputStream();
                final Binary[] binaries = data.getBinaryProperties().get(Constants.JCR_DATA);
                for (Binary binary : binaries) {
                    final InputStream stream = binary.getStream();
                    byte[] bytes = new byte[(int) binary.getSize()];
                    final int read = stream.read(bytes,0,(int) binary.getSize());
                    outputStream.write(bytes,0, read);
                }
            } catch (FileSystemException e) {
                logger.error(e.getMessage(), e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public String getDataType(FileObject fileObject) throws FileSystemException {
        final String path = fileObject.getName().getPath();
        if (path.endsWith(".css")) {
            return Constants.JAHIANT_CSSFILE;
        } else if (path.endsWith("/css")){
            return Constants.JAHIANT_CSSFOLDER;
        } else if (path.endsWith(".jsp")){
            return Constants.JAHIANT_VIEWFILE;
        } else if (path.endsWith(".cnd")){
            return Constants.JAHIANT_DEFINITIONFILE;
        } else if (path.endsWith("/javascript")){
            return Constants.JAHIANT_JAVASCRIPTFOLDER;
        } else if (path.endsWith(".js")){
            return Constants.JAHIANT_JAVASCRIPTFILE;
        }
        return super.getDataType(fileObject);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.setRoot(servletContext.getRealPath("/modules"));
    }

    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        ExternalData data = super.getItemByPath(path);
        if (path.endsWith(".jsp")) {
            Properties properties = new Properties();

            // set Properties
            try {
                properties.load(getFile(path.substring(0, path.lastIndexOf(".")) + ".properties").getContent().getInputStream());
                Map<String,String[]> dataProperties = new HashMap<String, String[]>();
                for (Iterator<?> iterator = properties.keySet().iterator(); iterator.hasNext();) {
                    String k = (String) iterator.next();
                    String v = properties.getProperty(k);
                    dataProperties.put(k,v.split(","));
                }
                data.getProperties().putAll(dataProperties);
            } catch (FileSystemException e) {
                //no properties files, do nothing
            } catch (IOException e) {
                logger.error("Cannot read property file",e);
            }
        }
        return data;
    }
}
