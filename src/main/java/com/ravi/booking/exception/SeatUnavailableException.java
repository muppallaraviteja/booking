package com.ravi.booking.exception;

public class SeatUnavailableException extends RuntimeException {
  public SeatUnavailableException(String message) {
    super(message);
  }
}
