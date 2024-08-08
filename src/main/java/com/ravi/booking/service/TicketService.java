package com.ravi.booking.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.ravi.model.GetReceiptRequest;
import org.ravi.model.GetUsersBySectionRequest;
import org.ravi.model.GetUsersBySectionResponse;
import org.ravi.model.GetUsersBySectionResponseList;
import org.ravi.model.PurchaseTicketRequest;
import org.ravi.model.Ticket;
import org.springframework.stereotype.Service;

import com.ravi.booking.Util.Util;
import com.ravi.booking.model.Section;
import com.ravi.booking.model.TicketEntity;
import com.ravi.booking.model.UserEntity;
import com.ravi.booking.repository.TicketRepository;
import com.ravi.booking.repository.UserRepository;
import com.ravi.booking.strategy.RandomStrategy;
import com.ravi.booking.strategy.SeatSelectionStrategy;

@Service
public class TicketService {
  private final UserRepository userRepository;
  private final TicketRepository ticketRepository;
  private final SeatSelectionStrategy selectionStrategy;

  public TicketService(UserRepository userRepository, TicketRepository ticketRepository) {
    this.userRepository = userRepository;
    this.ticketRepository = ticketRepository;
    this.selectionStrategy = new RandomStrategy();
  }

  public String purchaseTicket(PurchaseTicketRequest request) {

    UserEntity user = userRepository.findByEmail(request.getUser().getEmail());
    if (user == null) {
      user = new UserEntity(request.getUser().getLastName(),request.getUser().getFirstName(), request.getUser().getEmail());
      user = userRepository.save(user);
    }

    Section section = new Random().nextBoolean() ? Section.A : Section.B;
    int seatNumber = selectionStrategy.selectSeat(section, ticketRepository.getAllocatedSeats(section));
    double price = getPrice(section);

    TicketEntity ticket = new TicketEntity( user,price, section, seatNumber, request.getJourney().getTo(),request.getJourney().getFrom());
    ticket = ticketRepository.save(ticket);
    user.setTicketEntities(ticket);
    return ticket.getId();
    //
  }

  public Ticket getReceipt(String ticketId) {
    TicketEntity ticket = ticketRepository.findById(ticketId);
    return Util.toProto(ticket);
  }

  public void removeUserBooking(String ticketId) {
    ticketRepository.delete(ticketId);

  }



  public List<TicketEntity> getUsersList(Section section) {
    return ticketRepository.findBySection(section);

  }

  public Ticket modifySeat(String ticketId) {
    TicketEntity ticket = ticketRepository.findById(ticketId);
    Section section = new Random().nextBoolean() ? Section.A : Section.B;
    int seatNumber = selectionStrategy.selectSeat(section, ticketRepository.getAllocatedSeats(section));
    ticket.setSeatNo(seatNumber);
    return Util.toProto(ticket);

  }

  public double getPrice(Section section){
    if(section==Section.A)
      return 50.0;
    else
      return 100.0;
  }
}
