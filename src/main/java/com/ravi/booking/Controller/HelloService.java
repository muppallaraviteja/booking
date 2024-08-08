package com.ravi.booking.Controller;


import org.ravi.model.HelloReply;
import org.ravi.model.HelloRequest;
import org.ravi.model.MyServiceGrpc.MyServiceImplBase;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

//
@GrpcService
public class HelloService extends MyServiceImplBase {
  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    HelloReply reply = HelloReply.newBuilder()
        .setMessage("Hello ==> " + request.getName())
        .build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }
}
