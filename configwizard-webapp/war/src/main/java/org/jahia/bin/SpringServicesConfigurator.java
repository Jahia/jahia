/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * SpringServicesConfigurator
 *
 * @author <a href="mailto:cmailleux@jahia.com">Cedric Mailleux</a>
 * @version $Id: SpringServicesConfigurator.java 19821 2008-02-27 15:43:47Z sshyrkov $
 *
 * $Log$
 * Revision 1.2  2005/12/21 15:41:53  shuber
 * Modified the opening of the XML Spring files to make sure we also read & write them in UTF-8.
 *
 * Revision 1.1  2005/10/05 13:49:11  cmailleux
 * Add some configuration behavior against choice of database (Quartz locking for hypersonic, default Isolation Level for transaction (READ_COMMITTED for mysql), Support of nested transaction (true for mysql))

 * 

 * Allow now user to choose to store all files (DAV and Big Text) in database (choice made in Advanced Settings).
 *
 */
public class SpringServicesConfigurator
{
    private static Logger logger = Logger.getLogger(SpringServicesConfigurator.class);
    public static final String BIGTEXT_DB = "<bean class=\"org.jahia.services.files.JahiaTextFileDBBaseService\" factory-method=\"getInstance\" >\n" +
            "      <property name=\"bigTextDataManager\">\n" +
            "          <ref bean=\"org.jahia.hibernate.manager.JahiaBigTextDataManager\"/>\n" +
            "      </property>\n" +
            "  </bean>";

    public static final String BIGTEXT_NODB = "  <bean class=\"org.jahia.services.files.JahiaTextFileBaseService\" parent=\"jahiaServiceTemplate\" factory-method=\"getInstance\" >\n" +
                                              "      <property name=\"cacheService\">\n" +
                                              "          <ref bean=\"JahiaCacheService\"/>\n" +
                                              "      </property>\n" +
                                              "  </bean>";
    public static void updateDataSourceConfiguration(String fileName, String outputFileName, Map values)
    {

        File jahiaConfigFile = new File(fileName);
        if (jahiaConfigFile.exists()) {
            // let's load the file's content in memory, assuming it won't be
            // too big.
            StringBuffer fileContentBuf = new StringBuffer();
            try {
                FileInputStream fileInputStream = new FileInputStream(jahiaConfigFile);
                InputStreamReader fileReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufReader = new BufferedReader(fileReader);
                int ch = -1;
                while ( (ch = bufReader.read()) != -1) {
                    fileContentBuf.append( (char) ch);
                }
                bufReader.close();
                fileReader.close();
                fileInputStream.close();

                String fileContent = fileContentBuf.toString();
                String transactionIsolationLevel = getValue(values,"storeFilesInDB");
                if("".equals(transactionIsolationLevel)) {
                    fileContent = StringUtils.replace(fileContent, "@BIGTEXTBEAN@", BIGTEXT_NODB);
                } else {
                    fileContent = StringUtils.replace(fileContent, "@BIGTEXTBEAN@", BIGTEXT_DB);
                }
                // we have finished replacing values, let's save the modified
                // file.
                FileOutputStream fileOutputStream = new FileOutputStream(jahiaConfigFile);
                OutputStreamWriter fileWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                fileWriter.write(fileContent);
                fileWriter.close();
                fileOutputStream.close();

            } catch (java.io.FileNotFoundException fnfe) {
                logger.error("Error modifying repository config file " +
                             jahiaConfigFile.toString(), fnfe);
            } catch (java.io.IOException ioe) {
                logger.error("Error modifying repository config file " +
                             jahiaConfigFile.toString(), ioe);
            }

        }
    }

    private static String getValue (Map values, String key) {
        String replacement = (String) values.get(key);
        if (replacement == null) {
            return "";
        }
        replacement = replacement.replaceAll("&", "&amp;");
        return replacement;
    }
}
