/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kinetica.mesh.buffer.Normals;
import com.kinetica.mesh.buffer.Tangents;
import com.kinetica.mesh.buffer.TexCoords;
import com.kinetica.mesh.buffer.TriangleIndices;

import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;

/**
 * Build 3D Geometry from triangles or squares. Tangents, indices, and normals are automatically
 * generated.
 * @author Chad Juliano
 */
public class TriangleBuilder extends TopologyBuilder {
    
    private final static Logger LOG = LoggerFactory.getLogger(TriangleBuilder.class);
    
    /** indices keep track of connectivity between vertices. */
    protected final TriangleIndices _indices;
    
    /** Suppress additions of normal vectors  */
    private boolean _supressNormals = false;

    /** Material for the mesh */
    private Material _material = null;

    /**
     * @param _name Name of the glTF mesh node.
     */
    public TriangleBuilder(String _name) {
        super(_name, TopologyMode.TRIANGLES);
        this._indices = new TriangleIndices(_name);
    }
    
    /**
b    * Enable or disable suppression of normals.
     */
    public void supressNormals(boolean _isEnabled) {
        this._supressNormals = _isEnabled;
    }
    
    /**
     * Set a Material that will be used when generating the mesh.
     * @param _material Material from the GltfWriter
     * @see GltfWriter#newTextureMaterial(String)
     */
    public void setMaterial(Material _material) {
        this._material = _material;
    }
    
    /**
     * This method should be called when all shapes have added. It will serialize the MeshVertex
     * list and indices to buffers.
     * @param _material Material from the GltfWriter
     * @see GltfWriter#newTextureMaterial(String)
     * @deprecated Use {@link #setMaterial(Material)} instead
     */
    @Deprecated
    public Node build(GltfWriter _geoWriter, Material _material) throws Exception {
        this.setMaterial(_material);
        return build(_geoWriter);
    }
    
    /**
     * Add a 3D triangle specified by 3 vertices. All triangles should be added through this
     * method so that normals can be calculated.
     * @see TopologyBuilder#newVertex
     */
    public void addTriangle(MeshVertex _vtx0, MeshVertex _vtx1, MeshVertex _vtx2) {
        // add indices
        this._indices.add(_vtx0.getIndex(), _vtx1.getIndex(), _vtx2.getIndex());
        
        if(!this._supressNormals) {
            // calculate normal with cross product
            final Vector3f _vec01 = new Vector3f();
            _vec01.sub(_vtx0.getVertex(), _vtx1.getVertex());
            
            final Vector3f _vec21 = new Vector3f();
            _vec21.sub(_vtx2.getVertex(), _vtx1.getVertex());
            
            Vector3f _normal = new Vector3f();
            _normal.cross(_vec21, _vec01);
            _normal.normalize();
            
            if(Float.isNaN(_normal.x) || Float.isNaN(_normal.y) || Float.isNaN(_normal.z)) {
                LOG.debug("Could not calculate normal for triangle: {},{},{}", 
                        _vtx0.getIndex(), _vtx1.getIndex(), _vtx2.getIndex());
                // create a fake normal
                _normal = new Vector3f(1f, 1f, 1f);
                _normal.normalize();
            }
            
            // add this normal to each vertex
            _vtx0.addNormal(_normal);
            _vtx1.addNormal(_normal);
            _vtx2.addNormal(_normal);
        }
    }

    /**
     * Add a 3D square represented by 4 vertices specified counter clockwise. 
     * All squares should be added though this method so that normals can be calculated.
     * @param _vtx0 Start of square
     * @param _vtx1 common to both triangles
     * @param _vtx2 common to both triangles
     * @param _vtx3 End of square
     * @see TopologyBuilder#newVertex
     */
    public void addSquare(MeshVertex _vtx0, MeshVertex _vtx1, MeshVertex _vtx2, MeshVertex _vtx3) {
        // We need to connect the points with counter-clockwise triangles.
        // Any triangles will do as long as the are CC.
        addTriangle(_vtx0, _vtx1, _vtx2);
        addTriangle(_vtx2, _vtx1, _vtx3);
        
        // calculate tangents
        Vector3f _vec01 = new Vector3f();
        _vec01.sub(_vtx0.getVertex(), _vtx1.getVertex());
        _vtx0.addTangent(_vec01);
        _vtx1.addTangent(_vec01);
        
        Vector3f _vec23 = new Vector3f();
        _vec23.sub(_vtx2.getVertex(), _vtx3.getVertex());
        _vtx2.addTangent(_vec23);
        _vtx3.addTangent(_vec23);
    }

    @Override
    protected void buildBuffers(GltfWriter _geoWriter, MeshPrimitive _meshPrimitive) throws Exception {
        super.buildBuffers(_geoWriter, _meshPrimitive);
        
        if(this._material != null) {
            int _materialIdx = _geoWriter.getGltf().getMaterials().indexOf(this._material);
            _meshPrimitive.setMaterial(_materialIdx);
        }

        if(this._indices.size() == 0) {
            throw new Exception("Mesh has no indices: " + this.getName());
        }
        
        TexCoords _texCoords = new TexCoords(this.getName());
        Normals _normals = new Normals(this.getName());
        Tangents _tangents = new Tangents(this.getName());
        
        for(MeshVertex _meshVertex : this._vertexList) {
            Point2f _texCoord = _meshVertex.getTexCoord();
            if(_texCoord != null) {
                _texCoords.add(_texCoord);
            }
            
            Vector3f _normal = _meshVertex.getNormal();
            if(_normal != null) {
                _normals.add(_normal);
            }
            
            // leave out tangents for now.
            //this._tangents.add(_meshVertex.getTangent());
        }
        
        if(_normals.size() > 0 && _normals.size() != this._vertexList.size()) {
            throw new Exception("Each Vertex must have a normal");
        }
        
        if(_texCoords.size() > 0 && _texCoords.size() != this._vertexList.size()) {
            throw new Exception("Each Vertex must have a texCoord");
        }
        
        // flush all buffers to the primitive
        this._indices.build(_geoWriter, _meshPrimitive);
        _texCoords.build(_geoWriter, _meshPrimitive);
        _normals.build(_geoWriter, _meshPrimitive);
        _tangents.build(_geoWriter, _meshPrimitive);
    }
}
