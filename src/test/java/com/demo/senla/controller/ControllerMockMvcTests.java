package com.demo.senla.controller;

import com.demo.senla.service.EmployeeService;
import com.demo.senla.service.TravelRequestService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TravelRequestController.class, EmployeeController.class})
class ControllerMockMvcTests {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TravelRequestService travelRequestService;
    @MockitoBean private EmployeeService employeeService;

    @Test
    void rejectsInvalidDateRangeBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/travel-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"destination":"Berlin","startDate":"2026-11-20","endDate":"2026-11-19"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isNotEmpty());
        verifyNoInteractions(travelRequestService);
    }

    @Test
    void rejectsInvalidEmployeeEmail() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Test Employee","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
        verifyNoInteractions(employeeService);
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/travel-requests").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
        verifyNoInteractions(travelRequestService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN", "CANCELLED"})
    void rejectsUnsupportedStatusInJson(String invalidStatus) throws Exception {
        mockMvc.perform(post("/api/travel-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"destination":"Berlin","startDate":"2026-11-19",
                                 "endDate":"2026-11-20","status":"%s"}
                                """.formatted(invalidStatus)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid JSON, field value or transport type"));
        verifyNoInteractions(travelRequestService);
    }

    @Test
    void missingEmployeeDuringTransferReturnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Employee not found: 99"))
                .when(employeeService).transferToDepartmentJdbc(99L, 2L);

        mockMvc.perform(put("/api/employees/jdbc/99/department")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":2}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found: 99"));
    }

    @Test
    void dataAccessProgrammingErrorReturnsServerError() throws Exception {
        given(employeeService.findById(1L))
                .willThrow(new InvalidDataAccessApiUsageException("Private database details"));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void unexpectedErrorReturnsSafeServerError() throws Exception {
        given(employeeService.findById(1L)).willThrow(new IllegalStateException("Private internal details"));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}
