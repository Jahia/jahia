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
package org.jahia.services.importexport.validation;

import java.util.Collections;
import java.util.List;

import org.apache.jackrabbit.util.ISO9075;
import org.jahia.services.importexport.BaseDocumentViewHandler;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX handler that performs validation of the JCR content, provided in a document format.
 *
 * @author Benjamin Papez
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class DocumentViewValidationHandler extends BaseDocumentViewHandler implements
        ModuleDependencyAware {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentViewValidationHandler.class);

    private List<String> modules = Collections.emptyList();

    private String templateSetName;

    private List<ImportValidator> validators = Collections.emptyList();

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (validators.isEmpty()) {
            return;
        }
        pathes.pop();
    }

    /**
     * Returns the overall validation results.
     *
     * @return the overall validation results
     */
    public ValidationResults getResults() {
        ValidationResults results = new ValidationResults();
        for (ImportValidator validator : validators) {
            ValidationResult result = validator.getResult();
            if (result != null && !result.isSuccessful()) {
                logger.warn("Import validation fail: " + result.toString());
            }
            results.addResult(result);
        }

        return results;
    }

    @Override
    public void initDependencies(String templateSetName, List<String> modules) {
        this.templateSetName = templateSetName;
        if (modules != null) {
            this.modules = modules;
        } else {
            this.modules = Collections.emptyList();
        }
        initValidators();
    }

    private void initValidators() {
        if (!validators.isEmpty() && templateSetName != null) {
            for (ImportValidator validator : validators) {
                if (validator instanceof ModuleDependencyAware) {
                    ((ModuleDependencyAware) validator).initDependencies(templateSetName, modules);
                }
            }
        }
    }

    public void setValidators(List<ImportValidator> validators) {
        if (validators != null) {
            this.validators = validators;
        } else {
            validators = Collections.emptyList();
        }
        initValidators();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (validators.isEmpty()) {
            return;
        }

        try {
            String decodedLocalName = ISO9075.decode(localName);

            String decodedQName = qName.replace(localName, decodedLocalName);

            pathes.push(pathes.peek() + "/" + decodedQName);

            for (ImportValidator validator : validators) {
                validator.validate(decodedLocalName, decodedQName, pathes.peek(), atts);
            }
        } catch (Exception re) {
            throw new SAXException(re);
        }
    }
}
