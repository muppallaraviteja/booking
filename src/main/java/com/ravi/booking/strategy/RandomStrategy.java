package com.ravi.booking.strategy;

import com.ravi.booking.model.Section;

import java.util.*;

public class RandomStrategy implements SeatSelectionStrategy {

  private static final int MAX_SEATS = 50;
  private final Random random = new Random();

  @Override
  public int selectSeat(Section section, Set<Integer> allocatedSeats) {
    if (allocatedSeats.size() >= MAX_SEATS) {
      // All seats are taken, return -1 to indicate no available seats
      return -1;
    }

    int seat;
    do {
      seat = random.nextInt(MAX_SEATS) + 1;
    } while (allocatedSeats.contains(seat));

    allocatedSeats.add(seat);
    return seat;
  }
}

