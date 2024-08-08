package com.ravi.booking.controller;


import org.ravi.model.GetReceiptRequest;
import org.ravi.model.GetReceiptResponse;
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
    String ticketId = ticketService.purchaseTicket(request);
    PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder().setTicketId(ticketId).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getReceipt(GetReceiptRequest request,
      StreamObserver<GetReceiptResponse> responseObserver) {
    // Call service layer
    Ticket ticket = ticketService.getReceipt(request);
    GetReceiptResponse response = GetReceiptResponse.newBuilder().setTicket(ticket).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

/*


  @Override
  public void getUsersBySection(GetUsersBySectionRequest request, StreamObserver<UsersResponse> responseObserver) {
    // Call service layer
  }

  @Override
  public void removeUser(RemoveUserRequest request, StreamObserver<Empty> responseObserver) {
    // Call service layer
  }

  @Override
  public void modifySeat(ModifySeatRequest request, StreamObserver<Empty> responseObserver) {
    // Call service layer
  }
*/


}
