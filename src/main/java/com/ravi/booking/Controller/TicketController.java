package com.ravi.booking.controller;


import java.util.List;

import org.ravi.model.GetReceiptRequest;
import org.ravi.model.GetReceiptResponse;
import org.ravi.model.GetUsersBySectionRequest;
import org.ravi.model.GetUsersBySectionResponse;
import org.ravi.model.GetUsersBySectionResponseList;
import org.ravi.model.ModifySeatRequest;
import org.ravi.model.ModifySeatResponse;
import org.ravi.model.PurchaseTicketRequest;
import org.ravi.model.PurchaseTicketResponse;
import org.ravi.model.RemoveUserRequest;
import org.ravi.model.RemoveUserResponse;
import org.ravi.model.Section;
import org.ravi.model.Ticket;
import org.ravi.model.TrainBookingServiceGrpc;

import com.google.protobuf.Empty;
import com.ravi.booking.Util.Util;
import com.ravi.booking.exception.SeatUnavailableException;
import com.ravi.booking.exception.TicketNotFoundException;
import com.ravi.booking.model.TicketEntity;
import com.ravi.booking.repository.TicketRepository;
import com.ravi.booking.service.TicketService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
    try {
      String ticketId = ticketService.purchaseTicket(request);
      PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
          .setTicketId(ticketId)
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (SeatUnavailableException e) {
      responseObserver.onError(new StatusRuntimeException(Status.UNAVAILABLE.withDescription(e.getMessage())));
    } catch (Exception e) {
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void getReceipt(GetReceiptRequest request,
      StreamObserver<GetReceiptResponse> responseObserver) {

    try {
      Ticket ticket = ticketService.getReceipt(request.getTicketId());
      GetReceiptResponse response = GetReceiptResponse.newBuilder().setTicket(ticket).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }catch (TicketNotFoundException e) {
      responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
    } catch (Exception e) {
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void getUsersBySection(GetUsersBySectionRequest request,
      StreamObserver<GetUsersBySectionResponseList> responseObserver) {
    try {
      com.ravi.booking.model.Section section = null;
      if (request.getSection() == Section.S_A) {
        section = com.ravi.booking.model.Section.A;
      } else {
        section = com.ravi.booking.model.Section.B;

      }

      List<GetUsersBySectionResponse> list = ticketService.getUsersList(section).stream()
          .map(a -> GetUsersBySectionResponse.newBuilder()
              .setUserName(a.getUser().getFirstName() + " " + a.getUser().getLastName())
              .setSeatNo(a.getSeatNo()).build()
          ).toList();
      var response = GetUsersBySectionResponseList.newBuilder().addAllResponse(list).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
    catch (Exception e) {
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void removeUser(RemoveUserRequest request,
      StreamObserver<RemoveUserResponse> responseObserver) {
    try {
      ticketService.removeUserBooking(request.getTicketId());
      RemoveUserResponse response = RemoveUserResponse.newBuilder()
          .setRemoved(true)
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (TicketNotFoundException e) {
      responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
    } catch (Exception e) {
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void modifySeat(ModifySeatRequest request, StreamObserver<ModifySeatResponse> responseObserver) {
    try {
      Ticket ticket = ticketService.modifySeat(request.getTicketId());
      var response = ModifySeatResponse.newBuilder()
          .setNewSeatNumber(ticket.getSeat())
          .setSection(ticket.getSection())
          .setTickerId(ticket.getId()).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }catch (SeatUnavailableException e) {
      responseObserver.onError(new StatusRuntimeException(Status.UNAVAILABLE.withDescription(e.getMessage())));
    } catch (TicketNotFoundException e) {
      responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
    } catch (Exception e) {
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }


  }




}
