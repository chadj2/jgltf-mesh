/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.HashMap;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Generate 3d Icosphere.
 * @see <a href="http://www.songho.ca/opengl/gl_sphere.html">OpenGL Sphere</a>
 * @author Chad Juliano
 */
public class IcosphereBuilder extends TriangleBuilder {

    // don't change these
    private static final int IDX_TOP = 10;
    private static final int IDX_BOTTOM = 11;
    private static final int ICO_SIDES = 5;
    
    /**
     * Maximum leval of detail where zero is minimum. Higher levels of detail will
     * generate more vertices.
     */
    //private int _lod = 3;
    
    /**
     * Indicates if the sphere will be covered in a hexagonal pattern.
     */
    private boolean _isPatterned = false;
    
    /**
     * HSB color values.
     */
    private float[] _hsbVals;
    
    private Color _color;
    
    private double _radius = 1d;
    
    /**
     * index of midpoints used for de-duplication.
     */
    private final HashMap<String, MeshVertex> _midpointMap = new HashMap<>();
    
    
    public IcosphereBuilder(String _name) {
        super(_name);
        this.setColor(Color.WHITE);
        
        // don't invert X axis
        //this.getTransform().m00 = 1;
    }
    
    /**
     * Set the radius of the Icosphere.
     * @param radius
     */
    public void setRadius(double radius) { this._radius = radius; }
    
    /**
     * Color vertices in a pattern that reveals the Icosphere structure.
     * @param isPatterned
     */
    public void setIsPatterned(boolean isPatterned) { this._isPatterned = isPatterned; }

    /**
     * Set the Color of the Icosphere.
     * @param color
     */
    public void setColor(Color color) {
        this._hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this._color = color;
    }
    
    public Color getColor() { return this._color; }
    
    private Color getColor(int lod) {
        if(!this._isPatterned) {
            return this._color;
        }
        
        float val = this._hsbVals[2]/(lod+1f);
        return Color.getHSBColor(this._hsbVals[0], this._hsbVals[1], val);
    }
    
    /**
     * Add geometry for an Icosphere.
     * @param lod The level of detail where zero is minimum. Higher levels of detail will
     * generate more vertices.
     * @throws Exception 
     */
    public void addIcosphere(int lod) throws Exception {
        // get the 12 vertices of the Icosahedron
        MeshVertex[] vertices = getIcosahedronVertices();
        
        // recursively add triangles inside the 20 Icosahedron faces
        for(int idx = 0; idx < ICO_SIDES; idx++) {
            
            // row 1 indices. row1Next needs to be wrapped around to the start.
            int row1Idx = idx;
            int row1Next = (row1Idx + 1) % ICO_SIDES;
            
            // row 2 indices
            int row2Idx = row1Idx + ICO_SIDES;
            int row2Next = row1Next + ICO_SIDES;
            
            // top triangle
            addIcosphereTriangle(0, lod,
                    vertices[IDX_TOP], 
                    vertices[row1Next], 
                    vertices[row1Idx]);
            
            // downward middle triangle
            addIcosphereTriangle(0, lod,
                    vertices[row2Idx], 
                    vertices[row1Idx], 
                    vertices[row1Next]);
            
            // upward middle triangle
            addIcosphereTriangle(0, lod,
                    vertices[row1Next], 
                    vertices[row2Next], 
                    vertices[row2Idx]);
            
            // bottom triangle
            addIcosphereTriangle(0, lod,
                    vertices[IDX_BOTTOM], 
                    vertices[row2Idx], 
                    vertices[row2Next]);
        }
        
        this._midpointMap.clear();
    }

