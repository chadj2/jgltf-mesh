/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer for normal vector primitives.
 * @author Chad Juliano
 */
public class Normals extends BaseBuffer<Vector3f> {

    private final Vector3f _min = new Vector3f();
    private final Vector3f _max = new Vector3f();
    
    public Normals(String _name) {
        super(_name);
        clear();
    }
    
    @Override
    public void add(Vector3f _primitive) {
        super.add(_primitive);
        
        this._min.x = Math.min(this._min.x, _primitive.x);
        this._min.y = Math.min(this._min.y, _primitive.y);
        this._min.z = Math.min(this._min.z, _primitive.z);
        
        this._max.x = Math.max(this._max.x, _primitive.x);
        this._max.y = Math.max(this._max.y, _primitive.y);
        this._max.z = Math.max(this._max.z, _primitive.z);
    }
    
    @Override
    public void clear() {
        super.clear();
        
        this._min.x = Float.POSITIVE_INFINITY;
        this._min.y = Float.POSITIVE_INFINITY;
        this._min.z = Float.POSITIVE_INFINITY;
        
        this._max.x = Float.NEGATIVE_INFINITY;
        this._max.y = Float.NEGATIVE_INFINITY;
        this._max.z = Float.NEGATIVE_INFINITY;
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "NORMAL");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple3f _vec = this._list.get(_i);
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
                this._max.x, 
                this._max.y, 
                this._max.z });
        
        _accessor.setMin(new Float[] { 
                this._min.x, 
                this._min.y, 
                this._min.z  });
        
        return _accessor;
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(BaseBuffer.ARRAY_BUFFER);
        _bufferView.setByteStride(Float.BYTES * 3);
        return _bufferView;
    }
}
