/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils.spring.http.converter.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jahia.exceptions.JahiaRuntimeException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Own implementation of the JSON (using Jackson 2) message converter that handles correctly class loading. It looks up the corresponding
 * message converter instance in OSGi and delegates the work to it. This implementation
 * overrides only several methods from the parent, which are using the {@link ObjectMapper} instance, to be able to delegate to the
 * corresponding message converter delegate (see {@link #PARAMETER_TYPES} for the methods, we use for delegation).
 *
 * @author Sergiy Shyrkov
 */
public class JahiaMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final Map<String, Class<?>[]> PARAMETER_TYPES;

    static {
        PARAMETER_TYPES = new HashMap<>(5);
        PARAMETER_TYPES.put("canRead", new Class<?>[] { Type.class, Class.class, MediaType.class });
        PARAMETER_TYPES.put("canWrite", new Class<?>[] { Class.class, MediaType.class });
        PARAMETER_TYPES.put("read", new Class<?>[] { Type.class, Class.class, HttpInputMessage.class });
        PARAMETER_TYPES.put("readInternal", new Class<?>[] { Class.class, HttpInputMessage.class });
        PARAMETER_TYPES.put("writeInternal", new Class<?>[] { Object.class, HttpOutputMessage.class });
    }

    // This is an instance of the MappingJackson2HttpMessageConverter, injected from the OSGi. In order to avoid class loading issue
    // between the DX core and OSGi where this class is present, we use here Object as a type and use reflection to call corresponding
    // methods on this delegate object.
    private Object delegate;

    // Function, that looks up the target method, when not found in the cache.
    private final Function<String, Method> methodProvider = name -> {
        try {
            Method targetMethod = delegate.getClass().getDeclaredMethod(name, PARAMETER_TYPES.get(name));
            if (!targetMethod.isAccessible()) {
                targetMethod.setAccessible(true);
            }
            return targetMethod;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new JahiaRuntimeException(e);
        }
    };

    // Cache of the methods on the delegate object, we are calling.
    private final Map<String, Method> methods = new ConcurrentHashMap<>();

    private Object call(Method method, Object... args) {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return (boolean) call(getMethod("canRead"), type, contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return (boolean) call(getMethod("canWrite"), clazz, mediaType);
    }

    private Method getMethod(final String methodName) {
        return methods.computeIfAbsent(methodName, methodProvider);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return call(getMethod("read"), type, contextClass, inputMessage);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return call(getMethod("readInternal"), clazz, inputMessage);
    }

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
        methods.clear();
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        call(getMethod("writeInternal"), object, outputMessage);
    }
}
