import { Inter } from 'next/font/google';
import './globals.css';
import { ReactQueryProvider } from '@/lib/react-query';
import { AuthInitializer } from '@/components/AuthInitializer';
import { ToastContainer } from '@/components/ToastContainer';

const inter = Inter({ subsets: ['latin'] });

export const metadata = {
  title: 'TinySteps CDC - Healthcare Management System',
  description: 'Comprehensive healthcare management system for Tiny Steps CDC',
  icons: {
    icon: '/favicon.ico',
    shortcut: '/favicon.ico',
    apple: '/favicon.ico',
  },
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <ReactQueryProvider>
          <AuthInitializer />
          <ToastContainer />
          {children}
        </ReactQueryProvider>
      </body>
    </html>
  );
}

