/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.demo;

import java.io.File;

import javax.vecmath.Point3f;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kinetica.mesh.GltfWriter;
import com.kinetica.mesh.MeshBuilder;
import com.kinetica.mesh.MeshVertex;
import com.kinetica.mesh.GltfWriter.AlphaMode;
import com.kinetica.mesh.noise.OpenSimplexNoise;
import com.kinetica.mesh.noise.PerlinNoise;

import de.javagl.jgltf.impl.v2.Material;

public class TestTerrainNoise {

    private final static Logger LOG = LoggerFactory.getLogger(TestShapeModels.class);
    private final static String TEST_TEXTURE_PNG = "uv_grid_512.png";
    
    private final GltfWriter _geoWriter = new GltfWriter();
    
    @Before
    public void setup() {
        // set the path where the texture files will be found
        this._geoWriter.setBasePath(new File("src/test/resources"));
    }
    

    /**
     * Use the OpenSimplexNoise generator to create a terrain surface.
     * @throws Exception
     */
    @Test 
    public void testSimplexTerrain() throws Exception {

        final MeshBuilder _meshBuilder = new MeshBuilder("test_simplex");
        
        // length of a size
        final int _gridPoints = 100;
        
        final float _gridSize = 5f;
        
        // grid to hold mesh points
        final MeshVertex[][] _meshGrid = new MeshVertex[_gridPoints][_gridPoints];
        
        OpenSimplexNoise _noise = new OpenSimplexNoise(1);
        
        for(int _xIdx = 0; _xIdx < _gridPoints; _xIdx++) {
            // interpolate to within the range [-2,2]
            final float _xPos = MeshBuilder.interpFloat(_gridPoints, _gridSize, _xIdx) - _gridSize/2f;
            
            for(int _yIdx = 0; _yIdx < _gridPoints; _yIdx++) {
                // interpolate to within the range [-2,2]
                final float _zPos = MeshBuilder.interpFloat(_gridPoints, _gridSize, _yIdx) - _gridSize/2f;
                
                // get the terrain value
                final float _yPos = (float)_noise.eval(_xPos, _zPos);

                // add the point in the mesh
                Point3f _point = new Point3f(-1*_xPos, _yPos, _zPos);
                _meshGrid[_xIdx][_yIdx] = _meshBuilder.newVertex(_point);
            }
        }
        
        // render the vertices in the grid
        _meshBuilder.addPlane(_meshGrid, true);
        
        // Set rendering for both sides of the plane
        this._geoWriter.setAlphaMode(AlphaMode.OPAQUE_DS);

        // build the gltf buffers
        final Material _material = this._geoWriter.addTextureMaterial(TEST_TEXTURE_PNG);
        _meshBuilder.build(this._geoWriter, _material);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    @Test 
    public void testPerlinTerrain() throws Exception {

        final MeshBuilder _meshBuilder = new MeshBuilder("test_perlin");
        
        // length of a size
        final int _gridPoints = 100;
        
        final float _gridSize = 5f;
        
        // grid to hold mesh points
        final MeshVertex[][] _meshGrid = new MeshVertex[_gridPoints][_gridPoints];
        
        for(int _xIdx = 0; _xIdx < _gridPoints; _xIdx++) {
            // interpolate to within the range [-2,2]
            final float _xPos = MeshBuilder.interpFloat(_gridPoints, _gridSize, _xIdx) - _gridSize/2f;
            
            for(int _yIdx = 0; _yIdx < _gridPoints; _yIdx++) {
                // interpolate to within the range [-2,2]
                final float _zPos = MeshBuilder.interpFloat(_gridPoints, _gridSize, _yIdx) - _gridSize/2f;
                
                // get the terrain value
                final float _yPos = (float)PerlinNoise.noise(_xPos, _zPos, 0, 4, 0.3)*2f;

                // add the point in the mesh
                Point3f _point = new Point3f(-1*_xPos, _yPos, _zPos);
                _meshGrid[_xIdx][_yIdx] = _meshBuilder.newVertex(_point);
            }
        }
        
        // render the vertices in the grid
        _meshBuilder.addPlane(_meshGrid, true);
        
        // Set rendering for both sides of the plane
        this._geoWriter.setAlphaMode(AlphaMode.OPAQUE_DS);

        // build the gltf buffers
        final Material _material = this._geoWriter.addTextureMaterial(TEST_TEXTURE_PNG);
        _meshBuilder.build(this._geoWriter, _material);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
}
