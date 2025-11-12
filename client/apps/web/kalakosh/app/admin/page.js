'use client';

/**
 * Admin Dashboard Page
 */
export default function AdminDashboardPage() {
  return (
    <div>
      <h1 className="text-3xl mb-6 text-[rgb(var(--color-indigo))]">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-xl shadow-md p-6">
          <h3 className="text-lg mb-2 text-[rgb(var(--muted-foreground))]">Total Orders</h3>
          <p className="text-3xl font-semibold text-[rgb(var(--color-indigo))]">0</p>
        </div>
        <div className="bg-white rounded-xl shadow-md p-6">
          <h3 className="text-lg mb-2 text-[rgb(var(--muted-foreground))]">Total Products</h3>
          <p className="text-3xl font-semibold text-[rgb(var(--color-indigo))]">0</p>
        </div>
        <div className="bg-white rounded-xl shadow-md p-6">
          <h3 className="text-lg mb-2 text-[rgb(var(--muted-foreground))]">Total Revenue</h3>
          <p className="text-3xl font-semibold text-[rgb(var(--color-indigo))]">₹0</p>
        </div>
      </div>
    </div>
  );
}

