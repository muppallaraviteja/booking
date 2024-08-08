package com.ravi.booking.controller;


import java.util.List;

import org.ravi.model.GetReceiptRequest;
import org.ravi.model.GetReceiptResponse;
import org.ravi.model.GetUsersBySectionRequest;
import org.ravi.model.GetUsersBySectionResponse;
import org.ravi.model.GetUsersBySectionResponseList;
import org.ravi.model.PurchaseTicketRequest;
import org.ravi.model.PurchaseTicketResponse;
import org.ravi.model.Section;
import org.ravi.model.Ticket;
import org.ravi.model.TrainBookingServiceGrpc;

import com.ravi.booking.repository.TicketRepository;
import com.ravi.booking.service.TicketService;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class TicketController extends TrainBookingServiceGrpc.TrainBookingServiceImplBase {

  private final TicketService ticketService;
  private final TicketRepository ticketRepository;

  public TicketController(TicketService ticketService, TicketRepository ticketRepository) {
    this.ticketService = ticketService;
    this.ticketRepository = ticketRepository;
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
    Ticket ticket = ticketService.getReceipt(request.getTicketId());
    GetReceiptResponse response = GetReceiptResponse.newBuilder().setTicket(ticket).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getUsersBySection(GetUsersBySectionRequest request,
      StreamObserver<GetUsersBySectionResponseList> responseObserver) {
    com.ravi.booking.model.Section section = null;
    if(request.getSection()== Section.S_A){
      section = com.ravi.booking.model.Section.A;
    }
    else{
      section = com.ravi.booking.model.Section.B;

    }

    List<GetUsersBySectionResponse> list =ticketService.getUsersList(section).stream().map(a-> GetUsersBySectionResponse.newBuilder()
        .setUserName(a.getUser().getFirstName()+" "+a.getUser().getLastName())
        .setSeatNo(a.getSeatNo()).build()
    ).toList();
      var response  = GetUsersBySectionResponseList.newBuilder().addAllResponse(list).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
  }


/*



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
