package com.ravi.booking.server;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.ravi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "grpc.server.port=-1",
    "grpc.server.in-process-name=integration-test",
    "grpc.client.user-service.address=in-process:integration-test"
})
public class TicketServerTest {

  @GrpcClient("in-process")
  private TrainBookingServiceGrpc.TrainBookingServiceBlockingStub stub;

  @Autowired
  private TicketServer ticketServer;


  @Test
  public void testPurchaseTicket_SeatUnavailableException() {
    User userProto = User.newBuilder()
        .setFirstName("Jane")
        .setLastName("Doe")
        .setEmail("jane.doe@example.com")
        .build();

    PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
        .setUser(userProto)
        .setJourney(Journey.newBuilder().setFrom("Paris").setTo("Berlin").build())
        .build();

    assertThrows(StatusRuntimeException.class, () -> {
      stub.purchaseTicket(request);
    });
  }

  @Test
  public void testGetReceipt_TicketNotFoundException() {
    GetReceiptRequest request = GetReceiptRequest.newBuilder()
        .setTicketId("invalidTicketId")
        .build();

    assertThrows(StatusRuntimeException.class, () -> {
      stub.getReceipt(request);
    });
  }

  @Test
  public void testRemoveUser_TicketNotFoundException() {
    RemoveUserRequest request = RemoveUserRequest.newBuilder()
        .setTicketId("invalidTicketId")
        .build();

    assertThrows(StatusRuntimeException.class, () -> {
      stub.removeUser(request);
    });
  }

  @Test
  public void testModifySeat_SeatUnavailableException() {
    ModifySeatRequest request = ModifySeatRequest.newBuilder()
        .setTicketId("validTicketIdWithNoSeatsAvailable")
        .build();

    assertThrows(StatusRuntimeException.class, () -> {
      stub.modifySeat(request);
    });
  }

  @Test
  public void testModifySeat_TicketNotFoundException() {
    ModifySeatRequest request = ModifySeatRequest.newBuilder()
        .setTicketId("invalidTicketId")
        .build();

    assertThrows(StatusRuntimeException.class, () -> {
      stub.modifySeat(request);
    });
  }


  /*@Test
  public void testGetReceipt_Success() {
    User userProto = User.newBuilder()
        .setFirstName("John")
        .setLastName("Doe")
        .setEmail("john.doe@example.com")
        .build();

    PurchaseTicketRequest purchaseRequest = PurchaseTicketRequest.newBuilder()
        .setUser(userProto)
        .setJourney(Journey.newBuilder().setFrom("London").setTo("Paris").build())
        .build();

    PurchaseTicketResponse purchaseResponse = stub.purchaseTicket(purchaseRequest);
    String ticketId = purchaseResponse.getTicketId();

    GetReceiptRequest receiptRequest = GetReceiptRequest.newBuilder()
        .setTicketId(ticketId)
        .build();

    // Call the getReceipt method and get the response
    GetReceiptResponse receiptResponse = stub.getReceipt(receiptRequest);

    // Step 3: Validate the Receipt
    assertNotNull(receiptResponse.getTicket().getId());
    assertEquals(ticketId, receiptResponse.getTicket().getId());

    // You can also validate other fields, such as journey details, user details, etc.
    assertEquals("John Doe", receiptResponse.getTicket().getUser().getFirstName() + " " + receiptResponse.getTicket().getUser().getLastName());
    assertEquals("London", receiptResponse.getTicket().getFrom());
    assertEquals("Paris", receiptResponse.getTicket().getTo());
  }

  public void testGetUsersBySection_Success() {
    GetUsersBySectionRequest request = GetUsersBySectionRequest.newBuilder()
        .setSection(Section.S_A)
        .build();

    GetUsersBySectionResponseList response = stub.getUsersBySection(request);
    assertFalse(response.getResponseList().isEmpty());
  }

  @Test
  public void testRemoveUser_Success() {
    RemoveUserRequest request = RemoveUserRequest.newBuilder()
        .setTicketId("validTicketId")
        .build();

    RemoveUserResponse response = stub.removeUser(request);
    assertTrue(response.getRemoved());
  }

  @Test
  public void testModifySeat_Success() {
    ModifySeatRequest request = ModifySeatRequest.newBuilder()
        .setTicketId("validTicketId")
        .build();

    ModifySeatResponse response = stub.modifySeat(request);
    assertNotNull(response.getNewSeatNumber());
  }*/

/*  @Test
  public void testPurchaseTicket_Success() {
    User userProto = User.newBuilder()
        .setFirstName("John")
        .setLastName("Doe")
        .setEmail("john.doe@example.com")
        .build();

    PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
        .setUser(userProto)
        .setJourney(Journey.newBuilder().setFrom("London").setTo("France").build())
        .build();

    PurchaseTicketResponse response = stub.purchaseTicket(request);
    assertNotNull(response.getTicketId());
  }*/
}
