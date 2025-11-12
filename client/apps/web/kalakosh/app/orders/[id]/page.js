'use client';

import { useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useOrder } from '@/hooks/useOrders';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { Button } from '@/components/ui/button';
import { ImageWithFallback } from '@/components/figma/ImageWithFallback';

/**
 * Order Detail Page
 */
export default function OrderDetailPage() {
  const params = useParams();
  const router = useRouter();
  const orderId = params.id;
  const { isAuthenticated, hasCheckedAuth } = useAuthStore();
  const { openLogin } = useAuthModal();
  const { data: order, isLoading } = useOrder(orderId);

  useEffect(() => {
    if (hasCheckedAuth && !isAuthenticated) {
      openLogin();
      router.push('/');
    }
  }, [hasCheckedAuth, isAuthenticated, openLogin, router]);

  if (!hasCheckedAuth || !isAuthenticated) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">Loading...</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-lg">Order not found</p>
          <Button onClick={() => router.push('/orders')} className="mt-4">
            Back to Orders
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))] py-12">
      <div className="container mx-auto px-6 max-w-4xl">
        <Button
          onClick={() => router.push('/orders')}
          variant="ghost"
          className="mb-6"
        >
          ← Back to Orders
        </Button>

        <div className="bg-white rounded-xl shadow-lg p-8">
          <h1 className="text-3xl mb-6 text-[rgb(var(--color-indigo))]">Order Details</h1>
          
          <div className="grid md:grid-cols-2 gap-8 mb-8">
            <div>
              <h2 className="text-xl mb-4">Order Information</h2>
              <div className="space-y-2">
                <p><span className="font-semibold">Order ID:</span> {order.id}</p>
                <p><span className="font-semibold">Status:</span> <span className="capitalize">{order.status}</span></p>
                <p><span className="font-semibold">Date:</span> {new Date(order.createdAt).toLocaleDateString()}</p>
              </div>
            </div>
            <div>
              <h2 className="text-xl mb-4">Shipping Address</h2>
              {order.shippingAddress && (
                <div className="space-y-1">
                  <p>{order.shippingAddress.fullName}</p>
                  <p>{order.shippingAddress.addressLine1}</p>
                  {order.shippingAddress.addressLine2 && <p>{order.shippingAddress.addressLine2}</p>}
                  <p>{order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}</p>
                  <p>{order.shippingAddress.phone}</p>
                </div>
              )}
            </div>
          </div>

          <div className="mb-8">
            <h2 className="text-xl mb-4">Items</h2>
            <div className="space-y-4">
              {order.items.map((item) => (
                <div key={item.id} className="flex gap-4 p-4 bg-[rgb(var(--muted))] rounded-lg">
                  <div className="w-24 h-24 rounded-lg overflow-hidden bg-white flex-shrink-0">
                    <ImageWithFallback
                      src={item.productImage || ''}
                      alt={item.productName}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold text-lg">{item.productName}</p>
                    <p className="text-sm text-[rgb(var(--muted-foreground))]">
                      Quantity: {item.quantity}
                    </p>
                    <p className="text-lg mt-2">₹{item.totalPrice.toLocaleString('en-IN')}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="border-t pt-6">
            <div className="space-y-2 text-right">
              <div className="flex justify-between">
                <span>Subtotal</span>
                <span>₹{order.subtotal.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping</span>
                <span>₹{order.shippingCost.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-2xl font-semibold pt-4 border-t">
                <span>Total</span>
                <span className="text-[rgb(var(--color-terracotta))]">
                  ₹{order.totalAmount.toLocaleString('en-IN')}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

