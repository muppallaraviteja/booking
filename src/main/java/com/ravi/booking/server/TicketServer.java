package com.ravi.booking.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.ravi.booking.exception.SeatUnavailableException;
import com.ravi.booking.exception.TicketNotFoundException;
import com.ravi.booking.service.TicketService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class TicketServer extends TrainBookingServiceGrpc.TrainBookingServiceImplBase {

  private static final Logger logger = LoggerFactory.getLogger(TicketServer.class);

  private final TicketService ticketService;

  public TicketServer(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @Override
  public void purchaseTicket(PurchaseTicketRequest request,
      StreamObserver<PurchaseTicketResponse> responseObserver) {
    logger.info("Received purchaseTicket request: {}", request);
    try {
      String ticketId = ticketService.purchaseTicket(request);
      PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
          .setTicketId(ticketId)
          .build();
      logger.info("Successfully purchased ticket: {}", ticketId);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (SeatUnavailableException e) {
      logger.error("Seat unavailable: {}", e.getMessage());
      responseObserver.onError(new StatusRuntimeException(Status.UNAVAILABLE.withDescription(e.getMessage())));
    } catch (Exception e) {
      logger.error("Internal error during ticket purchase: {}", e.getMessage(), e);
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void getReceipt(GetReceiptRequest request,
      StreamObserver<GetReceiptResponse> responseObserver) {
    logger.info("Received getReceipt request: {}", request);
    try {
      Ticket ticket = ticketService.getReceipt(request.getTicketId());
      GetReceiptResponse response = GetReceiptResponse.newBuilder().setTicket(ticket).build();
      logger.info("Successfully retrieved receipt for ticket: {}", request.getTicketId());
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (TicketNotFoundException e) {
      logger.warn("Ticket not found: {}", e.getMessage());
      responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
    } catch (Exception e) {
      logger.error("Internal error during getReceipt: {}", e.getMessage(), e);
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void getUsersBySection(GetUsersBySectionRequest request,
      StreamObserver<GetUsersBySectionResponseList> responseObserver) {
    logger.info("Received getUsersBySection request: {}", request);
    try {
      com.ravi.booking.model.Section section = (request.getSection() == Section.S_A)
          ? com.ravi.booking.model.Section.A
          : com.ravi.booking.model.Section.B;

      List<GetUsersBySectionResponse> list = ticketService.getUsersList(section).stream()
          .map(a -> GetUsersBySectionResponse.newBuilder()
              .setUserName(a.getUser().getFirstName() + " " + a.getUser().getLastName())
              .setSeatNo(a.getSeatNo()).build()
          ).toList();
      var response = GetUsersBySectionResponseList.newBuilder().addAllResponse(list).build();
      logger.info("Successfully retrieved users by section: {}", section);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Internal error during getUsersBySection: {}", e.getMessage(), e);
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void removeUser(RemoveUserRequest request,
      StreamObserver<RemoveUserResponse> responseObserver) {
    logger.info("Received removeUser request: {}", request);
    try {
      ticketService.removeUserBooking(request.getTicketId());
      RemoveUserResponse response = RemoveUserResponse.newBuilder()
          .setRemoved(true)
          .build();
      logger.info("Successfully removed user booking for ticket: {}", request.getTicketId());
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (TicketNotFoundException e) {
      logger.warn("Ticket not found for removal: {}", e.getMessage());
      responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
    } catch (Exception e) {
      logger.error("Internal error during removeUser: {}", e.getMessage(), e);
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

  @Override
  public void modifySeat(ModifySeatRequest request, StreamObserver<ModifySeatResponse> responseObserver) {
    logger.info("Received modifySeat request: {}", request);
    try {
      Ticket ticket = ticketService.modifySeat(request.getTicketId());
      var response = ModifySeatResponse.newBuilder()
          .setNewSeatNumber(ticket.getSeat())
          .setSection(ticket.getSection())
          .setTickerId(ticket.getId()).build();
      logger.info("Successfully modified seat for ticket: {}", request.getTicketId());
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (SeatUnavailableException e) {
      logger.error("Seat unavailable during modifySeat: {}", e.getMessage());
      responseObserver.onError(new StatusRuntimeException(Status.UNAVAILABLE.withDescription(e.getMessage())));
    } catch (TicketNotFoundException e) {
      logger.warn("Ticket not found during modifySeat: {}", e.getMessage());
      responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
    } catch (Exception e) {
      logger.error("Internal error during modifySeat: {}", e.getMessage(), e);
      responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
    }
  }

}
