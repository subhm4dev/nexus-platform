'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'motion/react';
import { MapPin, CheckCircle2 } from 'lucide-react';
import { useCart } from '@/hooks/useCart';
import { useAddresses, useCreateAddress } from '@/hooks/useAddresses';
import { useCheckoutInitiate, useCheckoutComplete } from '@/hooks/useCheckout';
import { useCreateOrder } from '@/hooks/usePayment';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { initializeRazorpayCheckout } from '@/lib/razorpay';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

/**
 * Checkout Page
 * 
 * 3-step checkout flow:
 * 1. Address selection/creation
 * 2. Review order
 * 3. Razorpay payment → Complete checkout → Confirmation
 */
export default function CheckoutPage() {
  const router = useRouter();
  const { isAuthenticated, hasCheckedAuth, logout } = useAuthStore();
  const { openLogin } = useAuthModal();
  
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [showAddressForm, setShowAddressForm] = useState(false);
  const [orderSummary, setOrderSummary] = useState(null);
  const [orderConfirmation, setOrderConfirmation] = useState(null);
  const [paymentError, setPaymentError] = useState(null);
  
  const processedPaymentIds = useRef(new Set());
  const isProcessingPayment = useRef(false);

  const { data: cart } = useCart();
  const { data: addresses } = useAddresses();
  const createAddressMutation = useCreateAddress();
  const initiateCheckoutMutation = useCheckoutInitiate();
  const completeCheckoutMutation = useCheckoutComplete();
  const createOrderMutation = useCreateOrder();

  // Redirect if not authenticated
  useEffect(() => {
    if (hasCheckedAuth && !isAuthenticated) {
      openLogin();
      router.push('/');
    }
  }, [hasCheckedAuth, isAuthenticated, openLogin, router]);

  if (!hasCheckedAuth || !isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">Checking authentication...</div>
      </div>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    router.push('/cart');
    return null;
  }

  // Step 1: Address Selection
  const handleAddressSelect = (addressId) => {
    setSelectedAddressId(addressId);
    setShowAddressForm(false);
  };

  const handleAddressCreate = async (e) => {
    e.preventDefault();
    setPaymentError(null); // Clear any previous errors
    
    const formData = new FormData(e.target);
    
    // Format phone number to E.164 format if needed
    let phone = formData.get('phone')?.trim() || '';
    if (phone && !phone.startsWith('+')) {
      // If phone doesn't start with +, assume it's an Indian number and add +91
      if (phone.startsWith('0')) {
        phone = '+91' + phone.substring(1);
      } else if (phone.length === 10) {
        phone = '+91' + phone;
      } else {
        phone = '+91' + phone;
      }
    }
    
    // Map country name to ISO code
    const countryInput = formData.get('country')?.trim() || 'India';
    const countryMap = {
      'India': 'IN',
      'United States': 'US',
      'United Kingdom': 'GB',
    };
    const country = countryMap[countryInput] || (countryInput.length === 2 ? countryInput.toUpperCase() : 'IN');
    
    const addressData = {
      fullName: formData.get('fullName')?.trim() || null,
      phone: phone || undefined, // Required field
      street: formData.get('addressLine1')?.trim() || '', // Map addressLine1 to street
      street2: formData.get('addressLine2')?.trim() || null, // Map addressLine2 to street2
      city: formData.get('city')?.trim() || '',
      state: formData.get('state')?.trim() || '',
      postalCode: formData.get('postalCode')?.trim() || '',
      country: country, // Use ISO code
      type: 'HOME', // Default address type
      isDefault: false,
    };
    
    try {
      const newAddress = await createAddressMutation.mutateAsync(addressData);
      setSelectedAddressId(newAddress.id);
      setShowAddressForm(false);
    } catch (error) {
      console.error('Error creating address:', error);
      const errorMessage = error?.response?.data?.message || 
                          error?.message || 
                          'Failed to create address. Please check all fields and try again.';
      setPaymentError(errorMessage);
    }
  };

  const handleAddressStepNext = async () => {
    if (!selectedAddressId) {
      alert('Please select or create an address');
      return;
    }
    try {
      const summary = await initiateCheckoutMutation.mutateAsync({
        shippingAddressId: selectedAddressId,
      });
      setOrderSummary(summary);
      setCurrentStep(2);
    } catch (error) {
      console.error('Checkout initiate error:', error);
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to initiate checkout.';
      
      // Check if it's an authentication error (401/403)
      if (error?.response?.status === 401 || error?.response?.status === 403 || 
          errorMessage.toLowerCase().includes('session has expired') ||
          errorMessage.toLowerCase().includes('log in again')) {
        // Session expired, log out and redirect
        await logout();
        router.push('/');
        alert('Your session has expired. Please log in again.');
      } else {
        alert(errorMessage);
      }
    }
  };

  // Step 2: Review and Place Order
  const handlePlaceOrder = async () => {
    if (isProcessingPayment.current) return;
    isProcessingPayment.current = true;
    setPaymentError(null);

    try {
      const totalAmount = orderSummary.total || orderSummary.totalAmount || 0;
      if (totalAmount <= 0) {
        setPaymentError('Invalid order total. Please try again.');
        isProcessingPayment.current = false;
        return;
      }

      // Generate a temporary order ID for Razorpay order creation
      const tempOrderId = crypto.randomUUID();

      // Create Razorpay order
      const razorpayOrder = await createOrderMutation.mutateAsync({
        orderId: tempOrderId,
        amount: totalAmount,
        currency: orderSummary.currency || 'INR',
        paymentMethodType: 'CARD', // Default to CARD for Razorpay checkout
      });

      if (!razorpayOrder?.razorpayOrderId) {
        throw new Error('Failed to create Razorpay order. Please try again.');
      }

      // Open Razorpay checkout modal
      await initializeRazorpayCheckout({
        key: process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID,
        amount: totalAmount * 100, // Convert to paise
        currency: orderSummary.currency || 'INR',
        name: 'Kalakosh',
        description: `Order for ${orderSummary.items?.length || 0} item(s)`,
        order_id: razorpayOrder.razorpayOrderId,
        prefill: {
          name: addresses?.find(a => a.id === selectedAddressId)?.fullName || '',
          email: useAuthStore.getState().user?.email || '',
          contact: addresses?.find(a => a.id === selectedAddressId)?.phone || '',
        },
        handler: async (response) => {
          try {
            // Complete checkout with Razorpay payment_id
            // The checkout service will verify the payment with the payment service
            const confirmation = await completeCheckoutMutation.mutateAsync({
              shippingAddressId: selectedAddressId,
              paymentGatewayTransactionId: response.razorpay_payment_id,
            });

            setOrderConfirmation(confirmation);
            setCurrentStep(3);
          } catch (error) {
            setPaymentError('Payment verification failed. Please contact support.');
          } finally {
            isProcessingPayment.current = false;
          }
        },
        onError: (error) => {
          console.error('Razorpay checkout error:', error);
          setPaymentError(error.message || 'Payment failed. Please try again.');
          isProcessingPayment.current = false;
        },
      });
    } catch (error) {
      console.error('Payment initialization error:', error);
      const errorMessage = error?.response?.data?.message || 
                          error?.message || 
                          'Failed to initialize payment. Please try again.';
      setPaymentError(errorMessage);
      isProcessingPayment.current = false;
    }
  };

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))] py-12">
      <div className="container mx-auto px-6 max-w-4xl">
        {/* Steps Indicator */}
        <div className="flex items-center justify-center mb-12">
          {[1, 2, 3].map((step) => (
            <div key={step} className="flex items-center">
              <div
                className={`w-10 h-10 rounded-full flex items-center justify-center ${
                  currentStep >= step
                    ? 'bg-[rgb(var(--color-indigo))] text-white'
                    : 'bg-[rgb(var(--muted))] text-[rgb(var(--muted-foreground))]'
                }`}
              >
                {currentStep > step ? <CheckCircle2 className="w-6 h-6" /> : step}
              </div>
              {step < 3 && (
                <div
                  className={`w-24 h-1 mx-2 ${
                    currentStep > step
                      ? 'bg-[rgb(var(--color-indigo))]'
                      : 'bg-[rgb(var(--muted))]'
                  }`}
                />
              )}
            </div>
          ))}
        </div>

        {/* Step 1: Address Selection */}
        {currentStep === 1 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-white rounded-xl shadow-lg p-8"
          >
            <h2 className="text-2xl mb-6 text-[rgb(var(--color-indigo))]">Select Address</h2>
            
            {!showAddressForm ? (
              <>
                <div className="space-y-4 mb-6">
                  {addresses?.map((address) => (
                    <button
                      key={address.id}
                      onClick={() => handleAddressSelect(address.id)}
                      className={`w-full text-left p-4 rounded-lg border-2 transition-all ${
                        selectedAddressId === address.id
                          ? 'border-[rgb(var(--color-indigo))] bg-[rgb(var(--color-indigo)/0.05)]'
                          : 'border-[rgb(var(--border))] hover:border-[rgb(var(--color-indigo)/0.5)]'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <MapPin className="w-5 h-5 text-[rgb(var(--color-indigo))] mt-1" />
                        <div>
                          <p className="font-semibold">{address.fullName}</p>
                          <p className="text-sm text-[rgb(var(--muted-foreground))]">
                            {address.addressLine1}, {address.city}, {address.state} {address.postalCode}
                          </p>
                          <p className="text-sm text-[rgb(var(--muted-foreground))]">{address.phone}</p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
                <Button
                  onClick={() => setShowAddressForm(true)}
                  variant="outline"
                  className="w-full mb-6"
                >
                  Add New Address
                </Button>
                <Button
                  onClick={handleAddressStepNext}
                  disabled={!selectedAddressId}
                  size="lg"
                  className="w-full bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white"
                >
                  Continue to Review
                </Button>
              </>
            ) : (
              <form onSubmit={handleAddressCreate} className="space-y-4">
                {paymentError && (
                  <div className="p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
                    {paymentError}
                  </div>
                )}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="fullName">Full Name (Optional)</Label>
                    <Input id="fullName" name="fullName" placeholder="Your name" />
                  </div>
                  <div>
                    <Label htmlFor="phone">Phone *</Label>
                    <Input 
                      id="phone" 
                      name="phone" 
                      type="tel" 
                      placeholder="+919876543210 or 9876543210"
                      required 
                    />
                    <p className="text-xs text-[rgb(var(--muted-foreground))] mt-1">
                      Include country code (e.g., +91 for India)
                    </p>
                  </div>
                </div>
                <div>
                  <Label htmlFor="addressLine1">Address Line 1 *</Label>
                  <Input id="addressLine1" name="addressLine1" placeholder="Street address" required />
                </div>
                <div>
                  <Label htmlFor="addressLine2">Address Line 2 (Optional)</Label>
                  <Input id="addressLine2" name="addressLine2" placeholder="Apartment, suite, etc." />
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <Label htmlFor="city">City *</Label>
                    <Input id="city" name="city" required />
                  </div>
                  <div>
                    <Label htmlFor="state">State *</Label>
                    <Input id="state" name="state" required />
                  </div>
                  <div>
                    <Label htmlFor="postalCode">Postal Code *</Label>
                    <Input id="postalCode" name="postalCode" required />
                  </div>
                </div>
                <div>
                  <Label htmlFor="country">Country *</Label>
                  <Input 
                    id="country" 
                    name="country" 
                    defaultValue="India"
                    required 
                  />
                </div>
                <div className="flex gap-4">
                  <Button 
                    type="submit" 
                    className="flex-1"
                    disabled={createAddressMutation.isPending}
                  >
                    {createAddressMutation.isPending ? 'Saving...' : 'Save Address'}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => {
                      setShowAddressForm(false);
                      setPaymentError(null);
                    }}
                    className="flex-1"
                    disabled={createAddressMutation.isPending}
                  >
                    Cancel
                  </Button>
                </div>
              </form>
            )}
          </motion.div>
        )}

        {/* Step 2: Review Order */}
        {currentStep === 2 && orderSummary && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-white rounded-xl shadow-lg p-8"
          >
            <h2 className="text-2xl mb-6 text-[rgb(var(--color-indigo))]">Review Order</h2>
            
            <div className="space-y-4 mb-6">
              {orderSummary.items?.map((item) => (
                <div key={item.productId} className="flex justify-between py-4 border-b">
                  <div>
                    <p className="font-semibold">{item.productName || 'Product'}</p>
                    <p className="text-sm text-[rgb(var(--muted-foreground))]">Qty: {item.quantity || 1}</p>
                  </div>
                  <p className="text-lg">₹{(item.totalPrice || 0).toLocaleString('en-IN')}</p>
                </div>
              ))}
            </div>

            <Separator className="my-6" />

            <div className="space-y-2 mb-6">
              <div className="flex justify-between">
                <span>Subtotal</span>
                <span>₹{(orderSummary.subtotal || 0).toLocaleString('en-IN')}</span>
              </div>
              {(orderSummary.shippingCost || 0) > 0 && (
                <div className="flex justify-between">
                  <span>Shipping</span>
                  <span>₹{(orderSummary.shippingCost || 0).toLocaleString('en-IN')}</span>
                </div>
              )}
              {(orderSummary.discountAmount || 0) > 0 && (
                <div className="flex justify-between text-green-600">
                  <span>Discount</span>
                  <span>-₹{(orderSummary.discountAmount || 0).toLocaleString('en-IN')}</span>
                </div>
              )}
              {(orderSummary.taxAmount || 0) > 0 && (
                <div className="flex justify-between">
                  <span>Tax</span>
                  <span>₹{(orderSummary.taxAmount || 0).toLocaleString('en-IN')}</span>
                </div>
              )}
              <div className="flex justify-between text-2xl font-semibold pt-4 border-t">
                <span>Total</span>
                <span className="text-[rgb(var(--color-terracotta))]">
                  ₹{(orderSummary.total || orderSummary.totalAmount || 0).toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            {paymentError && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg">
                {paymentError}
              </div>
            )}

            <div className="flex gap-4">
              <Button
                onClick={() => setCurrentStep(1)}
                variant="outline"
                className="flex-1"
              >
                Back
              </Button>
              <Button
                onClick={handlePlaceOrder}
                disabled={isProcessingPayment.current}
                size="lg"
                className="flex-1 bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white"
              >
                {isProcessingPayment.current ? 'Processing...' : 'Place Order'}
              </Button>
            </div>
          </motion.div>
        )}

        {/* Step 3: Order Confirmation */}
        {currentStep === 3 && orderConfirmation && (
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-xl shadow-lg p-8 text-center"
          >
            <CheckCircle2 className="w-16 h-16 text-green-600 mx-auto mb-4" />
            <h2 className="text-3xl mb-4 text-[rgb(var(--color-indigo))]">Order Placed Successfully!</h2>
            <p className="text-lg text-[rgb(var(--muted-foreground))] mb-6">
              Your order ID: {orderConfirmation.orderId}
            </p>
            <div className="flex gap-4 justify-center">
              <Button
                onClick={() => router.push('/orders')}
                variant="outline"
              >
                View Orders
              </Button>
              <Button
                onClick={() => router.push('/shop')}
                className="bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white"
              >
                Continue Shopping
              </Button>
            </div>
          </motion.div>
        )}
      </div>
    </div>
  );
}

