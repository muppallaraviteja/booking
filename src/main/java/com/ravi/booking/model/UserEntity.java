package com.ravi.booking.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserEntity {

  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private TicketEntity ticketEntities;

  public TicketEntity getTicketEntities() {
    return ticketEntities;
  }

  public void setTicketEntities(TicketEntity ticketEntities) {
    this.ticketEntities = ticketEntities;
  }

  public UserEntity() {
  }

  public UserEntity(String lastName, String firstName, String email) {
    this.lastName = lastName;
    this.firstName = firstName;
    this.email = email;
    this.id = UUID.randomUUID().toString();
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

}
