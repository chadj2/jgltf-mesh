/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.buffer.BufferFloat3;
import io.github.chadj2.mesh.buffer.TriangleIndices;
import io.github.chadj2.mesh.buffer.VertexColors;

/**
 * Base class for constructing glTF Mesh geometry.
 * @author Chad Juliano
 */
public class TopologyBuilder extends BaseBuilder {

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
    
    /** List of vertices being added to this mesh */
    protected final List<MeshVertex> _vertexList = new ArrayList<MeshVertex>();
    
    /** Topology mode for MeshPrimitive. This indicates the type of data that will be output by the builder
     * and it can't be altered at runtime. */
    private final TopologyMode _topologyMode;

    /** minimum bounds of the vertices. */
    protected Tuple3f _minBounds;
    
    /** maximum bounds of the vertices. */
    protected Tuple3f _maxBounds;
    
    /**
     * @param _name Name of the mesh that will be populated in the glTF.
     * @param _topologyMode Indicates how buffers are interpreted by glTF.
     */
    public TopologyBuilder(String _name, TopologyMode _topologyMode) {
        super(_name);
        this._topologyMode = _topologyMode;
    }
    
    /**
     * Returns true if no triangles have been added.
     */
    public boolean isEmpty() { return this._vertexList.size() == 0; }

    /**
     * Clear out any added geometry.
     */
    public void clear() { this._vertexList.clear(); }
    
    
    /**
     * Get the minimum bounds of all vertices. Should only be called after build().
     */
    public Tuple3f getMinBounds() { return this._minBounds; }
    
    /**
     * Get the maximum bounds of all vertices. Should only be called after build().
     */
    public Tuple3f getMaxBounds() { return this._maxBounds; }


    /**
     * Create a new vertex and apply the current offset and scale. This vertex will be assigned
     * an unique index that will be referenced when adding squares or triangles.
     * @param _vertex 3D location of this vertex.
     * @throws Exception 
     */
    public MeshVertex newVertex(Tuple3f _vertex) throws Exception {
        Point3f _newVertex = new Point3f(_vertex);

        if(Float.isNaN(_vertex.x) || Float.isNaN(_vertex.y) || Float.isNaN(_vertex.z)) {
            throw new Exception("Can't add vertex with NaN: " + _vertex.toString());
        }
        
        // apply offset and scale
        getTransform().transform(_newVertex);
        
        if(Float.isNaN(_newVertex.x) || Float.isNaN(_newVertex.y) || Float.isNaN(_newVertex.z)) {
            throw new Exception("Transformed vertex has NaN: " + _newVertex.toString());
        }
        
        MeshVertex _meshVertex = new MeshVertex(this._vertexList.size(), _newVertex);
        
        if(this._vertexList.size() >= TriangleIndices.MAX_INDEX) {
            String msg = String.format("Trangle idex cannot exceed %d", TriangleIndices.MAX_INDEX);
            throw new Exception(msg);
        }
        
        this._vertexList.add(_meshVertex);
        return _meshVertex;
    }
    
    /**
     * Make a distinct copy of the vertex.
     * @see #newVertex
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
     * Serialize the MeshVertex list and indices to buffers.
     * This method should be called when all shapes have added.
     * @param _geoWriter Instance of writer class.
     * @return Node containing the mesh.
     */
    public Node build(GltfWriter _geoWriter) throws Exception {
        int _meshIdx = buildMesh(_geoWriter);
        
        Node _node = new Node();
        _node.setMesh(_meshIdx);
        _node.setName(this.getName() + "-node");
        
        _geoWriter.addNode(_node);
        return _node;
    }
    
    /**
     * Build a Mesh and return its index.
     * @param _geoWriter
     * @return
     * @throws Exception
     */
    public int buildMesh(GltfWriter _geoWriter) throws Exception {
        MeshPrimitive _meshPrimitive = new MeshPrimitive();
        _meshPrimitive.setMode(this._topologyMode.ordinal());

        Mesh _mesh = new Mesh();
        _geoWriter.getGltf().addMeshes(_mesh);
        int _meshIdx = _geoWriter.getGltf().getMeshes().indexOf(_mesh);
        
        //this._name = String.format("%s", this.getName());
        _mesh.setName(this.getName() + "-mesh");
        _mesh.addPrimitives(_meshPrimitive);

        buildBuffers(_geoWriter, _meshPrimitive);

        LOG.debug("New Mesh[{}]: idx=<{}>", _mesh.getName(), _meshIdx);
        this.clear();
        
        return _meshIdx;
    }
    
    protected BufferFloat3 _vertices = null;
    
    /**
     * Generate primitive lists from the MeshVertex list and serialize to buffers.
     * @param _geoWriter Instance of writer class.
     * @param _meshPrimitive The glTF section containing serialized buffers.
     */
    protected void buildBuffers(GltfWriter _geoWriter, MeshPrimitive _meshPrimitive) throws Exception {
        if(this._vertexList.size() == 0) {
            throw new Exception("No vertices to build!");
        }
        
        this._vertices = new BufferFloat3(this.getName(), "POSITION");
        VertexColors _colors = new VertexColors(this.getName());

        for(MeshVertex _meshVertex : this._vertexList) {
            this._vertices.add(_meshVertex.getVertex());
            
            Color _color = _meshVertex.getColor();
            if(_color != null) {
                _colors.add(_color);
            }
        }
        
        if(_colors.size() > 0 && _colors.size() != this._vertexList.size()) {
            throw new Exception("Each Vertex must have a color");
        }
        
        // save bounds for later
        this._minBounds = this._vertices.getMin();
        this._maxBounds = this._vertices.getMax();
        this._vertices.build(_geoWriter, _meshPrimitive);
        _colors.build(_geoWriter, _meshPrimitive);
    }
}
