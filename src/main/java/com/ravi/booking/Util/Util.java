package com.ravi.booking.Util;

import org.ravi.model.Section;
import org.ravi.model.Ticket;
import org.ravi.model.User;

import com.ravi.booking.model.TicketEntity;
import com.ravi.booking.model.UserEntity;

public class Util {
  public static Ticket toProto(TicketEntity entity) {
    if (entity == null) {
      return null;
    }
    Ticket.Builder ticketBuilder = Ticket.newBuilder();
    ticketBuilder.setId(entity.getId()); // Convert Long to int
    ticketBuilder.setPrice(entity.getPrice());
    ticketBuilder.setSeat(entity.getSeatNo());
    if (entity.getUser() != null) {
      UserEntity userEntity = entity.getUser();
      User.Builder userBuilder = User.newBuilder();

      userBuilder.setId(userEntity.getId())
          .setFirstName(userEntity.getFirstName())
          .setLastName(userEntity.getLastName())
          .setEmail(userEntity.getEmail());

      ticketBuilder.setUser(userBuilder.build());
    }
    ticketBuilder.setFrom(entity.getSource());
    ticketBuilder.setTo(entity.getDestination());
    if(entity.getSection()== com.ravi.booking.model.Section.A)
      ticketBuilder.setSection(Section.S_A);
    else
      ticketBuilder.setSection(Section.S_B);

    return ticketBuilder.build();
  }

  public static TicketEntity toEntity(Ticket ticket) {
    if (ticket == null) {
      return null;
    }
    TicketEntity entity = new TicketEntity();
    entity.setPrice(ticket.getPrice());

    // Handle User mapping (if needed)
    if (ticket.hasUser()) {
      User user = ticket.getUser();
      UserEntity userEntity = new UserEntity();
      // Set UserEntity fields based on User data (e.g., id, firstName, lastName)
      userEntity.setId(user.getId()); // Convert int to Long (assuming id in User)
      // ... set other userEntity fields
      entity.setUser(userEntity);
    }

    return entity;
  }

}
