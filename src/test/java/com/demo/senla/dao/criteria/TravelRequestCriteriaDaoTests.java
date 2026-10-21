package com.demo.senla.dao.criteria;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelRequestCriteriaDaoTests extends DaoTest {

    @Autowired private TravelRequestCriteriaDao travelRequestCriteriaDao;

    @Test
    void criteriaPagesRequestsAndSupportsEmptyAndPartialFilters() {
        Long first = createRequest(seedEmployeeId(), seedDepartmentId(), "Porto", TravelRequestStatus.APPROVED);
        Long second = createRequest(seedEmployeeId(), seedDepartmentId(), "PORTO West", TravelRequestStatus.APPROVED);
        createRequest(seedEmployeeId(), seedDepartmentId(), "Madrid", TravelRequestStatus.APPROVED);
        var firstPage = travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.APPROVED, "port", PageRequest.of(0, 1));
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(List.of(first), firstPage.stream().map(TravelRequest::getId).toList());
        var secondPage = travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.APPROVED, "port", PageRequest.of(1, 1));
        assertEquals(2, secondPage.getTotalElements());
        assertEquals(List.of(second), secondPage.stream().map(TravelRequest::getId).toList());
        assertEquals(3, travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.APPROVED, null, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(2, travelRequestCriteriaDao.findByStatusAndDestination(
                null, "PORT", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(4, travelRequestCriteriaDao.findByStatusAndDestination(
                null, " ", PageRequest.of(0, 10)).getTotalElements());
        assertTrue(travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.DRAFT, "PORT", PageRequest.of(0, 10)).isEmpty());
    }
}
