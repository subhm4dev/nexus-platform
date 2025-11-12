'use client';

import { useRouter } from 'next/navigation';
import { CreditCard, FileText } from 'lucide-react';

export default function PaymentsPage() {
  const router = useRouter();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-neutral-900">Payments</h1>
        <p className="text-sm text-neutral-600 mt-1">Payment management and processing</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <button
          onClick={() => router.push('/admin/payments/pos')}
          className="flex items-center gap-3 px-6 py-4 bg-white border border-neutral-200 rounded-lg hover:bg-neutral-50 transition-colors"
        >
          <CreditCard className="w-6 h-6 text-blue-600" />
          <div className="text-left">
            <h3 className="font-semibold text-neutral-900">POS Payment</h3>
            <p className="text-sm text-neutral-600">Process payments at point of sale</p>
          </div>
        </button>
        <button
          onClick={() => router.push('/admin/payments/manual')}
          className="flex items-center gap-3 px-6 py-4 bg-white border border-neutral-200 rounded-lg hover:bg-neutral-50 transition-colors"
        >
          <FileText className="w-6 h-6 text-green-600" />
          <div className="text-left">
            <h3 className="font-semibold text-neutral-900">Manual Payment</h3>
            <p className="text-sm text-neutral-600">Record manual payment entries</p>
          </div>
        </button>
      </div>
    </div>
  );
}

