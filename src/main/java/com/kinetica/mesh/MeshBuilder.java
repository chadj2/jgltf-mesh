/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh;

import java.awt.Color;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate 3D meshes for glTF based on 2D grid arrays
 * @author Chad Juliano
 */
public class MeshBuilder extends TriangleBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(MeshBuilder.class);
    
    public MeshBuilder(String _name) {
        super(_name);
    }
    
    /**
     * Create a an elevated surface from a 2D array.
     * @param _meshGrid 2D array containing vertices
     * @param _isTextured Indicates if this mesh will have a texture
     */
    public void addPlane(MeshVertex[][] _meshGrid, boolean _isTextured) {
        addGrid(_meshGrid, _isTextured, false, false);
    }
    
    /**
     * Join the ends of a 2D surface along the y-axis to create a cylindrical shape as if 
     * cut from a lathe.
     * @param _meshGrid 2D array containing vertices
     * @param _isTextured Indicates if this mesh will have a texture
     */
    public void addLathe(MeshVertex[][] _meshGrid, boolean _isTextured) {
        addGrid(_meshGrid, _isTextured, true, false);
    }
    
    /**
     * Join the ends of a 2D surface along the x-axis and y-axis to create a closed manifold.
     * @param _meshGrid 2D array containing vertices
     * @param _isTextured Indicates if this mesh will have a texture
     */
    public void addManifold(MeshVertex[][] _meshGrid, boolean _isTextured) {
        addGrid(_meshGrid, _isTextured, true, true);
    }

    /**
     * Convert a 2D vertex array into a mesh. If null vertex values are encountered then related 
     * parts of the mesh will not be generated.
     * @param _meshGrid 2D Vertex array that represents the mesh.
     * @param _isTextured Indicates if texture coordinates should be generated.
     * @param _wrapY Wrap the mesh about the Y axis.
     * @param _wrapX Wrap the mesh about the X axis.
     */
    public void addGrid(MeshVertex[][] _meshGrid, boolean _isTextured, boolean _wrapY, 
            boolean _wrapX) {

        int _xGridSize = _meshGrid.length;
        int _yGridSize = _meshGrid[0].length;
        
        LOG.debug("Render grid: mesh=<{}> grid=<{}x{}>, wrapXY<{},{}> isTextured=<{}>", 
                this.getName(), _xGridSize, _yGridSize, _wrapX, _wrapY, _isTextured);
        
        // generate geometry and add normals for non-textured manifold.
        renderMesh(_meshGrid, _wrapY, _wrapX);

        if(_isTextured) {
            // At this point the mesh is complete with correct normals except that we have no
            // texture coordinates.
            // If the mesh is textured and wrapped then we have a problem where we need to extend 
            // it so that there are separate points for the start and end vertices. If there is no 
            // wrapping then the extra points are not necessary.
            //
            // Note: It took a lot of time to determine the correct approach capable of handling 
            // a manifold that can be textured and wrapped on either axis independently. Many 
            // alternatives were tried before arriving at this relatively simple and correct method.
            
            // here we clear the vertices while preserving the normals. We want to keep the normals
            // and regenerate the grid.
            this._indices.clear();
            

            // create a new grid and extend it by a row or column if it is wrapped.
            int _xTexSize = _xGridSize;
            if(_wrapX) {
                _xTexSize += 1;
            }
            
            int _yTexSize = _yGridSize;
            if(_wrapY) {
                _yTexSize += 1;
            }
            
            MeshVertex[][] _texGrid = new MeshVertex[_xTexSize][_yTexSize];
            
            // populate the new grid
            for(int _xGridIdx = 0; _xGridIdx < _xTexSize; _xGridIdx++) {
                final float _uPos = interpFloat(_xTexSize - 1, 1, _xGridIdx);
                
                for(int _yGridIdx = 0; _yGridIdx < _yTexSize; _yGridIdx++) {
                    MeshVertex _vertex;
                    if(_xGridIdx >= _xGridSize || _yGridIdx >= _yGridSize) {
                        // We are in the expanded zone so wrap back to the beginning if necessary.
                        int _xIdxWrap = _xGridIdx;
                        if(_xIdxWrap >= _xGridSize) {
                            _xIdxWrap = 0;
                        }
                        
                        int _yIdxWrap = _yGridIdx;
                        if(_yIdxWrap >= _yGridSize) {
                            _yIdxWrap = 0;
                        }
                        
                        // We copy the vertex because start and end points should overlap.
                        _vertex = this.copyVertex(_meshGrid[_xIdxWrap][_yIdxWrap]);
                    }
                    else {
                        // if not in the expanded zone then use vertex from the original grid.
                        _vertex = _meshGrid[_xGridIdx][_yGridIdx];
                    }
                    
                    if(_vertex == null) {
                        // empty point is no rendered
                        continue;
                    }

                    final float _vPos = interpFloat(_yTexSize - 1, 1, _yGridIdx);
                    _vertex.setTexCoord(new Point2f(_uPos, _vPos));
                    
                    // Assign the vertex to the texture grid.
                    _texGrid[_xGridIdx][_yGridIdx] = _vertex;
                }
            }
            
            // Suppress generation of normals because we want to use the normals from the original
            // grid. We render the mesh with no wrapping because the start and end points overlap.
            this.supressNormals(true);
            renderMesh(_texGrid, false, false);
            this.supressNormals(false);
        }
    }

    /**
     * Iterate through the provided grid generating squares where possible.
     * @param _meshGrid
     * @param _wrapY
     * @param _wrapX
     */
    private void renderMesh(MeshVertex[][] _meshGrid, boolean _wrapY, boolean _wrapX) {
        final int _xGridSize = _meshGrid.length;
        final int _yGridSize = _meshGrid[0].length;
        
        // For every 4-vertex square in the mesh we call addSquare(). These squares overlap
        // which is necessary for the calculation of normals and tangents.
        for(int _xGridIdx = _wrapX ? 0 : 1; _xGridIdx < _xGridSize; _xGridIdx++) {
            for(int _yGridIdx = _wrapY ? 0 : 1; _yGridIdx < _yGridSize; _yGridIdx++) {

                // wrap around the y axis
                int _yGridPrev = _yGridIdx - 1;
                if(_yGridPrev < 0) {
                    _yGridPrev = _yGridSize - 1;
                }

                // wrap around the X axis
                int _xGridPrev = _xGridIdx - 1;
                if(_xGridPrev < 0) {
                    _xGridPrev = _xGridSize - 1;
                }
                
                final MeshVertex _vtx10 = _meshGrid[_xGridPrev][_yGridIdx];
                final MeshVertex _vtx11 = _meshGrid[_xGridPrev][_yGridPrev];
                final MeshVertex _vtx00 = _meshGrid[_xGridIdx][_yGridIdx];
                final MeshVertex _vtx01 = _meshGrid[_xGridIdx][_yGridPrev];
                
                // check if any of the vertices are out of bounds
                if(_vtx00 == null || _vtx01 == null || _vtx11 == null || _vtx10 == null) {
                    continue;
                }
                this.addSquare(_vtx11, _vtx10, _vtx01, _vtx00);
            }
        }
    }

    /**
     * Add a 3D cylinder oriented in XZ.
     * @param _position Base of the cylinder
     * @param _radius Cylinder radius
     * @param _height Cylinder height
     * @param _sides Number of vertices for the sides
     * @param _color Cylinder color
     */
    public void addCylinderMeshXZ(Point3f _position, float _radius, float _height, int _sides, 
            Color _color) {
        Point3f _bottomPos = new Point3f(_position);
        _bottomPos.sub(new Point3f(0f, _height, 0f));
        
        // add cylinder
        MeshVertex[][] _cylinderGrid = new MeshVertex[2][];
        _cylinderGrid[0] = this.addCircleVerticesXZ(_position, _radius, _sides, _color);
        _cylinderGrid[1] = this.addCircleVerticesXZ(_bottomPos, _radius, _sides, _color);
        this.addLathe(_cylinderGrid, false);
        
        // add top and bottom
        addDiscXZ(_position, _radius, _sides, _color);
        addDiscXZ(_bottomPos, -1*_radius, _sides, _color);
    }
    
    /**
     * Add a solid disc oriented in XZ.
     * @param _position Center of the disc
     * @param _radius Disc radius
     * @param _sides Number of vertices for the sides
     * @param _color Disc color
     */
    public void addDiscXZ(Point3f _position, float _radius, int _sides, Color _color) {
        // add center point
        MeshVertex _centerVtx = this.newVertex(_position);
        _centerVtx.setColor(_color);
        
        // create vertices for the boundary
        MeshVertex[] _discVertices = this.addCircleVerticesXZ(_position, _radius, _sides, _color);
        
        // add triangles to fill
        for(int _rIdx = 1; _rIdx < _sides; _rIdx++) {
            MeshVertex _curVtx = _discVertices[_rIdx];
            MeshVertex _lastVtx = _discVertices[_rIdx - 1];
            this.addTriangle(_curVtx, _lastVtx, _centerVtx);
        }
        this.addTriangle(_discVertices[0], _discVertices[_sides-1], _centerVtx);
    }
    
    /**
     * Generate a vertex array for a circle oriented in XZ.
     * @param _position Location of the center of the circle
     * @param _radius Radius of the circle
     * @param _sides Number of sides
     * @param _color Color of the vertices
     */
    public MeshVertex[] addCircleVerticesXZ(Point3f _position, float _radius, int _sides, 
            Color _color) {
        MeshVertex[] _result = new MeshVertex[_sides];
        
        float _flip = 1f;
        float _finalRadius = _radius;
        
        if(_radius < 0) {
            // flip orientation for triangle culling
            _flip *= -1f;
            _finalRadius *= -1f;
        }
        
        // draw a radial section
        for(int _rIdx = 0; _rIdx < _sides; _rIdx++) {
            final double _angle = 2*Math.PI*_rIdx*_flip/_sides;
            final float _xPos = (float)(Math.sin(_angle)*_finalRadius + _position.x);
            final float _yPos = (float)(Math.cos(_angle)*_finalRadius + _position.z);

            final MeshVertex _vertex = this.newVertex(new Point3f(_xPos, _position.y, _yPos));
            _result[_rIdx] = _vertex;
            _vertex.setColor(_color);
        }
        return _result;
    }
    
    /**
     * Helper function for interpolation between bounds returning a float.
     * @param _max Maximum bound
     * @param _part Number of parts
     * @param _idx Part index
     * @return float representing position given by the index.
     */
    public static float interpFloat(double _max, double _part, double _idx) {
        return (float)(_idx*_part/_max);
    }

    /**
     * Helper function for interpolation between bounds returning an int.
     * @param _max Maximum bound
     * @param _part Number of parts
     * @param _idx Part index
     * @return rounded integer representing position given by the index.
     */
    public static int interpInt(float _max, float _part, float _idx) {
        return Math.round(_idx*_part/_max);
    }
    
}