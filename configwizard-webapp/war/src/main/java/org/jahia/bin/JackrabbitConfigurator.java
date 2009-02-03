/*
 * Copyright 2002-2006 Jahia Ltd
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

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

/**
 * QuartzConfigurator
 *
 * @author <a href="mailto:cmailleux@jahia.com">Cedric Mailleux</a>
 * @version $Id: JackrabbitConfigurator.java 19589 2008-01-28 10:20:40Z tdraier $
 *
 * $Log$
 * Revision 1.1  2005/10/05 13:49:11  cmailleux
 * Add some configuration behavior against choice of database (Quartz locking for hypersonic, default Isolation Level for transaction (READ_COMMITTED for mysql), Support of nested transaction (true for mysql))
 *
 * Allow now user to choose to store all files (DAV and Big Text) in database (choice made in Advanced Settings).
 *
 */
public class JackrabbitConfigurator
{
    private static Logger logger = Logger.getLogger(JackrabbitConfigurator.class);

    public static void updateDataSourceConfiguration(String fileName, Map values)
    {

        File jahiaConfigFile = new File(fileName);
        if (jahiaConfigFile.exists()) {
            // let's load the file's content in memory, assuming it won't be
            // too big.
            StringBuffer fileContentBuf = new StringBuffer();
            try {
                FileReader fileReader = new FileReader(jahiaConfigFile);
                BufferedReader bufReader = new BufferedReader(fileReader);
                int ch = -1;
                while ( (ch = bufReader.read()) != -1) {
                    fileContentBuf.append( (char) ch);
                }
                bufReader.close();
                fileReader.close();

                String fileContent = fileContentBuf.toString();

                String storeFilesInDB = getValue(values,"storeFilesInDB");
                if("".equals(storeFilesInDB)) {
                    fileContent = fileContent.replaceAll("\\@EXTERNAL_BLOBS\\@", "true");
                } else {
                    fileContent = fileContent.replaceAll("\\@EXTERNAL_BLOBS\\@", "false");
                }

                fileContent = fileContent.replaceAll("\\@FILESYSTEM_CLASS\\@", getValue(values,"jahia.jackrabbit.filesystem"));
                fileContent = fileContent.replaceAll("\\@PERSISTENCE_CLASS\\@", getValue(values,"jahia.jackrabbit.persistence"));
                fileContent = fileContent.replaceAll("\\@JOURNAL_CLASS\\@", getValue(values,"jahia.jackrabbit.journal"));
                fileContent = fileContent.replaceAll("\\@SCHEMA\\@", getValue(values,"jahia.jackrabbit.schema"));

                // we have finished replacing values, let's save the modified
                // file.
                FileWriter fileWriter = new FileWriter(jahiaConfigFile);
                fileWriter.write(fileContent);
                fileWriter.close();

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
        replacement = replacement.trim();
        return replacement;
    }
}