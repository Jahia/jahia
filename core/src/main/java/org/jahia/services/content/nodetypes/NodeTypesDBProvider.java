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
package org.jahia.services.content.nodetypes;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * This class register cnd files in the DB so that they are accessible
 * for all nodes on a cluster.
 */
@Entity
@Table(name = "jahia_nodetypes_provider")
public class NodeTypesDBProvider {
    private static Logger logger = LoggerFactory.getLogger(NodeTypesDBProvider.class);

    private Integer id;
    private String filename;
    private String cndFile;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "jahia_nodetypes_provider_seq")
    @SequenceGenerator(initialValue = 1, allocationSize = 40, name = "jahia_nodetypes_provider_seq", sequenceName = "jahia_nodetypes_provider_seq")
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false)
    @Index(name = "jahia_nodetypes_provider_ix1")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Lob
    @Column(nullable = false)
    public String getCndFile() {
        return cndFile;
    }

    public void setCndFile(String cndFile) {
        this.cndFile = cndFile;
    }
}
