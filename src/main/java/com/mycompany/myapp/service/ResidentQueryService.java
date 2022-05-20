package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Resident;
import com.mycompany.myapp.repository.ResidentRepository;
import com.mycompany.myapp.service.criteria.ResidentCriteria;
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
 * Service for executing complex queries for {@link Resident} entities in the database.
 * The main input is a {@link ResidentCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Resident} or a {@link Page} of {@link Resident} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ResidentQueryService extends QueryService<Resident> {

    private final Logger log = LoggerFactory.getLogger(ResidentQueryService.class);

    private final ResidentRepository residentRepository;

    public ResidentQueryService(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    /**
     * Return a {@link List} of {@link Resident} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Resident> findByCriteria(ResidentCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Resident> specification = createSpecification(criteria);
        return residentRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Resident} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Resident> findByCriteria(ResidentCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Resident> specification = createSpecification(criteria);
        return residentRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ResidentCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Resident> specification = createSpecification(criteria);
        return residentRepository.count(specification);
    }

    /**
     * Function to convert {@link ResidentCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Resident> createSpecification(ResidentCriteria criteria) {
        Specification<Resident> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Resident_.id));
            }
            if (criteria.getFirstname() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFirstname(), Resident_.firstname));
            }
            if (criteria.getLastname() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastname(), Resident_.lastname));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Resident_.email));
            }
            if (criteria.getPhonenumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhonenumber(), Resident_.phonenumber));
            }
            if (criteria.getRoomId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getRoomId(), root -> root.join(Resident_.room, JoinType.LEFT).get(Room_.id))
                    );
            }
        }
        return specification;
    }
}
