package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Resident;
import com.mycompany.myapp.domain.Room;
import com.mycompany.myapp.repository.RoomRepository;
import com.mycompany.myapp.service.criteria.RoomCriteria;
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
 * Integration tests for the {@link RoomResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RoomResourceIT {

    private static final String DEFAULT_ROOMNO = "AAAAAAAAAA";
    private static final String UPDATED_ROOMNO = "BBBBBBBBBB";

    private static final Integer DEFAULT_FLOOR = 1;
    private static final Integer UPDATED_FLOOR = 2;
    private static final Integer SMALLER_FLOOR = 1 - 1;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/rooms";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRoomMockMvc;

    private Room room;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Room createEntity(EntityManager em) {
        Room room = new Room().roomno(DEFAULT_ROOMNO).floor(DEFAULT_FLOOR).type(DEFAULT_TYPE);
        return room;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Room createUpdatedEntity(EntityManager em) {
        Room room = new Room().roomno(UPDATED_ROOMNO).floor(UPDATED_FLOOR).type(UPDATED_TYPE);
        return room;
    }

    @BeforeEach
    public void initTest() {
        room = createEntity(em);
    }

    @Test
    @Transactional
    void createRoom() throws Exception {
        int databaseSizeBeforeCreate = roomRepository.findAll().size();
        // Create the Room
        restRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isCreated());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeCreate + 1);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getRoomno()).isEqualTo(DEFAULT_ROOMNO);
        assertThat(testRoom.getFloor()).isEqualTo(DEFAULT_FLOOR);
        assertThat(testRoom.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    void createRoomWithExistingId() throws Exception {
        // Create the Room with an existing ID
        room.setId(1L);

        int databaseSizeBeforeCreate = roomRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRoomnoIsRequired() throws Exception {
        int databaseSizeBeforeTest = roomRepository.findAll().size();
        // set the field null
        room.setRoomno(null);

        // Create the Room, which fails.

        restRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isBadRequest());

        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRooms() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList
        restRoomMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(room.getId().intValue())))
            .andExpect(jsonPath("$.[*].roomno").value(hasItem(DEFAULT_ROOMNO)))
            .andExpect(jsonPath("$.[*].floor").value(hasItem(DEFAULT_FLOOR)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)));
    }

    @Test
    @Transactional
    void getRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get the room
        restRoomMockMvc
            .perform(get(ENTITY_API_URL_ID, room.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(room.getId().intValue()))
            .andExpect(jsonPath("$.roomno").value(DEFAULT_ROOMNO))
            .andExpect(jsonPath("$.floor").value(DEFAULT_FLOOR))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE));
    }

    @Test
    @Transactional
    void getRoomsByIdFiltering() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        Long id = room.getId();

        defaultRoomShouldBeFound("id.equals=" + id);
        defaultRoomShouldNotBeFound("id.notEquals=" + id);

        defaultRoomShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRoomShouldNotBeFound("id.greaterThan=" + id);

        defaultRoomShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRoomShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRoomsByRoomnoIsEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where roomno equals to DEFAULT_ROOMNO
        defaultRoomShouldBeFound("roomno.equals=" + DEFAULT_ROOMNO);

        // Get all the roomList where roomno equals to UPDATED_ROOMNO
        defaultRoomShouldNotBeFound("roomno.equals=" + UPDATED_ROOMNO);
    }

    @Test
    @Transactional
    void getAllRoomsByRoomnoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where roomno not equals to DEFAULT_ROOMNO
        defaultRoomShouldNotBeFound("roomno.notEquals=" + DEFAULT_ROOMNO);

        // Get all the roomList where roomno not equals to UPDATED_ROOMNO
        defaultRoomShouldBeFound("roomno.notEquals=" + UPDATED_ROOMNO);
    }

    @Test
    @Transactional
    void getAllRoomsByRoomnoIsInShouldWork() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where roomno in DEFAULT_ROOMNO or UPDATED_ROOMNO
        defaultRoomShouldBeFound("roomno.in=" + DEFAULT_ROOMNO + "," + UPDATED_ROOMNO);

        // Get all the roomList where roomno equals to UPDATED_ROOMNO
        defaultRoomShouldNotBeFound("roomno.in=" + UPDATED_ROOMNO);
    }

    @Test
    @Transactional
    void getAllRoomsByRoomnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where roomno is not null
        defaultRoomShouldBeFound("roomno.specified=true");

        // Get all the roomList where roomno is null
        defaultRoomShouldNotBeFound("roomno.specified=false");
    }

    @Test
    @Transactional
    void getAllRoomsByRoomnoContainsSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where roomno contains DEFAULT_ROOMNO
        defaultRoomShouldBeFound("roomno.contains=" + DEFAULT_ROOMNO);

        // Get all the roomList where roomno contains UPDATED_ROOMNO
        defaultRoomShouldNotBeFound("roomno.contains=" + UPDATED_ROOMNO);
    }

    @Test
    @Transactional
    void getAllRoomsByRoomnoNotContainsSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where roomno does not contain DEFAULT_ROOMNO
        defaultRoomShouldNotBeFound("roomno.doesNotContain=" + DEFAULT_ROOMNO);

        // Get all the roomList where roomno does not contain UPDATED_ROOMNO
        defaultRoomShouldBeFound("roomno.doesNotContain=" + UPDATED_ROOMNO);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor equals to DEFAULT_FLOOR
        defaultRoomShouldBeFound("floor.equals=" + DEFAULT_FLOOR);

        // Get all the roomList where floor equals to UPDATED_FLOOR
        defaultRoomShouldNotBeFound("floor.equals=" + UPDATED_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsNotEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor not equals to DEFAULT_FLOOR
        defaultRoomShouldNotBeFound("floor.notEquals=" + DEFAULT_FLOOR);

        // Get all the roomList where floor not equals to UPDATED_FLOOR
        defaultRoomShouldBeFound("floor.notEquals=" + UPDATED_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsInShouldWork() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor in DEFAULT_FLOOR or UPDATED_FLOOR
        defaultRoomShouldBeFound("floor.in=" + DEFAULT_FLOOR + "," + UPDATED_FLOOR);

        // Get all the roomList where floor equals to UPDATED_FLOOR
        defaultRoomShouldNotBeFound("floor.in=" + UPDATED_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsNullOrNotNull() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor is not null
        defaultRoomShouldBeFound("floor.specified=true");

        // Get all the roomList where floor is null
        defaultRoomShouldNotBeFound("floor.specified=false");
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor is greater than or equal to DEFAULT_FLOOR
        defaultRoomShouldBeFound("floor.greaterThanOrEqual=" + DEFAULT_FLOOR);

        // Get all the roomList where floor is greater than or equal to UPDATED_FLOOR
        defaultRoomShouldNotBeFound("floor.greaterThanOrEqual=" + UPDATED_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor is less than or equal to DEFAULT_FLOOR
        defaultRoomShouldBeFound("floor.lessThanOrEqual=" + DEFAULT_FLOOR);

        // Get all the roomList where floor is less than or equal to SMALLER_FLOOR
        defaultRoomShouldNotBeFound("floor.lessThanOrEqual=" + SMALLER_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsLessThanSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor is less than DEFAULT_FLOOR
        defaultRoomShouldNotBeFound("floor.lessThan=" + DEFAULT_FLOOR);

        // Get all the roomList where floor is less than UPDATED_FLOOR
        defaultRoomShouldBeFound("floor.lessThan=" + UPDATED_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByFloorIsGreaterThanSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where floor is greater than DEFAULT_FLOOR
        defaultRoomShouldNotBeFound("floor.greaterThan=" + DEFAULT_FLOOR);

        // Get all the roomList where floor is greater than SMALLER_FLOOR
        defaultRoomShouldBeFound("floor.greaterThan=" + SMALLER_FLOOR);
    }

    @Test
    @Transactional
    void getAllRoomsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where type equals to DEFAULT_TYPE
        defaultRoomShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the roomList where type equals to UPDATED_TYPE
        defaultRoomShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllRoomsByTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where type not equals to DEFAULT_TYPE
        defaultRoomShouldNotBeFound("type.notEquals=" + DEFAULT_TYPE);

        // Get all the roomList where type not equals to UPDATED_TYPE
        defaultRoomShouldBeFound("type.notEquals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllRoomsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultRoomShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the roomList where type equals to UPDATED_TYPE
        defaultRoomShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllRoomsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where type is not null
        defaultRoomShouldBeFound("type.specified=true");

        // Get all the roomList where type is null
        defaultRoomShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    void getAllRoomsByTypeContainsSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where type contains DEFAULT_TYPE
        defaultRoomShouldBeFound("type.contains=" + DEFAULT_TYPE);

        // Get all the roomList where type contains UPDATED_TYPE
        defaultRoomShouldNotBeFound("type.contains=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllRoomsByTypeNotContainsSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList where type does not contain DEFAULT_TYPE
        defaultRoomShouldNotBeFound("type.doesNotContain=" + DEFAULT_TYPE);

        // Get all the roomList where type does not contain UPDATED_TYPE
        defaultRoomShouldBeFound("type.doesNotContain=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllRoomsByResidentIsEqualToSomething() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);
        Resident resident;
        if (TestUtil.findAll(em, Resident.class).isEmpty()) {
            resident = ResidentResourceIT.createEntity(em);
            em.persist(resident);
            em.flush();
        } else {
            resident = TestUtil.findAll(em, Resident.class).get(0);
        }
        em.persist(resident);
        em.flush();
        room.addResident(resident);
        roomRepository.saveAndFlush(room);
        Long residentId = resident.getId();

        // Get all the roomList where resident equals to residentId
        defaultRoomShouldBeFound("residentId.equals=" + residentId);

        // Get all the roomList where resident equals to (residentId + 1)
        defaultRoomShouldNotBeFound("residentId.equals=" + (residentId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRoomShouldBeFound(String filter) throws Exception {
        restRoomMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(room.getId().intValue())))
            .andExpect(jsonPath("$.[*].roomno").value(hasItem(DEFAULT_ROOMNO)))
            .andExpect(jsonPath("$.[*].floor").value(hasItem(DEFAULT_FLOOR)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)));

        // Check, that the count call also returns 1
        restRoomMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRoomShouldNotBeFound(String filter) throws Exception {
        restRoomMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRoomMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRoom() throws Exception {
        // Get the room
        restRoomMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room
        Room updatedRoom = roomRepository.findById(room.getId()).get();
        // Disconnect from session so that the updates on updatedRoom are not directly saved in db
        em.detach(updatedRoom);
        updatedRoom.roomno(UPDATED_ROOMNO).floor(UPDATED_FLOOR).type(UPDATED_TYPE);

        restRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedRoom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedRoom))
            )
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getRoomno()).isEqualTo(UPDATED_ROOMNO);
        assertThat(testRoom.getFloor()).isEqualTo(UPDATED_FLOOR);
        assertThat(testRoom.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, room.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(room))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(room))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRoomWithPatch() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room using partial update
        Room partialUpdatedRoom = new Room();
        partialUpdatedRoom.setId(room.getId());

        partialUpdatedRoom.floor(UPDATED_FLOOR).type(UPDATED_TYPE);

        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRoom.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRoom))
            )
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getRoomno()).isEqualTo(DEFAULT_ROOMNO);
        assertThat(testRoom.getFloor()).isEqualTo(UPDATED_FLOOR);
        assertThat(testRoom.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateRoomWithPatch() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room using partial update
        Room partialUpdatedRoom = new Room();
        partialUpdatedRoom.setId(room.getId());

        partialUpdatedRoom.roomno(UPDATED_ROOMNO).floor(UPDATED_FLOOR).type(UPDATED_TYPE);

        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRoom.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRoom))
            )
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getRoomno()).isEqualTo(UPDATED_ROOMNO);
        assertThat(testRoom.getFloor()).isEqualTo(UPDATED_FLOOR);
        assertThat(testRoom.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, room.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(room))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(room))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeDelete = roomRepository.findAll().size();

        // Delete the room
        restRoomMockMvc
            .perform(delete(ENTITY_API_URL_ID, room.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
