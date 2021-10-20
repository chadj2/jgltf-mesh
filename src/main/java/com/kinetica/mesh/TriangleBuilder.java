/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.chadj2.mesh.buffer.Normals;
import io.github.chadj2.mesh.buffer.Tangents;
import io.github.chadj2.mesh.buffer.TexCoords;
import io.github.chadj2.mesh.buffer.TriangleIndices;

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

    public TriangleBuilder(String _name) {
        super(_name, TopologyMode.TRIANGLES);
        this._indices = new TriangleIndices(_name);
    }
    
    /**
b    * Enable or disable suppression of normals.
     */
    public void supressNormals(boolean _supressNormals) {
        this._supressNormals = _supressNormals;
    }
    
    /**
     * Set a Material that will be used when generating the mesh.
     * @param _material
     */
    public void setMaterial(Material _material) {
        this._material = _material;
    }
    
    /**
     * This method should be called when all shapes have added. It will serialize the MeshVertex
     * list and indices to buffers.
     * <p> Use {@link #setMaterial(Material)} instead.
     * @param _geoWriter
     * @return
     * @throws Exception
     */
    @Deprecated
    public Node build(GltfWriter _geoWriter, Material _texture) throws Exception {
        this.setMaterial(_texture);
        return build(_geoWriter);
    }
    
    /**
     * Add a 3D triangle specified by 3 vertices. All triangles should be added through this
     * method so that normals can be calculated. 
     * @param _vtx0
     * @param _vtx1
     * @param _vtx2
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
     * Add a 3D square represented by 4 vertices. All squares should be added though this method
     * so that normals can be calculated. Points common to both triangles are 1 and 2.
     * @param _vtx0
     * @param _vtx1
     * @param _vtx2
     * @param _vtx3
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
            int _materialIdx = _geoWriter._gltf.getMaterials().indexOf(this._material);
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
