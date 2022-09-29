/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.ext;

import de.javagl.jgltf.impl.v2.Accessor;
import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.buffer.Vertices;

/**
 * Support EXT_mesh_gpu_instancing
 * @author Chad Juliano
 */
public class InstTranslation extends Vertices {

    public InstTranslation(String _name) {
        super(_name);
    }

    public Accessor build(GltfWriter _geoWriter, GlTFMeshGpuInstancing _meshInstancing) {
        buildAttrib(_geoWriter, _meshInstancing, "TRANSLATION");
        return null;
    }
    
    protected final Accessor buildAttrib(GltfWriter _geoWriter, GlTFMeshGpuInstancing _meshInstancing, String _attribute) {
        Accessor _accessor = buildBuffer(_geoWriter);
        if(_accessor == null) {
            return null;
        }
        
        int _accessorIdx = _geoWriter.getGltf().getAccessors().indexOf(_accessor);
        _meshInstancing.addAttributes(_attribute, _accessorIdx);
        return _accessor;
    }

}
