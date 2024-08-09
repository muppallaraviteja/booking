package com.ravi.booking.service;

import com.ravi.booking.Util.Util;
import com.ravi.booking.exception.SeatUnavailableException;
import com.ravi.booking.exception.TicketNotFoundException;
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

  public String purchaseTicket(PurchaseTicketRequest request) {
    try {
      UserEntity user = userRepository.findByEmail(request.getUser().getEmail());
      user = Optional.ofNullable(user)
          .orElseGet(() -> userRepository.save(new UserEntity(
              request.getUser().getLastName(),
              request.getUser().getFirstName(),
              request.getUser().getEmail()
          )));

      Section section = new Random().nextBoolean() ? Section.A : Section.B;
      int seatNumber = selectionStrategy.selectSeat(section, ticketRepository.getAllocatedSeats(section));

      if (seatNumber == -1) {
        Section otherSection = (section == Section.A) ? Section.B : Section.A;
        seatNumber = selectionStrategy.selectSeat(otherSection, ticketRepository.getAllocatedSeats(otherSection));

        if (seatNumber == -1) {
          throw new SeatUnavailableException("No seats available in either section " + Section.A + " or " + Section.B);
        }
        section = otherSection;
      }

      double price = getPrice(section);

      TicketEntity ticket = new TicketEntity(user, price, section, seatNumber, request.getJourney().getTo(), request.getJourney().getFrom());
      ticket = ticketRepository.save(ticket);

      if (ticket == null) {
        throw new RuntimeException("Ticket not saved properly.");
      }

      user.setTicketEntities(ticket);
      return ticket.getId();  // Only return if ticket creation is successful
    } catch (SeatUnavailableException e) {
      logger.error("Seat unavailable: {}", e.getMessage(), e);
      throw e;  // Rethrow the specific exception
    } catch (Exception e) {
      logger.error("Error purchasing ticket", e);
      throw new RuntimeException("Failed to purchase ticket: " + e.getMessage());
    }
  }

  public Ticket getReceipt(String ticketId) {
    try {
      TicketEntity ticket = ticketRepository.findById(ticketId);
      if (ticket == null) {
        throw new TicketNotFoundException("Ticket with ID " + ticketId + " not found");
      }
      return Util.toProto(ticket);
    }
    catch (TicketNotFoundException e) {
      logger.error("Ticket with ID {} not found", ticketId, e);
      throw e;  // Rethrow the specific exception
    }
    catch (Exception e) {
      logger.error("Error retrieving ticket receipt", e);
      throw new RuntimeException("Failed to retrieve ticket receipt: " + e.getMessage());
    }
  }

  public void removeUserBooking(String ticketId) {
    try {
      TicketEntity ticket = ticketRepository.findById(ticketId);
      if (ticket == null) {
        throw new TicketNotFoundException("Ticket with ID " + ticketId + " not found");
      }
      ticketRepository.delete(ticketId);
    } catch (TicketNotFoundException e) {
      logger.error("Ticket with ID {} not found", ticketId, e);
      throw e;  // Rethrow specific exception
    }catch (Exception e) {
      logger.error("Error removing user booking", e);
      throw new RuntimeException("Failed to remove user booking: " + e.getMessage());
    }
  }

  public List<TicketEntity> getUsersList(Section section) {
    try {
      return ticketRepository.findBySection(section);
    } catch (Exception e) {
      logger.error("Error getting users list", e);
      throw new RuntimeException("Failed to get users list: " + e.getMessage());
    }
  }

  public Ticket modifySeat(String ticketId) {
    try {
      TicketEntity ticket = ticketRepository.findById(ticketId);
      if (ticket == null) {
        throw new TicketNotFoundException("Ticket with ID " + ticketId + " not found");
      }

      Section section = new Random().nextBoolean() ? Section.A : Section.B;
      int seatNumber = selectionStrategy.selectSeat(section, ticketRepository.getAllocatedSeats(section));
      if(seatNumber == -1) {
        Section otherSection = (section == Section.A) ? Section.B : Section.A;
        seatNumber = selectionStrategy.selectSeat(otherSection, ticketRepository.getAllocatedSeats(otherSection));
        if (seatNumber == -1) {
          throw new SeatUnavailableException("No seats available in either section " + Section.A + " or " + Section.B);
        }
        section = otherSection;
      }
      ticket.setSection(section);
      ticket.setSeatNo(seatNumber);
      ticketRepository.save(ticket);
      return Util.toProto(ticket);
    } catch (Exception e) {
      logger.error("Error modifying seat", e);
      throw new RuntimeException("Failed to modify seat: " + e.getMessage());
    }
  }

  public double getPrice(Section section) {
    return section == Section.A ? 50.0 : 100.0;
  }
}
