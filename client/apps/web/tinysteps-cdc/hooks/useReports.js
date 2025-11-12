'use client';

import { useMutation } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Helper function to download blob as file
 */
function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
}

/**
 * Hook for generating appointment report
 */
export function useGenerateAppointmentReport() {
  return useMutation({
    mutationFn: async ({ startDate, endDate, filters = {} }) => {
      const blob = await healthcareApi.report.generateAppointmentReport(startDate, endDate, filters);
      const filename = `appointment-report-${startDate}-to-${endDate}.xlsx`;
      downloadBlob(blob, filename);
      return blob;
    },
  });
}

/**
 * Hook for generating payment reconciliation report
 */
export function useGeneratePaymentReconciliation() {
  return useMutation({
    mutationFn: async (date) => {
      const blob = await healthcareApi.report.generatePaymentReconciliation(date);
      const filename = `payment-reconciliation-${date}.xlsx`;
      downloadBlob(blob, filename);
      return blob;
    },
  });
}

