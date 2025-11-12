package com.nexus.healthcare.report.service;

import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportService {
    
    Resource generateAppointmentReportExcel(
        UUID tenantId,
        UUID domainId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String paymentStatus,
        UUID doctorId,
        UUID patientId
    );
    
    Resource generatePaymentReconciliationReport(
        UUID tenantId,
        UUID domainId,
        LocalDate date
    );
}

