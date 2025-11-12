'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useGSAP } from '@gsap/react';
import gsap from 'gsap';
import { motion, AnimatePresence } from 'motion/react';
import {
  LogOut,
  ChevronDown,
  UserCircle,
} from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { BranchSelector } from './BranchSelector';
import { navItems } from './navigation';

/**
 * Admin Layout for TinySteps CDC
 * 
 * Admin-specific layout with sidebar navigation for healthcare management.
 * Protected route - checks authentication and admin/receptionist roles.
 * Features nested navigation with expandable sub-items and GSAP animations.
 */
export default function AdminLayout({ children }) {
  const router = useRouter();
  const pathname = usePathname();
  const sidebarRef = useRef(null);
  const { isAuthenticated, user, logout, isAdmin, isReceptionist, hasCheckedAuth, isLoading } = useAuthStore();
  const [expandedItems, setExpandedItems] = useState(new Set());

  // Wait for auth check to complete before redirecting
  useEffect(() => {
    if (hasCheckedAuth && (!isAuthenticated || (!isAdmin() && !isReceptionist()))) {
      router.push('/login');
    }
  }, [hasCheckedAuth, isAuthenticated, user, router, isAdmin, isReceptionist]);

  // GSAP animations for sidebar
  useGSAP(() => {
    if (sidebarRef.current) {
      gsap.fromTo(
        '.nav-item',
        { opacity: 0, x: -20 },
        {
          opacity: 1,
          x: 0,
          duration: 0.5,
          stagger: 0.05,
          delay: 0.1,
        }
      );
    }
  }, []);

  // Show loading state while checking auth
  if (!hasCheckedAuth || isLoading) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-neutral-600">Checking authentication...</div>
        </div>
      </div>
    );
  }

  // Don't render if not authenticated or doesn't have required role
  if (!isAuthenticated || (!isAdmin() && !isReceptionist())) {
    return null;
  }

  const handleLogout = async () => {
    await logout();
    router.push('/login');
  };

  const handleItemClick = (item) => {
    if (item.subItems && item.subItems.length > 0) {
      // Toggle expansion
      setExpandedItems((prev) => {
        const newSet = new Set(prev);
        if (newSet.has(item.name)) {
          newSet.delete(item.name);
        } else {
          newSet.add(item.name);
        }
        return newSet;
      });
    } else {
      router.push(item.route);
    }
  };

  const handleSubItemClick = (subItem) => {
    router.push(subItem.route);
  };

  const isItemActive = (item) => {
    if (item.subItems && item.subItems.length > 0) {
      return item.subItems.some((subItem) => pathname === subItem.route || pathname?.startsWith(subItem.route + '/'));
    }
    return pathname === item.route || (item.route !== '/admin' && pathname?.startsWith(item.route));
  };

  const isSubItemActive = (subItem) => {
    return pathname === subItem.route || pathname?.startsWith(subItem.route + '/');
  };

  const getCurrentPageTitle = () => {
    for (const item of navItems) {
      if (item.subItems) {
        const activeSubItem = item.subItems.find((sub) => isSubItemActive(sub));
        if (activeSubItem) return activeSubItem.name;
      }
      if (isItemActive(item)) return item.name;
    }
    return 'Admin';
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Sidebar */}
      <motion.aside
        ref={sidebarRef}
        className="fixed left-0 top-0 h-full w-64 bg-white border-r border-neutral-200 z-40 shadow-sm"
        initial={{ x: -100, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.5, ease: [0.4, 0, 0.2, 1] }}
      >
        <div className="p-6 border-b border-neutral-200">
          <div className="flex items-center gap-2 mb-1">
            <div className="w-8 h-8 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-lg flex items-center justify-center">
              <span className="text-white text-xs font-bold">TS</span>
            </div>
            <h1 className="text-xl font-semibold text-neutral-900">TinySteps CDC</h1>
          </div>
          <p className="text-xs text-neutral-500">Healthcare Management</p>
        </div>

        <nav className="p-4 space-y-1 overflow-y-auto" style={{ maxHeight: 'calc(100vh - 200px)' }}>
          {navItems.map((item) => {
            const isActive = isItemActive(item);
            const isExpanded = expandedItems.has(item.name);
            const hasSubItems = item.subItems && item.subItems.length > 0;
            const Icon = item.icon;

            return (
              <div key={item.name} className="mb-1">
              <motion.button
                  className={`nav-item w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors ${
                  isActive
                    ? 'bg-blue-700 text-white shadow-md'
                    : 'text-neutral-700 hover:bg-neutral-100'
                }`}
                  onClick={() => handleItemClick(item)}
                  whileHover={{ x: 4 }}
                  whileTap={{ scale: 0.98 }}
              >
                  <Icon className="w-5 h-5 flex-shrink-0" />
                  <span className="font-medium flex-1">{item.name}</span>
                  {hasSubItems && (
                    <ChevronDown
                      className={`w-4 h-4 transition-transform duration-200 ${
                        isExpanded ? 'rotate-180' : ''
                      }`}
                    />
                  )}
                </motion.button>

                {/* Sub Items */}
                {hasSubItems && (
                  <AnimatePresence>
                    {isExpanded && (
                      <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ duration: 0.3 }}
                        className="ml-6 mt-1 space-y-1"
                      >
                        {item.subItems.map((subItem) => {
                          const isSubActive = isSubItemActive(subItem);
                          const SubIcon = subItem.icon;
                          return (
                            <motion.button
                              key={subItem.route}
                              className={`w-full flex items-center gap-3 px-4 py-2 rounded-lg text-left transition-colors text-sm ${
                                isSubActive
                                  ? 'bg-blue-100 text-blue-700 font-medium'
                                  : 'text-neutral-600 hover:bg-neutral-50 hover:text-neutral-900'
                              }`}
                              onClick={() => handleSubItemClick(subItem)}
                              whileHover={{ x: 2 }}
                              whileTap={{ scale: 0.98 }}
                            >
                              <SubIcon className="w-4 h-4 flex-shrink-0" />
                              <span>{subItem.name}</span>
              </motion.button>
                          );
                        })}
                      </motion.div>
                    )}
                  </AnimatePresence>
                )}
              </div>
            );
          })}
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-neutral-200 bg-white">
          <div className="mb-3 px-4 py-2">
            <p className="text-xs text-neutral-500 mb-1">Logged in as</p>
            <p className="text-sm font-medium text-neutral-900 truncate">
              {user?.email || 'User'}
            </p>
            {user?.roles && (
              <p className="text-xs text-neutral-500 mt-1">
                {Array.isArray(user.roles) ? user.roles.join(', ') : user.roles}
              </p>
            )}
          </div>
          <motion.button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left text-neutral-700 hover:bg-neutral-100 transition-colors"
            whileHover={{ x: 2 }}
            whileTap={{ scale: 0.98 }}
          >
            <LogOut className="w-5 h-5" />
            <span className="font-medium">Logout</span>
          </motion.button>
        </div>
      </motion.aside>

      {/* Main Content */}
      <div className="ml-64">
        {/* Top Bar */}
        <motion.header
          className="sticky top-0 z-30 bg-white border-b border-neutral-200 px-6 py-4 shadow-sm"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-neutral-900">
              {getCurrentPageTitle()}
            </h2>
            <div className="flex items-center gap-4">
              <BranchSelector />
              <div className="text-sm text-neutral-600">
                {user?.email || 'Admin'}
              </div>
            </div>
          </div>
        </motion.header>

        {/* Page Content */}
        <motion.main
          className="p-6"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3 }}
        >
          {children}
        </motion.main>
      </div>
    </div>
  );
}
