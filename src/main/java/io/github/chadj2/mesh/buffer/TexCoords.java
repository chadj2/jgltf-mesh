/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Tuple2f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer for texture coordinate primitives.
 * @author Chad Juliano
 */
public class TexCoords extends BaseBuffer {
    
    private final ArrayList<Tuple2f> _list = new ArrayList<>();
    private final Tuple2f _min = new Point2f();
    private final Tuple2f _max = new Point2f();
    
    public TexCoords(String _name) {
        super(_name);
        clear();
    }
    
    public void add(Tuple2f _coord) {
        this._min.x = Math.min(this._min.x, _coord.x);
        this._min.y = Math.min(this._min.y, _coord.y);
        
        this._max.x = Math.max(this._max.x, _coord.x);
        this._max.y = Math.max(this._max.y, _coord.y);
        
        this._list.add(_coord);
    }
    
    @Override
    public void clear() {
        this._list.clear();
        
        this._min.x = Float.POSITIVE_INFINITY;
        this._min.y = Float.POSITIVE_INFINITY;
        
        this._max.x = Float.NEGATIVE_INFINITY;
        this._max.y = Float.NEGATIVE_INFINITY;
    }

    @Override
    public int size() {
        return this._list.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "TEXCOORD_0");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple2f _vec = this._list.get(_i);
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
                this._max.x, 
                this._max.y });
        
        _accessor.setMin(new Float[] { 
                this._min.x, 
                this._min.y });
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