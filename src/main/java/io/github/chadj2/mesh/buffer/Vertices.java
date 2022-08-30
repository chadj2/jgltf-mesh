/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer vertex primitives.
 * @author Chad Juliano
 */
public class Vertices extends BaseBuffer {
    
    private final ArrayList<Point3f> _pointList = new ArrayList<>();
    private final Point3f _minPoint = new Point3f();
    private final Point3f _maxPoint = new Point3f();
    
    public Vertices(String _name) {
        super(_name);
        clear();
    }
    
    public void add(Point3f _vertex) {
        this._minPoint.x = Math.min(this._minPoint.x, _vertex.x);
        this._minPoint.y = Math.min(this._minPoint.y, _vertex.y);
        this._minPoint.z = Math.min(this._minPoint.z, _vertex.z);
        
        this._maxPoint.x = Math.max(this._maxPoint.x, _vertex.x);
        this._maxPoint.y = Math.max(this._maxPoint.y, _vertex.y);
        this._maxPoint.z = Math.max(this._maxPoint.z, _vertex.z);

        this._pointList.add(_vertex);
    }
    
    public Point3f getMinBounds() { return this._minPoint; }

    public Point3f getMaxBounds() { return this._maxPoint; }
    
    @Override
    public void clear() {
        this._pointList.clear();
        
        this._minPoint.x = Float.POSITIVE_INFINITY;
        this._minPoint.y = Float.POSITIVE_INFINITY;
        this._minPoint.z = Float.POSITIVE_INFINITY;
        
        this._maxPoint.x = Float.NEGATIVE_INFINITY;
        this._maxPoint.y = Float.NEGATIVE_INFINITY;
        this._maxPoint.z = Float.NEGATIVE_INFINITY;
    }
    
    public Point3f get(int idx) {
        return this._pointList.get(idx);
    }
    
    @Override
    public int size() {
        return this._pointList.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "POSITION");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._pointList.size(); _i++) {
            Point3f _vec = this._pointList.get(_i);
            _buffer.putFloat(_vec.x);
            _buffer.putFloat(_vec.y);
            _buffer.putFloat(_vec.z);
        }
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(BaseBuffer.FLOAT);
        _accessor.setType("VEC3");
        
        _accessor.setMax(new Float[] { 
                this._maxPoint.x, 
                this._maxPoint.y, 
                this._maxPoint.z });
        
        _accessor.setMin(new Float[] { 
                this._minPoint.x, 
                this._minPoint.y, 
                this._minPoint.z  });
        return _accessor;
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(BaseBuffer.ARRAY_BUFFER);
        _bufferView.setByteStride(12);
        return _bufferView;
    }
    
}