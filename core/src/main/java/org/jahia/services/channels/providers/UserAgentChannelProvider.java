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
package org.jahia.services.channels.providers;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelProvider;
import org.jahia.services.channels.ChannelService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic channel provider configured through Spring files
 */
public class UserAgentChannelProvider implements ChannelProvider, InitializingBean, DisposableBean, BeanNameAware {

    public static final String USER_AGENT_HEADER_NAME = "user-agent";

    private int priority;
    private String beanName;

    private Map<String,Channel> channels = new HashMap<String,Channel>();
    private Map<Pattern,Channel> userAgentChannels = new HashMap<Pattern,Channel>();

    // the following list is used for Spring initialization, it is not used later on.
    List<Channel> channelList = new ArrayList<Channel>();

    private ChannelService channelService;

    public void setChannelList(List<Channel> channelList) {
        this.channelList = channelList;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void afterPropertiesSet() throws Exception {
        for (Channel channel : channelList) {
            channels.put(channel.getIdentifier(), channel);
            if (channel.hasCapabilityValue("userAgentPattern")) {
                Pattern curPattern = Pattern.compile(channel.getCapability("userAgentPattern"));
                userAgentChannels.put(curPattern, channel);
            }
        }
        channelService.addProvider(this);
    }

    public Map<String, String> getChannelCapabilities(String identifier) {
        if (channels.containsKey(identifier)) {
            return channels.get(identifier).getCapabilities();
        }
        return null;
    }

    public String resolveChannel(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER_NAME);
        if (userAgent != null) {
            for (Map.Entry<Pattern,Channel> entry : userAgentChannels.entrySet()) {
                Pattern curPattern = entry.getKey();
                Matcher m = curPattern.matcher(userAgent);
                if (m.matches()) {
                    return entry.getValue().getIdentifier();
                }
            }
        }
        return null;
    }

    public List<String> getAllChannels() {
        return Collections.unmodifiableList(new ArrayList<String>(channels.keySet()));
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getFallBack(String identifier) {
        return channels.containsKey(identifier) ? channels.get(identifier).getFallBack() : null;
    }

    public boolean isVisible(String identifier) {
        return channels.containsKey(identifier) && channels.get(identifier).isVisible();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) return false;
        return ((UserAgentChannelProvider) obj).getBeanName().equals(beanName);
    }

    @Override
    public int hashCode() {
        return beanName != null ? beanName.hashCode() : 0;
    }

    @Override
    public void destroy() throws Exception {
        if (JahiaContextLoaderListener.isRunning()) {
            channelService.removeProvider(this);
        }
    }
}
