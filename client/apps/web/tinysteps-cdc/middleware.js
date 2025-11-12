import { NextResponse } from 'next/server';

/**
 * Decode JWT token payload (without validation)
 */
function decodeJwtPayload(token) {
  try {
    if (!token || typeof token !== 'string') {
      return null;
    }
    
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }
    
    const payload = parts[1];
    if (!payload) {
      return null;
    }
    
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const decoded = Buffer.from(padded, 'base64').toString('utf-8');
    
    if (!decoded) {
      return null;
    }
    
    return JSON.parse(decoded);
  } catch (error) {
    // Silently fail - invalid token
    return null;
  }
}

/**
 * Next.js Middleware for Healthcare Domain
 * 
 * Reads authentication cookies and:
 * 1. Protects admin routes (requires ADMIN or RECEPTIONIST role)
 * 2. Redirects unauthenticated users to login
 */
export function middleware(request) {
  try {
    const { pathname } = request.nextUrl;
    
    // Allow public routes
    if (pathname.startsWith('/login') || pathname.startsWith('/api')) {
      return NextResponse.next();
    }
    
    // Read accessToken cookie (set by backend)
    const accessToken = request.cookies.get('accessToken')?.value;
    
    // Check if accessing admin routes
    if (pathname.startsWith('/admin')) {
      if (!accessToken) {
        // Not authenticated - redirect to login
        const url = request.nextUrl.clone();
        url.pathname = '/login';
        return NextResponse.redirect(url);
      }
      
      // Decode JWT to check roles
      const payload = decodeJwtPayload(accessToken);
      if (payload) {
        const roles = payload.roles || (payload.role ? [payload.role] : []);
        const roleArray = Array.isArray(roles) ? roles : [roles];
        
        // Check if user has ADMIN or RECEPTIONIST role
        const hasAccess = roleArray.includes('ADMIN') || roleArray.includes('RECEPTIONIST');
        
        if (!hasAccess) {
          // Not authorized - redirect to login
          const url = request.nextUrl.clone();
          url.pathname = '/login';
          return NextResponse.redirect(url);
        }
      } else {
        // Couldn't decode token - redirect to login
        const url = request.nextUrl.clone();
        url.pathname = '/login';
        return NextResponse.redirect(url);
      }
    }
    
    return NextResponse.next();
  } catch (error) {
    // If middleware fails, allow the request to proceed (client-side will handle auth)
    console.error('Middleware error:', error);
    return NextResponse.next();
  }
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
};

