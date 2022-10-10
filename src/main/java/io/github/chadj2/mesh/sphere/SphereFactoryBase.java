/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.sphere;

import java.awt.Color;

import javax.vecmath.Point3f;

import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.BaseBuilder;

public abstract class SphereFactoryBase extends BaseBuilder {

    private float _radius = 1f;
    private Color _color = Color.WHITE;
    private int _lod = 2;
    
    public SphereFactoryBase(String _name) {
        super(_name);
    }
    
    public abstract Node addSphere(Point3f pos, String eventId) throws Exception;
    
    public abstract void build();

    public void setColor(Color color) { this._color = color; }
    
    protected Color getColor() { return this._color; }

    /**
     * Set the radius of the sphere. Note that if you translate points with setTransform() then the radius is in
     * the units of the transformed frame. 
     * @param radius
     */
    public void setRadius(float radius) { this._radius = radius; }
    
    protected float getRadius() { return this._radius; }

    public void setMaxDetail(int val) { this._lod = val; }
    
    protected int getMaxDetail() { return this._lod; }
    
}
