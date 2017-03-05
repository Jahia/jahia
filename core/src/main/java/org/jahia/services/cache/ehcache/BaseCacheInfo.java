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
package org.jahia.services.cache.ehcache;

import java.io.Serializable;

/**
 * Base cache information and statistics.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseCacheInfo implements Serializable {
    private static final long serialVersionUID = -2400940258119747703L;

    private String config;

    private long hitCount;

    private double hitRatio;

    private long localDiskSize;

    private long localDiskSizeInBytes;

    private String localDiskSizeInBytesHumanReadable;

    private long localHeapSize;

    private long localHeapSizeInBytes;

    private String localHeapSizeInBytesHumanReadable;

    private long localOffHeapSize;

    private long localOffHeapSizeInBytes;

    private String localOffHeapSizeInBytesHumanReadable;

    private long missCount;

    private String name;

    private boolean overflowToDisk;

    private boolean overflowToOffHeap;

    private long size;

    public long getAccessCount() {
        return hitCount + missCount;
    }

    public String getConfig() {
        return config;
    }

    public long getHitCount() {
        return hitCount;
    }

    public double getHitRatio() {
        return hitRatio;
    }

    public long getLocalDiskSize() {
        return localDiskSize;
    }

    public long getLocalDiskSizeInBytes() {
        return localDiskSizeInBytes;
    }

    public String getLocalDiskSizeInBytesHumanReadable() {
        return localDiskSizeInBytesHumanReadable;
    }

    public long getLocalHeapSize() {
        return localHeapSize;
    }

    public long getLocalHeapSizeInBytes() {
        return localHeapSizeInBytes;
    }

    public String getLocalHeapSizeInBytesHumanReadable() {
        return localHeapSizeInBytesHumanReadable;
    }

    public long getLocalOffHeapSize() {
        return localOffHeapSize;
    }

    public long getLocalOffHeapSizeInBytes() {
        return localOffHeapSizeInBytes;
    }

    public String getLocalOffHeapSizeInBytesHumanReadable() {
        return localOffHeapSizeInBytesHumanReadable;
    }

    public long getMissCount() {
        return missCount;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isOverflowToDisk() {
        return overflowToDisk;
    }

    public boolean isOverflowToOffHeap() {
        return overflowToOffHeap;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public void setHitRatio(double hitRatio) {
        this.hitRatio = hitRatio;
    }

    public void setLocalDiskSize(long localDiskSize) {
        this.localDiskSize = localDiskSize;
    }

    public void setLocalDiskSizeInBytes(long localDiskSizeInBytes) {
        this.localDiskSizeInBytes = localDiskSizeInBytes;
    }

    public void setLocalDiskSizeInBytesHumanReadable(String localDiskSizeInBytesHumanReadable) {
        this.localDiskSizeInBytesHumanReadable = localDiskSizeInBytesHumanReadable;
    }

    public void setLocalHeapSize(long localHeapSize) {
        this.localHeapSize = localHeapSize;
    }

    public void setLocalHeapSizeInBytes(long localHeapSizeInBytes) {
        this.localHeapSizeInBytes = localHeapSizeInBytes;
    }

    public void setLocalHeapSizeInBytesHumanReadable(String localHeapSizeInBytesHumanReadable) {
        this.localHeapSizeInBytesHumanReadable = localHeapSizeInBytesHumanReadable;
    }

    public void setLocalOffHeapSize(long localOffHeapSize) {
        this.localOffHeapSize = localOffHeapSize;
    }

    public void setLocalOffHeapSizeInBytes(long localOffHeapSizeInBytes) {
        this.localOffHeapSizeInBytes = localOffHeapSizeInBytes;
    }

    public void setLocalOffHeapSizeInBytesHumanReadable(String localOffHeapSizeInBytesHumanReadable) {
        this.localOffHeapSizeInBytesHumanReadable = localOffHeapSizeInBytesHumanReadable;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOverflowToDisk(boolean overflowToDisk) {
        this.overflowToDisk = overflowToDisk;
    }

    public void setOverflowToOffHeap(boolean overflowToOffHeap) {
        this.overflowToOffHeap = overflowToOffHeap;
    }

    public void setSize(long size) {
        this.size = size;
    }
}