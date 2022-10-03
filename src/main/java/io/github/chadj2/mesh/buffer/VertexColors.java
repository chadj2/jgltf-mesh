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
import de.javagl.jgltf.model.GltfConstants;
import io.github.chadj2.mesh.MeshGltfWriter;

/**
 * Serializer for vertex color primitives.
 * @author Chad Juliano
 */
public class VertexColors extends BufferArrayBase<Byte> {
    
    public VertexColors(String _name) {
        super(_name, Byte.BYTES * 4);
    }

    public Accessor build(MeshGltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "COLOR_0");
    }
    
    @Override
    public int size() { return this._list.size() / 4; }
    
    public void add(Color color) {
        Integer _r = color.getRed();
        Integer _g = color.getGreen();
        Integer _b = color.getBlue();
        Integer _a = color.getAlpha();
        
        add(_r.byteValue());
        add(_g.byteValue());
        add(_b.byteValue());
        add(_a.byteValue());
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_UNSIGNED_BYTE);
        _accessor.setType("VEC4");
        _accessor.setNormalized(true);
        return _accessor;
    }

    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(byte _b : this._list) {
            _buffer.put(_b);
        }
    }
}