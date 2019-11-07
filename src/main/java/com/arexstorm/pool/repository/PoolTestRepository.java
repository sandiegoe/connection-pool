package com.arexstorm.pool.repository;

import com.arexstorm.pool.domain.PoolTest;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the PoolTest entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PoolTestRepository extends JpaRepository<PoolTest, Long> {

}
