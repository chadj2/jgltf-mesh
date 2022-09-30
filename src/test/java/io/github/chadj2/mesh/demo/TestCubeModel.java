/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.demo;

import java.io.File;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Material;
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.MeshBuilder;
import io.github.chadj2.mesh.MeshVertex;
import io.github.chadj2.mesh.TriangleBuilder;

public class TestCubeModel {
    
    private final static Logger LOG = LoggerFactory.getLogger(TestCubeModel.class);
    
    /**
     * Add a cube made of 6 textured meshes.
     * @see MeshBuilder#addPlane
     */
    @Test
    public void testCube() throws Exception {
        MeshGltfWriter _geoWriter = new MeshGltfWriter();
        
        // set the path used for finding textures
        _geoWriter.setBasePath(new File("src/test/resources"));
        
        // create materials for each of the textures
        Material _kineticaMaterial = _geoWriter.newTextureMaterial("kinetica_logo.png");
        Material _gltfMaterial = _geoWriter.newTextureMaterial("gltf_logo.png");
        Material _uvGridMaterial = _geoWriter.newTextureMaterial("uv_grid_512.png");

        MeshBuilder _meshBuilder = null;
        MeshVertex[][] _meshGrid = null;
        
        _meshBuilder = new MeshBuilder("face-XY1");
        _meshGrid = new MeshVertex[2][2];
        _meshGrid[1][0] = _meshBuilder.newVertex(new Point3f(1, 1, 0));
        _meshGrid[0][0] = _meshBuilder.newVertex(new Point3f(0, 1, 0));
        _meshGrid[1][1] = _meshBuilder.newVertex(new Point3f(1, 0, 0));
        _meshGrid[0][1] = _meshBuilder.newVertex(new Point3f(0, 0, 0));
        _meshBuilder.setMaterial(_kineticaMaterial);
        _meshBuilder.addPlane(_meshGrid, true);
        _meshBuilder.build(_geoWriter);

        _meshBuilder = new MeshBuilder("face-XY2");
        _meshGrid = new MeshVertex[2][2];
        _meshGrid[1][0] = _meshBuilder.newVertex(new Point3f(0, 1, -1));
        _meshGrid[0][0] = _meshBuilder.newVertex(new Point3f(1, 1, -1));
        _meshGrid[1][1] = _meshBuilder.newVertex(new Point3f(0, 0, -1));
        _meshGrid[0][1] = _meshBuilder.newVertex(new Point3f(1, 0, -1));
        _meshBuilder.setMaterial(_kineticaMaterial);
        _meshBuilder.addPlane(_meshGrid, true);
        _meshBuilder.build(_geoWriter);

        _meshBuilder = new MeshBuilder("face-YZ1");
        _meshGrid = new MeshVertex[2][2];
        _meshGrid[1][0] = _meshBuilder.newVertex(new Point3f(0, 1, 0));
        _meshGrid[0][0] = _meshBuilder.newVertex(new Point3f(0, 1, -1));
        _meshGrid[1][1] = _meshBuilder.newVertex(new Point3f(0, 0, 0));
        _meshGrid[0][1] = _meshBuilder.newVertex(new Point3f(0, 0, -1));
        _meshBuilder.setMaterial(_gltfMaterial);
        _meshBuilder.addPlane(_meshGrid, true);
        _meshBuilder.build(_geoWriter);
        
        _meshBuilder = new MeshBuilder("face-YZ2");
        _meshGrid = new MeshVertex[2][2];
        _meshGrid[1][0] = _meshBuilder.newVertex(new Point3f(1, 1, -1));
        _meshGrid[0][0] = _meshBuilder.newVertex(new Point3f(1, 1, 0));
        _meshGrid[1][1] = _meshBuilder.newVertex(new Point3f(1, 0, -1));
        _meshGrid[0][1] = _meshBuilder.newVertex(new Point3f(1, 0, 0));
        _meshBuilder.setMaterial(_gltfMaterial);
        _meshBuilder.addPlane(_meshGrid, true);
        _meshBuilder.build(_geoWriter);
        
        _meshBuilder = new MeshBuilder("face-Top");
        _meshGrid = new MeshVertex[2][2];
        _meshGrid[1][0] = _meshBuilder.newVertex(new Point3f(1, 1, 0));
        _meshGrid[0][0] = _meshBuilder.newVertex(new Point3f(1, 1, -1));
        _meshGrid[1][1] = _meshBuilder.newVertex(new Point3f(0, 1, 0));
        _meshGrid[0][1] = _meshBuilder.newVertex(new Point3f(0, 1, -1));
        _meshBuilder.setMaterial(_uvGridMaterial);
        _meshBuilder.addPlane(_meshGrid, true);
        _meshBuilder.build(_geoWriter);
        
        _meshBuilder = new MeshBuilder("face-Bottom");
        _meshGrid = new MeshVertex[2][2];
        _meshGrid[1][0] = _meshBuilder.newVertex(new Point3f(1, 0, -1));
        _meshGrid[0][0] = _meshBuilder.newVertex(new Point3f(1, 0, 0));
        _meshGrid[1][1] = _meshBuilder.newVertex(new Point3f(0, 0, -1));
        _meshGrid[0][1] = _meshBuilder.newVertex(new Point3f(0, 0, 0));
        _meshBuilder.setMaterial(_uvGridMaterial);
        _meshBuilder.addPlane(_meshGrid, true);
        _meshBuilder.build(_geoWriter);

        File _outFile = TestShapeModels.getFile("test_cube");
        _geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    /**
     * Add a cube where texture coordinates are manually calculated.
     * @see TriangleBuilder#addSquare
     */
    @Test
    public void testCubeOrig() throws Exception {
        MeshGltfWriter _geoWriter = new MeshGltfWriter();
        
        // set the path used for finding textures
        _geoWriter.setBasePath(new File("src/test/resources"));
        
        // create materials for each of the textures
        Material _kineticaMaterial = _geoWriter.newTextureMaterial("kinetica_logo.png");
        Material _gltfMaterial = _geoWriter.newTextureMaterial("gltf_logo.png");
        Material _uvGridMaterial = _geoWriter.newTextureMaterial("uv_grid_512.png");
        
        MeshBuilder _meshBuilder = null;
        
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
        _meshBuilder.setMaterial(_kineticaMaterial);
        _meshBuilder.build(_geoWriter);

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
        _meshBuilder.setMaterial(_kineticaMaterial);
        _meshBuilder.build(_geoWriter);
        
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
        _meshBuilder.setMaterial(_gltfMaterial);
        _meshBuilder.build(_geoWriter);
        
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
        _meshBuilder.setMaterial(_gltfMaterial);
        _meshBuilder.build(_geoWriter);
        
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
        _meshBuilder.setMaterial(_uvGridMaterial);
        _meshBuilder.build(_geoWriter);
        
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
        _meshBuilder.setMaterial(_uvGridMaterial);
        _meshBuilder.build(_geoWriter);

        File _outFile = TestShapeModels.getFile("test_cube2");
        _geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
}
