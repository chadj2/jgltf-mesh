/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.buffer;

import java.nio.ByteBuffer;

import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.extsm.PropertyTableProperty;

/**
 * Buffer to create string properties for EXT_structural_metadata extension.
 * @author Chad Juliano
 */
public class BufferMetadataString extends BufferBase<String>  {
    
    private final BufferShort _offsets;

    public BufferMetadataString(String name) {
        super(name);
        this._offsets = new BufferShort(name);
    }
    
    /**
     * Create a property for a metadata table.
     * @param _writer
     * @return
     */
    public PropertyTableProperty createProperty(MeshGltfWriter _writer) {
        GlTF gltf = _writer.getGltf();
        PropertyTableProperty ptProp = new PropertyTableProperty();
        
        BufferView valuesBv = addBufferView(_writer.getGltf(), _writer.getBuffer());
        int valuesIdx = gltf.getBufferViews().indexOf(valuesBv);
        ptProp.setValues(valuesIdx);

        BufferView offsetsBv = this._offsets.addBufferView(_writer.getGltf(), _writer.getBuffer());
        int offsetsIdx = gltf.getBufferViews().indexOf(offsetsBv);
        ptProp.setStringOffsets(offsetsIdx);
        ptProp.setStringOffsetType("UINT16");
        return ptProp;
    }

    @Override
    public String getMin() {
        throw new UnsupportedOperationException("not implimented");
    }

    @Override
    public String getMax() {
        throw new UnsupportedOperationException("not implimented");
    }

    @Override
    protected void writeBuf(ByteBuffer buffer) {
        int startPos = buffer.position();
        
        for(String val : this._list) {
            int offset = buffer.position() - startPos;
            this._offsets.add((short)offset);
            
            byte[] bytes = val.getBytes();
            buffer.put(bytes);
        }
    }
}
