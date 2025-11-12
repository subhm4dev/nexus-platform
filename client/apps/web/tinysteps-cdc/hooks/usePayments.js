'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for processing POS payment
 */
export function useProcessPosPayment() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.payment.processPos(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

/**
 * Hook for processing manual payment
 */
export function useProcessManualPayment() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.payment.processManual(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

/**
 * Hook for getting cash register balance
 */
export function useCashRegisterBalance() {
  return useQuery({
    queryKey: ['payments', 'cash-register', 'balance'],
    queryFn: () => healthcareApi.payment.getCashRegisterBalance(),
  });
}

/**
 * Hook for opening cash register
 */
export function useOpenCashRegister() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ openingBalance, notes }) => 
      healthcareApi.payment.openCashRegister(openingBalance, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments', 'cash-register'] });
    },
  });
}

/**
 * Hook for closing cash register
 */
export function useCloseCashRegister() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ closingBalance, notes }) => 
      healthcareApi.payment.closeCashRegister(closingBalance, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments', 'cash-register'] });
    },
  });
}

/**
 * Hook for getting payment reconciliation
 */
export function usePaymentReconciliation(date) {
  return useQuery({
    queryKey: ['payments', 'reconciliation', date],
    queryFn: () => healthcareApi.payment.getReconciliation(date),
    enabled: !!date,
  });
}

