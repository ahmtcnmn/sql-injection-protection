package com.ahmtcnmn.manager.Search;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;

import com.ahmtcnmn.manager.model.Events;

public class EventSearch {

    public static Specification<Events> hasAgentId(Long agentId) {
        return (root, query, criteriaBuilder) -> {
            if (agentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("agent").get("id"), agentId);
        };
    }

    public static Specification<Events> hasType(String type){
        return (root,query, criteriaBuilder)->{
            if(type==null || type.isEmpty()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("eventType"), type);
        };
    }

    public static Specification<Events> severityBetween(Integer minSeverity, Integer maxSeverity){
        return (root,query, criteriaBuilder)->{
            if(minSeverity==null && maxSeverity==null){
                return criteriaBuilder.conjunction();
            }
            if(minSeverity!=null && maxSeverity!=null){
                return criteriaBuilder.between(root.get("severity"), minSeverity, maxSeverity);
            }
            if(minSeverity!=null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("severity"), minSeverity);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("severity"), maxSeverity);
        };
    }

    public static Specification<Events> timestampBetween(Instant startTimestamp, Instant endTimestamp){
        return (root,query, criteriaBuilder)->{
            if(startTimestamp==null && endTimestamp==null){
                return criteriaBuilder.conjunction();
            }
            if(startTimestamp!=null && endTimestamp!=null){
                return criteriaBuilder.between(root.get("timestamp"), startTimestamp, endTimestamp);
            }
            if(startTimestamp!=null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startTimestamp);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endTimestamp);
        };
    }

    public static Specification<Events> rawDataContains(String rawData){
        return (root,query, criteriaBuilder)->{
            if(rawData==null || rawData.isEmpty()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("rawData"), "%" + rawData + "%");
        };
    }

    public static Specification<Events> hasSourceIp(String sourceIp) {
        return (root, query, criteriaBuilder) -> {
            if (sourceIp == null || sourceIp.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("sourceIp"), sourceIp);
        };
    }
    
}
