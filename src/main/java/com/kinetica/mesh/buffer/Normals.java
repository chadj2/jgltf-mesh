/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import com.kinetica.mesh.GltfWriter;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;

public class Normals extends BaseBuffer {

    private final ArrayList<Vector3f> _vecList = new ArrayList<>();
    private final Vector3f _minVec = new Vector3f();
    private final Vector3f _maxVec = new Vector3f();
    
    public Normals(String _name) {
        super(_name);
        clear();
    }
    
    public void add(Vector3f _vertex) {
        this._minVec.x = Math.min(this._minVec.x, _vertex.x);
        this._minVec.y = Math.min(this._minVec.y, _vertex.y);
        this._minVec.z = Math.min(this._minVec.z, _vertex.z);
        
        this._maxVec.x = Math.max(this._maxVec.x, _vertex.x);
        this._maxVec.y = Math.max(this._maxVec.y, _vertex.y);
        this._maxVec.z = Math.max(this._maxVec.z, _vertex.z);

        this._vecList.add(_vertex);
    }
    
    @Override
    public void clear() {
        this._vecList.clear();
        
        this._minVec.x = Float.POSITIVE_INFINITY;
        this._minVec.y = Float.POSITIVE_INFINITY;
        this._minVec.z = Float.POSITIVE_INFINITY;
        
        this._maxVec.x = Float.NEGATIVE_INFINITY;
        this._maxVec.y = Float.NEGATIVE_INFINITY;
        this._maxVec.z = Float.NEGATIVE_INFINITY;
    }
    
    @Override
    public int size() {
        return this._vecList.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "NORMAL");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._vecList.size(); _i++) {
            Vector3f _vec = this._vecList.get(_i);
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
                this._maxVec.x, 
                this._maxVec.y, 
                this._maxVec.z });
        
        _accessor.setMin(new Float[] { 
                this._minVec.x, 
                this._minVec.y, 
                this._minVec.z  });
        
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
