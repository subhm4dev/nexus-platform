'use client';

import { motion } from 'motion/react';
import { ImageWithFallback } from '@/components/figma/ImageWithFallback';
import { PattachitraWatermark } from '@/components/PattachitraWatermark';

/**
 * About Us Page
 */
export default function AboutPage() {
  // Mock artists data (will be replaced with API call later)
  const artists = [
    {
      id: '1',
      name: 'Rajesh Mahapatra',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      bio: 'A fifth-generation Pattachitra artist from Raghurajpur, Rajesh has dedicated his life to preserving and promoting this ancient art form.',
      specialty: 'Mythology & Deities',
      worksCount: 145,
    },
    {
      id: '2',
      name: 'Sushila Dash',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      bio: 'An award-winning artist specializing in festival themes and natural pigments, Sushila brings vibrant life to every canvas.',
      specialty: 'Festivals & Celebrations',
      worksCount: 98,
    },
    {
      id: '3',
      name: 'Ramesh Pattnaik',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      bio: 'Master of large-format Pattachitra, Ramesh\'s works are displayed in museums and galleries worldwide.',
      specialty: 'Large Format Epics',
      worksCount: 76,
    },
    {
      id: '4',
      name: 'Anita Swain',
      image: 'https://images.unsplash.com/photo-1669276064249-4aefccd07be3?w=400&h=500&fit=crop',
      bio: 'A contemporary voice in traditional Pattachitra, Anita blends ancient techniques with modern themes.',
      specialty: 'Nature & Wildlife',
      worksCount: 112,
    },
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative py-24 bg-gradient-to-br from-[rgb(var(--color-indigo))] to-[rgb(var(--color-terracotta))] text-white overflow-hidden">
        <PattachitraWatermark />
        <div className="container mx-auto px-6 relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            className="max-w-3xl mx-auto text-center"
          >
            <h1 className="text-5xl md:text-6xl mb-6">About Kalakosh</h1>
            <p className="text-xl text-white/90">
              Preserving Odisha's cultural heritage, one brushstroke at a time
            </p>
          </motion.div>
        </div>
      </section>

      {/* Our Story */}
      <section className="py-24 bg-white">
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto">
            <h2 className="text-4xl mb-8 text-[rgb(var(--color-indigo))] text-center">Our Story</h2>
            <div className="space-y-6 text-lg text-[rgb(var(--muted-foreground))]">
              <p>
                Kalakosh was born from a deep passion for preserving and promoting the ancient art form 
                of Pattachitra. Founded in 2025, we work directly with master artisans from Raghurajpur, 
                the heritage village of Pattachitra artists in Odisha.
              </p>
              <p>
                Our mission is to bridge the gap between traditional artisans and art lovers worldwide, 
                ensuring that this UNESCO-recognized cultural treasure continues to thrive for generations 
                to come.
              </p>
              <p>
                Every artwork in our collection is handcrafted using traditional techniques passed down 
                through generations, using natural pigments derived from vegetables, minerals, and stones.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Artists */}
      <section className="py-24 bg-[rgb(var(--color-ivory))]">
        <div className="container mx-auto px-6">
          <h2 className="text-4xl mb-12 text-[rgb(var(--color-indigo))] text-center">Our Master Artisans</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {artists.map((artist, index) => (
              <motion.div
                key={artist.id}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="bg-white rounded-xl shadow-lg overflow-hidden"
              >
                <div className="relative aspect-[3/4] overflow-hidden">
                  <ImageWithFallback
                    src={artist.image}
                    alt={artist.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="p-6">
                  <h3 className="text-xl mb-2 text-[rgb(var(--color-indigo))]">{artist.name}</h3>
                  <p className="text-sm text-[rgb(var(--color-terracotta))] mb-3">{artist.specialty}</p>
                  <p className="text-sm text-[rgb(var(--muted-foreground))] line-clamp-3">{artist.bio}</p>
                  <p className="text-xs text-[rgb(var(--muted-foreground))] mt-3">
                    {artist.worksCount} artworks
                  </p>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Heritage Section */}
      <section className="py-24 bg-white">
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto">
            <h2 className="text-4xl mb-8 text-[rgb(var(--color-indigo))] text-center">Pattachitra Heritage</h2>
            <div className="space-y-6 text-lg text-[rgb(var(--muted-foreground))]">
              <p>
                Pattachitra, literally meaning "picture on cloth," is one of the oldest and most revered 
                art forms of India. Originating in Odisha over a thousand years ago, this art form has 
                been practiced continuously in the village of Raghurajpur, which has been declared a 
                heritage village by the Indian National Trust for Art and Cultural Heritage (INTACH).
              </p>
              <p>
                The art form is deeply rooted in the Jagannath cult and the temple traditions of Puri. 
                Traditional Pattachitra paintings depict mythological themes, especially stories from 
                the Ramayana, Mahabharata, and the life of Lord Krishna.
              </p>
              <p>
                What makes Pattachitra unique is the use of natural colors and the intricate brushwork. 
                Artists prepare their own colors from vegetables, minerals, and stones, and use brushes 
                made from animal hair. The process is time-consuming and requires immense skill, with 
                some artworks taking weeks or even months to complete.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

