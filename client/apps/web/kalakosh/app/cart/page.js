'use client';

import { useRouter } from 'next/navigation';
import { motion } from 'motion/react';
import { Trash2, Plus, Minus } from 'lucide-react';
import { useCart } from '@/hooks/useCart';
import { useUpdateCartItem, useRemoveCartItem } from '@/hooks/useCart';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { Button } from '@/components/ui/button';
import { ImageWithFallback } from '@/components/figma/ImageWithFallback';

/**
 * Cart Page
 */
export default function CartPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();
  const { openLogin } = useAuthModal();
  const { data: cart, isLoading } = useCart({ enabled: isAuthenticated });
  const updateCartItemMutation = useUpdateCartItem();
  const removeCartItemMutation = useRemoveCartItem();

  if (!isAuthenticated) {
    openLogin();
    router.push('/');
    return null;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">Loading...</div>
      </div>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-2xl mb-4 text-[rgb(var(--color-indigo))]">Your cart is empty</p>
          <Button onClick={() => router.push('/shop')}>
            Continue Shopping
          </Button>
        </div>
      </div>
    );
  }

  const handleUpdateQuantity = async (itemId, newQuantity) => {
    if (newQuantity <= 0) {
      await removeCartItemMutation.mutateAsync(itemId);
    } else {
      await updateCartItemMutation.mutateAsync({
        itemId,
        data: { quantity: newQuantity },
      });
    }
  };

  const handleRemoveItem = async (itemId) => {
    await removeCartItemMutation.mutateAsync(itemId);
  };

  // Use cart.subtotal if available, otherwise calculate from items
  const subtotal = cart.subtotal ?? cart.items.reduce((sum, item) => {
    const itemTotal = (item.totalPrice || (item.unitPrice || 0) * (item.quantity || 0));
    return sum + itemTotal;
  }, 0);
  const total = cart.total ?? (subtotal + (cart.shippingCost || 0) - (cart.discountAmount || 0));

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))] py-12">
      <div className="container mx-auto px-6">
        <h1 className="text-4xl mb-8 text-[rgb(var(--color-indigo))]">Shopping Cart</h1>

        <div className="grid lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {cart.items.map((item) => (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-white rounded-xl shadow-md p-6 flex gap-6"
              >
                <div className="w-32 h-32 rounded-lg overflow-hidden bg-[rgb(var(--muted))] flex-shrink-0">
                  <ImageWithFallback
                    src={item.image || ''}
                    alt={item.productName || 'Product'}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="flex-1">
                  <h3 className="text-xl mb-2 text-[rgb(var(--color-indigo))]">
                    {item.productName || 'Product'}
                  </h3>
                  <p className="text-lg text-[rgb(var(--color-terracotta))] mb-4">
                    ₹{(item.unitPrice || 0).toLocaleString('en-IN')}
                  </p>
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2 border rounded-lg">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleUpdateQuantity(item.id, (item.quantity || 1) - 1)}
                        className="h-8 w-8 p-0"
                      >
                        <Minus className="w-4 h-4" />
                      </Button>
                      <span className="w-12 text-center">{item.quantity || 1}</span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleUpdateQuantity(item.id, (item.quantity || 1) + 1)}
                        className="h-8 w-8 p-0"
                      >
                        <Plus className="w-4 h-4" />
                      </Button>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveItem(item.id)}
                      className="text-red-600 hover:text-red-700"
                    >
                      <Trash2 className="w-4 h-4 mr-2" />
                      Remove
                    </Button>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-md p-6 sticky top-24">
              <h2 className="text-2xl mb-6 text-[rgb(var(--color-indigo))]">Order Summary</h2>
              <div className="space-y-4 mb-6">
                <div className="flex justify-between">
                  <span>Subtotal</span>
                  <span>₹{(subtotal || 0).toLocaleString('en-IN')}</span>
                </div>
                {(cart.shippingCost || 0) > 0 && (
                  <div className="flex justify-between">
                    <span>Shipping</span>
                    <span>₹{(cart.shippingCost || 0).toLocaleString('en-IN')}</span>
                  </div>
                )}
                {(cart.discountAmount || 0) > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount</span>
                    <span>-₹{(cart.discountAmount || 0).toLocaleString('en-IN')}</span>
                  </div>
                )}
                <div className="border-t pt-4 flex justify-between text-xl font-semibold">
                  <span>Total</span>
                  <span className="text-[rgb(var(--color-terracotta))]">
                    ₹{(total || 0).toLocaleString('en-IN')}
                  </span>
                </div>
              </div>
              <Button
                onClick={() => router.push('/checkout')}
                size="lg"
                className="w-full bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white"
              >
                Proceed to Checkout
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

