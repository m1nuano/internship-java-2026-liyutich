package com.demo.senla.dao.criteria;

import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.entity.TravelRequest_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class TravelRequestCriteriaDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<TravelRequest> findByStatusAndDestination(TravelRequestStatus status, String destinationPart,
                                                        Pageable pageable) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createQuery(TravelRequest.class);
        var root = query.from(TravelRequest.class);

        query.select(root)
                .where(predicates(builder, root, status, destinationPart))
                .orderBy(builder.asc(root.get(TravelRequest_.id)));

        var typedQuery = entityManager.createQuery(query)
                .setFirstResult(Math.toIntExact(pageable.getOffset()))
                .setMaxResults(pageable.getPageSize());
        List<TravelRequest> requests = typedQuery.getResultList();

        var countQuery = builder.createQuery(Long.class);
        var countRoot = countQuery.from(TravelRequest.class);
        countQuery.select(builder.count(countRoot))
                .where(predicates(builder, countRoot, status, destinationPart));
        long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(requests, pageable, total);
    }

    private Predicate[] predicates(CriteriaBuilder builder, Root<TravelRequest> root,
                                   TravelRequestStatus status, String destinationPart) {
        List<Predicate> predicates = new ArrayList<>();
        if (status != null) {
            predicates.add(builder.equal(root.get(TravelRequest_.status), status));
        }
        if (destinationPart != null && !destinationPart.isBlank()) {
            predicates.add(builder.like(builder.lower(root.get(TravelRequest_.destination)),
                    "%" + destinationPart.toLowerCase(Locale.ROOT) + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }

}
