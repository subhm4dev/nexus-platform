'use client';

/**
 * Simple toast notification system
 * Uses browser's native notification or console as fallback
 */
class Toast {
  constructor() {
    this.toasts = [];
    this.listeners = [];
  }

  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  notify(message, type = 'info') {
    const toast = {
      id: Date.now().toString(),
      message,
      type,
      timestamp: Date.now(),
    };
    
    this.toasts.push(toast);
    this.listeners.forEach(listener => listener(this.toasts));
    
    // Auto remove after 5 seconds
    setTimeout(() => {
      this.remove(toast.id);
    }, 5000);
    
    return toast.id;
  }

  success(message) {
    return this.notify(message, 'success');
  }

  error(message) {
    return this.notify(message, 'error');
  }

  warning(message) {
    return this.notify(message, 'warning');
  }

  info(message) {
    return this.notify(message, 'info');
  }

  remove(id) {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.listeners.forEach(listener => listener(this.toasts));
  }

  clear() {
    this.toasts = [];
    this.listeners.forEach(listener => listener(this.toasts));
  }
}

export const toast = new Toast();

