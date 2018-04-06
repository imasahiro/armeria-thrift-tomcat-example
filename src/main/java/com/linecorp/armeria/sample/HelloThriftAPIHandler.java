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

import static com.linecorp.armeria.common.util.Functions.voidFunction;

import java.util.concurrent.CompletableFuture;

import javax.inject.Named;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.main.HelloThriftService;

@Named("helloServer1")
public class HelloThriftAPIHandler implements HelloThriftService.AsyncIface {
    private final HelloThriftService.AsyncIface client;

    public HelloThriftAPIHandler() {
        this.client = ClientFactory.DEFAULT.newClient("tjson+http://localhost:8080/thrift.json",
                                                      HelloThriftService.AsyncIface.class);
    }

    @Override
    public void hello(String name, AsyncMethodCallback<String> handler) throws TException {
        client.hello(name, new AsyncMethodCallback<String>() {
            @Override
            public void onComplete(String response) {
                handler.onComplete(response);
            }

            @Override
            public void onError(Exception exception) {
                handler.onError(exception);
            }
        });
    }
}
