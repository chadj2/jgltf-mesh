/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.demo;

import java.io.File;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kinetica.mesh.GltfWriter;
import com.kinetica.mesh.MeshBuilder;
import com.kinetica.mesh.MeshVertex;

import de.javagl.jgltf.impl.v2.Material;

public class TestCubeModel {
    
    private final static Logger LOG = LoggerFactory.getLogger(TestCubeModel.class);

    /**
     * Generate a cube with different textures on the faces.
     * @throws Exception
     */
    @Test
    public void testBox() throws Exception {
        
        GltfWriter _geoWriter = new GltfWriter();
        
        // set the path used for finding textures
        _geoWriter.setBasePath(new File("src/test/resources"));
        
        // create materials for each of the textures
        Material _kineticaMaterial = _geoWriter.addTextureMaterial("kinetica_logo.png");
        Material _gltfMaterial = _geoWriter.addTextureMaterial("gltf_logo.png");
        Material _uvGridMaterial = _geoWriter.addTextureMaterial("uv_grid_512.png");
        
        MeshBuilder _meshBuilder = null;
        
        // face XY1
        _meshBuilder = new MeshBuilder("face-XY1");
        MeshVertex _vertexXY1_0 = _meshBuilder.newVertex(new Point3f(0, 0, 0));
        MeshVertex _vertexXY1_1 = _meshBuilder.newVertex(new Point3f(1, 0, 0));
        MeshVertex _vertexXY1_2 = _meshBuilder.newVertex(new Point3f(0, 1, 0));
        MeshVertex _vertexXY1_3 = _meshBuilder.newVertex(new Point3f(1, 1, 0));
        _meshBuilder.addSquare(_vertexXY1_1, _vertexXY1_0, _vertexXY1_3, _vertexXY1_2);
        _vertexXY1_0.setTexCoord(new Point2f(1, 1));
        _vertexXY1_1.setTexCoord(new Point2f(0, 1));
        _vertexXY1_2.setTexCoord(new Point2f(1, 0));
        _vertexXY1_3.setTexCoord(new Point2f(0, 0));
        _meshBuilder.build(_geoWriter, _kineticaMaterial);

        // face XY2
        _meshBuilder = new MeshBuilder("face-XY2");
        MeshVertex _vertexXY2_0 = _meshBuilder.newVertex(new Point3f(1, 0, -1));
        MeshVertex _vertexXY2_1 = _meshBuilder.newVertex(new Point3f(0, 0, -1));
        MeshVertex _vertexXY2_2 = _meshBuilder.newVertex(new Point3f(1, 1, -1));
        MeshVertex _vertexXY2_3 = _meshBuilder.newVertex(new Point3f(0, 1, -1));
        _meshBuilder.addSquare(_vertexXY2_1, _vertexXY2_0, _vertexXY2_3, _vertexXY2_2);
        _vertexXY2_0.setTexCoord(new Point2f(1, 1));
        _vertexXY2_1.setTexCoord(new Point2f(0, 1));
        _vertexXY2_2.setTexCoord(new Point2f(1, 0));
        _vertexXY2_3.setTexCoord(new Point2f(0, 0));
        _meshBuilder.build(_geoWriter, _kineticaMaterial);
        
        // face YZ1
        _meshBuilder = new MeshBuilder("face-YZ1");
        MeshVertex _vertexYZ1_0 = _meshBuilder.newVertex(new Point3f(0, 0, -1));
        MeshVertex _vertexYZ1_1 = _meshBuilder.newVertex(new Point3f(0, 0, 0));
        MeshVertex _vertexYZ1_2 = _meshBuilder.newVertex(new Point3f(0, 1, -1));
        MeshVertex _vertexYZ1_3 = _meshBuilder.newVertex(new Point3f(0, 1, 0));
        _meshBuilder.addSquare(_vertexYZ1_1, _vertexYZ1_0, _vertexYZ1_3, _vertexYZ1_2);
        _vertexYZ1_0.setTexCoord(new Point2f(1, 1));
        _vertexYZ1_1.setTexCoord(new Point2f(0, 1));
        _vertexYZ1_2.setTexCoord(new Point2f(1, 0));
        _vertexYZ1_3.setTexCoord(new Point2f(0, 0));
        _meshBuilder.build(_geoWriter, _gltfMaterial);
        
        // face YZ2
        _meshBuilder = new MeshBuilder("face-YZ2");
        MeshVertex _vertexYZ2_0 = _meshBuilder.newVertex(new Point3f(1, 0, 0));
        MeshVertex _vertexYZ2_1 = _meshBuilder.newVertex(new Point3f(1, 0, -1));
        MeshVertex _vertexYZ2_2 = _meshBuilder.newVertex(new Point3f(1, 1, 0));
        MeshVertex _vertexYZ2_3 = _meshBuilder.newVertex(new Point3f(1, 1, -1));
        _meshBuilder.addSquare(_vertexYZ2_1, _vertexYZ2_0, _vertexYZ2_3, _vertexYZ2_2);
        _vertexYZ2_0.setTexCoord(new Point2f(1, 1));
        _vertexYZ2_1.setTexCoord(new Point2f(0, 1));
        _vertexYZ2_2.setTexCoord(new Point2f(1, 0));
        _vertexYZ2_3.setTexCoord(new Point2f(0, 0));
        _meshBuilder.build(_geoWriter, _gltfMaterial);
        
        // face top
        _meshBuilder = new MeshBuilder("face-Top");
        MeshVertex _vertexTop_0 = _meshBuilder.newVertex(new Point3f(0, 1, 0));
        MeshVertex _vertexTop_1 = _meshBuilder.newVertex(new Point3f(1, 1, 0));
        MeshVertex _vertexTop_2 = _meshBuilder.newVertex(new Point3f(0, 1, -1));
        MeshVertex _vertexTop_3 = _meshBuilder.newVertex(new Point3f(1, 1, -1));
        _meshBuilder.addSquare(_vertexTop_1, _vertexTop_0, _vertexTop_3, _vertexTop_2);
        _vertexTop_0.setTexCoord(new Point2f(1, 1));
        _vertexTop_1.setTexCoord(new Point2f(0, 1));
        _vertexTop_2.setTexCoord(new Point2f(1, 0));
        _vertexTop_3.setTexCoord(new Point2f(0, 0));
        _meshBuilder.build(_geoWriter, _uvGridMaterial);
        
        // face bottom
        _meshBuilder = new MeshBuilder("face-Bottom");
        MeshVertex _vertexBottom_0 = _meshBuilder.newVertex(new Point3f(0, 0, 0));
        MeshVertex _vertexBottom_1 = _meshBuilder.newVertex(new Point3f(0, 0, -1));
        MeshVertex _vertexBottom_2 = _meshBuilder.newVertex(new Point3f(1, 0, 0));
        MeshVertex _vertexBottom_3 = _meshBuilder.newVertex(new Point3f(1, 0, -1));
        _meshBuilder.addSquare(_vertexBottom_1, _vertexBottom_0, _vertexBottom_3, _vertexBottom_2);
        _vertexBottom_0.setTexCoord(new Point2f(1, 1));
        _vertexBottom_1.setTexCoord(new Point2f(0, 1));
        _vertexBottom_2.setTexCoord(new Point2f(1, 0));
        _vertexBottom_3.setTexCoord(new Point2f(0, 0));
        _meshBuilder.build(_geoWriter, _uvGridMaterial);

        File _outFile = TestShapeModels.getFile("test_cube");
        _geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
}
