package com.example.campaignservice.repository;

import com.example.campaignservice.domain.entity.Campaign;
import com.example.campaignservice.domain.enums.CampaignStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Campaign c where c.id = :id")
    Optional<Campaign> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select c
            from Campaign c
            where c.status = :status
              and c.startTime <= :now
              and c.endTime >= :now
              and c.usedCount < c.quota
            order by c.startTime asc
            """)
    List<Campaign> findEligibleActiveCampaigns(@Param("status") CampaignStatus status, @Param("now") Instant now);
}