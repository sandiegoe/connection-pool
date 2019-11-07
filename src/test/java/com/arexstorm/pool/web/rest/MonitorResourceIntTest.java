package com.arexstorm.pool.web.rest;

import com.arexstorm.pool.PoolApp;

import com.arexstorm.pool.domain.Monitor;
import com.arexstorm.pool.repository.MonitorRepository;
import com.arexstorm.pool.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.arexstorm.pool.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the MonitorResource REST controller.
 *
 * @see MonitorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PoolApp.class)
public class MonitorResourceIntTest {

    private static final String DEFAULT_TEST = "AAAAAAAAAA";
    private static final String UPDATED_TEST = "BBBBBBBBBB";

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restMonitorMockMvc;

    private Monitor monitor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final MonitorResource monitorResource = new MonitorResource(monitorRepository);
        this.restMonitorMockMvc = MockMvcBuilders.standaloneSetup(monitorResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Monitor createEntity(EntityManager em) {
        Monitor monitor = new Monitor()
            .test(DEFAULT_TEST);
        return monitor;
    }

    @Before
    public void initTest() {
        monitor = createEntity(em);
    }

    @Test
    @Transactional
    public void createMonitor() throws Exception {
        int databaseSizeBeforeCreate = monitorRepository.findAll().size();

        // Create the Monitor
        restMonitorMockMvc.perform(post("/api/monitors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(monitor)))
            .andExpect(status().isCreated());

        // Validate the Monitor in the database
        List<Monitor> monitorList = monitorRepository.findAll();
        assertThat(monitorList).hasSize(databaseSizeBeforeCreate + 1);
        Monitor testMonitor = monitorList.get(monitorList.size() - 1);
        assertThat(testMonitor.getTest()).isEqualTo(DEFAULT_TEST);
    }

    @Test
    @Transactional
    public void createMonitorWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = monitorRepository.findAll().size();

        // Create the Monitor with an existing ID
        monitor.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMonitorMockMvc.perform(post("/api/monitors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(monitor)))
            .andExpect(status().isBadRequest());

        // Validate the Monitor in the database
        List<Monitor> monitorList = monitorRepository.findAll();
        assertThat(monitorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTestIsRequired() throws Exception {
        int databaseSizeBeforeTest = monitorRepository.findAll().size();
        // set the field null
        monitor.setTest(null);

        // Create the Monitor, which fails.

        restMonitorMockMvc.perform(post("/api/monitors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(monitor)))
            .andExpect(status().isBadRequest());

        List<Monitor> monitorList = monitorRepository.findAll();
        assertThat(monitorList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMonitors() throws Exception {
        // Initialize the database
        monitorRepository.saveAndFlush(monitor);

        // Get all the monitorList
        restMonitorMockMvc.perform(get("/api/monitors?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(monitor.getId().intValue())))
            .andExpect(jsonPath("$.[*].test").value(hasItem(DEFAULT_TEST.toString())));
    }

    @Test
    @Transactional
    public void getMonitor() throws Exception {
        // Initialize the database
        monitorRepository.saveAndFlush(monitor);

        // Get the monitor
        restMonitorMockMvc.perform(get("/api/monitors/{id}", monitor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(monitor.getId().intValue()))
            .andExpect(jsonPath("$.test").value(DEFAULT_TEST.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMonitor() throws Exception {
        // Get the monitor
        restMonitorMockMvc.perform(get("/api/monitors/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMonitor() throws Exception {
        // Initialize the database
        monitorRepository.saveAndFlush(monitor);
        int databaseSizeBeforeUpdate = monitorRepository.findAll().size();

        // Update the monitor
        Monitor updatedMonitor = monitorRepository.findOne(monitor.getId());
        // Disconnect from session so that the updates on updatedMonitor are not directly saved in db
        em.detach(updatedMonitor);
        updatedMonitor
            .test(UPDATED_TEST);

        restMonitorMockMvc.perform(put("/api/monitors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedMonitor)))
            .andExpect(status().isOk());

        // Validate the Monitor in the database
        List<Monitor> monitorList = monitorRepository.findAll();
        assertThat(monitorList).hasSize(databaseSizeBeforeUpdate);
        Monitor testMonitor = monitorList.get(monitorList.size() - 1);
        assertThat(testMonitor.getTest()).isEqualTo(UPDATED_TEST);
    }

    @Test
    @Transactional
    public void updateNonExistingMonitor() throws Exception {
        int databaseSizeBeforeUpdate = monitorRepository.findAll().size();

        // Create the Monitor

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restMonitorMockMvc.perform(put("/api/monitors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(monitor)))
            .andExpect(status().isCreated());

        // Validate the Monitor in the database
        List<Monitor> monitorList = monitorRepository.findAll();
        assertThat(monitorList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteMonitor() throws Exception {
        // Initialize the database
        monitorRepository.saveAndFlush(monitor);
        int databaseSizeBeforeDelete = monitorRepository.findAll().size();

        // Get the monitor
        restMonitorMockMvc.perform(delete("/api/monitors/{id}", monitor.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Monitor> monitorList = monitorRepository.findAll();
        assertThat(monitorList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Monitor.class);
        Monitor monitor1 = new Monitor();
        monitor1.setId(1L);
        Monitor monitor2 = new Monitor();
        monitor2.setId(monitor1.getId());
        assertThat(monitor1).isEqualTo(monitor2);
        monitor2.setId(2L);
        assertThat(monitor1).isNotEqualTo(monitor2);
        monitor1.setId(null);
        assertThat(monitor1).isNotEqualTo(monitor2);
    }
}
