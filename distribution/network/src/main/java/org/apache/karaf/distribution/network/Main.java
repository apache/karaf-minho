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
package org.apache.karaf.distribution.network;

import org.apache.karaf.core.Karaf;
import org.apache.karaf.core.KarafConfig;

public class Main {

    private final String[] args;

    public static void main(String[] args) throws Exception {
        final Main main = new Main(args);
        main.launch();
    }

    public Main(String[] args) {
        this.args = args;
    }

    public void launch() throws Exception {
        KarafConfig config = KarafConfig.build();

        Karaf application = Karaf.withConfig(config);
        application.run();
        application.addModule("https://repo1.maven.org/maven2/org/ops4j/pax/url/pax-url-aether/2.6.3/pax-url");
    }

}