package com.echobound.sandbox;

import java.util.Random;

/**
 * Classic (Ken Perlin, improved 2002) gradient noise, seeded and deterministic — replaces the
 * sin(x)*cos(y) combinations WorldChunk used for elevation/biome/vegetation noise. Those
 * produce visible wavy, grid-aligned banding because a sum of a few pure sinusoids repeats
 * and interferes in predictable ways; gradient noise is what terrain generators actually use
 * because it has no periodicity at this scale and no axis-aligned artifacts.
 */
public class PerlinNoise {
    private final int[] perm = new int[512];

    public PerlinNoise(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        Random rng = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /** Single-octave noise, range approximately [-1, 1]. */
    public double noise(double x, double y) {
        int xi = ((int) Math.floor(x)) & 255;
        int yi = ((int) Math.floor(y)) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double u = fade(xf);
        double v = fade(yf);

        int aa = perm[perm[xi] + yi];
        int ab = perm[perm[xi] + yi + 1];
        int ba = perm[perm[xi + 1] + yi];
        int bb = perm[perm[xi + 1] + yi + 1];

        double x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf));
        double x2 = lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1));
        return lerp(v, x1, x2);
    }

    /** Fractal Brownian motion: layered octaves for richer, more natural detail than a single
     *  noise call — this is what actually gives terrain its organic, non-repeating look. */
    public double fbm(double x, double y, int octaves, double persistence) {
        double total = 0, amplitude = 1, frequency = 1, maxAmplitude = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }
        return maxAmplitude > 0 ? total / maxAmplitude : 0;
    }
}
