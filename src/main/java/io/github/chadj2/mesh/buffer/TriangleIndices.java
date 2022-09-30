/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;
import java.util.Collections;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.model.GltfConstants;
import io.github.chadj2.mesh.MeshGltfWriter;

/**
 * Serializer for triangle index primitives.
 * @author Chad Juliano
 */
public class TriangleIndices extends BufferBase<Short> {
    
    public static final int MAX_INDEX = 65535;
    
    public TriangleIndices(String _name) {
        super(_name);
    }

    @Override
    public void add(Short _primitive) {
        throw new UnsupportedOperationException("not implimented");
    }
    
    public void add(int _v1, int _v2, int _v3) throws Exception {
        
        if(_v1 >= MAX_INDEX || _v2 >= MAX_INDEX || _v3 >= MAX_INDEX) {
            String msg = String.format("Trangle idex cannot exceed %d", MAX_INDEX);
            throw new Exception(msg);
        } 
        
        this._list.add((short)_v1);
        this._list.add((short)_v2);
        this._list.add((short)_v3);
    }
    
    @Override
    public Accessor build(MeshGltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        Accessor _accessor = super.buildBuffer(_geoWriter);
        if(_accessor == null) {
            return null;
        }
        
        int _accessorIdx = _geoWriter.getGltf().getAccessors().indexOf(_accessor);
        _meshPirimitive.setIndices(_accessorIdx);
        return _accessor;
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
        _accessor.setComponentType(GltfConstants.GL_UNSIGNED_SHORT);
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
        _bufferView.setTarget(GltfConstants.GL_ELEMENT_ARRAY_BUFFER);
        BufferBase.alignWords(_buffer);
        return _bufferView;
    }
}