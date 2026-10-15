package com.demo.senla.dao.nativequery;

import java.math.BigDecimal;

public interface DepartmentRequestStatistics {
    String getDepartmentName();
    Long getRequestCount();
    BigDecimal getTotalEstimatedCost();
}
