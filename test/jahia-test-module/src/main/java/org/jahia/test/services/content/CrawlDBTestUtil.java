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
package org.jahia.test.services.content;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;

public class CrawlDBTestUtil {

    private static final Log LOG = LogFactory.getLog(CrawlDBTestUtil.class);

    /**
     * Creates synthetic crawldb
     * 
     * @param fs
     *            filesystem where db will be created
     * @param crawldb
     *            path were db will be created
     * @param init
     *            urls to be inserted, objects are of type URLCrawlDatum
     * @throws Exception
     */
    public static void createCrawlDb(Configuration conf, FileSystem fs,
            Path crawldb, List<URLCrawlDatum> init) throws Exception {
        LOG.trace("* creating crawldb: " + crawldb);
        Path dir = new Path(crawldb, CrawlDb.CURRENT_NAME);
        MapFile.Writer writer = new MapFile.Writer(conf, fs, new Path(dir,
                "part-00000").toString(), Text.class, CrawlDatum.class);
        Iterator<URLCrawlDatum> it = init.iterator();
        while (it.hasNext()) {
            URLCrawlDatum row = it.next();
            LOG.info("adding:" + row.url.toString());
            writer.append(new Text(row.url), row.datum);
        }
        writer.close();
    }

    /**
     * For now we need to manually construct our Configuration, because we need to override the default one and it is currently not possible
     * to use dynamically set values.
     * 
     * @return
     * @deprecated Use {@link #createConfiguration()} instead
     */
    public static Configuration create() {
        return createConfiguration();
    }

    /**
     * For now we need to manually construct our Configuration, because we need to override the default one and it is currently not possible
     * to use dynamically set values.
     * 
     * @return
     */
    public static Configuration createConfiguration() {
        Configuration conf = new Configuration();
        conf.addResource("nutch-default.xml");
        conf.addResource("crawl-tests.xml");
        return conf;
    }

    public static class URLCrawlDatum {

        Text url;

        CrawlDatum datum;

        public URLCrawlDatum(Text url, CrawlDatum datum) {
            this.url = url;
            this.datum = datum;
        }
    }

    /**
     * Generate seedlist
     * 
     * @throws IOException
     */
    public static void generateSeedList(FileSystem fs, Path urlPath,
            List<String> contents) throws IOException {
        FSDataOutputStream out;
        Path file = new Path(urlPath, "urls.txt");
        fs.mkdirs(urlPath);
        out = fs.create(file);
        Iterator<String> iterator = contents.iterator();
        while (iterator.hasNext()) {
            String url = iterator.next();
            out.writeBytes(url);
            out.writeBytes("\n");
        }
        out.flush();
        out.close();
    }
}
