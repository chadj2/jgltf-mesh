/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.demo;

import java.awt.Color;
import java.io.File;
import java.nio.file.Paths;

import javax.vecmath.Point3f;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kinetica.mesh.GltfWriter;
import com.kinetica.mesh.GltfWriter.AlphaMode;
import com.kinetica.mesh.MeshBuilder;
import com.kinetica.mesh.MeshVertex;

import de.javagl.jgltf.impl.v2.Material;

public class TestShapeModels {

    private final static Logger LOG = LoggerFactory.getLogger(TestShapeModels.class);
    
    private final static String TEST_TEXTURE_PNG = "uv_grid_512.png";
    private final static String OUT_PATH = "./demo";
    
    private final GltfWriter _geoWriter = new GltfWriter();
    
    @Before
    public void setup() {
        // set the path where the texture files will be found
        this._geoWriter.setBasePath(new File("src/test/resources"));
    }

    public static File getFile(String _name) {
        File _outFile = Paths.get(OUT_PATH, _name + ".gltf").toFile();
        _outFile.getParentFile().mkdirs();
        return _outFile;
    }
    
    /**
     * Create a plane with the function y = 2*x*exp(-(x^2 + y^2)) using addPlane().
     * @throws Exception
     */
    @Test 
    public void testPlane() throws Exception {
        // Set rendering for both sides of the plane
        this._geoWriter.setAlphaMode(AlphaMode.OPAQUE_DS);
        
        final MeshBuilder _meshBuilder = new MeshBuilder("test_plane");
        final Material _material = this._geoWriter.addTextureMaterial(TEST_TEXTURE_PNG);
        _meshBuilder.setMaterial(_material);
        
        // size of grid
        final int _length = 30;
        
        // size of coordinates
        final float _coordLength = 4f;
        
        // grid to hold mesh points
        final MeshVertex[][] _meshGrid = new MeshVertex[_length][_length];
        
        for(int _xIdx = 0; _xIdx < _length; _xIdx++) {
            // interpolate to within the range [-2,2]
            final float _xPos = MeshBuilder.interpFloat(_length, _coordLength, _xIdx) - _coordLength/2f;
            
            for(int _yIdx = 0; _yIdx < _length; _yIdx++) {
                // interpolate to within the range [-2,2]
                final float _zPos = MeshBuilder.interpFloat(_length, _coordLength, _yIdx) - _coordLength/2f;
                
                // calculate the function 2*x*exp(-(x^2 + y^2))
                final float _yPos = (float)(2*_xPos*Math.exp(-1*(_xPos*_xPos + _zPos*_zPos)));

                // add the point in the mesh
                Point3f _point = new Point3f(-1*_xPos, _yPos, _zPos);
                _meshGrid[_xIdx][_yIdx] = _meshBuilder.newVertex(_point);
            }
        }

        // render the vertices in the grid
        _meshBuilder.addPlane(_meshGrid, true);
        
        // build the gltf buffers
        _meshBuilder.build(this._geoWriter);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    /**
     * Generate a rainbow colored diamond shape using addLathe().
     * @throws Exception
     */
    @Test
    public void testDiamond() throws Exception {
        final MeshBuilder _meshBuilder = new MeshBuilder("test_diamond");
        Material _material = this._geoWriter.addDefaultMaterial();
        _meshBuilder.setMaterial(_material);

        // number of sides around the tube
        final int _sides = 12;
        
        // radiuses along the tube
        final float[] _radiusList = { 0.2f, 1f, 0.2f };
        
        // y positions for the tube.
        final float[] _yPosList = { 1f, 0f, -1f };
        final MeshVertex[][] _meshGrid = new MeshVertex[_yPosList.length][];
        
        for(int _yIdx = 0; _yIdx < _yPosList.length; _yIdx++) {
            final float _yPos = _yPosList[_yIdx];
            final float _radius = _radiusList[_yIdx];
            
            // origin of the circle
            final Point3f _circlePos = new Point3f(0, _yPos, 0);
            
            // create a rainbow effect along the y axis
            final Color _color = Color.getHSBColor((float)_yIdx/(float)_yPosList.length, 0.9f, 1.0f);
            
            // add a circle that is part of the tube
            _meshGrid[_yIdx] = _meshBuilder.addCircleVerticesXZ(_circlePos, _radius, _sides, _color);
        }
        
        // join the ends of the surface to create a tube
        _meshBuilder.addLathe(_meshGrid, false);

        // generate gltf buffers
        _meshBuilder.build(this._geoWriter);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }

    /**
     * Generate a textured helix with addLathe().
     * @throws Exception
     */
    @Test
    public void testHelix() throws Exception {
        // we create separate meshes for the textured helix and the ends
        // because we can't mix textured and non-textured.
        
        MeshBuilder _meshBuilder = new MeshBuilder("test_helix");
        Material _materialTexture = this._geoWriter.addTextureMaterial(TEST_TEXTURE_PNG);
        _meshBuilder.setMaterial(_materialTexture);
        
        MeshBuilder _meshBuilderEnds = new MeshBuilder("ends");
        Material _materialDefault = this._geoWriter.addDefaultMaterial();
        _meshBuilderEnds.setMaterial(_materialDefault);

        // height of the helix
        final float _ySize = 6f;
        
        // Number of rotations along the y-axis
        final int _yDivisions = 60;
        
        // radius of the helix rotation
        final float _helixRadius = 0.5f;
        
        // radius of the circle that constructs the helix
        final float _circleRadius = 1f;
        
        // number of levels of circles when building the grid
        final int _circleSides = 12;
        
        final MeshVertex[][] _meshGrid = new MeshVertex[_yDivisions][];
        
        for(int _yIdx = 0; _yIdx < _yDivisions; _yIdx++) {
            // Rotate 4pi radians from bottom to top
            final double _angle = MeshBuilder.interpFloat(_yDivisions, 4*Math.PI, _yIdx);
            final float _xPos = _helixRadius*(float)Math.cos(_angle);
            final float _zPos = _helixRadius*(float)Math.sin(_angle);
            
            // increase the height of the circles to build the helix
            final float _yPos = MeshBuilder.interpFloat(_yDivisions, _ySize, _yIdx);

            // position of the circle to draw
            final Point3f _circlePos = new Point3f(_xPos, -1*_yPos, _zPos);

            // add the circle that forms the helix
            _meshGrid[_yIdx] = _meshBuilder.addCircleVerticesXZ(_circlePos,  _circleRadius, _circleSides, null);
            
            // if this is the bottom then add a disc
            if(_yIdx == 0) {
                _meshBuilderEnds.addDiscXZ(_circlePos, _circleRadius, _circleSides, Color.RED);
            }
            
            // if this is the top then add a disc
            if(_yIdx == (_circleSides -1)) {
                _meshBuilderEnds.addDiscXZ(_circlePos, -1*_circleRadius, _circleSides, Color.GREEN);
            }
        }
        
        // join the ends of the surface to create a tube
        _meshBuilder.addLathe(_meshGrid, true);

        // generate gltf buffers for the cylindrical part
        _meshBuilder.build(this._geoWriter);

        // generate gltf buffers for the ends
        _meshBuilderEnds.build(this._geoWriter);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    /**
     * Generate a textured torus using addManifold().
     * @throws Exception
     */
    @Test 
    public void testTorus() throws Exception {
        MeshBuilder _meshBuilder = new MeshBuilder("test_torus");
        Material _material = this._geoWriter.addTextureMaterial(TEST_TEXTURE_PNG);
        _meshBuilder.setMaterial(_material);
        
        // sides of each circle in the vertical axis
        final int _sizesVertical = 24;
        
        // sides of each circle on the horizontal axis
        final int _sidesHorizontal = 48;
        
        // torus inner radius
        final double _innerRadius = 2d;
        
        // radius of the circles used to construct the torus
        final double _circleRadius = 2d;
        
        // grid to hold mesh vertices
        final MeshVertex[][] _meshGrid = new MeshVertex[_sizesVertical][];
        
        for(int _rIdx = 0; _rIdx < _sizesVertical; _rIdx++) {
            // Rotate about an angle from 0 to -2pi. 
            // If we rotate the other way then planes would render as inverted.
            final double _angle = MeshBuilder.interpFloat(_sizesVertical, -2*Math.PI, _rIdx) - Math.PI;
            
            // calculate points along a circle that rotates about the axis.
        	final double _radius = _circleRadius*Math.cos(_angle) + _circleRadius + _innerRadius;
        	final double _zPos = _circleRadius*Math.sin(_angle);
        	
        	// position of the circle to draw.
            final Point3f _circlePos = new Point3f(0, (float)_zPos, 0);
            
            // for each point on the first circle draw another circle perpendicular
            _meshGrid[_rIdx] = _meshBuilder.addCircleVerticesXZ(_circlePos, 
                    (float)_radius, _sidesHorizontal, null);
        }

        // join all corners of the surface to create a closed manifold.
        _meshBuilder.addManifold(_meshGrid, true);

        // generate gltf buffers
        _meshBuilder.build(this._geoWriter);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    @Test 
    public void testMinimalGrid() throws Exception {
        // Set rendering for both sides of the plane
        this._geoWriter.setAlphaMode(AlphaMode.OPAQUE_DS);
        
        final MeshBuilder _meshBuilder = new MeshBuilder("test_minimal_grid");
        final Material _material = this._geoWriter.addDefaultMaterial();
        _meshBuilder.setMaterial(_material);

        // size of grid
        final int _length = 3;

        // size of coordinates
        final float _coordLength = 4f;
        
        final MeshVertex[][] _meshGrid = new MeshVertex[_length][_length];
        
        // setup an flat white plane
        for(int _xIdx = 0; _xIdx < _length; _xIdx++) {
            final float _xPos = MeshBuilder.interpFloat(_length, _coordLength, _xIdx) - _coordLength/2f;
            
            for(int _yIdx = 0; _yIdx < _length; _yIdx++) {
                final float _zPos = MeshBuilder.interpFloat(_length, _coordLength, _yIdx) - _coordLength/2f;
                
                Point3f _point = new Point3f(-1*_xPos, 0, _zPos);
                MeshVertex _vertex = _meshBuilder.newVertex(_point);
                _meshGrid[_xIdx][_yIdx] = _vertex;
                _vertex.setColor(Color.WHITE);
            }
        }
        
        // add elevations at the edges and center.
        _meshGrid[0][1].getVertex().y = 0.25f;
        _meshGrid[1][0].getVertex().y = 0.25f;
        _meshGrid[1][2].getVertex().y = 0.25f;
        _meshGrid[2][1].getVertex().y = 0.25f;
        _meshGrid[1][1].getVertex().y = 1f;
        
        // disable normals
        _meshBuilder.supressNormals(false);
        
        // render the vertices in the grid
        _meshBuilder.addPlane(_meshGrid, true);

        // build the gltf buffers
        _meshBuilder.build(this._geoWriter);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
}

