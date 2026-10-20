package com.demo.senla.dao.jpql;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelRequestJpqlDaoTests extends DaoTest {

    @Autowired private TravelRequestJpqlDao travelRequestJpqlDao;

    @Test
    void jpqlPagesRequestsByDepartmentCode() {
        Long extra = createRequest(seedEmployeeId(), seedDepartmentId(), "Paris", TravelRequestStatus.DRAFT);
        var firstPage = travelRequestJpqlDao.findByDepartmentCode("ENG", PageRequest.of(0, 1));
        assertEquals(2, firstPage.getTotalElements());
        assertEquals("Berlin", firstPage.getContent().getFirst().getDestination());
        var secondPage = travelRequestJpqlDao.findByDepartmentCode("ENG", PageRequest.of(1, 1));
        assertEquals(List.of(extra), secondPage.stream().map(TravelRequest::getId).toList());
        assertTrue(travelRequestJpqlDao.findByDepartmentCode("UNKNOWN", PageRequest.of(0, 1)).isEmpty());
    }
}
