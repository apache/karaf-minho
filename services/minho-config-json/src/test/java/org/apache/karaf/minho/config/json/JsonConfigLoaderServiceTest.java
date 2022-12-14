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
package org.apache.karaf.minho.config.json;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.config.Application;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonConfigLoaderServiceTest {

    @Test
    public void loadingTestFromSystemProp() throws Exception {
        System.setProperty("minho.config", "target/test-classes/emptyrun.json");

        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ConfigService configService = new ConfigService();
        serviceRegistry.add(configService);
        JsonConfigLoaderService service = new JsonConfigLoaderService();
        service.onRegister(serviceRegistry);

        Config config = serviceRegistry.get(ConfigService.class);

        Assertions.assertEquals("bar", config.getProperty("foo"));
        Assertions.assertEquals(0, config.getProfiles().size());
        Assertions.assertEquals(0, config.getApplications().size());

        System.clearProperty("minho.config");
    }

    @Test
    public void loadingTestFromClasspath() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ConfigService configService = new ConfigService();
        serviceRegistry.add(configService);
        JsonConfigLoaderService service = new JsonConfigLoaderService();
        service.onRegister(serviceRegistry);

        Config config = serviceRegistry.get(Config.class);

        // properties
        Assertions.assertEquals("bar", config.getProperty("foo"));
        Assertions.assertTrue(Boolean.parseBoolean(config.getProperty("lifecycle.enabled")));
        Assertions.assertEquals("%m %n", config.getProperty("log.patternLayout"));
        Assertions.assertEquals("./osgi/cache", config.getProperty("osgi.storageDirectory"));
        Assertions.assertEquals(1, Long.parseLong(config.getProperty("osgi.priority")));

        // profiles
        Assertions.assertEquals(1, config.getProfiles().size());

        // applications
        Assertions.assertEquals(2, config.getApplications().size());
        Application springBootApp = config.getApplications().get(0);
        Assertions.assertEquals("/path/to/app/spring-boot.jar", springBootApp.getUrl());
        Assertions.assertEquals("spring-boot", springBootApp.getType());
        Assertions.assertTrue(Boolean.parseBoolean(springBootApp.getProperty("enableHttp")));
        Assertions.assertTrue(Boolean.parseBoolean(springBootApp.getProperty("enablePrometheus")));
    }

    @Test
    public void runTest() throws Exception {
        Minho minho = Minho.builder().build();
        minho.start();

        Config config = minho.getServiceRegistry().get(Config.class);

        Assertions.assertEquals(2, config.getApplications().size());
    }

}
