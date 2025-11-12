'use client';

import { motion } from 'motion/react';
import { TrendingUp, TrendingDown } from 'lucide-react';

export default function StatsCard({
  title,
  value,
  change,
  changeType = 'increase',
  icon: Icon,
  color = 'blue',
  description,
}) {
  const colorVariants = {
    blue: {
      bg: 'from-blue-500 to-blue-600',
      light: 'from-blue-50 to-blue-100',
      text: 'text-blue-600',
      border: 'border-blue-200',
    },
    green: {
      bg: 'from-green-500 to-green-600',
      light: 'from-green-50 to-green-100',
      text: 'text-green-600',
      border: 'border-green-200',
    },
    purple: {
      bg: 'from-purple-500 to-purple-600',
      light: 'from-purple-50 to-purple-100',
      text: 'text-purple-600',
      border: 'border-purple-200',
    },
    orange: {
      bg: 'from-orange-500 to-orange-600',
      light: 'from-orange-50 to-orange-100',
      text: 'text-orange-600',
      border: 'border-orange-200',
    },
    red: {
      bg: 'from-red-500 to-red-600',
      light: 'from-red-50 to-red-100',
      text: 'text-red-600',
      border: 'border-red-200',
    },
  };

  const variant = colorVariants[color];

  return (
    <motion.div
      className={`bg-gradient-to-br ${variant.light} border ${variant.border} rounded-2xl p-6 shadow-lg hover:shadow-xl transition-all duration-300`}
      whileHover={{ scale: 1.02, y: -4 }}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium text-gray-600 mb-1">{title}</p>
          <h3 className="text-3xl font-bold text-gray-900 mb-2">{value}</h3>

          {change && (
            <div className="flex items-center gap-1">
              {changeType === 'increase' ? (
                <TrendingUp size={16} className="text-green-500" />
              ) : (
                <TrendingDown size={16} className="text-red-500" />
              )}
              <span
                className={`text-sm font-medium ${
                  changeType === 'increase' ? 'text-green-600' : 'text-red-600'
                }`}
              >
                {change}
              </span>
              <span className="text-xs text-gray-500">vs last month</span>
            </div>
          )}

          {description && <p className="text-xs text-gray-500 mt-2">{description}</p>}
        </div>

        <motion.div
          className={`w-12 h-12 bg-gradient-to-br ${variant.bg} rounded-xl flex items-center justify-center shadow-lg`}
          whileHover={{ scale: 1.1, rotate: 5 }}
          transition={{ duration: 0.2 }}
        >
          <Icon size={24} className="text-white" />
        </motion.div>
      </div>
    </motion.div>
  );
}

