/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

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
            this._indicesList.clear();
            
            MeshVertex[][] _texGrid = createTexGrid(_meshGrid, _wrapX, _wrapY);
            
            // Suppress generation of normals because we want to use the normals from the original
            // grid. We render the mesh with no wrapping because the start and end points overlap.
            this.supressNormals(true);
            renderMesh(_texGrid, false, false);
            this.supressNormals(false);
        }
    }

    /**
     * If the mesh is textured and wrapped then we have a problem where we need to extend 
     * it so that there are separate points for the start and end vertices. If there is no 
     * wrapping then the extra points are not necessary.
     * @param _meshGrid
     * @param _xGridSize
     * @param _yGridSize
     * @param _wrapX
     * @param _wrapY
     * @return
     */
    private MeshVertex[][] createTexGrid(MeshVertex[][] _meshGrid, boolean _wrapX, boolean _wrapY) {
        int _xGridSize = _meshGrid.length;
        int _yGridSize = _meshGrid[0].length;
        
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
        return _texGrid;
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
                this.addSquare(_vtx11, _vtx10, _vtx01, _vtx00);
            }
        }
    }

    /**
     * Add a 3D cylinder oriented in XZ.
     * @param _bottomPos Base of the cylinder
     * @param _radius Cylinder radius
     * @param _height Cylinder height
     * @param _sides Number of vertices for the sides
     * @param _color Cylinder color
     * @throws Exception 
     */
    public void addCylinderMeshXZ(Point3f _bottomPos, float _radius, float _height, 
            int _sides, Color _color) throws Exception {
        LOG.debug("addCylinderMeshXZ: pos<{}>", _bottomPos, _radius);
        
        Point3f _topPos = new Point3f(_bottomPos);
        _topPos.add(new Point3f(0f, _height, 0f));
        
        // add cylinder
        MeshVertex[][] _cylinderGrid = new MeshVertex[2][];
        _cylinderGrid[1] = this.addCircleVerticesXZ(_topPos, _radius, _sides, _color);
        _cylinderGrid[0] = this.addCircleVerticesXZ(_bottomPos, _radius, _sides, _color);
        this.addLathe(_cylinderGrid, false);
        
        // add top and bottom
        addDiscXZ(_topPos, _radius, _sides, _color);
        addDiscXZ(_bottomPos, -1*_radius, _sides, _color);
    }
    
    /**
     * Add a solid disc oriented in XZ. Positive radius means plane is oriented up. 
     * Negative radius means plane is oriented down.
     * @param _position Center of the disc
     * @param _radius Disc radius
     * @param _sides Number of vertices for the sides
     * @param _color Disc color
     * @throws Exception 
     */
    public void addDiscXZ(Point3f _position, float _radius, int _sides, Color _color) throws Exception {
        LOG.debug("addDiscXZ: pos<{}> radius<{}>", _position, _radius);
        
        // add center point
        MeshVertex _centerVtx = this.newVertex(_position);
        _centerVtx.setColor(_color);
        
        // create vertices for the boundary
        MeshVertex[] _discVertices = this.addCircleVerticesXZ(_position, _radius, _sides, _color);
        
        // add triangles to fill
        for(int _rIdx = 1; _rIdx < _sides; _rIdx++) {
            MeshVertex _curVtx = _discVertices[_rIdx];
            MeshVertex _lastVtx = _discVertices[_rIdx - 1];
            this.addTriangle(_lastVtx, _curVtx, _centerVtx);
        }
        this.addTriangle(_discVertices[_sides-1], _discVertices[0], _centerVtx);
    }
    
    /**
     * Generate a vertex array for a circle oriented in XZ.
     * @param _position Location of the center of the circle
     * @param _radius Radius of the circle
     * @param _sides Number of sides
     * @param _color Color of the vertices
     * @return 
     * @throws Exception 
     */
    public MeshVertex[] addCircleVerticesXZ(Point3f _position, float _radius, int _sides, 
            Color _color) throws Exception {
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
    
    private final static Point3f ORIGIN_POINT = new Point3f(0f, 0f, 0f);
    
    /**
     * Add a pipe with given points and colors. Radius is in transformed coordinate system.
     * @param pointList
     * @param colorList
     * @param radius
     * @param sides
     * @throws Exception 
     */
    public void addPipe(List<Point3f> pointList, List<Color> colorList, float radius, int sides) throws Exception {
        List<Point3f> tPointList = new ArrayList<>();

        // save the original transform
        Matrix4f origM4 = new Matrix4f(this.getTransform());
        
        // transform each of the input points.
        for(Point3f point : pointList) {
            Point3f tPoint = new Point3f(point);
            origM4.transform(tPoint);
            tPointList.add(tPoint);
        }
        
        // Create the mesh that will contain the ring segments this is done in the destination coordinate 
        // system since input points are already transformed. 
        final MeshVertex[][] meshGrid = new MeshVertex[pointList.size()][];
        
        for(int idx = 0; idx < pointList.size(); idx++) {
            // get previous, current, and next points 
            Point3f prevPoint = null;
            Point3f currentPoint = tPointList.get(idx);
            Point3f nextPoint = null;
            
            if(idx > 0) {
                prevPoint = tPointList.get(idx - 1);
            }
            
            if(idx < (pointList.size() - 1)) {
                nextPoint = tPointList.get(idx + 1);
            }

            // create a rotation matrix based on the axis
            Vector3f axisVec = getPipeAxis(prevPoint, currentPoint, nextPoint);
            //LOG.info("axisVec: {}", axisVec);
            Matrix3f rotation3f = rotationFromY(axisVec);
            
            // create a matrix for the rotation and translation.
            Matrix4f rtsMatrix = new Matrix4f(rotation3f, new Vector3f(currentPoint), 1f);

            // We set the transformation so the ring segment will be in the correct location
            setTransform(rtsMatrix);
            
            Color color = colorList.get(idx);
            
            meshGrid[idx] = addCircleVerticesXZ(ORIGIN_POINT, radius, sides, color);
            
            // add caps to start and end
            if(idx == 0) {
                // cap the start
                addDiscXZ(ORIGIN_POINT, -radius, sides, color);
            }
            else if(idx == (tPointList.size() - 1)) {
                // cap the end
                addDiscXZ(ORIGIN_POINT, radius, sides, color);
            }
        }
        
        // restore original transform
        setTransform(origM4);
        
        // build the mesh
        addLathe(meshGrid, false);
    }
    
    /**
     * Get the axis or direction of the pipe.
     * @param prevPoint
     * @param curPoint
     * @param nextPoint
     * @return
     * @throws Exception
     */
    private static Vector3f getPipeAxis(Point3f prevPoint, Point3f curPoint, Point3f nextPoint) throws Exception {
        // average available segments
        Vector3f axisVec = new Vector3f();
        int segCount = 0;
        
        // prev-current segment
        if(prevPoint != null) {
            Vector3f seg = new Vector3f();
            seg.sub(curPoint, prevPoint);
            seg.normalize();
            axisVec.add(seg);
            segCount++;
        }
        
        // current-next segment
        if(nextPoint != null) {
            Vector3f seg = new Vector3f();
            seg.sub(nextPoint, curPoint);
            seg.normalize();
            axisVec.add(seg);
            segCount++;
        }
        
        // divide by number of segments
        axisVec.scale(1f/(float)segCount);
        
        if(Float.isNaN(axisVec.x) || Float.isNaN(axisVec.y) || Float.isNaN(axisVec.z)) {
            throw new Exception("Unable to calculate transform");
        }
        
        return axisVec;
    }
    
    //private final static Vector3f Y_UNIT_VECTOR = new Vector3f(0f, 1f, 0f);
    
    //private final static Vector3f X_UNIT_VECTOR = new Vector3f(1f, 0f, 0f);
    
    private final static Vector3f Z_UNIT_VECTOR = new Vector3f(0f, 0f, 1f);
    
    /**
     * Create a 3D transform in the direction of the axis.
     * @param axisVec
     * @return
     * @throws Exception 
     */
    public static Matrix3f rotationFromY(Vector3f axisVec) throws Exception {
        Vector3f zVec = new Vector3f();
        Vector3f yVec = new Vector3f(axisVec);
        Vector3f xVec = new Vector3f();
        
        // create zVec perpendicular to axisVec
        zVec.cross(axisVec, Z_UNIT_VECTOR);
        
        if(zVec.length() == 0f) {
            throw new Exception("Unable to calculate rotation transform for axis: " + axisVec.toString());
        }
        
        // create xVec perpendicular to zVec
        xVec.cross(axisVec, zVec);
        
        xVec.normalize();
        yVec.normalize();
        zVec.normalize();

        Matrix3f rotation = new Matrix3f();
        rotation.setColumn(0, xVec);
        rotation.setColumn(1, yVec);
        rotation.setColumn(2, zVec);
        
        return rotation;
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