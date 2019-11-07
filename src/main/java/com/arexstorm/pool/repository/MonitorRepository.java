package com.arexstorm.pool.repository;

import com.arexstorm.pool.domain.Monitor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Monitor entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

}
