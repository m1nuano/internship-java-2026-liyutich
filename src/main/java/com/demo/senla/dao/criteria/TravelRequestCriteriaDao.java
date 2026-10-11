package com.demo.senla.dao.criteria;

import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TravelRequestCriteriaDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<TravelRequest> findByStatusAndDestination(TravelRequestStatus status, String destinationPart) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createQuery(TravelRequest.class);
        var root = query.from(TravelRequest.class);
        List<Predicate> predicates = new ArrayList<>();
        if (status != null) predicates.add(builder.equal(root.get("status"), status));
        if (destinationPart != null && !destinationPart.isBlank()) {
            predicates.add(builder.like(builder.lower(root.get("destination")),
                    "%" + destinationPart.toLowerCase() + "%"));
        }
        query.select(root).where(predicates.toArray(Predicate[]::new)).orderBy(builder.asc(root.get("id")));
        return entityManager.createQuery(query).getResultList();
    }
}
