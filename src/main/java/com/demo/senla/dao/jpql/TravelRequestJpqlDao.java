package com.demo.senla.dao.jpql;

import com.demo.senla.entity.TravelRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class TravelRequestJpqlDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<TravelRequest> findByDepartmentCode(String departmentCode, Pageable pageable) {
        List<TravelRequest> requests = entityManager.createQuery(
                        "select tr from TravelRequest tr join tr.department d where d.code = :code order by tr.id",
                        TravelRequest.class)
                .setParameter("code", departmentCode)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        Long count = entityManager.createQuery(
                        "select count(tr) from TravelRequest tr join tr.department d where d.code = :code", Long.class)
                .setParameter("code", departmentCode).getSingleResult();
        return new PageImpl<>(requests, pageable, count);
    }
}
