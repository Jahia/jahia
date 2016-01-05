/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.model;

import org.apache.jackrabbit.ocm.exception.IncorrectAtomicTypeException;
import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.BinaryTypeConverterImpl;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Custom type converter for URL to JCR Binary values and back.
 * 
 * @author Sergiy Shyrkov
 */
public class UrlBinaryTypeConverterImpl extends BinaryTypeConverterImpl {

    /**
     * URL stream handler for a binary JCR value.
     * 
     * @author Sergiy Shyrkov
     */
    private class JCRURLStreamHandler extends URLStreamHandler {

        private Value jcrValue;

        public JCRURLStreamHandler(Value binaryValue) {
            super();
            jcrValue = binaryValue;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new URLConnection(u) {

                @Override
                public void connect() throws IOException {
                    // do nothing
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    try {
                        return jcrValue.getBinary().getStream();
                    } catch (RepositoryException e) {
                        throw new IOException(e);
                    }
                }

            };
        }

    }

    @Override
    public Object getObject(Value value) {
        try {
            return new URL("ocm", "localhost", -1, "", new JCRURLStreamHandler(value));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Value getValue(ValueFactory valueFactory, Object propValue) {
        if (propValue == null) {
            return null;
        }

        try {
            return super.getValue(valueFactory, ((URL) propValue).openStream());
        } catch (IOException ex) {
            throw new IncorrectAtomicTypeException("Impossible to create binary value from URL!", ex);
        }
    }
}
