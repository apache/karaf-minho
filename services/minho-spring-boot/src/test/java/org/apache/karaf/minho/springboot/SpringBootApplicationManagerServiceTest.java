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
package org.apache.karaf.minho.springboot;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.config.Application;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SpringBootApplicationManagerServiceTest {

    @Test
    public void simpleTypeCheck() throws Exception {
        ConfigService configService = new ConfigService();
        List<Application> applications = new ArrayList<>();
        Application application = new Application();
        application.setType("spring-boot");
        application.setName("my-test-app");
        application.setUrl("mvn:foo/bar/1.0");
        applications.add(application);
        configService.setApplications(applications);

        SpringBootApplicationManagerService service = new SpringBootApplicationManagerService();

        List<Application> loaded = service.getApplications(configService);

        Assertions.assertEquals(1, loaded.size());
        Assertions.assertEquals("my-test-app", loaded.get(0).getName());
    }

}
