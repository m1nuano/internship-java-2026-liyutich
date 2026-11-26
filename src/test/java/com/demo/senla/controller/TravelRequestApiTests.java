package com.demo.senla.controller;

import com.demo.logging.ServiceLoggingAspect;
import com.demo.senla.PostgresTestContainerConfig;
import com.demo.senla.dto.DepartmentDto;
import com.demo.senla.dto.DepartmentTransferRequest;
import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.dto.TravelRequestDto;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.exception.ApiError;
import com.demo.senla.service.TravelRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TravelRequestApiTests extends PostgresTestContainerConfig {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TravelRequestService travelRequestService;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private ServiceLoggingAspect loggingAspect;

    private Long departmentId;
    private Long employeeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from request_history");
        jdbcTemplate.update("delete from travel_requests");
        jdbcTemplate.update("delete from employees");
        jdbcTemplate.update("delete from departments");

        departmentId = createDepartment("Engineering", "ENG").id();
        ResponseEntity<EmployeeDto> employee = restTemplate.postForEntity("/api/employees",
                new EmployeeDto(null, "Test Employee", "api@example.com", "Java Developer",
                        departmentId, LocalDate.of(2024, 1, 1)), EmployeeDto.class);
        assertEquals(HttpStatus.CREATED, employee.getStatusCode());
        assertNotNull(employee.getBody());
        employeeId = employee.getBody().id();
    }

    @Test
    void createsDraftRequestAndIncrementsCreatedMetric() {
        double before = metric("travel.requests.created");
        TravelRequestDto created = createRequest(null, "Berlin");

        assertNotNull(created.id());
        assertEquals(TravelRequestStatus.DRAFT, created.status());
        assertEquals(employeeId, created.employeeId());
        assertEquals(departmentId, created.departmentId());
        assertNotNull(created.createdAt());
        assertNotNull(created.updatedAt());
        assertEquals("DRAFT", jdbcTemplate.queryForObject(
                "select status from travel_requests where id = ?", String.class, created.id()));
        assertEquals(before + 1, metric("travel.requests.created"));
        assertEquals(0, historyCount(created.id()));
    }

    @Test
    void logsEveryStatusChangeAndDoesNotLogUnchangedStatus() {
        TravelRequestDto created = createRequest(TravelRequestStatus.DRAFT, "Berlin");
        double before = metric("travel.requests.status.changed");

        ResponseEntity<TravelRequestDto> sameStatus = update(created.id(), TravelRequestStatus.DRAFT, "Paris");
        assertEquals(HttpStatus.OK, sameStatus.getStatusCode());
        assertEquals(0, historyCount(created.id()));
        assertEquals(before, metric("travel.requests.status.changed"));

        assertEquals(HttpStatus.OK, update(created.id(), TravelRequestStatus.SUBMITTED, "Paris").getStatusCode());
        assertEquals(HttpStatus.OK, update(created.id(), TravelRequestStatus.APPROVED, "Paris").getStatusCode());
        assertEquals(2, historyCount(created.id()));
        assertEquals(1, jdbcTemplate.queryForObject("""
                select count(*) from request_history
                where travel_request_id = ? and old_status = 'DRAFT' and new_status = 'SUBMITTED'
                    and changed_at is not null
                """, Integer.class, created.id()));
        assertEquals(1, jdbcTemplate.queryForObject("""
                select count(*) from request_history
                where travel_request_id = ? and old_status = 'SUBMITTED' and new_status = 'APPROVED'
                """, Integer.class, created.id()));
        assertEquals(before + 2, metric("travel.requests.status.changed"));
    }

    @ParameterizedTest
    @EnumSource(value = TravelRequestStatus.class, names = {"APPROVED", "REJECTED"})
    void cannotEditFinalizedRequest(TravelRequestStatus status) {
        TravelRequestDto created = createRequest(status, "Berlin");
        double before = metric("travel.requests.status.changed");

        ResponseEntity<ApiError> response = restTemplate.exchange("/api/travel-requests/{id}", HttpMethod.PUT,
                new HttpEntity<>(request(TravelRequestStatus.DRAFT, "Changed")), ApiError.class, created.id());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Travel request in status " + status + " cannot be changed", response.getBody().message());
        TravelRequestDto saved = restTemplate.getForObject("/api/travel-requests/{id}", TravelRequestDto.class, created.id());
        assertNotNull(saved);
        assertEquals(status, saved.status());
        assertEquals("Berlin", saved.destination());
        assertEquals(0, historyCount(created.id()));
        assertEquals(before, metric("travel.requests.status.changed"));
    }

    @ParameterizedTest
    @EnumSource(value = TravelRequestStatus.class, names = {"SUBMITTED", "REJECTED"})
    void logsChangesToOtherStatuses(TravelRequestStatus status) {
        TravelRequestDto created = createRequest(TravelRequestStatus.DRAFT, "Berlin");
        assertEquals(HttpStatus.OK, update(created.id(), status, "Berlin").getStatusCode());
        assertEquals(status.name(), jdbcTemplate.queryForObject(
                "select new_status from request_history where travel_request_id = ?", String.class, created.id()));
    }

    @Test
    void failedHistoryInsertRollsBackRequestUpdate() {
        TravelRequestDto created = createRequest(TravelRequestStatus.DRAFT, "Berlin");
        double before = metric("travel.requests.status.changed");
        jdbcTemplate.execute("alter table request_history add constraint test_reject_status_change "
                + "check (new_status <> 'SUBMITTED')");
        try {
            ResponseEntity<ApiError> response = restTemplate.exchange("/api/travel-requests/{id}", HttpMethod.PUT,
                    new HttpEntity<>(request(TravelRequestStatus.SUBMITTED, "Changed")), ApiError.class, created.id());
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertEquals("DRAFT", jdbcTemplate.queryForObject(
                    "select status from travel_requests where id = ?", String.class, created.id()));
            assertEquals("Berlin", jdbcTemplate.queryForObject(
                    "select destination from travel_requests where id = ?", String.class, created.id()));
            assertEquals(0, historyCount(created.id()));
            assertEquals(before, metric("travel.requests.status.changed"));
        } finally {
            jdbcTemplate.execute("alter table request_history drop constraint test_reject_status_change");
        }
    }

    @Test
    void rolledBackCreationDoesNotIncreaseMetric() {
        double before = metric("travel.requests.created");
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            travelRequestService.create(request(TravelRequestStatus.DRAFT, "Berlin"));
            transaction.setRollbackOnly();
        });

        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from travel_requests", Integer.class));
        assertEquals(before, metric("travel.requests.created"));
    }

    @Test
    void missingRequestReferenceReturnsNotFoundAndDoesNotCreateRequest() {
        double before = metric("travel.requests.created");
        TravelRequestDto invalid = new TravelRequestDto(null, Long.MAX_VALUE, departmentId, "Berlin",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21), "Visit partner office",
                null, null, null, new BigDecimal("500.00"));

        ResponseEntity<ApiError> response = restTemplate.postForEntity("/api/travel-requests", invalid, ApiError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from travel_requests", Integer.class));
        assertEquals(before, metric("travel.requests.created"));
    }

    @Test
    void filtersSortsAndPaginatesRequests() {
        createRequest(TravelRequestStatus.DRAFT, "Berlin A");
        TravelRequestDto second = createRequest(TravelRequestStatus.DRAFT, "Berlin B");
        createRequest(TravelRequestStatus.SUBMITTED, "Paris");

        JsonNode page = restTemplate.getForObject("/api/travel-requests?status=DRAFT&destination=berlin"
                        + "&departmentId={department}&employeeId={employee}&startFrom=2026-11-20&endTo=2026-11-21"
                        + "&minCost=400&maxCost=600&page=0&size=1&sort=id,desc",
                JsonNode.class, departmentId, employeeId);

        assertNotNull(page);
        assertEquals(2, page.path("totalElements").asInt());
        assertEquals(2, page.path("totalPages").asInt());
        assertEquals(1, page.path("content").size());
        assertEquals(second.id().longValue(), page.path("content").get(0).path("id").asLong());
    }

    @Test
    void invalidSortPropertyReturnsBadRequest() {
        ResponseEntity<ApiError> response = restTemplate.getForEntity(
                "/api/travel-requests?sort=unknownProperty,asc", ApiError.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid filter or sort property", response.getBody().message());
    }

    @Test
    void jdbcTransferMovesOnlyDraftRequests() {
        Long newDepartment = createDepartment("Finance", "FIN").id();
        TravelRequestDto draft = createRequest(TravelRequestStatus.DRAFT, "Berlin");
        TravelRequestDto approved = createRequest(TravelRequestStatus.APPROVED, "Paris");

        ResponseEntity<Void> response = restTemplate.exchange("/api/employees/jdbc/{id}/department", HttpMethod.PUT,
                new HttpEntity<>(new DepartmentTransferRequest(newDepartment)), Void.class, employeeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(newDepartment, jdbcTemplate.queryForObject(
                "select department_id from employees where id = ?", Long.class, employeeId));
        assertEquals(newDepartment, jdbcTemplate.queryForObject(
                "select department_id from travel_requests where id = ?", Long.class, draft.id()));
        assertEquals(departmentId, jdbcTemplate.queryForObject(
                "select department_id from travel_requests where id = ?", Long.class, approved.id()));
    }

    @Test
    void jdbcTransferOfMissingEmployeeReturnsNotFound() {
        ResponseEntity<ApiError> response = restTemplate.exchange("/api/employees/jdbc/{id}/department", HttpMethod.PUT,
                new HttpEntity<>(new DepartmentTransferRequest(departmentId)), ApiError.class, Long.MAX_VALUE);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Employee not found: " + Long.MAX_VALUE, response.getBody().message());
    }

    @Test
    void jdbcTransferToMissingDepartmentReturnsNotFound() {
        TravelRequestDto draft = createRequest(TravelRequestStatus.DRAFT, "Berlin");

        ResponseEntity<ApiError> response = restTemplate.exchange("/api/employees/jdbc/{id}/department", HttpMethod.PUT,
                new HttpEntity<>(new DepartmentTransferRequest(Long.MAX_VALUE)), ApiError.class, employeeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Department not found: " + Long.MAX_VALUE, response.getBody().message());
        assertEquals(departmentId, jdbcTemplate.queryForObject(
                "select department_id from employees where id = ?", Long.class, employeeId));
        assertEquals(departmentId, jdbcTemplate.queryForObject(
                "select department_id from travel_requests where id = ?", Long.class, draft.id()));
    }

    @Test
    void failedJdbcDraftTransferReturnsConflictAndRollsBackEmployee() {
        Long newDepartment = createDepartment("Finance", "FIN").id();
        TravelRequestDto draft = createRequest(TravelRequestStatus.DRAFT, "Berlin");

        jdbcTemplate.execute("alter table travel_requests add constraint test_reject_api_transfer "
                + "check (id <> " + draft.id() + " or department_id = " + departmentId + ")");
        try {
            ResponseEntity<ApiError> response = restTemplate.exchange("/api/employees/jdbc/{id}/department", HttpMethod.PUT,
                    new HttpEntity<>(new DepartmentTransferRequest(newDepartment)), ApiError.class, employeeId);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Data conflicts with a unique or foreign key constraint", response.getBody().message());
            assertEquals(departmentId, jdbcTemplate.queryForObject(
                    "select department_id from employees where id = ?", Long.class, employeeId));
            assertEquals(departmentId, jdbcTemplate.queryForObject(
                    "select department_id from travel_requests where id = ?", Long.class, draft.id()));
        } finally {
            jdbcTemplate.execute("alter table travel_requests drop constraint test_reject_api_transfer");
        }
    }

    @Test
    void exposesActuatorAndOpenApi() {
        assertNotNull(loggingAspect);
        JsonNode health = restTemplate.getForObject("/actuator/health", JsonNode.class);
        assertNotNull(health);
        assertEquals("UP", health.path("status").asText());

        JsonNode info = restTemplate.getForObject("/actuator/info", JsonNode.class);
        assertNotNull(info);
        assertEquals("senla", info.path("app").path("name").asText());

        JsonNode metrics = restTemplate.getForObject("/actuator/metrics", JsonNode.class);
        assertNotNull(metrics);
        assertTrue(metrics.path("names").toString().contains("travel.requests.created"));
        assertTrue(metrics.path("names").toString().contains("travel.requests.status.changed"));

        JsonNode api = restTemplate.getForObject("/v3/api-docs", JsonNode.class);
        assertNotNull(api);
        assertEquals("Travel Service API", api.path("info").path("title").asText());
        assertTrue(api.path("paths").has("/api/travel-requests"));
        assertTrue(api.path("paths").has("/api/employees/jdbc/{id}/department"));
        assertTrue(api.path("paths").has("/api/transports/joined"));
        assertFalse(api.toString().contains("\"CANCELLED\""));
        assertEquals(HttpStatus.OK, restTemplate.getForEntity("/swagger-ui/index.html", String.class).getStatusCode());
    }

    private DepartmentDto createDepartment(String name, String code) {
        ResponseEntity<DepartmentDto> response = restTemplate.postForEntity(
                "/api/departments", new DepartmentDto(null, name, code), DepartmentDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private TravelRequestDto request(TravelRequestStatus status, String destination) {
        return new TravelRequestDto(null, employeeId, departmentId, destination,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21), "Visit partner office",
                status, null, null, new BigDecimal("500.00"));
    }

    private TravelRequestDto createRequest(TravelRequestStatus status, String destination) {
        ResponseEntity<TravelRequestDto> response = restTemplate.postForEntity(
                "/api/travel-requests", request(status, destination), TravelRequestDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private ResponseEntity<TravelRequestDto> update(Long id, TravelRequestStatus status, String destination) {
        return restTemplate.exchange("/api/travel-requests/{id}", HttpMethod.PUT,
                new HttpEntity<>(request(status, destination)), TravelRequestDto.class, id);
    }

    private int historyCount(Long requestId) {
        return jdbcTemplate.queryForObject("select count(*) from request_history where travel_request_id = ?",
                Integer.class, requestId);
    }

    private double metric(String name) {
        JsonNode response = restTemplate.getForObject("/actuator/metrics/{name}", JsonNode.class, name);
        assertNotNull(response);
        return response.path("measurements").get(0).path("value").asDouble();
    }
}
