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
import com.kinetica.mesh.noise.NoiseGenerator;

import de.javagl.jgltf.impl.v2.Material;

public class TestTerrainNoise {

    private final static Logger LOG = LoggerFactory.getLogger(TestShapeModels.class);
    private final static String TEST_TEXTURE_PNG = "gltf_logo.png";
    
    private final GltfWriter _geoWriter = new GltfWriter();
    
    @Before
    public void setup() {
        // set the path where the texture files will be found
        this._geoWriter.setBasePath(new File("src/test/resources"));
    }
    
    /**
     * Generate terrain with Perlin Noise
     * @throws Exception
     */
    @Test 
    public void testPerlinTerrain() throws Exception {
        
        NoiseGenerator _noise = new NoiseGenerator.Perlin();
        _noise.setOctaves(3);
        _noise.setPersistence(0.4);
        
        final int _gridPoints = 100;
        final float _gridSize = 4f;
        
        createTerrain(_gridPoints, _gridSize, _noise, "test_perlin");
    }

    /**
     * Use the OpenSimplexNoise generator to create a terrain surface.
     * @throws Exception
     */
    @Test 
    public void testSimplexTerrain() throws Exception {
        
        NoiseGenerator _noise = new NoiseGenerator.OpenSimplex(999);
        _noise.setOctaves(3);
        _noise.setPersistence(0.4);
        
        final int _gridPoints = 100;
        final float _gridSize = 4f;
        
        createTerrain(_gridPoints, _gridSize, _noise, "test_simplex");
    }


    private void createTerrain(final int _gridPoints, final float _gridSize, NoiseGenerator _noise, String _name)
            throws Exception {
        final MeshBuilder _meshBuilder = new MeshBuilder(_name);
        
        // grid to hold mesh points
        final MeshVertex[][] _meshGrid = new MeshVertex[_gridPoints][_gridPoints];
        
        for(int _xIdx = 0; _xIdx < _gridPoints; _xIdx++) {
            // interpolate to within the range [-2,2]
            final float _xPos = MeshBuilder.interpFloat(_gridPoints, _gridSize, _xIdx) - _gridSize/2f;
            
            for(int _yIdx = 0; _yIdx < _gridPoints; _yIdx++) {
                // interpolate to within the range [-2,2]
                final float _zPos = MeshBuilder.interpFloat(_gridPoints, _gridSize, _yIdx) - _gridSize/2f;
                
                // get the terrain value
                final float _yPos = (float)_noise.getNoise(_xPos, _zPos);

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
