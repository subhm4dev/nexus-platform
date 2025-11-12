'use client';

import { useState, useMemo, useEffect, Suspense } from 'react';
import { motion } from 'motion/react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Filter, X } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { useProducts, useCategories } from '@/hooks/useProducts';
import { useAddToCart } from '@/hooks/useCart';
import { ProductCard } from '@/components/ProductCard';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';

/**
 * Shop Page Content Component
 * 
 * Product catalog with filtering and search.
 * Must be wrapped in Suspense due to useSearchParams usage.
 */
function ShopPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { isAuthenticated, setPendingAction } = useAuthStore();
  const { openLogin } = useAuthModal();
  
  const [filters, setFilters] = useState({
    categories: [],
    priceRange: [0, 100000],
  });
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [productsPerPage] = useState(12);
  const [showFilters, setShowFilters] = useState(true);

  // Initialize search from URL
  useEffect(() => {
    const searchParam = searchParams.get('search');
    if (searchParam) {
      setSearchQuery(decodeURIComponent(searchParam));
    }
  }, [searchParams]);

  // Build API params
  const apiParams = useMemo(() => {
    const params = {
      page: currentPage,
      size: productsPerPage,
    };
    if (searchQuery) {
      params.query = searchQuery;
    }
    if (filters.categories.length > 0) {
      params.categoryId = filters.categories[0];
    }
    if (filters.priceRange[1] < 100000) {
      params.minPrice = filters.priceRange[0];
      params.maxPrice = filters.priceRange[1];
    }
    return params;
  }, [currentPage, productsPerPage, searchQuery, filters]);

  const { data: productsData, isLoading } = useProducts(apiParams);
  const { data: categoriesData, isLoading: isLoadingCategories, error: categoriesError } = useCategories();
  const addToCartMutation = useAddToCart();

  const products = productsData?.content || [];
  const totalPages = productsData?.totalPages || 0;

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
      // Error handling
    }
  };

  const handleQuickView = (productId) => {
    router.push(`/products/${productId}`);
  };

  const toggleCategory = (categoryId) => {
    setFilters(prev => ({
      ...prev,
      categories: prev.categories.includes(categoryId)
        ? prev.categories.filter(id => id !== categoryId)
        : [...prev.categories, categoryId],
    }));
  };

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))]">
      {/* Header */}
      <section className="bg-gradient-to-r from-[rgb(var(--color-indigo))] to-[rgb(var(--color-indigo-light))] text-white py-16">
        <div className="container mx-auto px-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <h1 className="text-4xl md:text-5xl mb-4">Art Collection</h1>
            <p className="text-xl text-white/90">
              Discover {productsData?.totalElements || 0} authentic Pattachitra masterpieces
            </p>
          </motion.div>
        </div>
      </section>

      <div className="container mx-auto px-6 py-12">
        <div className="flex gap-8">
          {/* Mobile Filter Overlay */}
          {showFilters && (
            <div
              className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden transition-opacity"
              onClick={() => setShowFilters(false)}
            />
          )}

          {/* Filters Sidebar */}
          <aside
            className={`fixed lg:sticky top-0 left-0 h-full lg:h-auto w-64 bg-white z-50 lg:z-auto transform transition-transform duration-300 ease-in-out ${
              showFilters
                ? 'translate-x-0'
                : '-translate-x-full lg:translate-x-0'
            } overflow-y-auto lg:overflow-visible`}
          >
            <div className="bg-white rounded-xl shadow-lg p-6 sticky top-24 lg:static">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl text-[rgb(var(--color-indigo))]">Filters</h3>
                <button
                  onClick={() => setShowFilters(false)}
                  className="lg:hidden p-1 hover:bg-gray-100 rounded"
                  aria-label="Close filters"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              
              {/* Categories */}
              {isLoadingCategories ? (
                <div className="mb-6">
                  <Label className="mb-3 block">Categories</Label>
                  <p className="text-sm text-[rgb(var(--muted-foreground))]">Loading categories...</p>
                </div>
              ) : categoriesError ? (
                <div className="mb-6">
                  <Label className="mb-3 block">Categories</Label>
                  <p className="text-sm text-red-600">Error loading categories</p>
                </div>
              ) : categoriesData && Array.isArray(categoriesData) && categoriesData.length > 0 ? (
                <div className="mb-6">
                  <Label className="mb-3 block">Categories</Label>
                  <div className="space-y-2">
                    {categoriesData.map((category) => (
                      <div key={category.id} className="flex items-center space-x-2">
                        <Checkbox
                          id={`category-${category.id}`}
                          checked={filters.categories.includes(category.id)}
                          onCheckedChange={() => toggleCategory(category.id)}
                        />
                        <Label htmlFor={`category-${category.id}`} className="cursor-pointer">
                          {category.name}
                        </Label>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="mb-6">
                  <Label className="mb-3 block">Categories</Label>
                  <p className="text-sm text-[rgb(var(--muted-foreground))]">No categories available</p>
                </div>
              )}
            </div>
          </aside>

          {/* Products Grid */}
          <div className="flex-1">
            {/* Filter Toggle Button (Mobile) */}
            <div className="mb-6 flex items-center justify-between">
              <h2 className="text-2xl font-semibold text-[rgb(var(--color-indigo))]">Products</h2>
              <Button
                variant="outline"
                onClick={() => setShowFilters(!showFilters)}
                className="lg:hidden"
              >
                <Filter className="w-4 h-4 mr-2" />
                Filters
              </Button>
            </div>
            {isLoading ? (
              <div className="text-center py-12">Loading...</div>
            ) : products.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-lg text-[rgb(var(--muted-foreground))]">No products found</p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {products.map((product) => (
                    <ProductCard
                      key={product.id}
                      id={product.id}
                      title={product.name}
                      artist={product.sellerName || 'Master Artisan'}
                      price={product.price}
                      image={product.images?.[0] || ''}
                      size={product.attributes?.size || 'Standard'}
                      onAddToCart={handleAddToCart}
                      onQuickView={handleQuickView}
                    />
                  ))}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="flex justify-center gap-2 mt-12">
                    <Button
                      variant="outline"
                      onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                      disabled={currentPage === 0}
                    >
                      Previous
                    </Button>
                    <span className="flex items-center px-4">
                      Page {currentPage + 1} of {totalPages}
                    </span>
                    <Button
                      variant="outline"
                      onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                      disabled={currentPage >= totalPages - 1}
                    >
                      Next
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * Shop Page
 * 
 * Wraps ShopPageContent in Suspense for useSearchParams compatibility.
 */
export default function ShopPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-[rgb(var(--color-ivory))] flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-[rgb(var(--color-terracotta))]"></div>
          <p className="mt-4 text-[rgb(var(--muted-foreground))]">Loading shop...</p>
        </div>
      </div>
    }>
      <ShopPageContent />
    </Suspense>
  );
}

