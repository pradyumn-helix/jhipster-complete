package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Facility;
import com.mycompany.myapp.repository.FacilityRepository;
import com.mycompany.myapp.service.criteria.FacilityCriteria;
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
 * Service for executing complex queries for {@link Facility} entities in the database.
 * The main input is a {@link FacilityCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Facility} or a {@link Page} of {@link Facility} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class FacilityQueryService extends QueryService<Facility> {

    private final Logger log = LoggerFactory.getLogger(FacilityQueryService.class);

    private final FacilityRepository facilityRepository;

    public FacilityQueryService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    /**
     * Return a {@link List} of {@link Facility} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Facility> findByCriteria(FacilityCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Facility> specification = createSpecification(criteria);
        return facilityRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Facility} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Facility> findByCriteria(FacilityCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Facility> specification = createSpecification(criteria);
        return facilityRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FacilityCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Facility> specification = createSpecification(criteria);
        return facilityRepository.count(specification);
    }

    /**
     * Function to convert {@link FacilityCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Facility> createSpecification(FacilityCriteria criteria) {
        Specification<Facility> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Facility_.id));
            }
            if (criteria.getaC() != null) {
                specification = specification.and(buildSpecification(criteria.getaC(), Facility_.aC));
            }
            if (criteria.getParking() != null) {
                specification = specification.and(buildSpecification(criteria.getParking(), Facility_.parking));
            }
            if (criteria.getWifi() != null) {
                specification = specification.and(buildSpecification(criteria.getWifi(), Facility_.wifi));
            }
            if (criteria.getRoomId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getRoomId(), root -> root.join(Facility_.room, JoinType.LEFT).get(Room_.id))
                    );
            }
        }
        return specification;
    }
}
