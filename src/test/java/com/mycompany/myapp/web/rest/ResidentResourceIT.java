package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Resident;
import com.mycompany.myapp.domain.Room;
import com.mycompany.myapp.repository.ResidentRepository;
import com.mycompany.myapp.service.criteria.ResidentCriteria;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ResidentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ResidentResourceIT {

    private static final String DEFAULT_FIRSTNAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRSTNAME = "BBBBBBBBBB";

    private static final String DEFAULT_LASTNAME = "AAAAAAAAAA";
    private static final String UPDATED_LASTNAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PHONENUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONENUMBER = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/residents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restResidentMockMvc;

    private Resident resident;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Resident createEntity(EntityManager em) {
        Resident resident = new Resident()
            .firstname(DEFAULT_FIRSTNAME)
            .lastname(DEFAULT_LASTNAME)
            .email(DEFAULT_EMAIL)
            .phonenumber(DEFAULT_PHONENUMBER);
        return resident;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Resident createUpdatedEntity(EntityManager em) {
        Resident resident = new Resident()
            .firstname(UPDATED_FIRSTNAME)
            .lastname(UPDATED_LASTNAME)
            .email(UPDATED_EMAIL)
            .phonenumber(UPDATED_PHONENUMBER);
        return resident;
    }

    @BeforeEach
    public void initTest() {
        resident = createEntity(em);
    }

    @Test
    @Transactional
    void createResident() throws Exception {
        int databaseSizeBeforeCreate = residentRepository.findAll().size();
        // Create the Resident
        restResidentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resident)))
            .andExpect(status().isCreated());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeCreate + 1);
        Resident testResident = residentList.get(residentList.size() - 1);
        assertThat(testResident.getFirstname()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testResident.getLastname()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testResident.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testResident.getPhonenumber()).isEqualTo(DEFAULT_PHONENUMBER);
    }

    @Test
    @Transactional
    void createResidentWithExistingId() throws Exception {
        // Create the Resident with an existing ID
        resident.setId(1L);

        int databaseSizeBeforeCreate = residentRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restResidentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resident)))
            .andExpect(status().isBadRequest());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = residentRepository.findAll().size();
        // set the field null
        resident.setEmail(null);

        // Create the Resident, which fails.

        restResidentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resident)))
            .andExpect(status().isBadRequest());

        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllResidents() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList
        restResidentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(resident.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstname").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastname").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phonenumber").value(hasItem(DEFAULT_PHONENUMBER)));
    }

    @Test
    @Transactional
    void getResident() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get the resident
        restResidentMockMvc
            .perform(get(ENTITY_API_URL_ID, resident.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(resident.getId().intValue()))
            .andExpect(jsonPath("$.firstname").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastname").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.phonenumber").value(DEFAULT_PHONENUMBER));
    }

    @Test
    @Transactional
    void getResidentsByIdFiltering() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        Long id = resident.getId();

        defaultResidentShouldBeFound("id.equals=" + id);
        defaultResidentShouldNotBeFound("id.notEquals=" + id);

        defaultResidentShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultResidentShouldNotBeFound("id.greaterThan=" + id);

        defaultResidentShouldBeFound("id.lessThanOrEqual=" + id);
        defaultResidentShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllResidentsByFirstnameIsEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where firstname equals to DEFAULT_FIRSTNAME
        defaultResidentShouldBeFound("firstname.equals=" + DEFAULT_FIRSTNAME);

        // Get all the residentList where firstname equals to UPDATED_FIRSTNAME
        defaultResidentShouldNotBeFound("firstname.equals=" + UPDATED_FIRSTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByFirstnameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where firstname not equals to DEFAULT_FIRSTNAME
        defaultResidentShouldNotBeFound("firstname.notEquals=" + DEFAULT_FIRSTNAME);

        // Get all the residentList where firstname not equals to UPDATED_FIRSTNAME
        defaultResidentShouldBeFound("firstname.notEquals=" + UPDATED_FIRSTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByFirstnameIsInShouldWork() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where firstname in DEFAULT_FIRSTNAME or UPDATED_FIRSTNAME
        defaultResidentShouldBeFound("firstname.in=" + DEFAULT_FIRSTNAME + "," + UPDATED_FIRSTNAME);

        // Get all the residentList where firstname equals to UPDATED_FIRSTNAME
        defaultResidentShouldNotBeFound("firstname.in=" + UPDATED_FIRSTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByFirstnameIsNullOrNotNull() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where firstname is not null
        defaultResidentShouldBeFound("firstname.specified=true");

        // Get all the residentList where firstname is null
        defaultResidentShouldNotBeFound("firstname.specified=false");
    }

    @Test
    @Transactional
    void getAllResidentsByFirstnameContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where firstname contains DEFAULT_FIRSTNAME
        defaultResidentShouldBeFound("firstname.contains=" + DEFAULT_FIRSTNAME);

        // Get all the residentList where firstname contains UPDATED_FIRSTNAME
        defaultResidentShouldNotBeFound("firstname.contains=" + UPDATED_FIRSTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByFirstnameNotContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where firstname does not contain DEFAULT_FIRSTNAME
        defaultResidentShouldNotBeFound("firstname.doesNotContain=" + DEFAULT_FIRSTNAME);

        // Get all the residentList where firstname does not contain UPDATED_FIRSTNAME
        defaultResidentShouldBeFound("firstname.doesNotContain=" + UPDATED_FIRSTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByLastnameIsEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where lastname equals to DEFAULT_LASTNAME
        defaultResidentShouldBeFound("lastname.equals=" + DEFAULT_LASTNAME);

        // Get all the residentList where lastname equals to UPDATED_LASTNAME
        defaultResidentShouldNotBeFound("lastname.equals=" + UPDATED_LASTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByLastnameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where lastname not equals to DEFAULT_LASTNAME
        defaultResidentShouldNotBeFound("lastname.notEquals=" + DEFAULT_LASTNAME);

        // Get all the residentList where lastname not equals to UPDATED_LASTNAME
        defaultResidentShouldBeFound("lastname.notEquals=" + UPDATED_LASTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByLastnameIsInShouldWork() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where lastname in DEFAULT_LASTNAME or UPDATED_LASTNAME
        defaultResidentShouldBeFound("lastname.in=" + DEFAULT_LASTNAME + "," + UPDATED_LASTNAME);

        // Get all the residentList where lastname equals to UPDATED_LASTNAME
        defaultResidentShouldNotBeFound("lastname.in=" + UPDATED_LASTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByLastnameIsNullOrNotNull() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where lastname is not null
        defaultResidentShouldBeFound("lastname.specified=true");

        // Get all the residentList where lastname is null
        defaultResidentShouldNotBeFound("lastname.specified=false");
    }

    @Test
    @Transactional
    void getAllResidentsByLastnameContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where lastname contains DEFAULT_LASTNAME
        defaultResidentShouldBeFound("lastname.contains=" + DEFAULT_LASTNAME);

        // Get all the residentList where lastname contains UPDATED_LASTNAME
        defaultResidentShouldNotBeFound("lastname.contains=" + UPDATED_LASTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByLastnameNotContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where lastname does not contain DEFAULT_LASTNAME
        defaultResidentShouldNotBeFound("lastname.doesNotContain=" + DEFAULT_LASTNAME);

        // Get all the residentList where lastname does not contain UPDATED_LASTNAME
        defaultResidentShouldBeFound("lastname.doesNotContain=" + UPDATED_LASTNAME);
    }

    @Test
    @Transactional
    void getAllResidentsByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where email equals to DEFAULT_EMAIL
        defaultResidentShouldBeFound("email.equals=" + DEFAULT_EMAIL);

        // Get all the residentList where email equals to UPDATED_EMAIL
        defaultResidentShouldNotBeFound("email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllResidentsByEmailIsNotEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where email not equals to DEFAULT_EMAIL
        defaultResidentShouldNotBeFound("email.notEquals=" + DEFAULT_EMAIL);

        // Get all the residentList where email not equals to UPDATED_EMAIL
        defaultResidentShouldBeFound("email.notEquals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllResidentsByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where email in DEFAULT_EMAIL or UPDATED_EMAIL
        defaultResidentShouldBeFound("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL);

        // Get all the residentList where email equals to UPDATED_EMAIL
        defaultResidentShouldNotBeFound("email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllResidentsByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where email is not null
        defaultResidentShouldBeFound("email.specified=true");

        // Get all the residentList where email is null
        defaultResidentShouldNotBeFound("email.specified=false");
    }

    @Test
    @Transactional
    void getAllResidentsByEmailContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where email contains DEFAULT_EMAIL
        defaultResidentShouldBeFound("email.contains=" + DEFAULT_EMAIL);

        // Get all the residentList where email contains UPDATED_EMAIL
        defaultResidentShouldNotBeFound("email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllResidentsByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where email does not contain DEFAULT_EMAIL
        defaultResidentShouldNotBeFound("email.doesNotContain=" + DEFAULT_EMAIL);

        // Get all the residentList where email does not contain UPDATED_EMAIL
        defaultResidentShouldBeFound("email.doesNotContain=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllResidentsByPhonenumberIsEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where phonenumber equals to DEFAULT_PHONENUMBER
        defaultResidentShouldBeFound("phonenumber.equals=" + DEFAULT_PHONENUMBER);

        // Get all the residentList where phonenumber equals to UPDATED_PHONENUMBER
        defaultResidentShouldNotBeFound("phonenumber.equals=" + UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void getAllResidentsByPhonenumberIsNotEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where phonenumber not equals to DEFAULT_PHONENUMBER
        defaultResidentShouldNotBeFound("phonenumber.notEquals=" + DEFAULT_PHONENUMBER);

        // Get all the residentList where phonenumber not equals to UPDATED_PHONENUMBER
        defaultResidentShouldBeFound("phonenumber.notEquals=" + UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void getAllResidentsByPhonenumberIsInShouldWork() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where phonenumber in DEFAULT_PHONENUMBER or UPDATED_PHONENUMBER
        defaultResidentShouldBeFound("phonenumber.in=" + DEFAULT_PHONENUMBER + "," + UPDATED_PHONENUMBER);

        // Get all the residentList where phonenumber equals to UPDATED_PHONENUMBER
        defaultResidentShouldNotBeFound("phonenumber.in=" + UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void getAllResidentsByPhonenumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where phonenumber is not null
        defaultResidentShouldBeFound("phonenumber.specified=true");

        // Get all the residentList where phonenumber is null
        defaultResidentShouldNotBeFound("phonenumber.specified=false");
    }

    @Test
    @Transactional
    void getAllResidentsByPhonenumberContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where phonenumber contains DEFAULT_PHONENUMBER
        defaultResidentShouldBeFound("phonenumber.contains=" + DEFAULT_PHONENUMBER);

        // Get all the residentList where phonenumber contains UPDATED_PHONENUMBER
        defaultResidentShouldNotBeFound("phonenumber.contains=" + UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void getAllResidentsByPhonenumberNotContainsSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        // Get all the residentList where phonenumber does not contain DEFAULT_PHONENUMBER
        defaultResidentShouldNotBeFound("phonenumber.doesNotContain=" + DEFAULT_PHONENUMBER);

        // Get all the residentList where phonenumber does not contain UPDATED_PHONENUMBER
        defaultResidentShouldBeFound("phonenumber.doesNotContain=" + UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void getAllResidentsByRoomIsEqualToSomething() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);
        Room room;
        if (TestUtil.findAll(em, Room.class).isEmpty()) {
            room = RoomResourceIT.createEntity(em);
            em.persist(room);
            em.flush();
        } else {
            room = TestUtil.findAll(em, Room.class).get(0);
        }
        em.persist(room);
        em.flush();
        resident.setRoom(room);
        residentRepository.saveAndFlush(resident);
        Long roomId = room.getId();

        // Get all the residentList where room equals to roomId
        defaultResidentShouldBeFound("roomId.equals=" + roomId);

        // Get all the residentList where room equals to (roomId + 1)
        defaultResidentShouldNotBeFound("roomId.equals=" + (roomId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultResidentShouldBeFound(String filter) throws Exception {
        restResidentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(resident.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstname").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastname").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phonenumber").value(hasItem(DEFAULT_PHONENUMBER)));

        // Check, that the count call also returns 1
        restResidentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultResidentShouldNotBeFound(String filter) throws Exception {
        restResidentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restResidentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingResident() throws Exception {
        // Get the resident
        restResidentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewResident() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        int databaseSizeBeforeUpdate = residentRepository.findAll().size();

        // Update the resident
        Resident updatedResident = residentRepository.findById(resident.getId()).get();
        // Disconnect from session so that the updates on updatedResident are not directly saved in db
        em.detach(updatedResident);
        updatedResident.firstname(UPDATED_FIRSTNAME).lastname(UPDATED_LASTNAME).email(UPDATED_EMAIL).phonenumber(UPDATED_PHONENUMBER);

        restResidentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedResident.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedResident))
            )
            .andExpect(status().isOk());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
        Resident testResident = residentList.get(residentList.size() - 1);
        assertThat(testResident.getFirstname()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testResident.getLastname()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testResident.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testResident.getPhonenumber()).isEqualTo(UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void putNonExistingResident() throws Exception {
        int databaseSizeBeforeUpdate = residentRepository.findAll().size();
        resident.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResidentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resident.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resident))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchResident() throws Exception {
        int databaseSizeBeforeUpdate = residentRepository.findAll().size();
        resident.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResidentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(resident))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamResident() throws Exception {
        int databaseSizeBeforeUpdate = residentRepository.findAll().size();
        resident.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResidentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(resident)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateResidentWithPatch() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        int databaseSizeBeforeUpdate = residentRepository.findAll().size();

        // Update the resident using partial update
        Resident partialUpdatedResident = new Resident();
        partialUpdatedResident.setId(resident.getId());

        restResidentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResident.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResident))
            )
            .andExpect(status().isOk());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
        Resident testResident = residentList.get(residentList.size() - 1);
        assertThat(testResident.getFirstname()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testResident.getLastname()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testResident.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testResident.getPhonenumber()).isEqualTo(DEFAULT_PHONENUMBER);
    }

    @Test
    @Transactional
    void fullUpdateResidentWithPatch() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        int databaseSizeBeforeUpdate = residentRepository.findAll().size();

        // Update the resident using partial update
        Resident partialUpdatedResident = new Resident();
        partialUpdatedResident.setId(resident.getId());

        partialUpdatedResident
            .firstname(UPDATED_FIRSTNAME)
            .lastname(UPDATED_LASTNAME)
            .email(UPDATED_EMAIL)
            .phonenumber(UPDATED_PHONENUMBER);

        restResidentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResident.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResident))
            )
            .andExpect(status().isOk());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
        Resident testResident = residentList.get(residentList.size() - 1);
        assertThat(testResident.getFirstname()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testResident.getLastname()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testResident.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testResident.getPhonenumber()).isEqualTo(UPDATED_PHONENUMBER);
    }

    @Test
    @Transactional
    void patchNonExistingResident() throws Exception {
        int databaseSizeBeforeUpdate = residentRepository.findAll().size();
        resident.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResidentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, resident.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resident))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchResident() throws Exception {
        int databaseSizeBeforeUpdate = residentRepository.findAll().size();
        resident.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResidentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(resident))
            )
            .andExpect(status().isBadRequest());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamResident() throws Exception {
        int databaseSizeBeforeUpdate = residentRepository.findAll().size();
        resident.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResidentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(resident)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Resident in the database
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteResident() throws Exception {
        // Initialize the database
        residentRepository.saveAndFlush(resident);

        int databaseSizeBeforeDelete = residentRepository.findAll().size();

        // Delete the resident
        restResidentMockMvc
            .perform(delete(ENTITY_API_URL_ID, resident.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Resident> residentList = residentRepository.findAll();
        assertThat(residentList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
