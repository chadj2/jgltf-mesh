/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import javax.vecmath.Tuple2f;
import javax.vecmath.Vector2f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.GltfConstants;

public class BufferVecFloat2 extends BufferVecBase<Tuple2f>  {

    public BufferVecFloat2(String _name) {
        super(_name, Float.BYTES * 2);
    }
    
    @Override
    public Tuple2f getMin() {
        Tuple2f min = new Vector2f();
        min.x = Float.POSITIVE_INFINITY;
        min.y = Float.POSITIVE_INFINITY;
        
        for(Tuple2f val : this._list) {
            min.x =  Math.min(min.x, val.x);
            min.y =  Math.min(min.y, val.y);
        }
        
        return min;
    }
    
    @Override
    public Tuple2f getMax() {
        Tuple2f max = new Vector2f();
        max.x = Float.NEGATIVE_INFINITY;
        max.y = Float.NEGATIVE_INFINITY;
        
        for(Tuple2f val : this._list) {
            max.x =  Math.max(max.x, val.x);
            max.y =  Math.max(max.y, val.y);
        }
        
        return max;
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_FLOAT);
        _accessor.setType("VEC2");

        Tuple2f min = this.getMin();
        _accessor.setMin(new Number[] { min.x, min.y });
        
        Tuple2f max = this.getMin();
        _accessor.setMax(new Number[] { max.x, max.y });

        return _accessor;
    }
    
    @Override
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple2f _vec = this._list.get(_i);
            _buffer.putFloat(_vec.x);
            _buffer.putFloat(_vec.y);
        }
    }
}
