/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import javax.vecmath.Tuple4f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.GltfConstants;

/**
 * Support EXT_mesh_gpu_instancing
 * @author Chad Juliano
 */
public class BufferByte4 extends BufferFloat4 {
    
    public BufferByte4(String _name, String _attrib) {
        super(_name, _attrib);
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple4f _vec = this._list.get(_i);
            _buffer.put(floatToByte(_vec.x));
            _buffer.put(floatToByte(_vec.y));
            _buffer.put(floatToByte(_vec.z));
            _buffer.put(floatToByte(_vec.w));
        }
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_BYTE);
        _accessor.setType("VEC4");
        
        _accessor.setMax(new Integer[] { 
                (int)floatToByte(this._max.x), 
                (int)floatToByte(this._max.y), 
                (int)floatToByte(this._max.z), 
                (int)floatToByte(this._max.w) });

        _accessor.setMin(new Integer[] { 
                (int)floatToByte(this._min.x), 
                (int)floatToByte(this._min.y), 
                (int)floatToByte(this._min.z), 
                (int)floatToByte(this._min.w) });
        
        return _accessor;
    }
    
    /**
     * @see https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#animations
     * @param fVal
     * @return
     */
    private static byte floatToByte(float fVal) {
        Integer iVal = Math.round(fVal * Byte.MAX_VALUE);
        if(iVal < Byte.MIN_VALUE || iVal > Byte.MAX_VALUE) {
            throw new RuntimeException(String.format("Value overflow: %d", iVal));
        }
        return iVal.byteValue();
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(GltfConstants.GL_ARRAY_BUFFER);
        _bufferView.setByteStride(Byte.BYTES * 4);
        return _bufferView;
    }
}
