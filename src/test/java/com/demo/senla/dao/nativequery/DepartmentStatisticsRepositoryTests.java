package com.demo.senla.dao.nativequery;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.entity.TravelRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DepartmentStatisticsRepositoryTests extends DaoTest {

    @Autowired private DepartmentStatisticsRepository departmentStatisticsRepository;

    @Test
    void nativeQueryReturnsDepartmentCountsAndCosts() {
        var engineering = departmentStatisticsRepository.findStatistics().stream()
                .filter(stat -> "Engineering".equals(stat.getDepartmentName())).findFirst().orElseThrow();
        assertEquals(1L, engineering.getRequestCount());
        assertEquals(0, new BigDecimal("1250.00").compareTo(engineering.getTotalEstimatedCost()));

        createRequest(seedEmployeeId(), seedDepartmentId(), "Paris", TravelRequestStatus.DRAFT);
        var updated = departmentStatisticsRepository.findStatistics().stream()
                .filter(stat -> "Engineering".equals(stat.getDepartmentName())).findFirst().orElseThrow();
        assertEquals(2L, updated.getRequestCount());
        assertEquals(0, new BigDecimal("1750.00").compareTo(updated.getTotalEstimatedCost()));
    }
}
