package com.ravi.booking.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.ravi.model.GetReceiptRequest;
import org.ravi.model.PurchaseTicketRequest;
import org.ravi.model.Ticket;
import org.springframework.stereotype.Service;

import com.ravi.booking.Util.Util;
import com.ravi.booking.model.Section;
import com.ravi.booking.model.TicketEntity;
import com.ravi.booking.model.UserEntity;
import com.ravi.booking.repository.TicketRepository;
import com.ravi.booking.repository.UserRepository;

@Service
public class TicketService {
  private final UserRepository userRepository;
  private final TicketRepository ticketRepository;

  private final Map<Section, AtomicInteger> seatCounters = new HashMap<>();

  public TicketService(UserRepository userRepository, TicketRepository ticketRepository) {
    this.userRepository = userRepository;
    this.ticketRepository = ticketRepository;
    seatCounters.put(Section.A, new AtomicInteger(1));
    seatCounters.put(Section.B, new AtomicInteger(1));
  }

  public String purchaseTicket(PurchaseTicketRequest request) {
    // Create user and ticket
    // Save user and ticket to database
    UserEntity user = userRepository.findByEmail(request.getUser().getEmail());
    if (user == null) {
      user = new UserEntity(request.getUser().getLastName(),request.getUser().getFirstName(), request.getUser().getEmail());
      user = userRepository.save(user);
    }

    Random random = new Random();
    Section section = random.nextBoolean() ? Section.A : Section.B;
    double price = getPrice(section);
    int seatNumber = seatCounters.get(section).getAndIncrement();
    TicketEntity ticket = new TicketEntity( user,price, section, seatNumber, request.getJourney().getTo(),request.getJourney().getFrom());
    ticket = ticketRepository.save(ticket);
    user.addTicket(ticket);
    return ticket.getId();
    //
  }

  public Ticket getReceipt(GetReceiptRequest request) {
    TicketEntity ticket = ticketRepository.findById(request.getTicketId());
    return Util.toProto(ticket);
  }

  public double getPrice(Section section){
    if(section==Section.A)
      return 50.0;
    else
      return 100.0;
  }
}
