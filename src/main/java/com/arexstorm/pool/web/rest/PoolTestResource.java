package com.arexstorm.pool.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.arexstorm.pool.domain.PoolTest;

import com.arexstorm.pool.repository.PoolTestRepository;
import com.arexstorm.pool.web.rest.errors.BadRequestAlertException;
import com.arexstorm.pool.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing PoolTest.
 */
@RestController
@RequestMapping("/api")
public class PoolTestResource {

    private final Logger log = LoggerFactory.getLogger(PoolTestResource.class);

    private static final String ENTITY_NAME = "poolTest";

    private final PoolTestRepository poolTestRepository;

    public PoolTestResource(PoolTestRepository poolTestRepository) {
        this.poolTestRepository = poolTestRepository;
    }

    /**
     * POST  /pool-tests : Create a new poolTest.
     *
     * @param poolTest the poolTest to create
     * @return the ResponseEntity with status 201 (Created) and with body the new poolTest, or with status 400 (Bad Request) if the poolTest has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/pool-tests")
    @Timed
    public ResponseEntity<PoolTest> createPoolTest(@Valid @RequestBody PoolTest poolTest) throws URISyntaxException {
        log.debug("REST request to save PoolTest : {}", poolTest);
        if (poolTest.getId() != null) {
            throw new BadRequestAlertException("A new poolTest cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PoolTest result = poolTestRepository.save(poolTest);
        return ResponseEntity.created(new URI("/api/pool-tests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /pool-tests : Updates an existing poolTest.
     *
     * @param poolTest the poolTest to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated poolTest,
     * or with status 400 (Bad Request) if the poolTest is not valid,
     * or with status 500 (Internal Server Error) if the poolTest couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/pool-tests")
    @Timed
    public ResponseEntity<PoolTest> updatePoolTest(@Valid @RequestBody PoolTest poolTest) throws URISyntaxException {
        log.debug("REST request to update PoolTest : {}", poolTest);
        if (poolTest.getId() == null) {
            return createPoolTest(poolTest);
        }
        PoolTest result = poolTestRepository.save(poolTest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, poolTest.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pool-tests : get all the poolTests.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of poolTests in body
     */
    @GetMapping("/pool-tests")
    @Timed
    public List<PoolTest> getAllPoolTests() {
        log.debug("REST request to get all PoolTests");
        return poolTestRepository.findAll();
        }

    /**
     * GET  /pool-tests/:id : get the "id" poolTest.
     *
     * @param id the id of the poolTest to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the poolTest, or with status 404 (Not Found)
     */
    @GetMapping("/pool-tests/{id}")
    @Timed
    public ResponseEntity<PoolTest> getPoolTest(@PathVariable Long id) {
        log.debug("REST request to get PoolTest : {}", id);
        PoolTest poolTest = poolTestRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(poolTest));
    }

    /**
     * DELETE  /pool-tests/:id : delete the "id" poolTest.
     *
     * @param id the id of the poolTest to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/pool-tests/{id}")
    @Timed
    public ResponseEntity<Void> deletePoolTest(@PathVariable Long id) {
        log.debug("REST request to delete PoolTest : {}", id);
        poolTestRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
