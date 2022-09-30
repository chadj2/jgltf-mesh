/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.model.GltfConstants;
import io.github.chadj2.mesh.GltfWriter;

public abstract class BufferFloatBase<T> extends BufferBase<T> {
    
    protected T _min;
    protected T _max;
    protected final String _attrib;
    private final int _byteStride;

    public BufferFloatBase(String _name, String _attrib, int byteStride) {
        super(String.format("%s-%s", _name, _attrib));
        this._attrib = _attrib;
        this._byteStride = byteStride;
    }

    public T getMin() { return this._min; }

    public T getMax() { return this._max; }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, this._attrib);
    }

    public Accessor build(GltfWriter _geoWriter, GlTFMeshGpuInstancing _meshInstancing) {
        return buildAttrib(_geoWriter, _meshInstancing, this._attrib);
    }
    
    protected final Accessor buildAttrib(GltfWriter _geoWriter, GlTFMeshGpuInstancing _meshInstancing,
            String _attribute) {
        Accessor _accessor = buildBuffer(_geoWriter);
        if(_accessor == null) {
            return null;
        }
        
        int _accessorIdx = _geoWriter.getGltf().getAccessors().indexOf(_accessor);
        _meshInstancing.addAttributes(_attribute, _accessorIdx);
        return _accessor;
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(GltfConstants.GL_ARRAY_BUFFER);
        _bufferView.setByteStride(this._byteStride);
        return _bufferView;
    }
}
