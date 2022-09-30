/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.model.GltfConstants;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer vertex primitives.
 * @author Chad Juliano
 */
public class Vertices extends BaseBuffer<Point3f> {
    
    private final Tuple3f _min = new Point3f();
    private final Tuple3f _max = new Point3f();
    
    public Vertices(String _name) {
        super(_name);
        clear();
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
    public void add(Point3f _vertex) {
        super.add(_vertex);
        
        this._min.x = Math.min(this._min.x, _vertex.x);
        this._min.y = Math.min(this._min.y, _vertex.y);
        this._min.z = Math.min(this._min.z, _vertex.z);
        
        this._max.x = Math.max(this._max.x, _vertex.x);
        this._max.y = Math.max(this._max.y, _vertex.y);
        this._max.z = Math.max(this._max.z, _vertex.z);
    }
    
    public Tuple3f getMin() { return this._min; }

    public Tuple3f getMax() { return this._max; }
    
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "POSITION");
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
        _accessor.setComponentType(GltfConstants.GL_FLOAT);
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
        _bufferView.setTarget(GltfConstants.GL_ARRAY_BUFFER);
        _bufferView.setByteStride(Float.BYTES * 3);
        return _bufferView;
    }
    
}