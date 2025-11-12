import { Inter, Playfair_Display } from 'next/font/google';
import './globals.css';
import { ReactQueryProvider } from '@/lib/react-query';
import { AuthInitializer } from '@/components/AuthInitializer';
import { Header } from '@/components/Header';
import { Footer } from '@/components/Footer';
import { AuthModal } from '@/components/AuthModal';

const inter = Inter({ 
  subsets: ['latin'],
  variable: '--font-inter',
  display: 'swap',
});

const playfairDisplay = Playfair_Display({ 
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-playfair',
  display: 'swap',
});

export const metadata = {
  title: 'Kalakosh - A Treasure of Odisha\'s Pride Possession - Pattachitra',
  description: 'Discover the timeless elegance of traditional Pattachitra art from Odisha. Handcrafted paintings with authentic Odisha patterns and mythological themes.',
  icons: {
    icon: '/favicon.ico',
    shortcut: '/favicon.ico',
    apple: '/favicon.ico',
  },
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className={`${inter.variable} ${playfairDisplay.variable} ${inter.className}`}>
        <ReactQueryProvider>
          <AuthInitializer />
          <div className="min-h-screen flex flex-col">
            <Header />
            <main className="flex-1">
              {children}
            </main>
            <Footer />
            <AuthModal />
          </div>
        </ReactQueryProvider>
      </body>
    </html>
  );
}

