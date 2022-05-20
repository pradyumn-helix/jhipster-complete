package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Facility;
import com.mycompany.myapp.domain.Room;
import com.mycompany.myapp.repository.FacilityRepository;
import com.mycompany.myapp.service.criteria.FacilityCriteria;
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
 * Integration tests for the {@link FacilityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FacilityResourceIT {

    private static final Boolean DEFAULT_A_C = false;
    private static final Boolean UPDATED_A_C = true;

    private static final Boolean DEFAULT_PARKING = false;
    private static final Boolean UPDATED_PARKING = true;

    private static final Boolean DEFAULT_WIFI = false;
    private static final Boolean UPDATED_WIFI = true;

    private static final String ENTITY_API_URL = "/api/facilities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFacilityMockMvc;

    private Facility facility;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Facility createEntity(EntityManager em) {
        Facility facility = new Facility().aC(DEFAULT_A_C).parking(DEFAULT_PARKING).wifi(DEFAULT_WIFI);
        return facility;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Facility createUpdatedEntity(EntityManager em) {
        Facility facility = new Facility().aC(UPDATED_A_C).parking(UPDATED_PARKING).wifi(UPDATED_WIFI);
        return facility;
    }

    @BeforeEach
    public void initTest() {
        facility = createEntity(em);
    }

    @Test
    @Transactional
    void createFacility() throws Exception {
        int databaseSizeBeforeCreate = facilityRepository.findAll().size();
        // Create the Facility
        restFacilityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(facility)))
            .andExpect(status().isCreated());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeCreate + 1);
        Facility testFacility = facilityList.get(facilityList.size() - 1);
        assertThat(testFacility.getaC()).isEqualTo(DEFAULT_A_C);
        assertThat(testFacility.getParking()).isEqualTo(DEFAULT_PARKING);
        assertThat(testFacility.getWifi()).isEqualTo(DEFAULT_WIFI);
    }

    @Test
    @Transactional
    void createFacilityWithExistingId() throws Exception {
        // Create the Facility with an existing ID
        facility.setId(1L);

        int databaseSizeBeforeCreate = facilityRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFacilityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(facility)))
            .andExpect(status().isBadRequest());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllFacilities() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList
        restFacilityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(facility.getId().intValue())))
            .andExpect(jsonPath("$.[*].aC").value(hasItem(DEFAULT_A_C.booleanValue())))
            .andExpect(jsonPath("$.[*].parking").value(hasItem(DEFAULT_PARKING.booleanValue())))
            .andExpect(jsonPath("$.[*].wifi").value(hasItem(DEFAULT_WIFI.booleanValue())));
    }

    @Test
    @Transactional
    void getFacility() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get the facility
        restFacilityMockMvc
            .perform(get(ENTITY_API_URL_ID, facility.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(facility.getId().intValue()))
            .andExpect(jsonPath("$.aC").value(DEFAULT_A_C.booleanValue()))
            .andExpect(jsonPath("$.parking").value(DEFAULT_PARKING.booleanValue()))
            .andExpect(jsonPath("$.wifi").value(DEFAULT_WIFI.booleanValue()));
    }

    @Test
    @Transactional
    void getFacilitiesByIdFiltering() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        Long id = facility.getId();

        defaultFacilityShouldBeFound("id.equals=" + id);
        defaultFacilityShouldNotBeFound("id.notEquals=" + id);

        defaultFacilityShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultFacilityShouldNotBeFound("id.greaterThan=" + id);

        defaultFacilityShouldBeFound("id.lessThanOrEqual=" + id);
        defaultFacilityShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllFacilitiesByaCIsEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where aC equals to DEFAULT_A_C
        defaultFacilityShouldBeFound("aC.equals=" + DEFAULT_A_C);

        // Get all the facilityList where aC equals to UPDATED_A_C
        defaultFacilityShouldNotBeFound("aC.equals=" + UPDATED_A_C);
    }

    @Test
    @Transactional
    void getAllFacilitiesByaCIsNotEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where aC not equals to DEFAULT_A_C
        defaultFacilityShouldNotBeFound("aC.notEquals=" + DEFAULT_A_C);

        // Get all the facilityList where aC not equals to UPDATED_A_C
        defaultFacilityShouldBeFound("aC.notEquals=" + UPDATED_A_C);
    }

    @Test
    @Transactional
    void getAllFacilitiesByaCIsInShouldWork() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where aC in DEFAULT_A_C or UPDATED_A_C
        defaultFacilityShouldBeFound("aC.in=" + DEFAULT_A_C + "," + UPDATED_A_C);

        // Get all the facilityList where aC equals to UPDATED_A_C
        defaultFacilityShouldNotBeFound("aC.in=" + UPDATED_A_C);
    }

    @Test
    @Transactional
    void getAllFacilitiesByaCIsNullOrNotNull() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where aC is not null
        defaultFacilityShouldBeFound("aC.specified=true");

        // Get all the facilityList where aC is null
        defaultFacilityShouldNotBeFound("aC.specified=false");
    }

    @Test
    @Transactional
    void getAllFacilitiesByParkingIsEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where parking equals to DEFAULT_PARKING
        defaultFacilityShouldBeFound("parking.equals=" + DEFAULT_PARKING);

        // Get all the facilityList where parking equals to UPDATED_PARKING
        defaultFacilityShouldNotBeFound("parking.equals=" + UPDATED_PARKING);
    }

    @Test
    @Transactional
    void getAllFacilitiesByParkingIsNotEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where parking not equals to DEFAULT_PARKING
        defaultFacilityShouldNotBeFound("parking.notEquals=" + DEFAULT_PARKING);

        // Get all the facilityList where parking not equals to UPDATED_PARKING
        defaultFacilityShouldBeFound("parking.notEquals=" + UPDATED_PARKING);
    }

    @Test
    @Transactional
    void getAllFacilitiesByParkingIsInShouldWork() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where parking in DEFAULT_PARKING or UPDATED_PARKING
        defaultFacilityShouldBeFound("parking.in=" + DEFAULT_PARKING + "," + UPDATED_PARKING);

        // Get all the facilityList where parking equals to UPDATED_PARKING
        defaultFacilityShouldNotBeFound("parking.in=" + UPDATED_PARKING);
    }

    @Test
    @Transactional
    void getAllFacilitiesByParkingIsNullOrNotNull() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where parking is not null
        defaultFacilityShouldBeFound("parking.specified=true");

        // Get all the facilityList where parking is null
        defaultFacilityShouldNotBeFound("parking.specified=false");
    }

    @Test
    @Transactional
    void getAllFacilitiesByWifiIsEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where wifi equals to DEFAULT_WIFI
        defaultFacilityShouldBeFound("wifi.equals=" + DEFAULT_WIFI);

        // Get all the facilityList where wifi equals to UPDATED_WIFI
        defaultFacilityShouldNotBeFound("wifi.equals=" + UPDATED_WIFI);
    }

    @Test
    @Transactional
    void getAllFacilitiesByWifiIsNotEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where wifi not equals to DEFAULT_WIFI
        defaultFacilityShouldNotBeFound("wifi.notEquals=" + DEFAULT_WIFI);

        // Get all the facilityList where wifi not equals to UPDATED_WIFI
        defaultFacilityShouldBeFound("wifi.notEquals=" + UPDATED_WIFI);
    }

    @Test
    @Transactional
    void getAllFacilitiesByWifiIsInShouldWork() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where wifi in DEFAULT_WIFI or UPDATED_WIFI
        defaultFacilityShouldBeFound("wifi.in=" + DEFAULT_WIFI + "," + UPDATED_WIFI);

        // Get all the facilityList where wifi equals to UPDATED_WIFI
        defaultFacilityShouldNotBeFound("wifi.in=" + UPDATED_WIFI);
    }

    @Test
    @Transactional
    void getAllFacilitiesByWifiIsNullOrNotNull() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        // Get all the facilityList where wifi is not null
        defaultFacilityShouldBeFound("wifi.specified=true");

        // Get all the facilityList where wifi is null
        defaultFacilityShouldNotBeFound("wifi.specified=false");
    }

    @Test
    @Transactional
    void getAllFacilitiesByRoomIsEqualToSomething() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);
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
        facility.setRoom(room);
        facilityRepository.saveAndFlush(facility);
        Long roomId = room.getId();

        // Get all the facilityList where room equals to roomId
        defaultFacilityShouldBeFound("roomId.equals=" + roomId);

        // Get all the facilityList where room equals to (roomId + 1)
        defaultFacilityShouldNotBeFound("roomId.equals=" + (roomId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFacilityShouldBeFound(String filter) throws Exception {
        restFacilityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(facility.getId().intValue())))
            .andExpect(jsonPath("$.[*].aC").value(hasItem(DEFAULT_A_C.booleanValue())))
            .andExpect(jsonPath("$.[*].parking").value(hasItem(DEFAULT_PARKING.booleanValue())))
            .andExpect(jsonPath("$.[*].wifi").value(hasItem(DEFAULT_WIFI.booleanValue())));

        // Check, that the count call also returns 1
        restFacilityMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFacilityShouldNotBeFound(String filter) throws Exception {
        restFacilityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFacilityMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingFacility() throws Exception {
        // Get the facility
        restFacilityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewFacility() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();

        // Update the facility
        Facility updatedFacility = facilityRepository.findById(facility.getId()).get();
        // Disconnect from session so that the updates on updatedFacility are not directly saved in db
        em.detach(updatedFacility);
        updatedFacility.aC(UPDATED_A_C).parking(UPDATED_PARKING).wifi(UPDATED_WIFI);

        restFacilityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedFacility.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedFacility))
            )
            .andExpect(status().isOk());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
        Facility testFacility = facilityList.get(facilityList.size() - 1);
        assertThat(testFacility.getaC()).isEqualTo(UPDATED_A_C);
        assertThat(testFacility.getParking()).isEqualTo(UPDATED_PARKING);
        assertThat(testFacility.getWifi()).isEqualTo(UPDATED_WIFI);
    }

    @Test
    @Transactional
    void putNonExistingFacility() throws Exception {
        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();
        facility.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFacilityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, facility.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(facility))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFacility() throws Exception {
        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();
        facility.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacilityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(facility))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFacility() throws Exception {
        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();
        facility.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacilityMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(facility)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFacilityWithPatch() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();

        // Update the facility using partial update
        Facility partialUpdatedFacility = new Facility();
        partialUpdatedFacility.setId(facility.getId());

        partialUpdatedFacility.parking(UPDATED_PARKING);

        restFacilityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFacility.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFacility))
            )
            .andExpect(status().isOk());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
        Facility testFacility = facilityList.get(facilityList.size() - 1);
        assertThat(testFacility.getaC()).isEqualTo(DEFAULT_A_C);
        assertThat(testFacility.getParking()).isEqualTo(UPDATED_PARKING);
        assertThat(testFacility.getWifi()).isEqualTo(DEFAULT_WIFI);
    }

    @Test
    @Transactional
    void fullUpdateFacilityWithPatch() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();

        // Update the facility using partial update
        Facility partialUpdatedFacility = new Facility();
        partialUpdatedFacility.setId(facility.getId());

        partialUpdatedFacility.aC(UPDATED_A_C).parking(UPDATED_PARKING).wifi(UPDATED_WIFI);

        restFacilityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFacility.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFacility))
            )
            .andExpect(status().isOk());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
        Facility testFacility = facilityList.get(facilityList.size() - 1);
        assertThat(testFacility.getaC()).isEqualTo(UPDATED_A_C);
        assertThat(testFacility.getParking()).isEqualTo(UPDATED_PARKING);
        assertThat(testFacility.getWifi()).isEqualTo(UPDATED_WIFI);
    }

    @Test
    @Transactional
    void patchNonExistingFacility() throws Exception {
        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();
        facility.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFacilityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, facility.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(facility))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFacility() throws Exception {
        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();
        facility.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacilityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(facility))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFacility() throws Exception {
        int databaseSizeBeforeUpdate = facilityRepository.findAll().size();
        facility.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacilityMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(facility)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Facility in the database
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFacility() throws Exception {
        // Initialize the database
        facilityRepository.saveAndFlush(facility);

        int databaseSizeBeforeDelete = facilityRepository.findAll().size();

        // Delete the facility
        restFacilityMockMvc
            .perform(delete(ENTITY_API_URL_ID, facility.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Facility> facilityList = facilityRepository.findAll();
        assertThat(facilityList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
