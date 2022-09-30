/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.ext;

import java.nio.ByteBuffer;

import javax.vecmath.Tuple4f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.model.GltfConstants;
import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.buffer.BufferVec4;

/**
 * Support EXT_mesh_gpu_instancing
 * @author Chad Juliano
 */
public class InstRotation extends BufferVec4 {
    
    public InstRotation(String _name) {
        super(_name, "ROTATION");
    }

    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        throw new UnsupportedOperationException("not implimented");
    }
    
    public Accessor build(GltfWriter _geoWriter, GlTFMeshGpuInstancing _meshInstancing) {
        buildAttrib(_geoWriter, _meshInstancing, this._attrib);
        return null;
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
    protected void writeBuf(ByteBuffer _buffer) {
        for(int _i = 0; _i < this._list.size(); _i++) {
            Tuple4f _vec = this._list.get(_i);
            _buffer.put(floatToByte(_vec.x));
            _buffer.put(floatToByte(_vec.y));
            _buffer.put(floatToByte(_vec.z));
            _buffer.put(floatToByte(_vec.w));
        }
    }
    
    private static byte floatToByte(float fVal) {
        Integer iVal = Math.round(fVal * 127f);
        if(iVal < -127 || iVal > 127) {
            throw new RuntimeException(String.format("Value overflow: %d", iVal));
        }
        return iVal.byteValue();
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_BYTE);
        _accessor.setType("VEC4");
        
        Tuple4f max = this.getMax();
        _accessor.setMax(new Integer[] { 
                (int)floatToByte(max.x), 
                (int)floatToByte(max.y), 
                (int)floatToByte(max.z), 
                (int)floatToByte(max.w) });

        Tuple4f min = this.getMin();
        _accessor.setMin(new Integer[] { 
                (int)floatToByte(min.x), 
                (int)floatToByte(min.y), 
                (int)floatToByte(min.z), 
                (int)floatToByte(min.w) });
        
        return _accessor;
    }
    
    @Override
    protected BufferView addBufferView(GlTF _gltf, ByteBuffer _buffer) {
        BufferView _bufferView = super.addBufferView(_gltf, _buffer);
        _bufferView.setTarget(GltfConstants.GL_ARRAY_BUFFER);
        _bufferView.setByteStride(Byte.BYTES * 4);
        return _bufferView;
    }

}
