/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.utils.xml.betwixt;

/**
 * The base class from which all descriptor beans are derived.
 *
 * Summit's XML configuration files are parse into descriptor beans
 * and the descriptor beans are processed to configure Summit.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class BaseDescriptor
{
    /**
     * Display name to use for this descriptor.
     */
    private String name;

    /**
     * Id to use for this descriptor.
     */
    private String id;

    /**
     * Give object that have not been given an explicit unique id
     * one that will keep betwixt happy.
     */
    private static int uniqueId;

    /**
     * Sets the name attribute
     *
     * @param name the new name value
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the name attribute
     *
     * @return the name attribute
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the id attribute of the BaseDescriptor object
     *
     * @param id the new id value
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Gets the id attribute
     *
     * @return the id attribute
     */
    public String getId()
    {
        if (id == null)
        {
            id = Integer.toString(uniqueId++);
        }

        return id;
    }

    /**
     * Return a string suitable for display/debugging
     *
     * @return the name attribute as a default
     */
    public String toString()
    {
        return name;
    }

    /**
     * Whether the passed object is the same as this one. In this case
     * the id is the unique qualifier. So two objects are equal
     * if they have equal id's
     * @param o any object
     * @return true if o is the same as this object, false otherwise
     */
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (getClass() != o.getClass())
        {
            return false;
        }

        if (getId() != null)
        {
            return getId().equals(((BaseDescriptor) o).getId());
        }
        else
        {
            return ((BaseDescriptor) o).getId() == null;
        }
    }

    /**
     * Provides the hashCode of this object, which is determined by simply
     * delegating the responsibility to the name property
     * @return the hashCode of the name if not null, otherwise delegate to the
     * parent class
     */
    public int hashCode()
    {
        if (getId() != null)
        {
            return getId().hashCode();
        }
        else
        {
            return super.hashCode();
        }
    }
}
