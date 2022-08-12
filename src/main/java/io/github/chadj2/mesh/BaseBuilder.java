/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class BaseBuilder {
    
    /** Indicates if the X axis should be inverted. This is necessary to correct orientations for Cesium. */
    //private static final int INVERT_X = -1;
    //private static final int INVERT_X = 1;

    /**
     * Create HSB colors with alpha transparency.
     * @param hue
     * @param sat
     * @param val
     * @param alpha
     * @return
     */
    public static Color createColorHSB(float hue, float sat, float val, float alpha) {
        Color color = Color.getHSBColor(hue, sat, val);
        return createColorTransparent(color, alpha);
    }

    /** 
     * Adjust saturation and brightness of a color.
     * @param color
     * @param satFactor
     * @param brFactor
     * @return 
     */
    public static Color adjustSatBr(Color color, float satFactor, float brFactor) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[1] = Math.min(satFactor * hsb[1], 1f);
        hsb[2] = Math.min(brFactor * hsb[2], 1f);
        
        int colorNum = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        
        int red = (colorNum >> 16) & 0xFF;
        int green = (colorNum >> 8) & 0xFF;
        int blue = (colorNum >> 0) & 0xFF;
        return  new Color(red, green, blue, color.getAlpha());
    }
    
    /**
     * Add transparency to color.
     * @param color
     * @param alpha
     * @return
     */
    public static Color createColorTransparent(Color color, float alpha) {
        int alphaInt = Math.round(alpha*255);
        Color alColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaInt);
        return alColor;
    }

    /** Mesh name used in metadata descriptions */
    private String _name;

    /** Transform scale and offset */
    private final Matrix4f _transform = new Matrix4f();

    public BaseBuilder(String _name) {
        this._name = _name;
        this._transform.setIdentity();
    }
    
    /** 
     * Change the name of this builder.
     * @param _name
     */
    public void setName(String _name) { this._name = _name; }

    /**
     * Return the mesh name.
     */
    public String getName() { return this._name; }

    /**
     * Set the transform used for offset and scale. This will replace any existing translations.
     */
    public void setTransform(Matrix4f _transform) { this._transform.set(_transform); }

    /**
     * Get the transformation matrix.
     */
    public Matrix4f getTransform() { return this._transform; }
    
    /**
     * Center all vertices about a point. This will update the transformation matrix.
     */
    public void setCenter(Point3f _offset) {
        Vector3f _vec = new Vector3f(_offset);
        
        // negate because we are centering
        _vec.negate();
        
        // invert the X axis
        //_vec.x *= INVERT_X;
        
        this._transform.setTranslation(_vec);
    }

    /**
     * Scale all vertices in X/Y/Z. This will update the transformation matrix.
     */
    public void setScale(Vector3f _scale) {
        this._transform.m00 = _scale.x;
        this._transform.m11 = _scale.y;
        this._transform.m22 = _scale.z;
        
        // invert the X axis
        //this._transform.m00 *= INVERT_X;
    }
}
