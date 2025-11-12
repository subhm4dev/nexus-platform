'use client';

import { useState } from 'react';
import { motion } from 'motion/react';
import { ShoppingCart, Eye, Heart } from 'lucide-react';
import { Button } from './ui/button';
import { ImageWithFallback } from './figma/ImageWithFallback';

export function ProductCard({
  id,
  title,
  artist,
  price,
  image,
  size,
  onAddToCart,
  onQuickView,
}) {
  const [isHovered, setIsHovered] = useState(false);
  const [isFavorite, setIsFavorite] = useState(false);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      onHoverStart={() => setIsHovered(true)}
      onHoverEnd={() => setIsHovered(false)}
      className="group relative bg-white rounded-xl overflow-hidden shadow-md hover:shadow-2xl transition-shadow duration-300"
    >
      {/* Image Container */}
      <div className="relative aspect-[3/4] overflow-hidden bg-[rgb(var(--muted))]">
        <ImageWithFallback
          src={image}
          alt={title}
          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
        />

        {/* Overlay */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: isHovered ? 1 : 0 }}
          transition={{ duration: 0.25 }}
          className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent flex flex-col items-center justify-center gap-3 p-4"
        >
          <Button
            onClick={() => onQuickView(id)}
            variant="secondary"
            className="bg-white/95 hover:bg-white text-[rgb(var(--color-indigo))] gap-2 shadow-lg"
          >
            <Eye className="w-4 h-4" />
            Quick View
          </Button>
          <Button
            onClick={() => onAddToCart(id)}
            className="bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white gap-2 shadow-lg"
          >
            <ShoppingCart className="w-4 h-4" />
            Add to Cart
          </Button>
        </motion.div>

        {/* Favorite Button */}
        <button
          onClick={() => setIsFavorite(!isFavorite)}
          className="absolute top-3 right-3 w-10 h-10 rounded-full bg-white/90 backdrop-blur-sm flex items-center justify-center shadow-lg hover:scale-110 transition-transform z-10"
        >
          <Heart
            className={`w-5 h-5 transition-colors ${
              isFavorite
                ? 'fill-[rgb(var(--color-terracotta))] text-[rgb(var(--color-terracotta))]'
                : 'text-[rgb(var(--color-indigo))]'
            }`}
          />
        </button>

        {/* Artist Tag */}
        <div className="absolute bottom-3 left-3 bg-white/90 backdrop-blur-sm px-3 py-1.5 rounded-full shadow-md">
          <p className="text-xs text-[rgb(var(--color-indigo))]">by {artist}</p>
        </div>
      </div>

      {/* Product Info */}
      <div className="p-4">
        <h3 className="text-lg mb-1 text-[rgb(var(--color-indigo))] line-clamp-1">
          {title}
        </h3>
        <p className="text-sm text-[rgb(var(--muted-foreground))] mb-3">{size}</p>
        <div className="flex items-center justify-between">
          <p className="text-2xl text-[rgb(var(--color-terracotta))]">
            ₹{price.toLocaleString('en-IN')}
          </p>
          <div className="flex items-center gap-1">
            <div className="w-2 h-2 rounded-full bg-[rgb(var(--color-gold))]" />
            <div className="w-2 h-2 rounded-full bg-[rgb(var(--color-terracotta))]" />
            <div className="w-2 h-2 rounded-full bg-[rgb(var(--color-indigo))]" />
          </div>
        </div>
      </div>

      {/* Decorative Border on Hover */}
      <motion.div
        initial={{ scaleX: 0 }}
        animate={{ scaleX: isHovered ? 1 : 0 }}
        transition={{ duration: 0.3 }}
        className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-[rgb(var(--color-gold))] via-[rgb(var(--color-terracotta))] to-[rgb(var(--color-gold))] origin-left"
      />
    </motion.div>
  );
}

