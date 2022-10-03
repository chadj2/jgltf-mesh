/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.MeshGltfWriter;

/**
 * Serializer for triangle index primitives.
 * @author Chad Juliano
 */
public class TriangleIndices extends BufferShort {
    
    public static final int MAX_INDEX = 65535;
    
    public TriangleIndices(String _name) {
        super(_name);
    }

    @Override
    public void add(Short _primitive) {
        throw new UnsupportedOperationException("not implimented");
    }
    
    public void add(int _v1, int _v2, int _v3) throws Exception {
        
        if(_v1 >= MAX_INDEX || _v2 >= MAX_INDEX || _v3 >= MAX_INDEX) {
            String msg = String.format("Trangle idex cannot exceed %d", MAX_INDEX);
            throw new Exception(msg);
        } 
        
        this._list.add((short)_v1);
        this._list.add((short)_v2);
        this._list.add((short)_v3);
    }

    public Accessor build(MeshGltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        Accessor _accessor = super.buildBuffer(_geoWriter);
        if(_accessor == null) {
            return null;
        }
        
        int _accessorIdx = _geoWriter.getGltf().getAccessors().indexOf(_accessor);
        _meshPirimitive.setIndices(_accessorIdx);
        return _accessor;
    }
}