    /**
     * Get the 12 vertices of the Icosahedron.
     * @return
     * @throws Exception 
     */
    private MeshVertex[] getIcosahedronVertices() throws Exception {
        final double vAngle = Math.atan(1.0d/2);  // 26.565 degrees
        final double zPos = this._radius * Math.sin(vAngle);
        final double xyPos = this._radius * Math.cos(vAngle);
        final double hAngle = (2d*Math.PI)/ICO_SIDES; // 72 degrees
        
        final MeshVertex[] vertices = new MeshVertex[12];
        
        // compute 10 vertices at 1st and 2nd rows
        for(int idx = 0; idx < ICO_SIDES; ++idx)
        {
            // add vertex in first row
            int row1Idx = idx;
            double row1hAngle = idx*hAngle;
            MeshVertex row1 = newVertex(new Point3f(
                    (float)(xyPos * Math.cos(row1hAngle)), 
                    (float)(xyPos * Math.sin(row1hAngle)), 
                    (float)zPos));
            row1.setColor(getColor(0));
            vertices[row1Idx] = row1;

            // add vertex in second row
            int row2Idx = row1Idx + ICO_SIDES;
            double row2hAngle = row1hAngle + hAngle/2;
            MeshVertex row2 = newVertex(new Point3f(
                    (float)(xyPos * Math.cos(row2hAngle)), 
                    (float)(xyPos * Math.sin(row2hAngle)), 
                    (float)-zPos));
            row2.setColor(getColor(0));
            vertices[row2Idx] = row2;
        }
        
        // top vertex
        vertices[IDX_TOP] = newVertex(new Point3f(0f, 0f, (float)this._radius));
        vertices[IDX_TOP].setColor(getColor(0));
        
        // bottom vertex
        vertices[IDX_BOTTOM] = newVertex(new Point3f(0f, 0f, (float)-this._radius));
        vertices[IDX_BOTTOM].setColor(getColor(0));
        
        return vertices;
    }
    
    /**
     * Recursively add triangles to the Icosahedron.
     * @param currentLod
     * @param maxLod
     * @param v1
     * @param v2
     * @param v3
     * @throws Exception 
     */
    private void addIcosphereTriangle(int currentLod, int maxLod,
            MeshVertex v1, MeshVertex v2, MeshVertex v3) throws Exception {
        
        if(currentLod >= maxLod) {
            // this is the bottom of the recursion tree so add the triangle.
            addTriangle(v1, v2, v3);
            return;
        }

        // compute 3 new vertices by splitting half on each edge
        //         v1       
        //        / \       
        // newV1 *---* newV3
        //      / \ / \     
        //    v2---*---v3   
        //       newV2 
        MeshVertex newV1 = getMidpoint(currentLod, v1, v2);
        MeshVertex newV2 = getMidpoint(currentLod, v2, v3);
        MeshVertex newV3 = getMidpoint(currentLod, v1, v3);
        
        // recurse
        addIcosphereTriangle(currentLod+1, maxLod, v1, newV3, newV1);
        addIcosphereTriangle(currentLod+1, maxLod, v2, newV1, newV2);
        addIcosphereTriangle(currentLod+1, maxLod, v3, newV2, newV3);
        addIcosphereTriangle(currentLod+1, maxLod, newV2, newV1, newV3);
    }
    
    /**
     * Find midpoint of 2 vertices. 
     * @param tBuilder
     * @param v1
     * @param v2
     * @return
     * @throws Exception 
     */
    public MeshVertex getMidpoint(int lod, MeshVertex v1, MeshVertex v2) throws Exception {
        // We need to check if this midpoint was already added.
        String key = String.format("%d_%d", 
                Math.min(v1.getIndex(), v2.getIndex()),
                Math.max(v1.getIndex(), v2.getIndex()));
        MeshVertex newMv = this._midpointMap.get(key);
        
        if(newMv != null) {
            // midpoint already exists.
            return newMv;
        }
        
        Vector3f p3 = new Vector3f();
        p3.add(v1.getVertex(), v2.getVertex());

        // new vertex must be resized, so the length is equal to the radius
        p3.normalize();
        p3.scale((float)this._radius);
        
        Point3f newPoint = new Point3f(p3);
        newMv = newVertex(newPoint);
        newMv.setColor(getColor(lod));
        
        // add new midpoint to the index
        this._midpointMap.put(key, newMv);
        return newMv;
    }
}
