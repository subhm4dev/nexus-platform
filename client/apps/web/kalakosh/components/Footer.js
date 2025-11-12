'use client';

import { Facebook, Instagram, Twitter, Mail, Phone, MapPin } from 'lucide-react';

export function Footer() {
  const footerLinks = {
    Shop: ['All Artworks', 'Featured Collections', 'New Arrivals', 'Best Sellers'],
    About: ['Our Story', 'Artists', 'Pattachitra Heritage', 'Authentication'],
    Support: ['Contact Us', 'Shipping & Returns', 'FAQs', 'Care Instructions'],
    Legal: ['Privacy Policy', 'Terms of Service', 'Refund Policy', 'Shipping Policy'],
  };

  return (
    <footer className="bg-[rgb(var(--color-indigo))] text-white mt-24">
      {/* Decorative Border */}
      <div className="h-1 bg-gradient-to-r from-[rgb(var(--color-gold))] via-[rgb(var(--color-terracotta))] to-[rgb(var(--color-gold))]" />
      
      <div className="container mx-auto px-6 py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-12">
          {/* Brand */}
          <div className="lg:col-span-2">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-12 h-12 bg-gradient-to-br from-[rgb(var(--color-gold))] to-[rgb(var(--color-terracotta))] rounded-full flex items-center justify-center">
                <span className="text-white text-xl">प</span>
              </div>
              <h3 className="text-2xl">Kalakosh</h3>
            </div>
            <p className="text-white/80 mb-6 max-w-sm">
              Celebrating the ancient art form of Pattachitra. Each piece is handcrafted by master artisans, 
              preserving centuries of tradition and cultural heritage.
            </p>
            <div className="space-y-2 text-sm text-white/70">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4" />
                <span>Raghurajpur, Puri, Odisha, India</span>
              </div>
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4" />
                <span>+91 98765 43210</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4" />
                <span>hello@kalakosh.art</span>
              </div>
            </div>
          </div>

          {/* Links */}
          {Object.entries(footerLinks).map(([category, links]) => (
            <div key={category}>
              <h4 className="text-lg mb-4 text-[rgb(var(--color-gold))]">{category}</h4>
              <ul className="space-y-2">
                {links.map((link) => (
                  <li key={link}>
                    <a
                      href="#"
                      className="text-white/70 hover:text-white transition-colors text-sm"
                    >
                      {link}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Social & Copyright */}
        <div className="mt-12 pt-8 border-t border-white/20 flex flex-col md:flex-row items-center justify-between gap-4">
          <p className="text-white/60 text-sm">
            © 2025 Kalakosh Art Gallery. All rights reserved.
          </p>
          <div className="flex items-center gap-4">
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
            >
              <Facebook className="w-5 h-5" />
            </a>
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
            >
              <Instagram className="w-5 h-5" />
            </a>
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
            >
              <Twitter className="w-5 h-5" />
            </a>
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
            >
              <Mail className="w-5 h-5" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}

