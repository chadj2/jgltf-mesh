/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import io.github.chadj2.mesh.GltfWriter;

/**
 * Serializer for normal vector primitives.
 * @author Chad Juliano
 */
public class Normals extends BufferVec3 {
    
    public Normals(String _name) {
        super(_name);
        clear();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "NORMAL");
    }
}
