package com.demo.senla.dao.nativequery;

import com.demo.senla.entity.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface DepartmentStatisticsRepository extends Repository<Department, Long> {

    @Query(value = """
            SELECT d.name AS "departmentName",
                   COUNT(tr.id) AS "requestCount",
                   COALESCE(SUM(tr.estimated_cost), 0) AS "totalEstimatedCost"
            FROM departments d
            LEFT JOIN travel_requests tr ON tr.department_id = d.id
            GROUP BY d.id, d.name
            ORDER BY d.name
            """, nativeQuery = true)
    List<DepartmentRequestStatistics> findStatistics();
}
