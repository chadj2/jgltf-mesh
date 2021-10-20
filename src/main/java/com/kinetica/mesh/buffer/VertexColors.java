/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */
package io.github.chadj2.mesh.buffer;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import io.github.chadj2.mesh.GltfWriter;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;

/**
 * Serializer for vertex color primitives.
 * @author Chad Juliano
 */
public class VertexColors extends BaseBuffer {

    private final ArrayList<Color> _cList = new ArrayList<>();
    
    public VertexColors(String _name) {
        super(_name);
    }
    
    public void add(Color _color) {
        this._cList.add(_color);
    }

    @Override
    public void clear() {
        this._cList.clear();
    }
    
    @Override
    public int size() {
        return this._cList.size();
    }

    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "COLOR_0");
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._cList.size(); _i++) {
            Color _color = this._cList.get(_i);
            
            Integer _r = _color.getRed();
            Integer _g = _color.getGreen();
            Integer _b = _color.getBlue();
            Integer _a = _color.getAlpha();
            
            _buffer.put((byte)_r.byteValue());
            _buffer.put((byte)_g.byteValue());
            _buffer.put((byte)_b.byteValue());
            _buffer.put((byte)_a.byteValue());
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
        _bufferView.setByteStride(4);
        return _bufferView;
    }
}