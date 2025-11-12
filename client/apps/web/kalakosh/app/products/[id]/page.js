'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { motion } from 'motion/react';
import { ShoppingCart, Heart, Share2 } from 'lucide-react';
import { useProduct } from '@/hooks/useProducts';
import { useAddToCart } from '@/hooks/useCart';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { Button } from '@/components/ui/button';
import { ImageWithFallback } from '@/components/figma/ImageWithFallback';

/**
 * Product Detail Page
 */
export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = params.id;
  const { isAuthenticated, setPendingAction } = useAuthStore();
  const { openLogin } = useAuthModal();
  
  const { data: product, isLoading } = useProduct(productId);
  const addToCartMutation = useAddToCart();
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [isFavorite, setIsFavorite] = useState(false);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      // Store pending action to execute after login/register
      setPendingAction(async () => {
        try {
          await addToCartMutation.mutateAsync({
            productId,
            quantity: 1,
          });
        } catch (error) {
          console.error('Error adding to cart after auth:', error);
        }
      });
      openLogin();
      return;
    }
    try {
      await addToCartMutation.mutateAsync({
        productId,
        quantity: 1,
      });
    } catch (error) {
      // Error handling
    }
  };

  const handleBuyNow = async () => {
    if (!isAuthenticated) {
      // Store pending action to execute after login/register (add to cart then navigate)
      setPendingAction(async () => {
        try {
          await addToCartMutation.mutateAsync({
            productId,
            quantity: 1,
          });
          router.push('/cart');
        } catch (error) {
          console.error('Error adding to cart after auth:', error);
        }
      });
      openLogin();
      return;
    }
    try {
      await addToCartMutation.mutateAsync({
        productId,
        quantity: 1,
      });
      router.push('/cart');
    } catch (error) {
      // Error handling
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">Loading...</div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-lg">Product not found</p>
          <Button onClick={() => router.push('/shop')} className="mt-4">
            Back to Shop
          </Button>
        </div>
      </div>
    );
  }

  const images = product.images || [];

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))] py-12">
      <div className="container mx-auto px-6">
        <div className="grid lg:grid-cols-2 gap-12">
          {/* Images */}
          <div>
            <div className="relative aspect-[3/4] rounded-xl overflow-hidden bg-[rgb(var(--muted))] mb-4">
              <ImageWithFallback
                src={images[selectedImageIndex] || images[0] || ''}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            </div>
            {images.length > 1 && (
              <div className="grid grid-cols-4 gap-2">
                {images.map((image, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedImageIndex(index)}
                    className={`relative aspect-square rounded-lg overflow-hidden border-2 ${
                      selectedImageIndex === index
                        ? 'border-[rgb(var(--color-gold))]'
                        : 'border-transparent'
                    }`}
                  >
                    <ImageWithFallback
                      src={image}
                      alt={`${product.name} - View ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Product Info */}
          <div>
            <h1 className="text-4xl md:text-5xl mb-4 text-[rgb(var(--color-indigo))]">
              {product.name}
            </h1>
            <p className="text-xl text-[rgb(var(--muted-foreground))] mb-6">
              by {product.sellerName || 'Master Artisan'}
            </p>
            <p className="text-4xl text-[rgb(var(--color-terracotta))] mb-8">
              ₹{product.price.toLocaleString('en-IN')}
            </p>

            <div className="space-y-4 mb-8">
              <div>
                <h3 className="text-lg font-semibold mb-2">Description</h3>
                <p className="text-[rgb(var(--muted-foreground))]">
                  {product.description || 'A beautiful Pattachitra artwork crafted by master artisans.'}
                </p>
              </div>
              {product.attributes?.size && (
                <div>
                  <h3 className="text-lg font-semibold mb-2">Size</h3>
                  <p className="text-[rgb(var(--muted-foreground))]">{product.attributes.size}</p>
                </div>
              )}
              {product.attributes?.materials && (
                <div>
                  <h3 className="text-lg font-semibold mb-2">Materials</h3>
                  <p className="text-[rgb(var(--muted-foreground))]">{product.attributes.materials}</p>
                </div>
              )}
            </div>

            <div className="flex gap-4">
              <Button
                onClick={handleAddToCart}
                size="lg"
                className="flex-1 bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white gap-2"
              >
                <ShoppingCart className="w-5 h-5" />
                Add to Cart
              </Button>
              <Button
                onClick={handleBuyNow}
                size="lg"
                variant="outline"
                className="flex-1 gap-2"
              >
                Buy Now
              </Button>
              <Button
                variant="ghost"
                size="lg"
                onClick={() => setIsFavorite(!isFavorite)}
                className="gap-2"
              >
                <Heart
                  className={`w-5 h-5 ${
                    isFavorite
                      ? 'fill-[rgb(var(--color-terracotta))] text-[rgb(var(--color-terracotta))]'
                      : ''
                  }`}
                />
              </Button>
              <Button variant="ghost" size="lg" className="gap-2">
                <Share2 className="w-5 h-5" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

