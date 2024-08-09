package com.ravi.booking.Client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.ravi.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class GrpcTicketClient {

  private static final Logger logger = LoggerFactory.getLogger(GrpcTicketClient.class);

  private final TrainBookingServiceGrpc.TrainBookingServiceBlockingStub blockingStub;
  private final ManagedChannel channel;

  public GrpcTicketClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build());
  }

  GrpcTicketClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = TrainBookingServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    try {
      if (channel != null) {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        logger.info("Channel shutdown completed.");
      }
    } catch (InterruptedException e) {
      logger.error("Channel shutdown interrupted", e);
      throw e;
    }
  }

  public String purchaseTicket(PurchaseTicketRequest request) {
    try {
      PurchaseTicketResponse response = blockingStub.purchaseTicket(request);
      logger.info("Ticket purchased with ID: {}", response.getTicketId());
      return response.getTicketId();
    } catch (StatusRuntimeException e) {
      logger.error("RPC failed: {}", e.getStatus(), e);
    }
    return null;
  }

  public void getReceipt(GetReceiptRequest request) {
    try {
      GetReceiptResponse response = blockingStub.getReceipt(request);
      logger.info("Receipt retrieved for ticket ID: {}", response.getTicket().getId());
    } catch (StatusRuntimeException e) {
      logger.error("RPC failed: {}", e.getStatus(), e);
    }
  }

  public void removeUser(RemoveUserRequest request) {
    try {
      RemoveUserResponse response = blockingStub.removeUser(request);
      logger.info("User removed for ticket ID: {}", request.getTicketId());
    } catch (StatusRuntimeException e) {
      logger.error("RPC failed: {}", e.getStatus(), e);
    }
  }

  public void modifySeat(ModifySeatRequest request) {
    try {
      ModifySeatResponse response = blockingStub.modifySeat(request);
      logger.info("Seat modified for ticket ID: {}", request.getTicketId());
    } catch (StatusRuntimeException e) {
      logger.error("RPC failed: {}", e.getStatus(), e);
    }
  }

  public void getUsersBySection(GetUsersBySectionRequest request) {
    try {
      GetUsersBySectionResponseList response = blockingStub.getUsersBySection(request);
      logger.info("Users retrieved for section: {}", request.getSection());
    } catch (StatusRuntimeException e) {
      logger.error("RPC failed: {}", e.getStatus(), e);
    }
  }

  public static void main(String[] args) {
    GrpcTicketClient client = new GrpcTicketClient("localhost", 6565);

    try {
      PurchaseTicketRequest purchaseRequest = PurchaseTicketRequest.newBuilder()
          .setUser(User.newBuilder().setFirstName("Ravi").setLastName("Muppalla").setEmail("ravi@gmail.com").build())
          .setJourney(Journey.newBuilder().setTo("Hyd").setFrom("Goa").build())
          .build();

      String ticketId = client.purchaseTicket(purchaseRequest);

      GetReceiptRequest receiptRequest = GetReceiptRequest.newBuilder()
          .setTicketId(ticketId)
          .build();

      client.getReceipt(receiptRequest);

      ModifySeatRequest modifySeatRequest = ModifySeatRequest.newBuilder()
          .setTicketId(ticketId)
          .build();

      client.modifySeat(modifySeatRequest);

      GetUsersBySectionRequest getUsersBySectionRequest = GetUsersBySectionRequest.newBuilder()
          .setSection(Section.S_A)
          .build();

      client.getUsersBySection(getUsersBySectionRequest);

      RemoveUserRequest removeUserRequest = RemoveUserRequest.newBuilder()
          .setTicketId(ticketId)
          .build();

      client.removeUser(removeUserRequest);

    } finally {
      try {
        client.shutdown();
      } catch (InterruptedException e) {
        logger.error("Shutdown interrupted", e);
      }
    }
  }
}
