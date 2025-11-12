'use client';

import { useProducts } from '@/hooks/useProducts';
import { Button } from '@/components/ui/button';

/**
 * Admin Catalog Page
 */
export default function AdminCatalogPage() {
  const { data: productsData, isLoading } = useProducts({ page: 0, size: 20 });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const products = productsData?.content || [];

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl text-[rgb(var(--color-indigo))]">Catalog</h1>
        <Button className="bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white">
          Add Product
        </Button>
      </div>
      {products.length === 0 ? (
        <p className="text-[rgb(var(--muted-foreground))]">No products yet</p>
      ) : (
        <div className="space-y-4">
          {products.map((product) => (
            <div key={product.id} className="bg-white rounded-lg shadow p-6">
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-semibold text-lg">{product.name}</p>
                  <p className="text-sm text-[rgb(var(--muted-foreground))]">{product.sku}</p>
                </div>
                <div className="text-right">
                  <p className="text-lg font-semibold text-[rgb(var(--color-terracotta))]">
                    ₹{product.price.toLocaleString('en-IN')}
                  </p>
                  <p className="text-sm capitalize">{product.status}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

