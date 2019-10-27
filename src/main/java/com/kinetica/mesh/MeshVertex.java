/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains details for a vertex in a 3D mesh.
 * @author Chad Juliano
 */
public class MeshVertex {
    
    private final static Logger LOG = LoggerFactory.getLogger(MeshVertex.class);

    // 3D location of this vertex
    private final Point3f _vertex;
    
    // index to be used in the indices list.
    private final int _idx;
    
    // position of this vertex within the texture
    private Point2f _texCoord = null;
    
    // optional color for this vertex
    private Color _color = null;
    
    // list of normals that will be averaged during build()
    private List<Vector3f> _normals = new ArrayList<Vector3f>();
    
    // list of tangents that will be averaged during build()
    private List<Vector3f> _tangents = new ArrayList<Vector3f>();
    
    protected MeshVertex(int _index, Point3f _vertex) {
        this._idx = _index;
        this._vertex = _vertex;
    }
    
    /**
     * Create copy of a vertex
     * @param _index
     * @param _mv
     */
    protected MeshVertex(int _index, MeshVertex _mv) {
        this._idx = _index;
        this._vertex = _mv._vertex;
        
        if(_mv._color != null) {
            this._color = _mv.getColor();
        }
        
        if(_mv._texCoord != null) {
            this._texCoord = new Point2f(_mv._texCoord);
        }
        
        for(Vector3f _normal : _mv._normals) {
            this._normals.add(new Vector3f(_normal));
        }
        
        for(Vector3f _tangent : _mv._tangents) {
            this._tangents.add(new Vector3f(_tangent));
        }
    }
    
    public Point3f getVertex() { return this._vertex; }
    
    public int getIndex() { return this._idx; }
    
    public Color getColor() { return this._color; }
    
    public void setColor(Color _color) {  this._color = _color; }
    
    public Point2f getTexCoord() { return this._texCoord; }
    
    public void setTexCoord(Point2f _coord) { this._texCoord = _coord; }
    
    @Override 
    public String toString() {
        return String.format("idx=[%d] vtx=(%.6f,%.6f,%.6f) normals<%d>", 
                this._idx, 
                this._vertex.x, this._vertex.y, this._vertex.z, 
                this._normals.size());
    }
    
    protected void addNormal(Vector3f _vec) { this._normals.add(_vec); }
    
    protected void addTangent(Vector3f _vec) { this._tangents.add(_vec); }
    
    /**
     * Calculate the average of the normal vectors.
     * @return
     * @throws Exception
     */
    protected Vector3f getNormal() throws Exception {
        if(this._normals.size() == 0) {
            return null;
        }
        
        Vector3f _avgNormal = new Vector3f();

        // calculate an average normal
        for(Vector3f _normal : this._normals) {
            _avgNormal.add(_normal);
        }
        
        _avgNormal.normalize();
        
        if(Float.isNaN(_avgNormal.x) || Float.isNaN(_avgNormal.y) || Float.isNaN(_avgNormal.z)) {
            LOG.debug("Could not calculate average normal for vertex: {}", this.getIndex());
            // create a fake normal
            _avgNormal = new Vector3f(1f, 1f, 1f);
            _avgNormal.normalize();
        }
        return _avgNormal;
    }
    
    /**
     * Calculate the average of teh tangent vector. 
     * @return
     * @throws Exception
     */
    protected Vector4f getTangent() throws Exception {
        if(this._normals.size() == 0) {
            throw new Exception("No tangents to average for vertex: " + this._idx);
        }
        
        Vector3f _avgTangent = new Vector3f();
        
        // calculate an average tangent
        for(Vector3f _tangent : this._tangents) {
            _avgTangent.add(_tangent);
        }
        
        _avgTangent.normalize();
        Vector4f _tangent = new Vector4f(
                _avgTangent.x, 
                _avgTangent.y, 
                _avgTangent.z, 
                1f);
        return _tangent;
    }
}
