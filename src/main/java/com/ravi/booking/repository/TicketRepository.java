package com.ravi.booking.repository;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.ravi.booking.model.TicketEntity;

@Component
public class TicketRepository{
  Map<String,TicketEntity> map = new HashMap<>();

  public TicketEntity save(TicketEntity entity){

    map.put(entity.getId(), entity);
    return entity;
  }

}