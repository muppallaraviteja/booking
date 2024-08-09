package com.ravi.booking.service;

import com.ravi.booking.exception.SeatUnavailableException;
import com.ravi.booking.exception.TicketNotFoundException;
import com.ravi.booking.model.Section;
import com.ravi.booking.model.TicketEntity;
import com.ravi.booking.model.UserEntity;
import com.ravi.booking.repository.TicketRepository;
import com.ravi.booking.repository.UserRepository;
import com.ravi.booking.strategy.SeatSelectionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ravi.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TicketServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TicketRepository ticketRepository;

  @Mock
  private SeatSelectionStrategy selectionStrategy;

  @InjectMocks
  private TicketService ticketService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testPurchaseTicket() {
    User userProto = User.newBuilder()
        .setFirstName("John")
        .setLastName("Doe")
        .setEmail("john.doe@example.com")
        .build();
    PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
        .setUser(userProto)
        .setJourney(Journey.newBuilder()
            .setFrom("London")
            .setTo("France")
            .build())
        .build();

    UserEntity user = new UserEntity("Doe", "John", "john.doe@example.com");
    TicketEntity ticket = new TicketEntity(user, 50.0, Section.A, 1, "France", "London");

    when(userRepository.findByEmail(anyString())).thenReturn(null);
    when(userRepository.save(any(UserEntity.class))).thenReturn(user);
    when(selectionStrategy.selectSeat(eq(Section.A), anySet())).thenReturn(1);
    when(ticketRepository.save(any(TicketEntity.class))).thenReturn(ticket);

    String ticketId = ticketService.purchaseTicket(request);

    assertNotNull(ticketId);
    verify(userRepository).save(any(UserEntity.class));
    verify(ticketRepository).save(any(TicketEntity.class));
  }

  @Test
  public void testGetReceipt() {
    // Create a user and ticket entity
    UserEntity user = new UserEntity("Doe", "John", "john.doe@example.com");
    TicketEntity ticket = new TicketEntity(user, 50.0, Section.A, 1, "France", "London");

    // Manually set the ticket ID
    String ticketId = "9fa92051-12ef-4e5a-8c18-ab6e3cac0e66"; // or use UUID.randomUUID().toString() for unique ID
    ticket.setId(ticketId);

    // Mock the behavior of the repository
    when(ticketRepository.findById(ticketId)).thenReturn(ticket);

    // Call the method under test
    Ticket receipt = ticketService.getReceipt(ticketId);

    // Verify the results
    assertNotNull(receipt);
    assertEquals("France", receipt.getFrom());
    assertEquals("London", receipt.getTo());
    assertEquals(50.0, receipt.getPrice(), 0.01);
    assertEquals(org.ravi.model.Section.S_A, receipt.getSection());

    // Verify the repository interaction
    verify(ticketRepository).findById(ticketId);
  }

  @Test
  public void testRemoveUserBooking() {
    TicketEntity ticket = new TicketEntity();
    when(ticketRepository.findById(anyString())).thenReturn(ticket);

    ticketService.removeUserBooking("ticketId");

    verify(ticketRepository).delete(anyString());
  }

  @Test
  public void testGetUsersList() {
    TicketEntity ticket1 = new TicketEntity();
    TicketEntity ticket2 = new TicketEntity();
    when(ticketRepository.findBySection(any(Section.class))).thenReturn(Arrays.asList(ticket1, ticket2));

    List<TicketEntity> usersList = ticketService.getUsersList(Section.A);

    assertNotNull(usersList);
    assertEquals(2, usersList.size());
    verify(ticketRepository).findBySection(any(Section.class));
  }

  @Test
  public void testModifySeat() {
    UserEntity user = new UserEntity("Doe", "John", "john.doe@example.com");
    TicketEntity ticket = new TicketEntity(user, 50.0, Section.A, 1, "France", "London");

    // Manually set the ticket ID if necessary
    String ticketId = "9fa92051-12ef-4e5a-8c18-ab6e3cac0e66";
    ticket.setId(ticketId);

    // Mock the behavior of the repository and selection strategy
    when(ticketRepository.findById(ticketId)).thenReturn(ticket);
    when(selectionStrategy.selectSeat(any(Section.class), anySet())).thenReturn(2);

    // Call the method under test
    Ticket updatedTicket = ticketService.modifySeat(ticketId);

    // Verify the results
    assertNotNull(updatedTicket);
    assertNotEquals(1, updatedTicket.getSeat());
    assertEquals(ticketId, updatedTicket.getId());

    // Verify the repository interaction
    verify(ticketRepository).findById(ticketId);
    verify(ticketRepository).save(ticket);
  }



  @Test
  public void testGetReceiptThrowsTicketNotFoundException() {
    when(ticketRepository.findById(anyString())).thenReturn(null);

    assertThrows(TicketNotFoundException.class, () -> ticketService.getReceipt("ticketId"));
  }

  @Test
  public void testRemoveUserBookingThrowsTicketNotFoundException() {
    when(ticketRepository.findById(anyString())).thenReturn(null);

    assertThrows(TicketNotFoundException.class, () -> ticketService.removeUserBooking("ticketId"));
  }

/*  @Test
  public void testPurchaseTicketThrowsSeatUnavailableException() {
    User userProto = User.newBuilder()
        .setFirstName("John")
        .setLastName("Doe")
        .setEmail("john.doe@example.com")
        .build();
    PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
        .setUser(userProto)
        .setJourney(Journey.newBuilder()
            .setFrom("London")
            .setTo("France")
            .build())
        .build();

    when(userRepository.findByEmail(anyString())).thenReturn(null);
    when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity("Doe", "John", "john.doe@example.com"));
    when(selectionStrategy.selectSeat(eq(Section.A), anySet())).thenReturn(-1);
    when(selectionStrategy.selectSeat(eq(Section.B), anySet())).thenReturn(-1);

    SeatUnavailableException exception = assertThrows(SeatUnavailableException.class, () -> ticketService.purchaseTicket(request));
    assertEquals("No seats available in either section A or B", exception.getMessage());
  }

  @Test
  public void testModifySeatThrowsSeatUnavailableException() {
    UserEntity user = new UserEntity("Doe", "John", "john.doe@example.com");
    TicketEntity ticket = new TicketEntity(user, 50.0, Section.A, 1, "France", "London");
    when(ticketRepository.findById(anyString())).thenReturn(ticket);
    when(selectionStrategy.selectSeat(eq(Section.A), anySet())).thenReturn(-1);
    when(selectionStrategy.selectSeat(eq(Section.B), anySet())).thenReturn(-1);

    assertThrows(SeatUnavailableException.class, () -> ticketService.modifySeat("ticketId"));
  }*/
}
