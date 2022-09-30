/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.demo;

import java.io.File;

import javax.vecmath.Point3f;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Material;
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.MeshGltfWriter.AlphaMode;
import io.github.chadj2.mesh.MeshBuilder;
import io.github.chadj2.mesh.MeshVertex;
import io.github.chadj2.mesh.noise.NoiseGenerator;

public class TestTerrainNoise {

    private final static Logger LOG = LoggerFactory.getLogger(TestShapeModels.class);
    private final static String TEST_TEXTURE_PNG = "gltf_logo.png";
    
    private final MeshGltfWriter _geoWriter = new MeshGltfWriter();
    
    @Before
    public void setup() {
        // set the path where the texture files will be found
        this._geoWriter.setBasePath(new File("src/test/resources"));
    }
    
    /**
     * Generate terrain with Perlin Noise
     * @see io.github.chadj2.mesh.noise.NoiseGenerator.Perlin
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
     * @see io.github.chadj2.mesh.noise.NoiseGenerator.OpenSimplex
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
        final Material _material = this._geoWriter.newTextureMaterial(TEST_TEXTURE_PNG);
        _meshBuilder.setMaterial(_material);
        
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
        _meshBuilder.build(this._geoWriter);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
}
