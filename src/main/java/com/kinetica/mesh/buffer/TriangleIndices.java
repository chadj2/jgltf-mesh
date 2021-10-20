/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import com.kinetica.mesh.GltfWriter;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;

/**
 * Serializer for triangle index primitives.
 * @author Chad Juliano
 */
public class TriangleIndices extends BaseBuffer {
    
    private final ArrayList<Short> _list = new ArrayList<>();
    
    public TriangleIndices(String _name) {
        super(_name);
    }
    
    public void add(int _v1, int _v2, int _v3) {
        this._list.add((short)_v1);
        this._list.add((short)_v2);
        this._list.add((short)_v3);
    }
    
    @Override
    public void clear() {
        this._list.clear();
        this._list.clear();
    }
    
    @Override
    public int size() {
        return this._list.size();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        Accessor _indicesAccessor = super.buildBuffer(_geoWriter);
        int _accessorIdx = _geoWriter.getGltf().getAccessors().indexOf(_indicesAccessor);
        _meshPirimitive.setIndices(_accessorIdx);
        return _indicesAccessor;
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(short _s : this._list) {
            _buffer.putShort(_s);
        }
    }

    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(BaseBuffer.UNSIGNED_SHORT);
        _accessor.setType("SCALAR");
        
        _accessor.setMax(new Short[] { 
                Collections.max(this._list) });
        
        _accessor.setMin(new Short[] { 
                Collections.min(this._list) });
        
        return _accessor;
    }

    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(BaseBuffer.ELEMENT_ARRAY_BUFFER);
        BaseBuffer.alignWords(_buffer);
        return _bufferView;
    }
}