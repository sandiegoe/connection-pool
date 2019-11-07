package com.arexstorm.pool.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.arexstorm.pool.domain.Monitor;

import com.arexstorm.pool.repository.MonitorRepository;
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
 * REST controller for managing Monitor.
 */
@RestController
@RequestMapping("/api")
public class MonitorResource {

    private final Logger log = LoggerFactory.getLogger(MonitorResource.class);

    private static final String ENTITY_NAME = "monitor";

    private final MonitorRepository monitorRepository;

    public MonitorResource(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    /**
     * POST  /monitors : Create a new monitor.
     *
     * @param monitor the monitor to create
     * @return the ResponseEntity with status 201 (Created) and with body the new monitor, or with status 400 (Bad Request) if the monitor has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/monitors")
    @Timed
    public ResponseEntity<Monitor> createMonitor(@Valid @RequestBody Monitor monitor) throws URISyntaxException {
        log.debug("REST request to save Monitor : {}", monitor);
        if (monitor.getId() != null) {
            throw new BadRequestAlertException("A new monitor cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Monitor result = monitorRepository.save(monitor);
        return ResponseEntity.created(new URI("/api/monitors/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /monitors : Updates an existing monitor.
     *
     * @param monitor the monitor to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated monitor,
     * or with status 400 (Bad Request) if the monitor is not valid,
     * or with status 500 (Internal Server Error) if the monitor couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/monitors")
    @Timed
    public ResponseEntity<Monitor> updateMonitor(@Valid @RequestBody Monitor monitor) throws URISyntaxException {
        log.debug("REST request to update Monitor : {}", monitor);
        if (monitor.getId() == null) {
            return createMonitor(monitor);
        }
        Monitor result = monitorRepository.save(monitor);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, monitor.getId().toString()))
            .body(result);
    }

    /**
     * GET  /monitors : get all the monitors.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of monitors in body
     */
    @GetMapping("/monitors")
    @Timed
    public List<Monitor> getAllMonitors() {
        log.debug("REST request to get all Monitors");
        return monitorRepository.findAll();
        }

    /**
     * GET  /monitors/:id : get the "id" monitor.
     *
     * @param id the id of the monitor to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the monitor, or with status 404 (Not Found)
     */
    @GetMapping("/monitors/{id}")
    @Timed
    public ResponseEntity<Monitor> getMonitor(@PathVariable Long id) {
        log.debug("REST request to get Monitor : {}", id);
        Monitor monitor = monitorRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(monitor));
    }

    /**
     * DELETE  /monitors/:id : delete the "id" monitor.
     *
     * @param id the id of the monitor to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/monitors/{id}")
    @Timed
    public ResponseEntity<Void> deleteMonitor(@PathVariable Long id) {
        log.debug("REST request to delete Monitor : {}", id);
        monitorRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
