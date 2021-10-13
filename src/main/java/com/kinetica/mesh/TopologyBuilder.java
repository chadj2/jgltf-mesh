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
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kinetica.mesh.buffer.VertexColors;
import com.kinetica.mesh.buffer.Vertices;

import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;

/**
 * Base class for constructing glTF Mesh geometry.
 * @author chadjuliano
 */
public class TopologyBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(TopologyBuilder.class);
    
    /**
     * Indicates the type of topology the data in this mesh represents.
     * @see <a href="https://www.khronos.org/registry/glTF/specs/2.0/glTF-2.0.html#_mesh_primitive_mode">
     * mesh.primitive.mode</a>
     */
    public enum TopologyMode { 
        POINTS,
        LINES,
        LINE_LOOP,
        LINE_STRIP,
        TRIANGLES,
        TRIANGLE_STRIP,
        TRIANGLE_FAN
    }
    
    /** Mesh name used in metadata descriptions */
    private final String _name;

    /** List of vertices being added to this mesh */
    protected final List<MeshVertex> _vertexList = new ArrayList<MeshVertex>();
    
    /** Transform scale and offset */
    private final Matrix4f _transform = new Matrix4f();
    
    /** Indicates if the X axis should be inverted. This is necessary to correct orientations for Cesium. */
    private static final int INVERT_X = -1;

    private Point3f _minBounds;
    private Point3f _maxBounds;
    
    /** Topology mode for MeshPrimitive. This indicates the type of data that will be output by the builder
     * and it can't be altered at runtime. */
    private final TopologyMode _topologyMode;
    
    public TopologyBuilder(String _name, TopologyMode _topologyMode) {
        this._name = _name;
        this._topologyMode = _topologyMode;
        setScale(new Vector3f(1,1,1));
    }
    
    /**
     * Returns true if no triangles have been added.
     */
    public boolean isEmpty() {
        return this._vertexList.size() == 0;
    }

    /**
     * Return the mesh name.
     */
    public String getName() {
        return this._name;
    }

    /**
     * Set the transform used for offset and scale
     */
    public void setTransform(Matrix4f _transform) {
        this._transform.set(_transform);
    }
    
    /**
     * Get the transformation matrix.
     */
    public Matrix4f getTransform() {
        return this._transform;
    }
    
    /**
     * Get the minimum bounds of all vertices. Should only be called after build().
     */
    public Point3f getMinBounds() {
        return this._minBounds;
    }
    
    /**
     * Get the maximum bounds of all vertices. Should only be called after build().
     */
    public Point3f getMaxBounds() {
        return this._maxBounds;
    }
    
    /**
     * Center all vertices about a point.
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
     */
    public void setScale(Vector3f _scale) {
        this._transform.m00 = _scale.x;
        this._transform.m11 = _scale.y;
        this._transform.m22 = _scale.z;
        
        // invert the X axis
        this._transform.m00 *= INVERT_X;
    }

    /**
     * Create a new vertex and apply the current offset and scale. This vertex will be assigned
     * an unique index that will be referenced when adding squares or triangles.
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
    
    /**
     * Make a distinct copy of the vertex.
     * @param _vertex
     * @return
     */
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
     * @param _geoWriter
     * @return
     * @throws Exception
     */
    public Node build(GltfWriter _geoWriter) throws Exception {
        MeshPrimitive _meshPrimitive = new MeshPrimitive();
        _meshPrimitive.setMode(this._topologyMode.ordinal());
        
        buildBuffers(_geoWriter, _meshPrimitive);

        Mesh _mesh = new Mesh();
        _geoWriter._gltf.addMeshes(_mesh);
        _mesh.setName(this.getName() + "-mesh");
        int _meshIdx = _geoWriter._gltf.getMeshes().indexOf(_mesh);
        LOG.debug("New Mesh[{}]: idx=<{}>", _mesh.getName(), _meshIdx);
        
        _mesh.addPrimitives(_meshPrimitive);
        
        Node _node = new Node();
        _geoWriter.addNode(_node);
        _node.setMesh(_meshIdx);
        _node.setName(this.getName() + "-node");

        return _node;
    }
    
    /**
     * Generate primitive lists from the MeshVertex list and serialize to buffers.
     * @param _geoWriter
     * @param _meshPrimitive
     * @throws Exception
     */
    protected void buildBuffers(GltfWriter _geoWriter, MeshPrimitive _meshPrimitive) throws Exception {
        if(this._vertexList.size() == 0) {
            throw new Exception("No vertices to build!");
        }
        
        Vertices _vertices = new Vertices(this.getName());
        VertexColors _colors = new VertexColors(this.getName());

        for(MeshVertex _meshVertex : this._vertexList) {
            _vertices.add(_meshVertex.getVertex());
            
            Color _color = _meshVertex.getColor();
            if(_color != null) {
                _colors.add(_color);
            }
        }
        
        if(_colors.size() > 0 && _colors.size() != this._vertexList.size()) {
            throw new Exception("Each Vertex must have a color");
        }
        
        // save bounds for later
        this._minBounds = _vertices.getMinBounds();
        this._maxBounds = _vertices.getMaxBounds();
        _vertices.build(_geoWriter, _meshPrimitive);
        _colors.build(_geoWriter, _meshPrimitive);
    }
}
