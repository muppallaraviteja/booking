package com.ravi.booking.model;

import java.util.UUID;

public class TicketEntity {
  private String id;
  private UserEntity user;
  private double price;
  private Section section;
  private int seatNo;
  private String source;
  private String destination;

  public TicketEntity(UserEntity user, double price, Section section, int seatNo, String source,
      String destination) {
    this.user = user;
    this.price = price;
    this.section = section;
    this.seatNo = seatNo;
    this.source = source;
    this.destination = destination;
    this.id = UUID.randomUUID().toString();
  }

  public String getId() {
    return id;
  }

  public TicketEntity() {
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity userEntity) {
    this.user = userEntity;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }


  public int getSeatNo() {
    return seatNo;
  }

  public void setSeatNo(int seatNo) {
    this.seatNo = seatNo;
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String from) {
    this.destination = from;
  }
}
