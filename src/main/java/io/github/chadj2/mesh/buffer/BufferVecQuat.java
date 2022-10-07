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
public class BufferVecQuat extends BufferVecFloat4 {
    
    public BufferVecQuat(String _name) {
        super(_name);
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_BYTE);
        _accessor.setType("VEC4");
        
        Tuple4f max = this.getMin();
        _accessor.setMax(new Number[] { 
                floatToByte(max.x), 
                floatToByte(max.y), 
                floatToByte(max.z), 
                floatToByte(max.w) });

        Tuple4f min = this.getMin();
        _accessor.setMax(new Number[] { 
                floatToByte(min.x), 
                floatToByte(min.y), 
                floatToByte(min.z), 
                floatToByte(min.w) });
        
        return _accessor;
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
    
    /**
     * @see "https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#animations"
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
