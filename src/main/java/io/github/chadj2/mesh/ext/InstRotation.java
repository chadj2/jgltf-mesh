package io.github.chadj2.mesh.ext;

import java.nio.ByteBuffer;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple4f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.model.GltfConstants;
import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.buffer.BufferBase;

public class InstRotation extends BufferBase<Quat4f> {

    private final Tuple4f _min = new Quat4f();
    private final Tuple4f _max = new Quat4f();
    
    public InstRotation(String _name) {
        super(_name);
        clear();
    }

    @Override
    public void clear() {
        super.clear();
        
        this._min.x = Float.POSITIVE_INFINITY;
        this._min.y = Float.POSITIVE_INFINITY;
        this._min.z = Float.POSITIVE_INFINITY;
        this._min.w = Float.POSITIVE_INFINITY;
        
        this._max.x = Float.NEGATIVE_INFINITY;
        this._max.y = Float.NEGATIVE_INFINITY;
        this._max.z = Float.NEGATIVE_INFINITY;
        this._max.w = Float.NEGATIVE_INFINITY;
    }

    @Override
    public void add(Quat4f _quat) {
        super.add(_quat);
        
        this._min.x = Math.min(this._min.x, _quat.x);
        this._min.y = Math.min(this._min.y, _quat.y);
        this._min.z = Math.min(this._min.z, _quat.z);
        this._min.w = Math.min(this._min.w, _quat.w);

        this._max.x = Math.max(this._min.x, _quat.x);
        this._max.y = Math.max(this._min.y, _quat.y);
        this._max.z = Math.max(this._min.z, _quat.z);
        this._max.w = Math.max(this._min.w, _quat.w);
    }

    @Override
    public Accessor build(GltfWriter _geoWriter, MeshPrimitive _meshPirimitive) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Accessor build(GltfWriter _geoWriter, GlTFMeshGpuInstancing _meshInstancing) {
        buildAttrib(_geoWriter, _meshInstancing, "ROTATION");
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
        int iVal = Math.round(fVal * 127f);
        if(iVal < -127 || iVal > 127) {
            throw new RuntimeException(String.format("Value overflow: %d", iVal));
        }
        return (byte)iVal;
    }
    
    @Override
    protected Accessor addAccessor(GlTF _gltf, BufferView _bufferView) {
        Accessor _accessor = super.addAccessor(_gltf, _bufferView);
        _accessor.setComponentType(GltfConstants.GL_BYTE);
        _accessor.setType("VEC4");
        
        _accessor.setMax(new Integer[] { 
                (int)floatToByte(this._max.x), 
                (int)floatToByte(this._max.y), 
                (int)floatToByte(this._max.z), 
                (int)floatToByte(this._max.w) });
        
        _accessor.setMin(new Integer[] { 
                (int)floatToByte(this._min.x), 
                (int)floatToByte(this._min.y), 
                (int)floatToByte(this._min.z), 
                (int)floatToByte(this._min.w) });
        
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
