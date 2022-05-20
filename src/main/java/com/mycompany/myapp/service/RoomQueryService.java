package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Room;
import com.mycompany.myapp.repository.RoomRepository;
import com.mycompany.myapp.service.criteria.RoomCriteria;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Room} entities in the database.
 * The main input is a {@link RoomCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Room} or a {@link Page} of {@link Room} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RoomQueryService extends QueryService<Room> {

    private final Logger log = LoggerFactory.getLogger(RoomQueryService.class);

    private final RoomRepository roomRepository;

    public RoomQueryService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Return a {@link List} of {@link Room} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Room> findByCriteria(RoomCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Room> specification = createSpecification(criteria);
        return roomRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Room} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Room> findByCriteria(RoomCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Room> specification = createSpecification(criteria);
        return roomRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RoomCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Room> specification = createSpecification(criteria);
        return roomRepository.count(specification);
    }

    /**
     * Function to convert {@link RoomCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Room> createSpecification(RoomCriteria criteria) {
        Specification<Room> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Room_.id));
            }
            if (criteria.getRoomno() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRoomno(), Room_.roomno));
            }
            if (criteria.getFloor() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFloor(), Room_.floor));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getType(), Room_.type));
            }
            if (criteria.getResidentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getResidentId(), root -> root.join(Room_.residents, JoinType.LEFT).get(Resident_.id))
                    );
            }
        }
        return specification;
    }
}
