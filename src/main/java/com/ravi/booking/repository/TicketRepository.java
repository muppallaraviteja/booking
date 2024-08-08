package com.ravi.booking.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ravi.booking.model.SeatAssignment;
import com.ravi.booking.model.Section;
import com.ravi.booking.model.TicketEntity;

@Component
public class TicketRepository{
  private final Map<String,TicketEntity> map = new HashMap<>();
  private final Map<SeatAssignment, String> seatToTicketMap = new HashMap<>();
  private final Map<Section, Set<Integer>> allocatedSeats = new HashMap<>();


  public TicketRepository() {
    allocatedSeats.put(Section.A, new HashSet<>());
    allocatedSeats.put(Section.B, new HashSet<>());
  }
  public TicketEntity save(TicketEntity entity){

    map.put(entity.getId(), entity);
    return entity;
  }

  public TicketEntity findById(String ticketId) {
    if(map.containsKey(ticketId))
      return map.get(ticketId);
    return null;
  }

  public void assignSeat(String ticketId, int seatNumber, Section section) {
    SeatAssignment seatAssignment = new SeatAssignment(seatNumber, section);
    seatToTicketMap.put(seatAssignment, ticketId);
    allocatedSeats.get(section).add(seatNumber);
  }

  public List<TicketEntity> findBySection(Section section) {
    return map.values().stream()
        .filter(ticket -> ticket.getSection() == section)
        .collect(Collectors.toList());
  }

  public SeatAssignment getSeatAssignmentByTicketId(String ticketId) {
    for (Map.Entry<SeatAssignment, String> entry : seatToTicketMap.entrySet()) {
      if (entry.getValue().equals(ticketId)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public Set<Integer> getAllocatedSeats(Section section) {
    return allocatedSeats.get(section);
  }
}