/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.automation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3 août 2007
 * Time: 15:15:56
 * To change this template use File | Settings | File Templates.
 */
public class ExifExtractor implements Extractor {

    public Map<String, Object> extract(InputStream content) throws Exception {
        Map<String, Object> m = new HashMap<String, Object>();
        Metadata metadata = JpegMetadataReader.readMetadata(content);
        Iterator<?> directories = metadata.getDirectoryIterator();
        while (directories.hasNext()) {
            Directory directory = (Directory)directories.next();
            // iterate through tags and print to System.out
            Iterator<?> tags = directory.getTagIterator();
            while (tags.hasNext()) {
                Tag tag = (Tag)tags.next();
                m.put(directory.getName() + "-" + tag.getTagName(), tag.getDescription());
            }
        }
        return m;
    }
}