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

public class BufferFloat3 extends BufferArrayBase<Tuple3f>  {

    public BufferFloat3(String _name) {
        super(_name, Float.BYTES * 3);
        this._max = new Vector3f();
        this._min = new Vector3f();
        clear();
    }

    @Override
    public void add(Tuple3f _vec3) {
        super.add(_vec3);
        
        this._min.x = Math.min(this._min.x, _vec3.x);
        this._min.y = Math.min(this._min.y, _vec3.y);
        this._min.z = Math.min(this._min.z, _vec3.z);
        
        this._max.x = Math.max(this._max.x, _vec3.x);
        this._max.y = Math.max(this._max.y, _vec3.y);
        this._max.z = Math.max(this._max.z, _vec3.z);
    }
    
    @Override
    public void clear() {
        super.clear();
        
        this._min.x = Float.POSITIVE_INFINITY;
        this._min.y = Float.POSITIVE_INFINITY;
        this._min.z = Float.POSITIVE_INFINITY;
        
        this._max.x = Float.NEGATIVE_INFINITY;
        this._max.y = Float.NEGATIVE_INFINITY;
        this._max.z = Float.NEGATIVE_INFINITY;
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
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_FLOAT);
        _accessor.setType("VEC3");
        
        _accessor.setMax(new Float[] { 
                this._max.x, 
                this._max.y, 
                this._max.z });
        
        _accessor.setMin(new Float[] { 
                this._min.x, 
                this._min.y, 
                this._min.z  });
        return _accessor;
    }
}
