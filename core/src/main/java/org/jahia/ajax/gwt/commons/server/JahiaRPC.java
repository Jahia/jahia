/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.commons.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Custom RPC helper class to be able to have more control on the execution of GWT methods. For example, perform logging or occurred errors.
 * It is using parts of the code of the {@link RPC} one and adds a possibility to react on errors, which occur during invocation of target
 * service method. The handling of error is done in {@link #onError(Method, InvocationTargetException)} method.
 *
 * @author Sergiy Shyrkov
 */
final class JahiaRPC {

    private static final Logger logger = LoggerFactory.getLogger(JahiaRPC.class);

    static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
            SerializationPolicy serializationPolicy) throws SerializationException {
        return invokeAndEncodeResponse(target, serviceMethod, args, serializationPolicy, AbstractSerializationStream.DEFAULT_FLAGS);
    }

    private static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
            SerializationPolicy serializationPolicy, int flags) throws SerializationException {
        if (serviceMethod == null) {
            throw new NullPointerException("serviceMethod");
        }

        if (serializationPolicy == null) {
            throw new NullPointerException("serializationPolicy");
        }

        String responsePayload;
        try {
            Object result = serviceMethod.invoke(target, args);

            responsePayload = RPC.encodeResponseForSuccess(serviceMethod, result, serializationPolicy, flags);
        } catch (IllegalAccessException e) {
            SecurityException securityException = new SecurityException(
                    formatIllegalAccessErrorMessage(target, serviceMethod));
            securityException.initCause(e);
            throw securityException;
        } catch (IllegalArgumentException e) {
            SecurityException securityException = new SecurityException(
                    formatIllegalArgumentErrorMessage(target, serviceMethod, args));
            securityException.initCause(e);
            throw securityException;
        } catch (InvocationTargetException e) {
            onError(serviceMethod, e);
            // Try to encode the caught exception
            //
            Throwable cause = e.getCause();

            responsePayload = RPC.encodeResponseForFailure(serviceMethod, cause, serializationPolicy, flags);
        }

        return responsePayload;
    }

    private static String formatIllegalAccessErrorMessage(Object target, Method serviceMethod) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to access inaccessible method '");
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'");

        if (target != null) {
            sb.append(" on target '");
            sb.append(printTypeName(target.getClass()));
            sb.append("'");
        }

        sb.append("; this is either misconfiguration or a hack attempt");

        return sb.toString();
    }

    private static String formatIllegalArgumentErrorMessage(Object target, Method serviceMethod, Object[] args) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to invoke method '");
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'");

        if (target != null) {
            sb.append(" on target '");
            sb.append(printTypeName(target.getClass()));
            sb.append("'");
        }

        sb.append(" with invalid arguments");

        if (args != null && args.length > 0) {
            sb.append(Arrays.asList(args));
        }

        return sb.toString();
    }

    /**
     * Returns the source representation for a method signature.
     *
     * @param method method to get the source signature for
     * @return source representation for a method signature
     */
    private static String getSourceRepresentation(Method method) {
        return method.toString().replace('$', '.');
    }

    /**
     * Implements handling of an error, which occurred by executing the target GWT service.
     *
     * @param serviceMethod the service method which execution results in error
     * @param e the occurred exception
     */
    private static void onError(Method serviceMethod, InvocationTargetException e) {
        Throwable cause = e;
        if (e.getCause() != null) {
            cause = e.getCause();
        }
        logger.error("An error occurred calling the GWT service method " + serviceMethod + ". Cause: " + cause.getMessage(), cause);
    }

    /**
     * Straight copy from {@link com.google.gwt.dev.util.TypeInfo#getSourceRepresentation(Class)} to avoid runtime dependency on gwt-dev.
     */
    private static String printTypeName(Class<?> type) {
        // Primitives
        //
        if (type.equals(Integer.TYPE)) {
            return "int";
        } else if (type.equals(Long.TYPE)) {
            return "long";
        } else if (type.equals(Short.TYPE)) {
            return "short";
        } else if (type.equals(Byte.TYPE)) {
            return "byte";
        } else if (type.equals(Character.TYPE)) {
            return "char";
        } else if (type.equals(Boolean.TYPE)) {
            return "boolean";
        } else if (type.equals(Float.TYPE)) {
            return "float";
        } else if (type.equals(Double.TYPE)) {
            return "double";
        }

        // Arrays
        //
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return printTypeName(componentType) + "[]";
        }

        // Everything else
        //
        return type.getName().replace('$', '.');
    }

    private JahiaRPC() {
    }
}
