'use client';

import { motion, AnimatePresence } from "motion/react";
import {
  X,
  Home,
  ShoppingBag,
  Users,
  BookOpen,
  User,
  LogOut,
  Package,
} from "lucide-react";
import { useEffect } from "react";

export function MobileMenu({
  isOpen,
  onClose,
  currentPath,
  onNavigate,
  isLoggedIn,
  userName,
  onLoginClick,
  onLogout,
}) {
  const navItems = [
    {
      label: "Home",
      path: "/",
      icon: Home,
      description: "Discover beautiful art",
    },
    {
      label: "Shop",
      path: "/shop",
      icon: ShoppingBag,
      description: "Browse our collection",
    },
    {
      label: "About & Artists",
      path: "/about",
      icon: Users,
      description: "Our story & artisans",
    },
    {
      label: "Blog & Stories",
      path: "/blog",
      icon: BookOpen,
      description: "Art insights & tales",
    },
  ];

  // Prevent body scroll when menu is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }

    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isOpen]);

  const handleNavigate = (path) => {
    onNavigate(path);
    onClose();
  };

  const isActive = (path) => {
    if (path === '/') {
      return currentPath === '/';
    }
    return currentPath.startsWith(path);
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm z-[100]"
            onClick={onClose}
          />

          {/* Drawer */}
          <motion.div
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{
              type: "spring",
              damping: 30,
              stiffness: 300,
            }}
            className="fixed top-0 right-0 bottom-0 w-[85%] max-w-sm bg-white z-[100] shadow-2xl overflow-y-auto"
          >
            {/* Header with Traditional Border */}
            <div className="relative bg-gradient-to-br from-[rgb(var(--color-indigo))] to-[rgb(var(--color-indigo-light))] p-6 text-white">
              {/* Traditional Pattern Background */}
              <div className="absolute inset-0 opacity-10">
                <svg
                  className="w-full h-full"
                  viewBox="0 0 100 100"
                  fill="none"
                >
                  {[...Array(5)].map((_, i) => (
                    <circle
                      key={i}
                      cx="50"
                      cy="50"
                      r={10 + i * 8}
                      stroke="white"
                      strokeWidth="0.5"
                      fill="none"
                    />
                  ))}
                </svg>
              </div>

              <div className="relative flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-white/20 backdrop-blur-sm rounded-full flex items-center justify-center border-2 border-white/30">
                    <span className="text-2xl">प</span>
                  </div>
                  <div>
                    <h2 className="text-xl">Kalakosh</h2>
                    <p className="text-xs opacity-90">
                      Traditional Art Gallery
                    </p>
                  </div>
                </div>
                <button
                  onClick={onClose}
                  className="p-2 hover:bg-white/20 rounded-lg transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* User Info */}
              {isLoggedIn ? (
                <div className="relative mt-4 p-4 bg-white/10 backdrop-blur-sm rounded-lg border border-white/20">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-[rgb(var(--color-gold))] to-[rgb(var(--color-terracotta))] rounded-full flex items-center justify-center">
                      <User className="w-5 h-5 text-white" />
                    </div>
                    <div>
                      <p className="text-sm">
                        {userName || "User"}
                      </p>
                      <p className="text-xs opacity-75">
                        Art Enthusiast
                      </p>
                    </div>
                  </div>
                </div>
              ) : (
                <button
                  onClick={() => {
                    onLoginClick();
                    onClose();
                  }}
                  className="relative mt-4 w-full p-4 bg-white/10 backdrop-blur-sm rounded-lg border border-white/20 hover:bg-white/20 transition-colors flex items-center justify-center gap-2"
                >
                  <User className="w-5 h-5" />
                  <span>Sign In / Register</span>
                </button>
              )}

              {/* Decorative Border */}
              <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-[rgb(var(--color-gold))] via-[rgb(var(--color-terracotta))] to-[rgb(var(--color-gold))]" />
            </div>

            {/* Navigation */}
            <nav className="p-6 pb-48">
              <div className="space-y-2">
                {navItems.map((item, index) => {
                  const Icon = item.icon;
                  const active = isActive(item.path);

                  return (
                    <motion.button
                      key={item.path}
                      initial={{ opacity: 0, x: 20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: index * 0.1 }}
                      onClick={() => handleNavigate(item.path)}
                      className={`w-full group relative overflow-hidden rounded-xl transition-all ${
                        active
                          ? "bg-gradient-to-r from-[rgb(var(--color-indigo)/0.1)] to-[rgb(var(--color-terracotta)/0.1)]"
                          : "hover:bg-[rgb(var(--muted))]"
                      }`}
                    >
                      {/* Active Indicator */}
                      {active && (
                        <motion.div
                          layoutId="mobileActiveNav"
                          className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-[rgb(var(--color-gold))] to-[rgb(var(--color-terracotta))]"
                          transition={{
                            type: "spring",
                            stiffness: 380,
                            damping: 30,
                          }}
                        />
                      )}

                      <div className="flex items-center gap-4 p-4">
                        <div
                          className={`w-12 h-12 rounded-lg flex items-center justify-center transition-all ${
                            active
                              ? "bg-gradient-to-br from-[rgb(var(--color-indigo))] to-[rgb(var(--color-terracotta))] text-white"
                              : "bg-[rgb(var(--muted))] text-[rgb(var(--color-indigo))] group-hover:scale-110"
                          }`}
                        >
                          <Icon className="w-5 h-5" />
                        </div>
                        <div className="flex-1 text-left">
                          <p
                            className={`transition-colors ${
                              active
                                ? "text-[rgb(var(--color-indigo))]"
                                : "text-[rgb(var(--foreground))] group-hover:text-[rgb(var(--color-indigo))]"
                            }`}
                          >
                            {item.label}
                          </p>
                          <p className="text-xs text-[rgb(var(--muted-foreground))]">
                            {item.description}
                          </p>
                        </div>
                      </div>
                    </motion.button>
                  );
                })}
              </div>
            </nav>

            {/* Bottom Actions */}
            <div className="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-white via-white to-white/95 z-10 border-t border-[rgb(var(--border))]">
              {/* Decorative Border */}
              <div className="mb-4 h-px bg-gradient-to-r from-transparent via-[rgb(var(--color-gold))] to-transparent" />

              {isLoggedIn && (
                <>
                  <button
                    onClick={() => {
                      handleNavigate('/orders');
                    }}
                    className="w-full flex items-center justify-center gap-2 p-3 bg-white border border-[rgb(var(--border))] rounded-lg hover:bg-[rgb(var(--muted))] transition-colors text-[rgb(var(--color-indigo))] mb-2"
                  >
                    <Package className="w-4 h-4" />
                    <span>My Orders</span>
                  </button>
                  <button
                    onClick={() => {
                      onLogout();
                      onClose();
                    }}
                    className="w-full flex items-center justify-center gap-2 p-3 bg-white border border-[rgb(var(--border))] rounded-lg hover:bg-[rgb(var(--muted))] transition-colors text-[rgb(var(--color-indigo))]"
                  >
                    <LogOut className="w-4 h-4" />
                    <span>Logout</span>
                  </button>
                </>
              )}

              {/* Traditional Pattern */}
              <div className="mt-4 flex justify-center gap-2 opacity-30">
                {[...Array(5)].map((_, i) => (
                  <motion.div
                    key={i}
                    className="w-2 h-2 bg-gradient-to-br from-[rgb(var(--color-indigo))] to-[rgb(var(--color-terracotta))] rounded-full"
                    animate={{
                      scale: [1, 1.3, 1],
                      opacity: [0.5, 1, 0.5],
                    }}
                    transition={{
                      duration: 2,
                      repeat: Infinity,
                      delay: i * 0.2,
                    }}
                  />
                ))}
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

