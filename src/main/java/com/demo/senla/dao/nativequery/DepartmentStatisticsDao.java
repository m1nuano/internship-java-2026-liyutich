package com.demo.senla.dao.nativequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DepartmentStatisticsDao {

    private final DepartmentStatisticsRepository departmentStatisticsRepository;

    @Transactional(readOnly = true)
    public List<DepartmentRequestStatistics> findStatistics() {
        return departmentStatisticsRepository.findStatistics();
    }
}
