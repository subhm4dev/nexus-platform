'use client';

import { motion } from 'motion/react';

export function PattachitraWatermark() {
  return (
    <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-[0.04]">
      <motion.svg
        className="w-full h-full"
        viewBox="0 0 800 800"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        {/* Traditional Pattachitra Motif Pattern */}
        <g opacity="0.6">
          {/* Center Mandala with Rotation Animation */}
          <motion.circle
            cx="400"
            cy="400"
            r="150"
            stroke="rgb(27, 43, 73)"
            strokeWidth="2"
            fill="none"
            animate={{ rotate: 360 }}
            transition={{ duration: 120, repeat: Infinity, ease: "linear" }}
            style={{ transformOrigin: "400px 400px" }}
          />
          <motion.circle
            cx="400"
            cy="400"
            r="120"
            stroke="rgb(169, 66, 49)"
            strokeWidth="1.5"
            fill="none"
            animate={{ rotate: -360 }}
            transition={{ duration: 100, repeat: Infinity, ease: "linear" }}
            style={{ transformOrigin: "400px 400px" }}
          />
          <motion.circle
            cx="400"
            cy="400"
            r="90"
            stroke="rgb(212, 175, 55)"
            strokeWidth="1"
            fill="none"
            animate={{ rotate: 360 }}
            transition={{ duration: 80, repeat: Infinity, ease: "linear" }}
            style={{ transformOrigin: "400px 400px" }}
          />
          
          {/* Petals with Pulse Animation */}
          {[...Array(8)].map((_, i) => {
            const angle = (i * 45 * Math.PI) / 180;
            const x = 400 + Math.cos(angle) * 140;
            const y = 400 + Math.sin(angle) * 140;
            return (
              <motion.g
                key={i}
                animate={{
                  scale: [1, 1.1, 1],
                  opacity: [0.6, 0.8, 0.6],
                }}
                transition={{
                  duration: 3,
                  repeat: Infinity,
                  delay: i * 0.2,
                  ease: "easeInOut"
                }}
                style={{ transformOrigin: `${x}px ${y}px` }}
              >
                <ellipse
                  cx={x}
                  cy={y}
                  rx="30"
                  ry="50"
                  transform={`rotate(${i * 45}, ${x}, ${y})`}
                  stroke="rgb(169, 66, 49)"
                  strokeWidth="1.5"
                  fill="none"
                />
              </motion.g>
            );
          })}
          
          {/* Decorative Border Elements */}
          <motion.rect
            x="50"
            y="50"
            width="700"
            height="700"
            stroke="rgb(212, 175, 55)"
            strokeWidth="3"
            fill="none"
            animate={{
              strokeDasharray: ["0, 2800", "2800, 0"],
            }}
            transition={{
              duration: 8,
              repeat: Infinity,
              ease: "linear"
            }}
          />
          <rect x="70" y="70" width="660" height="660" stroke="rgb(27, 43, 73)" strokeWidth="1" fill="none" />
          
          {/* Corner Decorations with Pulse */}
          {[[100, 100], [700, 100], [700, 700], [100, 700]].map(([x, y], i) => (
            <motion.g
              key={`corner-${i}`}
              animate={{
                scale: [1, 1.2, 1],
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                delay: i * 0.3,
                ease: "easeInOut"
              }}
              style={{ transformOrigin: `${x}px ${y}px` }}
            >
              <circle cx={x} cy={y} r="20" stroke="rgb(184, 134, 71)" strokeWidth="1.5" fill="none" />
              <circle cx={x} cy={y} r="10" stroke="rgb(169, 66, 49)" strokeWidth="1" fill="none" />
            </motion.g>
          ))}

          {/* Additional Odia Motifs - Conch Shells */}
          {[...Array(4)].map((_, i) => {
            const positions = [[200, 200], [600, 200], [600, 600], [200, 600]];
            const [x, y] = positions[i];
            return (
              <motion.g
                key={`conch-${i}`}
                animate={{
                  opacity: [0.3, 0.6, 0.3],
                }}
                transition={{
                  duration: 4,
                  repeat: Infinity,
                  delay: i * 0.5,
                  ease: "easeInOut"
                }}
              >
                <path
                  d={`M ${x} ${y} Q ${x + 20} ${y - 15}, ${x + 30} ${y} Q ${x + 20} ${y + 15}, ${x} ${y} Z`}
                  stroke="rgb(169, 66, 49)"
                  strokeWidth="1"
                  fill="none"
                />
              </motion.g>
            );
          })}
        </g>
      </motion.svg>
    </div>
  );
}

