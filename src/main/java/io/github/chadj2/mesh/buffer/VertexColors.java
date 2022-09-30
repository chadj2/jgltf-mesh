/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */
package io.github.chadj2.mesh.buffer;

import java.awt.Color;
import java.nio.ByteBuffer;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer for vertex color primitives.
 * @author Chad Juliano
 */
public class VertexColors extends BaseBuffer<Color> {
    
    public VertexColors(String _name) {
        super(_name);
    }

    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "COLOR_0");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Color _color = this._list.get(_i);
            
            Integer _r = _color.getRed();
            Integer _g = _color.getGreen();
            Integer _b = _color.getBlue();
            Integer _a = _color.getAlpha();
            
            _buffer.put(_r.byteValue());
            _buffer.put(_g.byteValue());
            _buffer.put(_b.byteValue());
            _buffer.put(_a.byteValue());
        }
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(BaseBuffer.UNSIGNED_BYTE);
        _accessor.setType("VEC4");
        _accessor.setNormalized(true);
        return _accessor;
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(BaseBuffer.ARRAY_BUFFER);
        _bufferView.setByteStride(Integer.BYTES);
        return _bufferView;
    }
}