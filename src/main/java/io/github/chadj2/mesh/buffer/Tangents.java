/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Tuple4f;
import javax.vecmath.Vector4f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer for Tangent primitives.
 * @author Chad Juliano
 */
public class Tangents extends BaseBuffer {
    
    private final ArrayList<Tuple4f> _list = new ArrayList<>();
    private final Tuple4f _min = new Vector4f();
    private final Tuple4f _max = new Vector4f();
    
    public Tangents(String _name) {
        super(_name);
        clear();
    }
    
    public void add(Tuple4f _tangent) {
        this._min.x = Math.min(this._min.x, _tangent.x);
        this._min.y = Math.min(this._min.y, _tangent.y);
        this._min.z = Math.min(this._min.z, _tangent.z);
        this._min.w = Math.min(this._min.w, _tangent.w);

        this._max.x = Math.max(this._max.x, _tangent.x);
        this._max.y = Math.max(this._max.y, _tangent.y);
        this._max.z = Math.max(this._max.z, _tangent.z);
        this._max.w = Math.max(this._max.w, _tangent.w);
        
        this._list.add(_tangent);
    }
    
    @Override
    public void clear() {
        this._list.clear();
        
        this._min.x = Float.POSITIVE_INFINITY;
        this._min.y = Float.POSITIVE_INFINITY;
        this._min.z = Float.POSITIVE_INFINITY;
        this._min.w = Float.POSITIVE_INFINITY;
        
        this._max.x = Float.NEGATIVE_INFINITY;
        this._max.y = Float.NEGATIVE_INFINITY;
        this._max.z = Float.NEGATIVE_INFINITY;
        this._max.w = Float.NEGATIVE_INFINITY;
    }
    
    @Override
    public int size() {
        return this._list.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "TANGENT");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple4f _vec = this._list.get(_i);
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
                this._max.x, 
                this._max.y, 
                this._max.z, 
                this._max.w });
        
        _accessor.setMin(new Float[] { 
                this._min.x, 
                this._min.y, 
                this._min.z, 
                this._min.w });
        
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
