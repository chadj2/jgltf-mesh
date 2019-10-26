/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.noise;

/**
 * Wrapper for Noise generators.
 * @author chadjuliano
 *
 */
public abstract class NoiseGenerator {
    
    /**
     * Wrapper for PerlinNoise generator.
     * @author chadjuliano
     *
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
     * @author chadjuliano
     *
     */
    public static class OpenSimplex extends NoiseGenerator {

        private final OpenSimplexNoise _noise;
        
        public OpenSimplex() {
            this._noise = new OpenSimplexNoise();
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
    
    public NoiseGenerator() {
    }
    
    /**
     * Set number of octaves. Each additional octave adds finer detail.
     * @param _value
     */
    public void setOctaves(int _value) {
        this.octaves = _value;
    }
    
    /**
     * Set the persistance. If values lower than 1.0 will reduce higher octaves 
     * leading to a smoother surface.
     * @param _value
     */
    public void setPersistence(double _value) {
        this.persistence = _value;
    }
    
    /**
     * Get a 3D noise value.
     * @param x
     * @param y
     * @param z
     * @return
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
     * Get a 3D noise value
     * @param x
     * @param y
     * @return
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
    
    protected abstract double getNoiseInternal(double x, double y, double z);

    protected abstract double getNoiseInternal(double x, double y);
}
