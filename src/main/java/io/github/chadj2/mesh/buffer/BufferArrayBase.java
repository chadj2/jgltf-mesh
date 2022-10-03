/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.GltfConstants;

public abstract class BufferArrayBase<T> extends BufferBase<T> {
    
    protected T _min;
    protected T _max;
    private final int _byteStride;

    public BufferArrayBase(String _name, int byteStride) {
        super(_name);
        this._byteStride = byteStride;
    }

    public T getMin() { return this._min; }

    public T getMax() { return this._max; }
    
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(GltfConstants.GL_ARRAY_BUFFER);
        _bufferView.setByteStride(this._byteStride);
        return _bufferView;
    }
}
