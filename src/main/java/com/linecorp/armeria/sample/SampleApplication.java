/*
 * Copyright 2017 Masahiro Ide
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.sample;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.context.annotation.Bean;

import com.linecorp.armeria.main.HelloService;
import com.linecorp.armeria.server.PathMapping;
import com.linecorp.armeria.server.http.tomcat.TomcatService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.HttpServiceRegistrationBean;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

@SpringBootApplication
public class SampleApplication {

    @Bean
    ArmeriaServerConfigurator serviceInitializer() {
        return sb -> {};
    }

    @Bean
    HttpServiceRegistrationBean springMvcTomcatService(final EmbeddedWebApplicationContext applicationContext) {
        final TomcatEmbeddedServletContainer container =
                (TomcatEmbeddedServletContainer) applicationContext.getEmbeddedServletContainer();
        Connector tomcatConnector = container.getTomcat().getConnector();
        if (tomcatConnector == null) {
            try {
                Field serviceConnectorsField = TomcatEmbeddedServletContainer.class.getDeclaredField(
                        "serviceConnectors");
                serviceConnectorsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<Service, Connector[]> connectors =
                        (Map<Service, Connector[]>) serviceConnectorsField.get(container);
                tomcatConnector = connectors.values().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException("Connectors not found"))[0];
            } catch (NoSuchFieldException | IllegalAccessException e1) {
                throw new IllegalStateException(e1);
            }
        }

        return new HttpServiceRegistrationBean()
                .setServiceName("springMvcTomcatService")
                .setService(TomcatService.forConnector(tomcatConnector)
                                         .decorate(LoggingService.newDecorator()))
                .setPathMapping(PathMapping.ofPrefix("/my-tomcat-service"));
    }

    @Bean
    ThriftServiceRegistrationBean thriftService(HelloService.AsyncIface helloService) {
        return new ThriftServiceRegistrationBean()
                .setServiceName("hello")
                .setService(THttpService.of(helloService))
                .setPath("/thrift");
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
