/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Point2f;

import com.kinetica.mesh.GltfWriter;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;

public class TexCoords extends BaseBuffer {
    
    private final ArrayList<Point2f> _pointList = new ArrayList<>();
    private final Point2f _minPoint = new Point2f();
    private final Point2f _maxPoint = new Point2f();
    
    public TexCoords(String _name) {
        super(_name);
        clear();
    }
    
    public void add(Point2f _coord) {
        this._minPoint.x = Math.min(this._minPoint.x, _coord.x);
        this._minPoint.y = Math.min(this._minPoint.y, _coord.y);
        
        this._maxPoint.x = Math.max(this._maxPoint.x, _coord.x);
        this._maxPoint.y = Math.max(this._maxPoint.y, _coord.y);
        
        this._pointList.add(_coord);
    }
    
    @Override
    public void clear() {
        this._pointList.clear();
        
        this._minPoint.x = Float.POSITIVE_INFINITY;
        this._minPoint.y = Float.POSITIVE_INFINITY;
        
        this._maxPoint.x = Float.NEGATIVE_INFINITY;
        this._maxPoint.y = Float.NEGATIVE_INFINITY;
    }

    @Override
    public int size() {
        return this._pointList.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "TEXCOORD_0");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._pointList.size(); _i++) {
            Point2f _vec = this._pointList.get(_i);
            _buffer.putFloat(_vec.x);
            _buffer.putFloat(_vec.y);
        }
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(BaseBuffer.FLOAT);
        _accessor.setType("VEC2");
        
        _accessor.setMax(new Float[] { 
                this._maxPoint.x, 
                this._maxPoint.y });
        
        _accessor.setMin(new Float[] { 
                this._minPoint.x, 
                this._minPoint.y });
        return _accessor;
    }

    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(BaseBuffer.ARRAY_BUFFER);
        _bufferView.setByteStride(8);
        return _bufferView;
    }
}