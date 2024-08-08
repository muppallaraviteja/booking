package com.ravi.booking.controller;


import org.ravi.model.PurchaseTicketRequest;
import org.ravi.model.PurchaseTicketResponse;
import org.ravi.model.Ticket;
import org.ravi.model.TrainBookingServiceGrpc;

import com.ravi.booking.service.TicketService;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class TicketController extends TrainBookingServiceGrpc.TrainBookingServiceImplBase {

  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @Override
  public void purchaseTicket(PurchaseTicketRequest request,
      StreamObserver<PurchaseTicketResponse> responseObserver) {
    Ticket ticket = ticketService.purchaseTicket(request);
    PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder().setTicket(ticket).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
