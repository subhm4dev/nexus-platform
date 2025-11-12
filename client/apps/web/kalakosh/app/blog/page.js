'use client';

import { useRouter } from 'next/navigation';
import { motion } from 'motion/react';
import { Calendar, Clock, User } from 'lucide-react';
import { ImageWithFallback } from '@/components/figma/ImageWithFallback';

/**
 * Blog Page
 */
export default function BlogPage() {
  const router = useRouter();

  // Mock blog posts (will be replaced with API call later)
  const blogPosts = [
    {
      id: '1',
      title: 'The Sacred Art of Pattachitra: A Thousand-Year Legacy',
      excerpt: 'Discover the ancient techniques and spiritual significance behind Odisha\'s most treasured art form.',
      author: 'Dr. Ananya Mishra',
      authorImage: 'https://images.unsplash.com/photo-1725477830944-e303634c50cf?w=400&h=400&fit=crop',
      date: '2025-11-08',
      image: 'https://images.unsplash.com/photo-1713103659707-15c3eeb33e35?w=800&h=600&fit=crop',
      category: 'Art & Culture',
      readTime: '8 min',
      featured: true,
    },
    {
      id: '2',
      title: 'From Palm Leaves to Canvas: The Evolution of Pattachitra',
      excerpt: 'Explore how master artisans have adapted traditional techniques for modern mediums.',
      author: 'Rajesh Mahapatra',
      authorImage: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=400&fit=crop',
      date: '2025-11-05',
      image: 'https://images.unsplash.com/photo-1762186540868-a7f3328c161d?w=800&h=600&fit=crop',
      category: 'Techniques',
      readTime: '6 min',
      featured: true,
    },
    {
      id: '3',
      title: 'Natural Pigments: The Colors of Tradition',
      excerpt: 'Learn about the organic materials used to create the vibrant colors that define Pattachitra art.',
      author: 'Sushila Dash',
      authorImage: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=400&fit=crop',
      date: '2025-11-01',
      image: 'https://images.unsplash.com/photo-1553334490-011441d86dbb?w=800&h=600&fit=crop',
      category: 'Techniques',
      readTime: '5 min',
      featured: false,
    },
  ];

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))]">
      {/* Header */}
      <section className="bg-gradient-to-r from-[rgb(var(--color-indigo))] to-[rgb(var(--color-indigo-light))] text-white py-16">
        <div className="container mx-auto px-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="max-w-3xl mx-auto text-center"
          >
            <h1 className="text-4xl md:text-5xl mb-4">Blog & Stories</h1>
            <p className="text-xl text-white/90">
              Insights, techniques, and tales from the world of Pattachitra
            </p>
          </motion.div>
        </div>
      </section>

      {/* Featured Post */}
      {blogPosts.find(p => p.featured) && (
        <section className="py-12 bg-white">
          <div className="container mx-auto px-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="max-w-4xl mx-auto"
            >
              <span className="text-sm uppercase tracking-wider text-[rgb(var(--color-terracotta))] mb-2 block">
                Featured
              </span>
              {(() => {
                const featured = blogPosts.find(p => p.featured);
                return (
                  <div className="grid md:grid-cols-2 gap-8 items-center">
                    <div className="relative aspect-[4/3] rounded-xl overflow-hidden">
                      <ImageWithFallback
                        src={featured.image}
                        alt={featured.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div>
                      <span className="text-sm text-[rgb(var(--color-terracotta))] mb-2 block">
                        {featured.category}
                      </span>
                      <h2 className="text-3xl mb-4 text-[rgb(var(--color-indigo))]">{featured.title}</h2>
                      <p className="text-lg text-[rgb(var(--muted-foreground))] mb-6">{featured.excerpt}</p>
                      <div className="flex items-center gap-4 text-sm text-[rgb(var(--muted-foreground))] mb-6">
                        <div className="flex items-center gap-2">
                          <User className="w-4 h-4" />
                          <span>{featured.author}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4" />
                          <span>{new Date(featured.date).toLocaleDateString()}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Clock className="w-4 h-4" />
                          <span>{featured.readTime}</span>
                        </div>
                      </div>
                      <button
                        onClick={() => router.push(`/blog/${featured.id}`)}
                        className="text-[rgb(var(--color-indigo))] hover:text-[rgb(var(--color-terracotta))] font-semibold"
                      >
                        Read More →
                      </button>
                    </div>
                  </div>
                );
              })()}
            </motion.div>
          </div>
        </section>
      )}

      {/* Blog Posts Grid */}
      <section className="py-12 bg-[rgb(var(--color-ivory))]">
        <div className="container mx-auto px-6">
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {blogPosts.map((post, index) => (
              <motion.div
                key={post.id}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="bg-white rounded-xl shadow-md overflow-hidden cursor-pointer hover:shadow-xl transition-shadow"
                onClick={() => router.push(`/blog/${post.id}`)}
              >
                <div className="relative aspect-[4/3] overflow-hidden">
                  <ImageWithFallback
                    src={post.image}
                    alt={post.title}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="p-6">
                  <span className="text-xs text-[rgb(var(--color-terracotta))] mb-2 block">
                    {post.category}
                  </span>
                  <h3 className="text-xl mb-3 text-[rgb(var(--color-indigo))] line-clamp-2">
                    {post.title}
                  </h3>
                  <p className="text-sm text-[rgb(var(--muted-foreground))] mb-4 line-clamp-2">
                    {post.excerpt}
                  </p>
                  <div className="flex items-center gap-4 text-xs text-[rgb(var(--muted-foreground))]">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      <span>{new Date(post.date).toLocaleDateString()}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      <span>{post.readTime}</span>
                    </div>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

