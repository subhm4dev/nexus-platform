'use client';

import { useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { motion, AnimatePresence } from 'motion/react';
import { Search, ShoppingCart, User, LogOut, Menu, Package } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { useCart } from '@/hooks/useCart';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { MobileMenu } from './MobileMenu';

/**
 * Header Component for Kalakosh
 * 
 * Navigation header with cart, user menu, and search.
 */
export function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { isAuthenticated, user, logout } = useAuthStore();
  const { openLogin } = useAuthModal();
  const { data: cart } = useCart({ enabled: isAuthenticated });
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const navItems = [
    { label: 'Home', path: '/' },
    { label: 'Shop', path: '/shop' },
    { label: 'About & Artists', path: '/about' },
    { label: 'Blog & Stories', path: '/blog' },
  ];

  // Calculate cart item count
  const cartItemCount = cart?.items?.reduce((total, item) => total + (item.quantity || 0), 0) || 0;

  const handleNavigate = (path) => {
    router.push(path);
  };

  const handleLogout = async () => {
    await logout();
    router.push('/');
  };

  const isActive = (path) => {
    if (path === '/') {
      return pathname === '/';
    }
    return pathname.startsWith(path);
  };

  return (
    <>
      <motion.header
        initial={{ y: -100 }}
        animate={{ y: 0 }}
        className="sticky top-0 z-50 bg-white/95 backdrop-blur-md border-b border-[rgb(var(--border))] shadow-sm"
      >
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between gap-8">
            {/* Logo */}
            <button
              onClick={() => handleNavigate('/')}
              className="flex items-center gap-3 group"
            >
              <div className="w-12 h-12 bg-gradient-to-br from-[rgb(var(--color-indigo))] to-[rgb(var(--color-terracotta))] rounded-full flex items-center justify-center">
                <span className="text-white text-xl">प</span>
              </div>
              <div>
                <h1 className="text-xl text-[rgb(var(--color-indigo))] group-hover:text-[rgb(var(--color-terracotta))] transition-colors">
                  Kalakosh
                </h1>
                <p className="text-xs text-[rgb(var(--muted-foreground))]">Traditional Art Gallery</p>
              </div>
            </button>

            {/* Navigation */}
            <nav className="hidden md:flex items-center gap-1">
              {navItems.map((item) => (
                <button
                  key={item.path}
                  onClick={() => handleNavigate(item.path)}
                  className={`px-4 py-2 rounded-lg transition-all relative ${
                    isActive(item.path)
                      ? 'text-[rgb(var(--color-indigo))]'
                      : 'text-[rgb(var(--muted-foreground))] hover:text-[rgb(var(--color-indigo))]'
                  }`}
                >
                  {item.label}
                  {isActive(item.path) && (
                    <motion.div
                      layoutId="activeNav"
                      className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-[rgb(var(--color-gold))] to-[rgb(var(--color-terracotta))]"
                      transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                    />
                  )}
                </button>
              ))}
            </nav>

            {/* Search Bar */}
            <div className="hidden lg:flex flex-1 max-w-md relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[rgb(var(--muted-foreground))]" />
              <Input
                type="search"
                placeholder="Search artworks, artists..."
                className="pl-10 bg-[rgb(var(--muted))] border-none"
              />
            </div>

            {/* Actions */}
            <div className="flex items-center gap-3">
              {/* Cart */}
              <button
                onClick={() => handleNavigate('/cart')}
                className="relative p-2 hover:bg-[rgb(var(--muted))] rounded-lg transition-colors"
              >
                <ShoppingCart className="w-5 h-5 text-[rgb(var(--color-indigo))]" />
                <AnimatePresence>
                  {cartItemCount > 0 && (
                    <motion.span
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      exit={{ scale: 0 }}
                      className="absolute -top-1 -right-1 w-5 h-5 bg-[rgb(var(--color-terracotta))] text-white text-xs rounded-full flex items-center justify-center"
                    >
                      {cartItemCount}
                    </motion.span>
                  )}
                </AnimatePresence>
              </button>

              {/* User */}
              {isAuthenticated ? (
                <div className="flex items-center gap-2">
                  <div className="hidden sm:flex items-center gap-2 px-3 py-2 bg-[rgb(var(--muted))] rounded-lg">
                    <User className="w-4 h-4 text-[rgb(var(--color-indigo))]" />
                    <span className="text-sm">{user?.fullName || 'User'}</span>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleNavigate('/orders')}
                    className="gap-2"
                    title="My Orders"
                  >
                    <Package className="w-4 h-4" />
                    <span className="hidden sm:inline">Orders</span>
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleLogout}
                    className="gap-2"
                  >
                    <LogOut className="w-4 h-4" />
                    <span className="hidden sm:inline">Logout</span>
                  </Button>
                </div>
              ) : (
                <Button
                  onClick={openLogin}
                  className="bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white gap-2"
                >
                  <User className="w-4 h-4" />
                  <span className="hidden sm:inline">Login</span>
                </Button>
              )}
            </div>

            {/* Mobile Menu Toggle */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="md:hidden p-2 hover:bg-[rgb(var(--muted))] rounded-lg transition-colors"
            >
              <Menu className="w-5 h-5" />
            </button>
          </div>

          {/* Mobile Search */}
          <div className="lg:hidden mt-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[rgb(var(--muted-foreground))]" />
              <Input
                type="search"
                placeholder="Search artworks..."
                className="pl-10 bg-[rgb(var(--muted))] border-none"
              />
            </div>
          </div>
        </div>
      </motion.header>

      {/* Mobile Menu Drawer */}
      <MobileMenu
        isOpen={isMobileMenuOpen}
        onClose={() => setIsMobileMenuOpen(false)}
        currentPath={pathname}
        onNavigate={handleNavigate}
        isLoggedIn={isAuthenticated}
        userName={user?.fullName}
        onLoginClick={openLogin}
        onLogout={handleLogout}
      />
    </>
  );
}

