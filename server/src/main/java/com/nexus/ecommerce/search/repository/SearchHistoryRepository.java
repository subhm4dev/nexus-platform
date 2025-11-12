package com.nexus.ecommerce.search.repository;

import com.nexus.ecommerce.search.entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Search History Repository
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    
    Page<SearchHistory> findByUserIdAndTenantIdOrderByCreatedAtDesc(
        UUID userId, 
        UUID tenantId, 
        Pageable pageable
    );
    
    @Query("SELECT sh.query, COUNT(sh) as count FROM SearchHistory sh " +
           "WHERE sh.tenantId = :tenantId " +
           "AND sh.createdAt >= :since " +
           "GROUP BY sh.query " +
           "ORDER BY count DESC")
    List<Object[]> findPopularQueries(
        @Param("tenantId") UUID tenantId,
        @Param("since") LocalDateTime since
    );
}

