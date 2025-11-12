'use client';

import { useParams } from 'next/navigation';
import { useOrder } from '@/hooks/useOrders';

/**
 * Admin Order Detail Page
 */
export default function AdminOrderDetailPage() {
  const params = useParams();
  const orderId = params.id;
  const { data: order, isLoading } = useOrder(orderId);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!order) {
    return <div>Order not found</div>;
  }

  return (
    <div>
      <h1 className="text-3xl mb-6 text-[rgb(var(--color-indigo))]">Order Details</h1>
      <div className="bg-white rounded-lg shadow p-6">
        <div className="grid md:grid-cols-2 gap-6 mb-6">
          <div>
            <h2 className="text-xl mb-4">Order Information</h2>
            <p><span className="font-semibold">Order ID:</span> {order.id}</p>
            <p><span className="font-semibold">Status:</span> <span className="capitalize">{order.status}</span></p>
            <p><span className="font-semibold">Date:</span> {new Date(order.createdAt).toLocaleDateString()}</p>
          </div>
          <div>
            <h2 className="text-xl mb-4">Customer</h2>
            {order.customer && (
              <>
                <p>{order.customer.fullName || order.customer.email}</p>
                <p>{order.customer.email}</p>
                {order.customer.phone && <p>{order.customer.phone}</p>}
              </>
            )}
          </div>
        </div>
        <div className="mb-6">
          <h2 className="text-xl mb-4">Items</h2>
          <div className="space-y-2">
            {order.items.map((item) => (
              <div key={item.id} className="flex justify-between py-2 border-b">
                <div>
                  <p className="font-semibold">{item.productName}</p>
                  <p className="text-sm text-[rgb(var(--muted-foreground))]">Qty: {item.quantity}</p>
                </div>
                <p>₹{item.totalPrice.toLocaleString('en-IN')}</p>
              </div>
            ))}
          </div>
        </div>
        <div className="text-right">
          <p className="text-2xl font-semibold text-[rgb(var(--color-terracotta))]">
            Total: ₹{order.totalAmount.toLocaleString('en-IN')}
          </p>
        </div>
      </div>
    </div>
  );
}

