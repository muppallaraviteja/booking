package com.ravi.booking.model;

import java.util.Objects;

public class SeatAssignment {
  private final int seatNumber;
  private final Section section;

  public SeatAssignment(int seatNumber, Section section) {
    this.seatNumber = seatNumber;
    this.section = section;
  }

  public int getSeatNumber() {
    return seatNumber;
  }

  public Section getSection() {
    return section;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SeatAssignment that = (SeatAssignment) o;
    return seatNumber == that.seatNumber && section == that.section;
  }

  @Override
  public int hashCode() {
    return Objects.hash(seatNumber, section);
  }

  @Override
  public String toString() {
    return "SeatAssignment{" +
        "seatNumber=" + seatNumber +
        ", section=" + section +
        '}';
  }
}
