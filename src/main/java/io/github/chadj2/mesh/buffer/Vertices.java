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
 * Serializer vertex primitives.
 * @author Chad Juliano
 */
public class Vertices extends BufferVec3 {
    
    public Vertices(String _name) {
        super(_name);
        clear();
    }
    
    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        return buildAttrib(_geoWriter, _meshPirimitive, "POSITION");
    }
}
