package org.jahia.services.cache;

import org.jahia.registries.ServicesRegistry;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class ClassLoaderAwareCacheEntry implements Serializable {
    private Object value;
    private transient String moduleName;

    public ClassLoaderAwareCacheEntry(Object value, String moduleName) {
        this.value = value;
        this.moduleName = moduleName;
    }

    public Object getValue() {
        return value;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        moduleName = (String) in.readObject();
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(moduleName).getClassLoader());
        try {
            in.defaultReadObject();
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(moduleName);
        out.defaultWriteObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        //
    }

}

