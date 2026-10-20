package com.demo.senla.dao.jdbc;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.entity.TravelRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeJdbcDaoTests extends DaoTest {

    @Autowired private EmployeeJdbcDao employeeJdbcDao;

    @Test
    void jdbcFindsCompleteDtoAndPreservesNullableFields() {
        Long employeeId = createEmployee("Jdbc Test", "jdbc@example.com", seedDepartmentId(), "Analyst",
                LocalDate.of(2024, 3, 1));
        assertEquals(new EmployeeDto(employeeId, "Jdbc Test", "jdbc@example.com", "Analyst",
                seedDepartmentId(), LocalDate.of(2024, 3, 1)), employeeJdbcDao.findById(employeeId).orElseThrow());

        Long nullableId = createEmployee("Nullable Test", "nullable@example.com", null, null);
        assertEquals(new EmployeeDto(nullableId, "Nullable Test", "nullable@example.com", null, null, null),
                employeeJdbcDao.findById(nullableId).orElseThrow());
        assertTrue(employeeJdbcDao.findById(-1L).isEmpty());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void jdbcTransfersEmployeeAndOnlyDraftRequests() {
        Long oldDepartment = createDepartment("Old Department", "OLD");
        Long newDepartment = createDepartment("New Department", "NEW");
        Long employeeId = createEmployee("Transfer Test", "transfer@example.com", oldDepartment, null);
        Long otherEmployee = createEmployee("Other Employee", "other@example.com", oldDepartment, null);
        try {
            List<Long> requestIds = new ArrayList<>();
            for (TravelRequestStatus status : TravelRequestStatus.values()) {
                requestIds.add(createRequest(employeeId, oldDepartment, "Berlin", status));
            }
            Long otherDraft = createRequest(otherEmployee, oldDepartment, "Berlin", TravelRequestStatus.DRAFT);

            assertEquals(1, employeeJdbcDao.transferToDepartment(employeeId, newDepartment));
            assertEquals(newDepartment, employeeDepartment(employeeId));
            for (int i = 0; i < TravelRequestStatus.values().length; i++) {
                TravelRequestStatus status = TravelRequestStatus.values()[i];
                Long requestId = requestIds.get(i);
                assertEquals(status == TravelRequestStatus.DRAFT ? newDepartment : oldDepartment,
                        requestDepartment(requestId));
                LocalDateTime updatedAt = jdbcTemplate.queryForObject(
                        "select updated_at from travel_requests where id = ?", LocalDateTime.class, requestId);
                if (status == TravelRequestStatus.DRAFT) {
                    assertTrue(updatedAt.isAfter(LocalDateTime.of(2026, 1, 1, 12, 0)));
                } else {
                    assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), updatedAt);
                }
            }
            assertEquals(oldDepartment, requestDepartment(otherDraft));
            assertEquals(0, employeeJdbcDao.transferToDepartment(-1, newDepartment));
        } finally {
            jdbcTemplate.update("delete from travel_requests where employee_id in (?, ?)", employeeId, otherEmployee);
            jdbcTemplate.update("delete from employees where id in (?, ?)", employeeId, otherEmployee);
            jdbcTemplate.update("delete from departments where id in (?, ?)", oldDepartment, newDepartment);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void jdbcRollsBackEmployeeWhenDraftUpdateFails() {
        Long oldDepartment = createDepartment("Rollback Old", "RBOLD");
        Long newDepartment = createDepartment("Rollback New", "RBNEW");
        Long employeeId = createEmployee("Rollback Test", "rollback@example.com", oldDepartment, null);
        Long requestId = createRequest(employeeId, oldDepartment, "Berlin", TravelRequestStatus.DRAFT);
        try {
            jdbcTemplate.execute("alter table travel_requests add constraint test_reject_draft_transfer "
                    + "check (id <> " + requestId + " or department_id = " + oldDepartment + ")");
            assertThrows(RuntimeException.class, () -> employeeJdbcDao.transferToDepartment(employeeId, newDepartment));
            assertEquals(oldDepartment, employeeDepartment(employeeId));
            assertEquals(oldDepartment, requestDepartment(requestId));
        } finally {
            jdbcTemplate.execute("alter table travel_requests drop constraint if exists test_reject_draft_transfer");
            jdbcTemplate.update("delete from travel_requests where id = ?", requestId);
            jdbcTemplate.update("delete from employees where id = ?", employeeId);
            jdbcTemplate.update("delete from departments where id in (?, ?)", oldDepartment, newDepartment);
        }
    }
}
