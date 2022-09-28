/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Base class for primitive serializers.
 * @author Chad Juliano
 */
public abstract class BaseBuffer {
    
    private final static Logger LOG = LoggerFactory.getLogger(BaseBuffer.class);
    
    // accessor types
    protected static final int UNSIGNED_SHORT = 5123;
    protected static final int UNSIGNED_BYTE = 5121;
    protected static final int FLOAT = 5126;
    
    // buffer types
    protected static final int ELEMENT_ARRAY_BUFFER = 34963;
    protected static final int ARRAY_BUFFER = 34962;
    
    public abstract int size();
    public abstract void clear();
    public abstract Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive);

    protected abstract void writeBuf(ByteBuffer _buffer);
    
    protected final String _name;
    
    public BaseBuffer(String _name) {
        this._name = _name;
    }
    
    protected final Accessor buildAttrib(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive, String _attribute) {
        Accessor _accessor = buildBuffer(_geoWriter);
        if(_accessor == null) {
            return null;
        }
        
        int _accessorIdx = _geoWriter.getGltf().getAccessors().indexOf(_accessor);
        _meshPirimitive.addAttributes(_attribute, _accessorIdx);
        return _accessor;
    }
    
    protected final Accessor buildBuffer(GltfWriter _geoWriter) {
        if(size() == 0) {
            return null;
        }
        
        BufferView _bufferView = this.addBufferView(_geoWriter.getGltf(), _geoWriter.getBuffer());
        Accessor _accessor = this.addAccessor(_geoWriter.getGltf(), _bufferView);
        return _accessor;
    }
    
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        int _bufferIdx = _gltf.getBufferViews().indexOf(_bufferView);
        
        Accessor _accessor = new Accessor();
        _gltf.addAccessors(_accessor);
        
        _accessor.setBufferView(_bufferIdx);
        _accessor.setByteOffset(0);
        _accessor.setCount(this.size());

        //int _idx = _gltf.getAccessors().indexOf(_accessor);
        String _type = this.getClass().getSimpleName();
        String _accessorName = String.format("%s-%s", this._name, _type);
        _accessor.setName(_accessorName);
        LOG.debug("Accessor[{}]: buffer={} count={}", _accessorName, _bufferIdx, this.size());
        return _accessor;
    }
    
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        int _startPos = _buffer.position();
        this.writeBuf(_buffer);
        int _length = _buffer.position() - _startPos;

        BufferView _bufferView = new BufferView();
        _gltf.addBufferViews(_bufferView);
        _bufferView.setBuffer(0);
        _bufferView.setByteOffset(_startPos);
        _bufferView.setByteLength(_length);

        //int _idx = _gltf.getBufferViews().indexOf(_bufferView);
        String _type = this.getClass().getSimpleName();
        String _bufViewName = String.format("%s-%s", this._name, _type);
        _bufferView.setName(_bufViewName);
        LOG.debug("BufferView[{}]: start={}, size={}", _bufViewName, _startPos, _length);
        return _bufferView;
    }

    protected static void alignWords(ByteBuffer _byteBuffer) {
        int _limit = _byteBuffer.position();
        int _padding = _limit % 4;
        
        for(int _i = 0; _i < _padding; _i++) {
            _byteBuffer.put((byte)0);
        }
    }
}