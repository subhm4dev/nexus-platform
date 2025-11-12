'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { motion } from 'motion/react';
import { LayoutDashboard, Package, BookOpen, Users, LogOut } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';

/**
 * Admin Layout
 * 
 * Admin-specific layout with sidebar navigation.
 * Protected route (middleware checks ADMIN role).
 */
export default function AdminLayout({ children }) {
  const router = useRouter();
  const pathname = usePathname();
  const { isAuthenticated, user, logout, isAdmin } = useAuthStore();

  // Redirect if not authenticated or not admin
  useEffect(() => {
    if (!isAuthenticated || !isAdmin()) {
      router.push('/');
    }
  }, [isAuthenticated, user, router]);

  // Don't render if not authenticated or not admin
  if (!isAuthenticated || !isAdmin()) {
    return null;
  }

  const navItems = [
    { path: '/admin', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/admin/orders', label: 'Orders', icon: Package },
    { path: '/admin/catalog', label: 'Catalog', icon: BookOpen },
    { path: '/admin/sellers', label: 'Sellers', icon: Users },
  ];

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))]">
      {/* Sidebar */}
      <aside className="fixed left-0 top-0 h-full w-64 bg-white border-r border-[rgb(var(--border))] z-40 shadow-sm">
        <div className="p-6 border-b border-[rgb(var(--border))]">
          <div className="flex items-center gap-2 mb-1">
            <div className="w-8 h-8 bg-gradient-to-br from-[rgb(var(--color-indigo))] to-[rgb(var(--color-terracotta))] rounded-lg flex items-center justify-center">
              <span className="text-white text-xs">प</span>
            </div>
            <h1 className="text-xl font-semibold text-[rgb(var(--color-indigo))]">Admin Panel</h1>
          </div>
          <p className="text-xs text-[rgb(var(--muted-foreground))]">Kalakosh</p>
        </div>

        <nav className="p-4 space-y-1">
          {navItems.map((item) => {
            const isActive = pathname === item.path || (item.path !== '/admin' && pathname?.startsWith(item.path));
            const Icon = item.icon;
            return (
              <motion.button
                key={item.path}
                whileHover={{ x: 4 }}
                onClick={() => router.push(item.path)}
                className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors ${
                  isActive
                    ? 'bg-[rgb(var(--color-indigo))] text-white shadow-md'
                    : 'text-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--muted))]'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span className="font-medium">{item.label}</span>
              </motion.button>
            );
          })}
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-[rgb(var(--border))]">
          <div className="mb-3 px-4 py-2">
            <p className="text-xs text-[rgb(var(--muted-foreground))] mb-1">Logged in as</p>
            <p className="text-sm font-medium text-[rgb(var(--color-indigo))] truncate">
              {user?.email || 'Admin'}
            </p>
          </div>
          <button
            onClick={() => {
              logout();
              router.push('/');
            }}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left text-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--muted))] transition-colors"
          >
            <LogOut className="w-5 h-5" />
            <span className="font-medium">Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="ml-64">
        {/* Top Bar */}
        <header className="sticky top-0 z-30 bg-white border-b border-[rgb(var(--border))] px-6 py-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-[rgb(var(--color-indigo))]">
              {navItems.find((item) => pathname === item.path || (item.path !== '/admin' && pathname?.startsWith(item.path)))?.label || 'Admin'}
            </h2>
            <div className="flex items-center gap-3">
              <div className="text-sm text-[rgb(var(--muted-foreground))]">
                {user?.fullName || user?.email || 'Admin'}
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="p-6">
          {children}
        </main>
      </div>
    </div>
  );
}

