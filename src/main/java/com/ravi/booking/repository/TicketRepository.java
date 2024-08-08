package com.ravi.booking.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ravi.booking.model.Section;
import com.ravi.booking.model.TicketEntity;

@Component
public class TicketRepository{
  Map<String,TicketEntity> map = new HashMap<>();

  public TicketEntity save(TicketEntity entity){

    map.put(entity.getId(), entity);
    return entity;
  }

  public TicketEntity findById(String ticketId) {
    if(map.containsKey(ticketId))
      return map.get(ticketId);
    return null;
  }

  public List<TicketEntity> findBySection(Section section) {
    return map.values().stream()
        .filter(ticket -> ticket.getSection() == section)
        .collect(Collectors.toList());
  }
}