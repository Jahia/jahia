/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
import org.osgi.framework.BundleReference;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Own implementation of the JSON (using Jackson 2) message converter that handles correctly class loading. In case of an object, coming
 * from a bundle, it looks up the corresponding message converter instance in OSGi and delegates the work to it. This implementation
 * overrides only several methods from the parent, which are using the {@link ObjectMapper} instance, to be able to delegate to the
 * corresponding message converter delegate (see {@link #PARAMETER_TYPES} for the methods, we use for delegation).
 *
 * @author Sergiy Shyrkov
 */
public class JahiaMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final Map<String, Class<?>[]> PARAMETER_TYPES;
    static {
        PARAMETER_TYPES = new HashMap<>(5);
        PARAMETER_TYPES.put("canRead", new Class<?>[] {Type.class, Class.class, MediaType.class});
        PARAMETER_TYPES.put("canWrite", new Class<?>[] {Class.class, MediaType.class});
        PARAMETER_TYPES.put("read", new Class<?>[] {Type.class, Class.class, HttpInputMessage.class});
        PARAMETER_TYPES.put("readInternal", new Class<?>[] {Class.class, HttpInputMessage.class});
        PARAMETER_TYPES.put("writeInternal", new Class<?>[] {Object.class, HttpOutputMessage.class});
    }

    // This is an instance of the MappingJackson2HttpMessageConverter, injected from the OSGi. In order to avoid class loading issue
    // between the DX core and OSGi where this class is present, we use here Object as a type and use reflection to call corresponding
    // methods on this delegate object.
    private Object delegate;

    // Function, that looks up the target method, when not found in the cache.
    private Function<String, Method> methodProvider = new Function<String, Method>() {

        @Override
        public Method apply(String name) {
            try {
                Method targetMethod = delegate.getClass().getDeclaredMethod(name, PARAMETER_TYPES.get(name));
                if (!targetMethod.isAccessible()) {
                    targetMethod.setAccessible(true);
                }
                return targetMethod;
            } catch (NoSuchMethodException | SecurityException e) {
                throw new JahiaRuntimeException(e);
            }
        }
    };

    // Cache of the methods on the delegate object, we are calling.
    private Map<String, Method> methods = new ConcurrentHashMap<>();

    private Object call(Method method, Object... args) {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        if (shouldDelegate(type)) {
            return (boolean) call(getMethod("canRead"), type, contextClass, mediaType);
        }
        return super.canRead(type, contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (shouldDelegate(clazz)) {
            return (boolean) call(getMethod("canWrite"), clazz, mediaType);
        }
        return super.canWrite(clazz, mediaType);
    }

    private Method getMethod(final String methodName) {
        return methods.computeIfAbsent(methodName, methodProvider);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        if (shouldDelegate(type)) {
            return call(getMethod("read"), type, contextClass, inputMessage);
        }
        return super.read(type, contextClass, inputMessage);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        if (shouldDelegate(clazz)) {
            return call(getMethod("readInternal"), clazz, inputMessage);
        }
        return super.readInternal(clazz, inputMessage);
    }

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
        methods.clear();
    }

    protected boolean shouldDelegate(Object obj) {

        if (delegate != null && obj != null) {

            // We obtain the class loader for the supplied object (distinguishing, if it is a Class or an Object).
            ClassLoader classLoader = (obj instanceof Class) ? ((Class<?>) obj).getClassLoader() : obj.getClass().getClassLoader();

            // If the class loader is an instance of BundleReference, than it is a an OSGi class loader and we should delegate.
            return (classLoader instanceof BundleReference);
        }

        // If nothing of above is not applicable, then no need to delegate.
        return false;
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (shouldDelegate(object)) {
            call(getMethod("writeInternal"), object, outputMessage);
        } else {
            super.writeInternal(object, outputMessage);
        }
    }
}
