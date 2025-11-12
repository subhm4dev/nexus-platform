'use client';

import { useOrders } from '@/hooks/useOrders';
import { Button } from '@/components/ui/button';

/**
 * Admin Orders Page
 */
export default function AdminOrdersPage() {
  const { data: ordersData, isLoading } = useOrders();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const orders = ordersData?.content || [];

  return (
    <div>
      <h1 className="text-3xl mb-6 text-[rgb(var(--color-indigo))]">Orders</h1>
      {orders.length === 0 ? (
        <p className="text-[rgb(var(--muted-foreground))]">No orders yet</p>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.id} className="bg-white rounded-lg shadow p-6">
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-semibold">{order.id}</p>
                  <p className="text-sm text-[rgb(var(--muted-foreground))]">
                    {new Date(order.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="text-right">
                  <p className="capitalize">{order.status}</p>
                  <p className="text-lg font-semibold text-[rgb(var(--color-terracotta))]">
                    ₹{order.totalAmount.toLocaleString('en-IN')}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

