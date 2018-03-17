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

import static com.linecorp.armeria.common.thrift.ThriftSerializationFormats.JSON;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.thrift.TBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.main.HelloThriftService;
import com.linecorp.armeria.main.HelloThriftService.hello_args;
import com.linecorp.armeria.server.PathMapping;
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.tomcat.TomcatService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.HttpServiceRegistrationBean;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

@SpringBootApplication
public class SampleApplication {

    /**
     * Register gRPC service.
     * TODO(ide) Add ServiceRegistrationBean for GRPC.
     */
    @Bean
    ArmeriaServerConfigurator serviceInitializer(HelloGrpcApiHandler handler) {
        return sb -> sb.service("/grpc",
                                new GrpcServiceBuilder().addService(handler)
                                                        .enableUnframedRequests(true)
                                                        .build());
    }

    @Bean
    HttpServiceRegistrationBean springMvcTomcatService(
            final ServletWebServerApplicationContext applicationContext) {
        final TomcatWebServer container =
                (TomcatWebServer) applicationContext.getWebServer();
        Connector tomcatConnector = container.getTomcat().getConnector();
        if (tomcatConnector == null) {
            try {
                Field serviceConnectorsField = TomcatWebServer.class.getDeclaredField(
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
                .setPathMapping(PathMapping.ofPrefix("/tomcat"));
    }

    @Bean
    ThriftServiceRegistrationBean thriftService(HelloThriftService.AsyncIface helloService) {
        return new ThriftServiceRegistrationBean()
                .setServiceName("tbinary")
                .setService(THttpService.of(helloService))
                .setPath("/thrift");
    }

    @Bean
    ThriftServiceRegistrationBean tjsonService(HelloThriftService.AsyncIface helloService) {
        return new ThriftServiceRegistrationBean()
                .setServiceName("tjson")
                .setService(THttpService.of(helloService, JSON))
                .setPath("/thrift.json")
                .setExampleRequests(createSampleRequests());
    }

    private static List<TBase<?, ?>> createSampleRequests() {
        return ImmutableList.of(new hello_args().setName("world"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
