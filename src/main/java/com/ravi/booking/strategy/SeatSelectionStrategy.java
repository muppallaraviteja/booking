package com.ravi.booking.strategy;

import java.util.Map;
import java.util.Set;

import com.ravi.booking.model.Section;

public interface SeatSelectionStrategy {
  int selectSeat(Section section,  Set<Integer> allocatedSeats);


}
