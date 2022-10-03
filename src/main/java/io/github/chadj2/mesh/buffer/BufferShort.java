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
import de.javagl.jgltf.model.GltfConstants;

public class BufferShort extends BufferBase<Short> {
    
    public BufferShort(String _name) {
        super(_name);
    }

    public Short getMin() { 
        return Collections.min(this._list);
    }
    
    public Short getMax() {
        return Collections.max(this._list);
    }

    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_UNSIGNED_SHORT);
        _accessor.setType("SCALAR");
        
        _accessor.setMax(new Short[] { 
                getMax() });
        
        _accessor.setMin(new Short[] { 
                getMin() });
        
        return _accessor;
    }

    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(short _s : this._list) {
            _buffer.putShort(_s);
        }
    }

    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(GltfConstants.GL_ELEMENT_ARRAY_BUFFER);
        BufferBase.alignWords(_buffer);
        return _bufferView;
    }
    
}
