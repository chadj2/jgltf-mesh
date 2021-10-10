/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kinetica.mesh.buffer.Normals;
import com.kinetica.mesh.buffer.Tangents;
import com.kinetica.mesh.buffer.TexCoords;
import com.kinetica.mesh.buffer.TriangleIndices;
import com.kinetica.mesh.buffer.VertexColors;
import com.kinetica.mesh.buffer.Vertices;

import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;

/**
 * Build 3D Geometry from triangles or squares. Tangents, indices, and normals are automatically
 * generated.
 * @author Chad Juliano
 */
public class GeometryBuilder {
    
    private final static Logger LOG = LoggerFactory.getLogger(GeometryBuilder.class);

    // indicates if the X axis should be inverted. 
    // This is necessary to correct orientations for Cesium.
    private static final int INVERT_X = -1;

    // mesh name used in metadata descriptions
    private final String _name;
    
    // list of vertices being added to this mesh.
    private final List<MeshVertex> _vertexList = new ArrayList<MeshVertex>();
    
    // indices keep track of connectivity between vertices.
    protected final TriangleIndices _indices;
    
    private boolean _hasNormals = true;
    
    // Transform scale and offset
    private final Matrix4f _transform = new Matrix4f();
    
    // min and max bounds are generated during build().
    private Point3f _minBounds;
    private Point3f _maxBounds;
    
    // suppress additions of normal vectors. 
    // This is a workaround for rendering textures.
    boolean _suppressNormals = false;
    

    public GeometryBuilder(String _name) {
        this._name = _name;
        this._indices = new TriangleIndices(_name);
        
        setScale(new Vector3f(1,1,1));
    }
    
    /**
     * Set the transform used for offset and scale
     * @param _transform
     */
    public void setTransform(Matrix4f _transform) {
        this._transform.set(_transform);
    }
    
    /**
     * Get the transformation matrix.
     * @return
     */
    public Matrix4f getTransform() {
        return this._transform;
    }
    
    /**
     * Center all vertices about a point.
     * 
     * @param _offset
     */
    public void setCenter(Vector3f _offset) {
        Vector3f _vec = new Vector3f(_offset);
        
        // negate because we are centering
        _vec.negate();
        
        // invert the X axis
        _vec.x *= INVERT_X;
        
        this._transform.setTranslation(_vec);
    }
    
    /**
     * Scale all vertices in X/Y/Z.
     * 
     * @param _scale
     */
    public void setScale(Vector3f _scale) {
        this._transform.m00 = _scale.x;
        this._transform.m11 = _scale.y;
        this._transform.m22 = _scale.z;
        
        // invert the X axis
        this._transform.m00 *= INVERT_X;
    }
    
    /**
b    * Enable or disable normals.
     * @param _hasNormals
     */
    public void setNormals(boolean _hasNormals) {
        this._hasNormals = _hasNormals;
    }
    
    /**
     * Get the minimum bounds of all vertices. Should only be called after build().
     * 
     * @return
     */
    public Point3f getMinBounds() {
        return this._minBounds;
    }
    
    /**
     * Get the maximum bounds of all vertices. Should only be called after build().
     * 
     * @return
     */
    public Point3f getMaxBounds() {
        return this._maxBounds;
    }
    
    /**
     * Returns true if no triangles have been added.
     * @return
     */
    public boolean isEmpty() {
        return this._indices.size() == 0;
    }
    
    /**
     * Return the name used in the constructor.
     * @return
     */
    public String getName() {
        return this._name;
    }

    /**
     * Create a new vertex and apply the current offset and scale. This vertex will be assigned
     * an unique index that will be referenced when adding squares or triangles.
     * 
     * @param _vertex 3D location of this vertex.
     * @return
     */
    public MeshVertex newVertex(Point3f _vertex) {
        Point3f _newVertex = new Point3f(_vertex);
        
        // apply offset and scale
        this._transform.transform(_newVertex);
        
        MeshVertex _meshVertex = new MeshVertex(this._vertexList.size(), _newVertex);
        this._vertexList.add(_meshVertex);
        return _meshVertex;
    }
    
    public MeshVertex copyVertex(MeshVertex _vertex) {
        if(_vertex == null) {
            // nothing to copy
            return null;
        }
        
        MeshVertex _meshVertex = new MeshVertex(this._vertexList.size(), _vertex);
        this._vertexList.add(_meshVertex);
        return _meshVertex;
    }

