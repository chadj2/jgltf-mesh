/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import javax.vecmath.Tuple4f;
import javax.vecmath.Vector4f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.GltfConstants;

public class BufferVecFloat4 extends BufferVecBase<Tuple4f> {
    
    public BufferVecFloat4(String _name) {
        super(_name, Float.BYTES * 4);
    }
    
    @Override
    public Tuple4f getMin() {
        Tuple4f min = new Vector4f();
        min.x = Float.POSITIVE_INFINITY;
        min.y = Float.POSITIVE_INFINITY;
        min.z = Float.POSITIVE_INFINITY;
        min.w = Float.POSITIVE_INFINITY;
        
        for(Tuple4f val : this._list) {
            min.x =  Math.min(min.x, val.x);
            min.y =  Math.min(min.y, val.y);
            min.z =  Math.min(min.z, val.z);
            min.w =  Math.min(min.w, val.w);
        }
        
        return min;
    }
    
    @Override
    public Tuple4f getMax() {
        Tuple4f max = new Vector4f();
        max.x = Float.NEGATIVE_INFINITY;
        max.y = Float.NEGATIVE_INFINITY;
        max.z = Float.NEGATIVE_INFINITY;
        max.w = Float.NEGATIVE_INFINITY;
        
        for(Tuple4f val : this._list) {
            max.x =  Math.max(max.x, val.x);
            max.y =  Math.max(max.y, val.y);
            max.z =  Math.max(max.z, val.z);
            max.w =  Math.min(max.w, val.w);
        }
        
        return max;
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_FLOAT);
        _accessor.setType("VEC4");
        
        Tuple4f min = this.getMin();
        _accessor.setMin(new Number[] { min.x, min.y, min.z, min.w });
        
        Tuple4f max = this.getMin();
        _accessor.setMax(new Number[] { max.x, max.y, max.z, max.w });
        
        return _accessor;
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple4f _vec = this._list.get(_i);
            _buffer.putFloat(_vec.x);
            _buffer.putFloat(_vec.y);
            _buffer.putFloat(_vec.z);
            _buffer.putFloat(_vec.w);
        }
    }
}
