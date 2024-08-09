package com.ravi.booking.service;

import com.ravi.booking.Util.Util;
import com.ravi.booking.exception.SeatUnavailableException;
import com.ravi.booking.exception.TicketNotFoundException;
import com.ravi.booking.model.SeatAssignment;
import com.ravi.booking.model.Section;
import com.ravi.booking.model.TicketEntity;
import com.ravi.booking.model.UserEntity;
import com.ravi.booking.repository.TicketRepository;
import com.ravi.booking.repository.UserRepository;
import com.ravi.booking.strategy.RandomStrategy;
import com.ravi.booking.strategy.SeatSelectionStrategy;
import org.ravi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TicketService {

  private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

  private final UserRepository userRepository;
  private final TicketRepository ticketRepository;
  private final SeatSelectionStrategy selectionStrategy;

  public TicketService(UserRepository userRepository, TicketRepository ticketRepository) {
    this.userRepository = userRepository;
    this.ticketRepository = ticketRepository;
    this.selectionStrategy = new RandomStrategy();
  }

  private UserEntity findOrCreateUser(PurchaseTicketRequest request) {
    logger.info("Finding or creating user with email: {}", request.getUser().getEmail());
    return Optional.ofNullable(userRepository.findByEmail(request.getUser().getEmail()))
        .orElseGet(() -> {
          UserEntity newUser = new UserEntity(
              request.getUser().getLastName(),
              request.getUser().getFirstName(),
              request.getUser().getEmail()
          );
          logger.info("User not found, creating new user: {}", newUser);
          return userRepository.save(newUser);
        });
  }

  private Section determineSection() {
    Section section = new Random().nextBoolean() ? Section.A : Section.B;
    logger.info("Determined section: {}", section);
    return section;
  }

  private SeatAssignment findAvailableSeat(Section section) {
    logger.info("Finding available seat in section: {}", section);
    int seatNumber = selectionStrategy.selectSeat(section, ticketRepository.getAllocatedSeats(section));
    if (seatNumber == -1) {
      logger.info("No seats available in section: {}, checking other section.", section);
      Section otherSection = (section == Section.A) ? Section.B : Section.A;
      seatNumber = selectionStrategy.selectSeat(otherSection, ticketRepository.getAllocatedSeats(otherSection));
      if (seatNumber == -1) {
        logger.error("No seats available in either section A or B");
        throw new SeatUnavailableException("No seats available in either section " + Section.A + " or " + Section.B);
      }
      section = otherSection;
    }
    logger.info("Seat found: {} in section: {}", seatNumber, section);
    return new SeatAssignment(seatNumber, section);
  }

  public String purchaseTicket(PurchaseTicketRequest request) {
    logger.info("Initiating ticket purchase for user: {}", request.getUser().getEmail());
    try {
      UserEntity user = findOrCreateUser(request);
      Section section = determineSection();
      SeatAssignment seatAssignment = findAvailableSeat(section);
      double price = getPrice(section);

      TicketEntity ticket = new TicketEntity(user, price, seatAssignment.getSection(), seatAssignment.getSeatNumber(), request.getJourney().getTo(), request.getJourney().getFrom());
      ticket = ticketRepository.save(ticket);

      if (ticket == null) {
        logger.error("Ticket not saved properly for user: {}", user.getEmail());
        throw new RuntimeException("Ticket not saved properly.");
      }

      user.setTicketEntities(ticket);
      logger.info("Ticket successfully purchased: {}", ticket.getId());
      return ticket.getId();  // Only return if ticket creation is successful
    } catch (SeatUnavailableException e) {
      logger.error("Seat unavailable: {}", e.getMessage(), e);
      throw e;  // Rethrow the specific exception
    } catch (Exception e) {
      logger.error("Error purchasing ticket for user: {}", request.getUser().getEmail(), e);
      throw new RuntimeException("Failed to purchase ticket: " + e.getMessage());
    }
  }

  public Ticket getReceipt(String ticketId) {
    logger.info("Retrieving receipt for ticket ID: {}", ticketId);
    try {
      TicketEntity ticket = ticketRepository.findById(ticketId);
      if (ticket == null) {
        logger.error("Ticket with ID {} not found", ticketId);
        throw new TicketNotFoundException("Ticket with ID " + ticketId + " not found");
      }
      logger.info("Receipt retrieved successfully for ticket ID: {}", ticketId);
      return Util.toProto(ticket);
    } catch (TicketNotFoundException e) {
      logger.error("Ticket with ID {} not found", ticketId, e);
      throw e;  // Rethrow the specific exception
    } catch (Exception e) {
      logger.error("Error retrieving ticket receipt for ID: {}", ticketId, e);
      throw new RuntimeException("Failed to retrieve ticket receipt: " + e.getMessage());
    }
  }

  public void removeUserBooking(String ticketId) {
    logger.info("Removing booking for ticket ID: {}", ticketId);
    try {
      TicketEntity ticket = ticketRepository.findById(ticketId);
      if (ticket == null) {
        logger.error("Ticket with ID {} not found", ticketId);
        throw new TicketNotFoundException("Ticket with ID " + ticketId + " not found");
      }
      ticketRepository.delete(ticketId);
      logger.info("Successfully removed booking for ticket ID: {}", ticketId);
    } catch (TicketNotFoundException e) {
      logger.error("Ticket with ID {} not found", ticketId, e);
      throw e;  // Rethrow specific exception
    } catch (Exception e) {
      logger.error("Error removing user booking for ticket ID: {}", ticketId, e);
      throw new RuntimeException("Failed to remove user booking: " + e.getMessage());
    }
  }

  public List<TicketEntity> getUsersList(Section section) {
    logger.info("Retrieving users list for section: {}", section);
    try {
      List<TicketEntity> tickets = ticketRepository.findBySection(section);
      logger.info("Users list retrieved successfully for section: {}", section);
      return tickets;
    } catch (Exception e) {
      logger.error("Error getting users list for section: {}", section, e);
      throw new RuntimeException("Failed to get users list: " + e.getMessage());
    }
  }

  public Ticket modifySeat(String ticketId) {
    logger.info("Modifying seat for ticket ID: {}", ticketId);
    try {
      TicketEntity ticket = ticketRepository.findById(ticketId);
      if (ticket == null) {
        logger.error("Ticket with ID {} not found", ticketId);
        throw new TicketNotFoundException("Ticket with ID " + ticketId + " not found");
      }

      SeatAssignment seatAssignment = findAvailableSeat(ticket.getSection());
      ticket.setSection(seatAssignment.getSection());
      ticket.setSeatNo(seatAssignment.getSeatNumber());
      ticketRepository.save(ticket);

      logger.info("Seat modification successful for ticket ID: {}", ticketId);
      return Util.toProto(ticket);
    } catch (TicketNotFoundException e) {
      logger.error("Ticket with ID {} not found", ticketId, e);
      throw e;  // Rethrow specific exception
    } catch (Exception e) {
      logger.error("Error modifying seat for ticket ID: {}", ticketId, e);
      throw new RuntimeException("Failed to modify seat: " + e.getMessage());
    }
  }

  public double getPrice(Section section) {
    double price = section == Section.A ? 50.0 : 100.0;
    logger.info("Price determined for section {}: {}", section, price);
    return price;
  }
}
