'use client';

import { useState } from 'react';
import { motion } from 'motion/react';
import { ArrowLeft } from 'lucide-react';

export default function FlippableCard({ frontContent, backContent, className = '' }) {
  const [isFlipped, setIsFlipped] = useState(false);

  const handleCardClick = () => {
    setIsFlipped(!isFlipped);
  };

  const handleBackClick = (e) => {
    e.stopPropagation();
    setIsFlipped(false);
  };

  return (
    <div className={`relative w-full h-80 [perspective:1000px] ${className}`}>
      <motion.div
        className="relative w-full h-full [transform-style:preserve-3d] cursor-pointer"
        animate={{
          rotateY: isFlipped ? 180 : 0,
        }}
        transition={{
          duration: 0.7,
          ease: 'easeInOut',
        }}
        onClick={handleCardClick}
      >
        {/* Front of the Card */}
        <div className="absolute w-full h-full [backface-visibility:hidden]">
          <motion.div
            className="w-full h-full bg-white/80 backdrop-blur-lg border border-white/30 rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 flex items-center justify-center p-6"
            style={{
              background:
                'linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.8) 100%)',
            }}
            whileHover={{ scale: 1.02, y: -5 }}
            transition={{ duration: 0.2 }}
          >
            {frontContent}
          </motion.div>
        </div>

        {/* Back of the Card */}
        <div
          className="absolute w-full h-full [backface-visibility:hidden] [transform:rotateY(180deg)]"
          onClick={(e) => e.stopPropagation()}
        >
          <motion.div
            className="w-full h-full bg-white/90 backdrop-blur-lg border border-white/40 rounded-2xl shadow-xl flex flex-col overflow-hidden"
            style={{
              background:
                'linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%)',
            }}
          >
            {/* Back Button */}
            <motion.button
              className="absolute top-4 left-4 z-10 p-2 rounded-full bg-white/80 hover:bg-white shadow-md transition-colors duration-200"
              onClick={handleBackClick}
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
            >
              <ArrowLeft size={16} className="text-gray-700" />
            </motion.button>

            {/* Back Content */}
            <div className="flex-1 pt-14 pb-6 px-6 overflow-y-auto">{backContent}</div>
          </motion.div>
        </div>
      </motion.div>
    </div>
  );
}

