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
package org.apache.karaf.core;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KarafConfig {

    private final static String DEFAULT_HOME = "${java.io.tmpdir}/karaf";
    private final static String DEFAULT_DATA = "${karaf.home}/data";
    private final static String DEFAULT_ETC = "${karaf.home}/etc";
    private final static String DEFAULT_CACHE = "${karaf.data}/cache";
    private final static boolean DEFAULT_CLEAR_CACHE = false;
    private final static int DEFAULT_BUNDLE_START_LEVEL = 80;
    private final static String DEFAULT_MAVEN_REPOSITORIES = "file:${user.home}/.m2/repository," +
            "file:${java.io.tmpdir}/karaf/system," +
            "https://repo1.maven.org/maven2";

    public String homeDirectory;
    public String dataDirectory;
    public String etcDirectory;
    public String cacheDirectory;
    public boolean clearCache;
    public int defaultBundleStartLevel;
    public String mavenRepositories;

    private KarafConfig(String homeDirectory,
                        String dataDirectory,
                        String etcDirectory,
                        String cacheDirectory,
                        boolean clearCache,
                        int defaultBundleStartLevel,
                        String mavenRepositories) {
        this.homeDirectory = homeDirectory;
        this.dataDirectory = dataDirectory;
        this.etcDirectory = etcDirectory;
        this.cacheDirectory = cacheDirectory;
        this.clearCache = clearCache;
        this.defaultBundleStartLevel = defaultBundleStartLevel;
        this.mavenRepositories = mavenRepositories;
    }

    public static KarafConfigBuilder builder() {
        return new KarafConfigBuilder();
    }

    public static class KarafConfigBuilder {

        private final Map<String, String> candidates;

        private String homeDirectory = DEFAULT_HOME;
        private String dataDirectory = DEFAULT_DATA;
        private String etcDirectory = DEFAULT_ETC;
        private String cacheDirectory = DEFAULT_CACHE;
        private boolean clearCache = DEFAULT_CLEAR_CACHE;
        private int defaultBundleStartLevel = DEFAULT_BUNDLE_START_LEVEL;
        private String mavenRepositories = DEFAULT_MAVEN_REPOSITORIES;

        KarafConfigBuilder() {
            candidates = new HashMap<>();
            for (String property : System.getProperties().stringPropertyNames()) {
                candidates.put(property, System.getProperty(property));
            }
        }

        public KarafConfigBuilder homeDirectory(String homeDirectory) {
            this.homeDirectory = homeDirectory;
            return this;
        }

        public KarafConfigBuilder dataDirectory(String dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public KarafConfigBuilder etcDirectory(String etcDirectory) {
            this.etcDirectory = etcDirectory;
            return this;
        }

        public KarafConfigBuilder cacheDirectory(String cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
            return this;
        }

        public KarafConfigBuilder clearCache(boolean clearCache) {
            this.clearCache = clearCache;
            return this;
        }

        public KarafConfigBuilder defaultBundleStartLevel(int defaultBundleStartLevel) {
            this.defaultBundleStartLevel = defaultBundleStartLevel;
            return this;
        }

        public KarafConfigBuilder mavenRepositories(String mavenRepositories) {
            this.mavenRepositories = mavenRepositories;
            return this;
        }

        public KarafConfig build() {
            loadSys("system.properties");

            // populate
            String home = get("karaf.home", "KARAF_HOME", homeDirectory);
            String data = get("karaf.data", "KARAF_DATA", dataDirectory);
            String etc = get("karaf.etc", "KARAF_ETC", etcDirectory);

            loadSys(etc + "/system.properties");

            String cache = get("karaf.cache", "KARAF_CACHE", cacheDirectory);
            String maven = get("karaf.repositories", "KARAF_MAVEN_REPOSITORIES", mavenRepositories);

            return new KarafConfig(home, data, etc, cache, clearCache, defaultBundleStartLevel, maven);
        }

        private void loadSys(String location) {
            File systemProperties = new File(location);
            if (systemProperties.exists()) {
                Properties properties = new Properties();
                try {
                    properties.load(new FileReader(systemProperties));
                    for (String key : properties.stringPropertyNames()) {
                        System.setProperty(key, (String) properties.get(key));
                    }
                } catch (Exception e) {
                    // no-op
                }
            }
        }

        private String get(String sysProperty, String env, String value) {
            String result = null;
            result = value;
            if (System.getProperty(sysProperty) != null) {
                result = System.getProperty(sysProperty);
            }
            if (System.getenv(env) != null) {
                result = System.getenv(env);
            }
            result = substitute(result, candidates);
            candidates.put(sysProperty, result);
            return result;
        }

        private String substitute(String raw, Map<String, String> candidates) {
            if (raw == null) {
                return null;
            }
            String result = raw;
            for (String property : candidates.keySet()) {
                result = result.replaceAll("\\$\\{" + property + "}", candidates.get(property));
            }
            return result;
        }

    }

}
