'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'motion/react';
import { Package } from 'lucide-react';
import { useOrders } from '@/hooks/useOrders';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { OrderCard } from '@ecom/components';
import { Button } from '@/components/ui/button';

/**
 * Orders Page
 * 
 * Displays user's order history with:
 * - Order list with status badges
 * - Filter by status
 * - Pagination
 * - Click to view order details
 */
export default function OrdersPage() {
  const router = useRouter();
  const { isAuthenticated, hasCheckedAuth } = useAuthStore();
  const { openLogin } = useAuthModal();
  const [statusFilter, setStatusFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);

  const { data: ordersData, isLoading, error } = useOrders({
    page: currentPage,
    size: 10,
    status: statusFilter || undefined,
  }, {
    enabled: isAuthenticated,
  });

  useEffect(() => {
    if (hasCheckedAuth && !isAuthenticated) {
      openLogin();
      router.push('/');
    }
  }, [hasCheckedAuth, isAuthenticated, openLogin, router]);

  if (!hasCheckedAuth || !isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[rgb(var(--color-ivory))]">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-[rgb(var(--color-indigo))]"></div>
          <p className="mt-4 text-[rgb(var(--muted-foreground))]">Checking authentication...</p>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[rgb(var(--color-ivory))]">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-[rgb(var(--color-indigo))]"></div>
          <p className="mt-4 text-[rgb(var(--muted-foreground))]">Loading orders...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[rgb(var(--color-ivory))]">
        <div className="text-center">
          <p className="text-red-600 mb-4">Failed to load orders</p>
          <Button
            onClick={() => window.location.reload()}
            variant="outline"
          >
            Try again
          </Button>
        </div>
      </div>
    );
  }

  const orders = ordersData?.content || [];
  const totalPages = ordersData?.totalPages || 0;

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
      className="min-h-screen bg-[rgb(var(--color-ivory))] py-12"
    >
      <div className="container mx-auto px-6 max-w-6xl">
        <motion.h1
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-4xl mb-8 text-[rgb(var(--color-indigo))] font-semibold"
        >
          My Orders {orders.length > 0 && `(${ordersData?.totalElements || orders.length})`}
        </motion.h1>

        {/* Status Filter */}
        <div className="mb-6">
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setCurrentPage(0);
            }}
            className="px-4 py-2 border border-[rgb(var(--border))] rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-[rgb(var(--color-indigo))] focus:border-transparent"
          >
            <option value="">All Orders</option>
            <option value="PENDING">Pending</option>
            <option value="PLACED">Placed</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="SHIPPED">Shipped</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="RETURNED">Returned</option>
          </select>
        </div>

        {/* Orders List */}
        {orders.length === 0 ? (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="max-w-md mx-auto text-center py-24"
          >
            <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center mx-auto mb-6 shadow-md">
              <Package className="w-12 h-12 text-[rgb(var(--muted-foreground))]" />
            </div>
            <h2 className="text-2xl mb-4 text-[rgb(var(--color-indigo))] font-semibold">No orders yet</h2>
            <p className="text-[rgb(var(--muted-foreground))] mb-6">
              When you place an order, it will appear here.
            </p>
            <Button 
              onClick={() => router.push('/shop')} 
              className="bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white"
            >
              Start Shopping
            </Button>
          </motion.div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <OrderCard
                key={order.id}
                order={order}
                onClick={() => router.push(`/orders/${order.id}`)}
              />
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex justify-center items-center gap-2 mt-8">
            <Button
              variant="outline"
              onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
              disabled={currentPage === 0}
              className="disabled:opacity-30 disabled:cursor-not-allowed"
            >
              Previous
            </Button>
            <div className="flex gap-1">
              {Array.from({ length: totalPages }, (_, i) => i).map((page) => {
                const displayPage = page + 1;
                if (
                  page === 0 ||
                  page === totalPages - 1 ||
                  (page >= currentPage - 1 && page <= currentPage + 1)
                ) {
                  return (
                    <Button
                      key={page}
                      variant={currentPage === page ? "default" : "outline"}
                      onClick={() => setCurrentPage(page)}
                      className={`min-w-[40px] ${
                        currentPage === page
                          ? 'bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white'
                          : ''
                      }`}
                    >
                      {displayPage}
                    </Button>
                  );
                } else if (page === currentPage - 2 || page === currentPage + 2) {
                  return <span key={page} className="px-2 text-[rgb(var(--muted-foreground))]">...</span>;
                }
                return null;
              })}
            </div>
            <Button
              variant="outline"
              onClick={() => setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))}
              disabled={currentPage >= totalPages - 1}
              className="disabled:opacity-30 disabled:cursor-not-allowed"
            >
              Next
            </Button>
          </div>
        )}
      </div>
    </motion.div>
  );
}

