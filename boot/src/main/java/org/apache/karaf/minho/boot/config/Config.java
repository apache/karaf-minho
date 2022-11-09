/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.minho.boot.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Config {

    private Map<String, String> properties = new HashMap<>();
    private List<Profile> profiles = new ArrayList<>();
    private List<Application> applications = new ArrayList<>();

    public void merge(final Config config) {
        if (config == null) {
            return;
        }
        // TODO better merge (by id etc with error when merge can't be guessed probably)
        properties.putAll(config.getProperties());
        profiles.addAll(config.getProfiles());
        applications.addAll(config.getApplications());
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        return getProperty(key, this.properties, defaultValue);
    }

    protected static String getProperty(String key, Map<String, String> properties, String defaultValue) {
        String envKey = key.replaceAll("\\.", "_").toUpperCase();
        if (System.getenv(envKey) != null) {
            return System.getenv(envKey);
        }
        if (System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        if (properties.get(key) != null) {
            return properties.get(key);
        }
        return defaultValue;
    }

}
