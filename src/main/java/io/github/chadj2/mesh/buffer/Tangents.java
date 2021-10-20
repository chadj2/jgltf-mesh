/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Vector4f;

import io.github.chadj2.mesh.GltfWriter;
import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;

/**
 * Serializer for Tangent primitives.
 * @author Chad Juliano
 */
public class Tangents extends BaseBuffer {
    
    private final ArrayList<Vector4f> _vecList = new ArrayList<>();
    private final Vector4f _minVec = new Vector4f();
    private final Vector4f _maxVec = new Vector4f();
    
    public Tangents(String _name) {
        super(_name);
        clear();
    }
    
    public void add(Vector4f _tangent) {
        this._minVec.x = Math.min(this._minVec.x, _tangent.x);
        this._minVec.y = Math.min(this._minVec.y, _tangent.y);
        this._minVec.z = Math.min(this._minVec.z, _tangent.z);
        this._minVec.w = Math.min(this._minVec.w, _tangent.w);

        this._maxVec.x = Math.max(this._maxVec.x, _tangent.x);
        this._maxVec.y = Math.max(this._maxVec.y, _tangent.y);
        this._maxVec.z = Math.max(this._maxVec.z, _tangent.z);
        this._maxVec.w = Math.max(this._maxVec.w, _tangent.w);
        
        this._vecList.add(_tangent);
    }
    
    @Override
    public void clear() {
        this._vecList.clear();
        
        this._minVec.x = Float.POSITIVE_INFINITY;
        this._minVec.y = Float.POSITIVE_INFINITY;
        this._minVec.z = Float.POSITIVE_INFINITY;
        this._minVec.w = Float.POSITIVE_INFINITY;
        
        this._maxVec.x = Float.NEGATIVE_INFINITY;
        this._maxVec.y = Float.NEGATIVE_INFINITY;
        this._maxVec.z = Float.NEGATIVE_INFINITY;
        this._maxVec.w = Float.NEGATIVE_INFINITY;
    }
    
    @Override
    public int size() {
        return this._vecList.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "TANGENT");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._vecList.size(); _i++) {
            Vector4f _vec = this._vecList.get(_i);
            _buffer.putFloat(_vec.x);
            _buffer.putFloat(_vec.y);
            _buffer.putFloat(_vec.z);
            _buffer.putFloat(_vec.w);
        }
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(BaseBuffer.FLOAT);
        _accessor.setType("VEC4");
        
        _accessor.setMax(new Float[] { 
                this._maxVec.x, 
                this._maxVec.y, 
                this._maxVec.z, 
                this._maxVec.w });
        
        _accessor.setMin(new Float[] { 
                this._minVec.x, 
                this._minVec.y, 
                this._minVec.z, 
                this._minVec.w });
        
        return _accessor;
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(BaseBuffer.ARRAY_BUFFER);
        _bufferView.setByteStride(16);
        return _bufferView;
    }
}
