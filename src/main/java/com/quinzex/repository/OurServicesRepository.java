package com.quinzex.repository;

import com.quinzex.entity.OurServices;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OurServicesRepository extends JpaRepository<OurServices, Long> {
    @Query("SELECT s FROM OurServices s WHERE s.id > :cursor ORDER BY s.id ASC")
    List<OurServices> findNextServices(@Param("cursor") Long cursor, Pageable pageable);
}
