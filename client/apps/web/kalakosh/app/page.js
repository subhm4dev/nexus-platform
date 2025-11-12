'use client';

import { motion } from 'motion/react';
import { ArrowRight, Award, Sparkles, Users } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { useProducts } from '@/hooks/useProducts';
import { useAddToCart } from '@/hooks/useCart';
import { Button } from '@/components/ui/button';
import { PattachitraWatermark } from '@/components/PattachitraWatermark';
import { ProductCard } from '@/components/ProductCard';
import { ImageWithFallback } from '@/components/figma/ImageWithFallback';

/**
 * Home Page
 * 
 * Main homepage featuring:
 * - Hero section
 * - Featured Collection
 * - About Pattachitra
 * - Featured Artists
 * - Trust Indicators
 */
export default function Home() {
  const router = useRouter();
  const { isAuthenticated, setPendingAction } = useAuthStore();
  const { openLogin } = useAuthModal();
  
  // Fetch featured products (first page, 4 products)
  const { data: featuredProductsData, isLoading: isLoadingProducts, error: productsError } = useProducts({ page: 0, size: 4 });
  
  // Add to cart mutation
  const addToCartMutation = useAddToCart();

  // Extract featured products
  const featuredProducts = featuredProductsData?.content?.slice(0, 4) || [];

  // Handle add to cart
  const handleAddToCart = async (productId) => {
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
      // Error feedback could be shown via toast notification
    }
  };

  // Handle quick view
  const handleQuickView = (productId) => {
    router.push(`/products/${productId}`);
  };

  // Mock artists data (will be replaced with API call later)
  const artists = [
    {
      id: '1',
      name: 'Rajesh Mahapatra',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      specialty: 'Mythological Themes',
      worksCount: 12,
      bio: 'Master artisan specializing in traditional mythological narratives',
    },
    {
      id: '2',
      name: 'Priyanka Das',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      specialty: 'Festival Art',
      worksCount: 8,
      bio: 'Expert in creating vibrant festival-themed Pattachitra artworks',
    },
    {
      id: '3',
      name: 'Suresh Patnaik',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      specialty: 'Nature & Life',
      worksCount: 15,
      bio: 'Renowned for capturing the essence of nature and daily life',
    },
    {
      id: '4',
      name: 'Meera Nayak',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      specialty: 'Deity Portraits',
      worksCount: 10,
      bio: 'Specializes in creating detailed deity portraits with traditional techniques',
    },
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative min-h-[85vh] flex items-center justify-center overflow-hidden bg-gradient-to-br from-[rgb(var(--color-indigo))] via-[rgb(var(--color-indigo-light))] to-[rgb(var(--color-terracotta))]">
        <PattachitraWatermark />
        
        <div className="container mx-auto px-6 relative z-10">
          <div className="max-w-4xl mx-auto text-center">
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8 }}
            >
              <h1 className="text-5xl md:text-7xl mb-6 text-white">
                Timeless Art,
                <br />
                <span className="bg-gradient-to-r from-[rgb(var(--color-gold))] to-[rgb(var(--color-ochre))] bg-clip-text text-transparent">
                  Eternal Beauty
                </span>
              </h1>
              <p className="text-xl md:text-2xl text-white/90 mb-10 leading-relaxed max-w-2xl mx-auto">
                Discover authentic Pattachitra masterpieces, handcrafted by master artisans 
                preserving centuries of Indian artistic heritage.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button
                  onClick={() => router.push('/shop')}
                  size="lg"
                  className="bg-white text-[rgb(var(--color-indigo))] hover:bg-white/90 gap-2 h-14 px-8 text-lg"
                >
                  Explore Collection
                  <ArrowRight className="w-5 h-5" />
                </Button>
                <Button
                  onClick={() => router.push('/about')}
                  size="lg"
                  variant="outline"
                  className="border-2 border-white text-white hover:bg-white/10 h-14 px-8 text-lg"
                >
                  Meet Our Artists
                </Button>
              </div>
            </motion.div>
          </div>
        </div>

        {/* Scroll Indicator */}
        <motion.div
          animate={{ y: [0, 10, 0] }}
          transition={{ duration: 2, repeat: Infinity }}
          className="absolute bottom-8 left-1/2 -translate-x-1/2"
        >
          <div className="w-6 h-10 border-2 border-white/50 rounded-full flex justify-center p-2">
            <div className="w-1.5 h-3 bg-white/50 rounded-full" />
          </div>
        </motion.div>
      </section>

      {/* Error Display */}
      {productsError && (
        <section className="py-12 bg-red-50">
          <div className="container mx-auto px-6">
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              <p className="font-semibold">Error loading products:</p>
              <p className="text-sm mt-1">{productsError.message || 'Unknown error'}</p>
              {productsError.message?.includes('NEXT_PUBLIC_APP_TENANT_ID') && (
                <div className="mt-2 text-sm">
                  <p>Please create a <code>.env.local</code> file with:</p>
                  <pre className="mt-2 bg-red-200 p-2 rounded text-xs">
{`NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
NEXT_PUBLIC_APP_TENANT_ID=371e4723-6d8c-40d2-934e-dd82a80e6541`}
                  </pre>
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      {/* Loading State */}
      {isLoadingProducts && !productsError && (
        <section className="py-24 bg-white">
          <div className="container mx-auto px-6">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-[rgb(var(--color-terracotta))]"></div>
              <p className="mt-4 text-[rgb(var(--muted-foreground))]">Loading products...</p>
            </div>
          </div>
        </section>
      )}

      {/* Empty State */}
      {!productsError && !isLoadingProducts && featuredProducts.length === 0 && (
        <section className="py-24 bg-white">
          <div className="container mx-auto px-6">
            <div className="text-center">
              <p className="text-lg text-[rgb(var(--muted-foreground))]">No products found. Please run the seed script to populate the database.</p>
            </div>
          </div>
        </section>
      )}

      {/* Featured Collections */}
      {!productsError && !isLoadingProducts && featuredProducts.length > 0 && (
        <section className="py-24 bg-white">
          <div className="container mx-auto px-6">
            <div className="text-center mb-16">
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.6 }}
              >
                <span className="text-sm uppercase tracking-wider text-[rgb(var(--color-terracotta))] mb-2 block">
                  Curated Selection
                </span>
                <h2 className="text-4xl md:text-5xl mb-4 text-[rgb(var(--color-indigo))]">
                  Featured Masterpieces
                </h2>
                <p className="text-lg text-[rgb(var(--muted-foreground))] max-w-2xl mx-auto">
                  Each artwork tells a story of devotion, tradition, and artistic excellence
                </p>
              </motion.div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              {featuredProducts.map((product, index) => (
                <motion.div
                  key={product.id}
                  initial={{ opacity: 0, y: 30 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: index * 0.1 }}
                >
                  <ProductCard
                    id={product.id}
                    title={product.name}
                    artist={product.sellerName || 'Master Artisan'}
                    price={product.price}
                    image={product.images?.[0] || ''}
                    size={product.attributes?.size || 'Standard'}
                    onAddToCart={handleAddToCart}
                    onQuickView={handleQuickView}
                  />
                </motion.div>
              ))}
            </div>

            <div className="text-center mt-12">
              <Button
                onClick={() => router.push('/shop')}
                size="lg"
                variant="outline"
                className="gap-2"
              >
                View All Artworks
                <ArrowRight className="w-5 h-5" />
              </Button>
            </div>
          </div>
        </section>
      )}

      {/* About Pattachitra */}
      <section className="py-24 bg-gradient-to-br from-[rgb(var(--color-ivory))] to-[rgb(var(--muted))] relative overflow-hidden">
        <div className="absolute inset-0 opacity-5">
          <PattachitraWatermark />
        </div>
        
        <div className="container mx-auto px-6 relative z-10">
          <div className="grid lg:grid-cols-2 gap-16 items-center">
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.8 }}
            >
              <span className="text-sm uppercase tracking-wider text-[rgb(var(--color-terracotta))] mb-4 block">
                Heritage & Tradition
              </span>
              <h2 className="text-4xl md:text-5xl mb-6 text-[rgb(var(--color-indigo))]">
                The Art of Pattachitra
              </h2>
              <div className="space-y-4 text-lg text-[rgb(var(--muted-foreground))]">
                <p>
                  Pattachitra is one of the oldest and most revered art forms of India, 
                  originating from Odisha over a thousand years ago. The name derives from 
                  'patta' (cloth) and 'chitra' (picture).
                </p>
                <p>
                  Each piece is created using natural colors derived from vegetables, minerals, 
                  and stones, applied with brushes made from animal hair. The process, passed 
                  down through generations, can take weeks to months for a single artwork.
                </p>
                <p>
                  Every brushstroke carries the weight of history, devotion, and the master 
                  artisan's dedication to preserving this UNESCO-recognized cultural treasure.
                </p>
              </div>
              <Button
                onClick={() => router.push('/about')}
                size="lg"
                className="mt-8 bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white gap-2"
              >
                Learn More
                <ArrowRight className="w-5 h-5" />
              </Button>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 30 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.8 }}
              className="relative"
            >
              <div className="relative aspect-[4/5] rounded-2xl overflow-hidden shadow-2xl">
                <ImageWithFallback
                  src="https://images.unsplash.com/photo-1669276064249-4aefccd07be3?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMGFydGlzdCUyMHdvcmtpbmd8ZW58MXx8fHwxNzYyNjY5NTQ2fDA&ixlib=rb-4.1.0&q=80&w=1080"
                  alt="Pattachitra Artist at Work"
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
              </div>
              {/* Decorative Element */}
              <div className="absolute -bottom-6 -right-6 w-32 h-32 bg-gradient-to-br from-[rgb(var(--color-gold))] to-[rgb(var(--color-terracotta))] rounded-full opacity-20 blur-2xl" />
            </motion.div>
          </div>
        </div>
      </section>

      {/* Featured Artists */}
      <section className="py-24 bg-white">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6 }}
            >
              <span className="text-sm uppercase tracking-wider text-[rgb(var(--color-terracotta))] mb-2 block">
                Master Artisans
              </span>
              <h2 className="text-4xl md:text-5xl mb-4 text-[rgb(var(--color-indigo))]">
                Meet Our Artists
              </h2>
              <p className="text-lg text-[rgb(var(--muted-foreground))] max-w-2xl mx-auto">
                The brilliant minds and skilled hands behind every masterpiece
              </p>
            </motion.div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {artists.map((artist, index) => (
              <motion.div
                key={artist.id}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="group"
              >
                <div className="relative aspect-[3/4] rounded-xl overflow-hidden mb-4 shadow-lg">
                  <ImageWithFallback
                    src={artist.image}
                    alt={artist.name}
                    className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  <div className="absolute bottom-4 left-4 right-4 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <p className="text-sm">{artist.specialty}</p>
                    <p className="text-xs text-white/80">{artist.worksCount} artworks</p>
                  </div>
                </div>
                <h3 className="text-xl mb-1 text-[rgb(var(--color-indigo))]">{artist.name}</h3>
                <p className="text-sm text-[rgb(var(--muted-foreground))] line-clamp-2">
                  {artist.bio}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Trust Indicators */}
      <section className="py-20 bg-[rgb(var(--color-indigo))] text-white">
        <div className="container mx-auto px-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
            {[
              {
                icon: Award,
                title: 'Certified Authentic',
                description: 'Every artwork comes with a certificate of authenticity from the artist',
              },
              {
                icon: Sparkles,
                title: 'Handcrafted Excellence',
                description: 'Created using traditional techniques passed down through generations',
              },
              {
                icon: Users,
                title: 'Support Artisans',
                description: 'Direct support to master craftspeople and their communities',
              },
            ].map((item, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="text-center"
              >
                <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-white/10 flex items-center justify-center">
                  <item.icon className="w-8 h-8 text-[rgb(var(--color-gold))]" />
                </div>
                <h3 className="text-xl mb-3">{item.title}</h3>
                <p className="text-white/80">{item.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

