package com.linecorp.armeria.sample;

import javax.inject.Named;

import com.example.grpc.hello.Hello.HelloReply;
import com.example.grpc.hello.Hello.HelloRequest;
import com.example.grpc.hello.HelloGrpcServiceGrpc.HelloGrpcServiceImplBase;

import io.grpc.stub.StreamObserver;

@Named
public class HelloGrpcApiHandler extends HelloGrpcServiceImplBase {
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder().setMessage("world").build());
    }
}
