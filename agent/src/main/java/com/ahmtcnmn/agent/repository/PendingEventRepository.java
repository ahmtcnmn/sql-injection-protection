package com.ahmtcnmn.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ahmtcnmn.agent.model.PendingEvent;

public interface PendingEventRepository extends JpaRepository<PendingEvent, Long> {
    
}