    /**
     * This method should be called when all shapes have added. It will serialize the MeshVertex
     * list and indices to buffers.
     *
     * @param _geoWriter
     * @param _texture
     * @param _meshPrimitive
     * @return
     * @throws Exception
     */
    public Node build(GltfWriter _geoWriter, Material _texture, MeshPrimitive _meshPrimitive) throws Exception {

        if(this._vertexList.size() == 0) {
            throw new Exception("No vertices to build!");
        }

        Mesh _mesh = new Mesh();
        _geoWriter._gltf.addMeshes(_mesh);
        _mesh.setName(this._name + "-mesh");
        int _meshIdx = _geoWriter._gltf.getMeshes().indexOf(_mesh);

        _mesh.addPrimitives(_meshPrimitive);

        if(_texture != null) {
            int _materialIdx = _geoWriter._gltf.getMaterials().indexOf(_texture);
            _meshPrimitive.setMaterial(_materialIdx);
        }

        buildBuffers(_geoWriter, _meshPrimitive);

        LOG.debug("New Mesh[{}]: idx=<{}>", _mesh.getName(), _meshIdx);

        Node _node = new Node();
        _geoWriter.addNode(_node);
        _node.setMesh(_meshIdx);
        _node.setName(this._name + "-node");

        return _node;
    }

    /**
     * This method should be called when all shapes have added. It will serialize the MeshVertex
     * list and indices to buffers.
     * 
     * @param _geoWriter
     * @param _texture
     * @return
     * @throws Exception
     */
    public Node build(GltfWriter _geoWriter, Material _texture) throws Exception {
        MeshPrimitive _meshPrimitive = new MeshPrimitive();
        return build(_geoWriter, _texture, _meshPrimitive);
    }
    
    /**
     * Add a 3D triangle specified by 3 vertices. All triangles should be added through this
     * method so that normals can be calculated. 
     * 
     * @param _vtx0
     * @param _vtx1
     * @param _vtx2
     */
    public void addTriangle(MeshVertex _vtx0, MeshVertex _vtx1, MeshVertex _vtx2) {
        
        // add indices
        this._indices.add(_vtx0.getIndex(), _vtx1.getIndex(), _vtx2.getIndex());
        
        if(!this._suppressNormals) {
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

    /**
     * Generate primitive lists from the MeshVertex list and serialize to buffers.
     * 
     * @param _geoWriter
     * @param _meshPrimitive
     * @throws Exception
     */
    private void buildBuffers(GltfWriter _geoWriter, MeshPrimitive _meshPrimitive) 
            throws Exception {
        Vertices _vertices = new Vertices(this._name);
        TexCoords _texCoords = new TexCoords(this._name);
        VertexColors _colors = new VertexColors(this._name);
        Normals _normals = new Normals(this._name);
        Tangents _tangents = new Tangents(this._name);
        
        if(this._indices.size() == 0) {
        	throw new Exception("Mesh has no indices: " + this._name);
        }
        
        for(MeshVertex _meshVertex : this._vertexList) {

            _vertices.add(_meshVertex.getVertex());
            
            Point2f _texCoord = _meshVertex.getTexCoord();
            if(_texCoord != null) {
                _texCoords.add(_texCoord);
            }
            
            Color _color = _meshVertex.getColor();
            if(_color != null) {
                _colors.add(_color);
            }
            
            if(this._hasNormals) {
                // it is important to calculate normals here after all neighboring vertices
                // have been added.
                Vector3f _normal = _meshVertex.getNormal();
                if(_normal == null) {
                    LOG.warn("Vertex is not part of a triangle: " + _meshVertex.getIndex());
                    // create a fake normal
                    _normal = new Vector3f(1f, 1f, 1f);
                    _normal.normalize();
                }
                _normals.add(_normal);
            }
            
            // leave out tangents for now.
            //this._tangents.add(_meshVertex.getTangent());
        }
        
        if(_colors.size() > 0 && _colors.size() != _vertices.size()) {
            throw new Exception("Each Vertex must have a color.");
        }
        
        if(_texCoords.size() > 0 && _texCoords.size() != _vertices.size()) {
            throw new Exception("Each Vertex must have a texCoord.");
        }
        
        // save bounds for later
        this._minBounds = _vertices.getMinBounds();
        this._maxBounds = _vertices.getMaxBounds();
        
        
        // flush all buffers to the primitive
        this._indices.build(_geoWriter, _meshPrimitive);
        _vertices.build(_geoWriter, _meshPrimitive);
        _texCoords.build(_geoWriter, _meshPrimitive);
        _normals.build(_geoWriter, _meshPrimitive);
        _tangents.build(_geoWriter, _meshPrimitive);
        _colors.build(_geoWriter, _meshPrimitive);
    }
}
