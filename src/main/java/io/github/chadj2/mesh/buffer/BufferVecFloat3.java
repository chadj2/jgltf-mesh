/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.GltfConstants;

public class BufferVecFloat3 extends BufferVecBase<Tuple3f>  {

    public BufferVecFloat3(String _name) {
        super(_name, Float.BYTES * 3);
    }
    
    @Override
    public Tuple3f getMin() {
        Tuple3f min = new Vector3f();
        min.x = Float.POSITIVE_INFINITY;
        min.y = Float.POSITIVE_INFINITY;
        min.z = Float.POSITIVE_INFINITY;
        
        for(Tuple3f val : this._list) {
            min.x =  Math.min(min.x, val.x);
            min.y =  Math.min(min.y, val.y);
            min.z =  Math.min(min.z, val.z);
        }
        
        return min;
    }
    
    @Override
    public Tuple3f getMax() {
        Tuple3f max = new Vector3f();
        max.x = Float.NEGATIVE_INFINITY;
        max.y = Float.NEGATIVE_INFINITY;
        max.z = Float.NEGATIVE_INFINITY;
        
        for(Tuple3f val : this._list) {
            max.x =  Math.max(max.x, val.x);
            max.y =  Math.max(max.y, val.y);
            max.z =  Math.max(max.z, val.z);
        }
        
        return max;
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_FLOAT);
        _accessor.setType("VEC3");
        
        Tuple3f min = this.getMin();
        _accessor.setMin(new Number[] { min.x, min.y, min.z });
        
        Tuple3f max = this.getMin();
        _accessor.setMax(new Number[] { max.x, max.y, max.z });
        
        return _accessor;
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple3f _vec = this._list.get(_i);
            _buffer.putFloat(_vec.x);
            _buffer.putFloat(_vec.y);
            _buffer.putFloat(_vec.z);
        }
    }
}
