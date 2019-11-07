package com.arexstorm.pool.web.rest;

import com.arexstorm.pool.PoolApp;

import com.arexstorm.pool.domain.PoolTest;
import com.arexstorm.pool.repository.PoolTestRepository;
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
 * Test class for the PoolTestResource REST controller.
 *
 * @see PoolTestResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PoolApp.class)
public class PoolTestResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAA";
    private static final String UPDATED_URL = "BBBBB";

    private static final String DEFAULT_POOL_SIZE = "AAAAAAAAAA";
    private static final String UPDATED_POOL_SIZE = "BBBBBBBBBB";

    @Autowired
    private PoolTestRepository poolTestRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPoolTestMockMvc;

    private PoolTest poolTest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PoolTestResource poolTestResource = new PoolTestResource(poolTestRepository);
        this.restPoolTestMockMvc = MockMvcBuilders.standaloneSetup(poolTestResource)
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
    public static PoolTest createEntity(EntityManager em) {
        PoolTest poolTest = new PoolTest()
            .name(DEFAULT_NAME)
            .url(DEFAULT_URL)
            .poolSize(DEFAULT_POOL_SIZE);
        return poolTest;
    }

    @Before
    public void initTest() {
        poolTest = createEntity(em);
    }

    @Test
    @Transactional
    public void createPoolTest() throws Exception {
        int databaseSizeBeforeCreate = poolTestRepository.findAll().size();

        // Create the PoolTest
        restPoolTestMockMvc.perform(post("/api/pool-tests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poolTest)))
            .andExpect(status().isCreated());

        // Validate the PoolTest in the database
        List<PoolTest> poolTestList = poolTestRepository.findAll();
        assertThat(poolTestList).hasSize(databaseSizeBeforeCreate + 1);
        PoolTest testPoolTest = poolTestList.get(poolTestList.size() - 1);
        assertThat(testPoolTest.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPoolTest.getUrl()).isEqualTo(DEFAULT_URL);
        assertThat(testPoolTest.getPoolSize()).isEqualTo(DEFAULT_POOL_SIZE);
    }

    @Test
    @Transactional
    public void createPoolTestWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = poolTestRepository.findAll().size();

        // Create the PoolTest with an existing ID
        poolTest.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPoolTestMockMvc.perform(post("/api/pool-tests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poolTest)))
            .andExpect(status().isBadRequest());

        // Validate the PoolTest in the database
        List<PoolTest> poolTestList = poolTestRepository.findAll();
        assertThat(poolTestList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = poolTestRepository.findAll().size();
        // set the field null
        poolTest.setName(null);

        // Create the PoolTest, which fails.

        restPoolTestMockMvc.perform(post("/api/pool-tests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poolTest)))
            .andExpect(status().isBadRequest());

        List<PoolTest> poolTestList = poolTestRepository.findAll();
        assertThat(poolTestList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPoolTests() throws Exception {
        // Initialize the database
        poolTestRepository.saveAndFlush(poolTest);

        // Get all the poolTestList
        restPoolTestMockMvc.perform(get("/api/pool-tests?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(poolTest.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL.toString())))
            .andExpect(jsonPath("$.[*].poolSize").value(hasItem(DEFAULT_POOL_SIZE.toString())));
    }

    @Test
    @Transactional
    public void getPoolTest() throws Exception {
        // Initialize the database
        poolTestRepository.saveAndFlush(poolTest);

        // Get the poolTest
        restPoolTestMockMvc.perform(get("/api/pool-tests/{id}", poolTest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(poolTest.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL.toString()))
            .andExpect(jsonPath("$.poolSize").value(DEFAULT_POOL_SIZE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPoolTest() throws Exception {
        // Get the poolTest
        restPoolTestMockMvc.perform(get("/api/pool-tests/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePoolTest() throws Exception {
        // Initialize the database
        poolTestRepository.saveAndFlush(poolTest);
        int databaseSizeBeforeUpdate = poolTestRepository.findAll().size();

        // Update the poolTest
        PoolTest updatedPoolTest = poolTestRepository.findOne(poolTest.getId());
        // Disconnect from session so that the updates on updatedPoolTest are not directly saved in db
        em.detach(updatedPoolTest);
        updatedPoolTest
            .name(UPDATED_NAME)
            .url(UPDATED_URL)
            .poolSize(UPDATED_POOL_SIZE);

        restPoolTestMockMvc.perform(put("/api/pool-tests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPoolTest)))
            .andExpect(status().isOk());

        // Validate the PoolTest in the database
        List<PoolTest> poolTestList = poolTestRepository.findAll();
        assertThat(poolTestList).hasSize(databaseSizeBeforeUpdate);
        PoolTest testPoolTest = poolTestList.get(poolTestList.size() - 1);
        assertThat(testPoolTest.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPoolTest.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testPoolTest.getPoolSize()).isEqualTo(UPDATED_POOL_SIZE);
    }

    @Test
    @Transactional
    public void updateNonExistingPoolTest() throws Exception {
        int databaseSizeBeforeUpdate = poolTestRepository.findAll().size();

        // Create the PoolTest

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPoolTestMockMvc.perform(put("/api/pool-tests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poolTest)))
            .andExpect(status().isCreated());

        // Validate the PoolTest in the database
        List<PoolTest> poolTestList = poolTestRepository.findAll();
        assertThat(poolTestList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePoolTest() throws Exception {
        // Initialize the database
        poolTestRepository.saveAndFlush(poolTest);
        int databaseSizeBeforeDelete = poolTestRepository.findAll().size();

        // Get the poolTest
        restPoolTestMockMvc.perform(delete("/api/pool-tests/{id}", poolTest.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<PoolTest> poolTestList = poolTestRepository.findAll();
        assertThat(poolTestList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PoolTest.class);
        PoolTest poolTest1 = new PoolTest();
        poolTest1.setId(1L);
        PoolTest poolTest2 = new PoolTest();
        poolTest2.setId(poolTest1.getId());
        assertThat(poolTest1).isEqualTo(poolTest2);
        poolTest2.setId(2L);
        assertThat(poolTest1).isNotEqualTo(poolTest2);
        poolTest1.setId(null);
        assertThat(poolTest1).isNotEqualTo(poolTest2);
    }
}
