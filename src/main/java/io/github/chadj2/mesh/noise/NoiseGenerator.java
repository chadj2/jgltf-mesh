/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.noise;

/**
 * Common base class for noise generators.
 * @author Chad Juliano
 */
public abstract class NoiseGenerator {
    
    /**
     * Wrapper for PerlinNoise generator.
     * @see PerlinNoise
     */
    public static class Perlin extends NoiseGenerator {

        private final PerlinNoise _noise;
        
        public Perlin() {
            this._noise = new PerlinNoise();
        }

        @Override
        protected double getNoiseInternal(double x, double y, double z) {
            return this._noise.noise(x, y, z);
        }

        @Override
        protected double getNoiseInternal(double x, double y) {
            return this._noise.noise(x, y, 0);
        }
    }
    
    /**
     * Wrapper for OpenSimplexNoise generator.
     * @see OpenSimplexNoise
     */
    public static class OpenSimplex extends NoiseGenerator {

        private final OpenSimplexNoise _noise;
        
        public OpenSimplex() {
            this._noise = new OpenSimplexNoise();
        }
        
        public OpenSimplex(long _seed) {
            this._noise = new OpenSimplexNoise(_seed);
        }

        @Override
        protected double getNoiseInternal(double x, double y, double z) {
            return this._noise.eval(x, y, z);
        }

        @Override
        protected double getNoiseInternal(double x, double y) {
            return this._noise.eval(x, y);
        }
    }
    
    private double lacunarity = 2;
    private int octaves = 5;
    private double persistence = 0.4;
    
    public NoiseGenerator() { }

    /**
     * To be implemented by the subclass.
     */
    protected abstract double getNoiseInternal(double x, double y, double z);

    /**
     * To be implemented by the subclass.
     */
    protected abstract double getNoiseInternal(double x, double y);
    
    /**
     * Set number of octaves. Each additional octave adds finer detail.
     */
    public void setOctaves(int _octaves) {  this.octaves = _octaves; }
    
    /**
     * Set the persistance. If values lower than 1.0 will reduce higher octaves 
     * leading to a smoother surface.
     */
    public void setPersistence(double _persistence) { this.persistence = _persistence; }
    
    /**
     * Get a 3D noise value.
     */
    public double getNoise(double x, double y, double z) {
        double xVal = x;
        double yVal = y;
        double zVal = z;
        double amp = 1.0;
        double max = 1.0;
        double sum = 0;

        for (int octIdx = 0; octIdx < this.octaves; octIdx++) {
            sum += getNoiseInternal(xVal, yVal, zVal) * amp;
            xVal *= this.lacunarity;
            yVal *= this.lacunarity;
            zVal *= this.lacunarity;
            amp *= this.persistence;
            max += amp;
        }

        return sum / max;
    }
    
    /**
     * Get a 2D noise value
     */
    public double getNoise(double x, double y) {
        double xVal = x;
        double yVal = y;
        double amp = 1.0;
        double max = 1.0;
        double sum = getNoiseInternal(xVal, yVal);

        for (int octIdx = 0; octIdx < this.octaves; octIdx++) {
            sum += getNoiseInternal(xVal, yVal) * amp;
            xVal *= this.lacunarity;
            yVal *= this.lacunarity;
            amp *= this.persistence;
            max += amp;
        }

        return sum / max;
    }
}
