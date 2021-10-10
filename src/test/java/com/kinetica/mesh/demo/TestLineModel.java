/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh.demo;

import com.kinetica.mesh.GltfWriter;
import com.kinetica.mesh.MeshBuilder;
import com.kinetica.mesh.MeshVertex;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3f;
import java.awt.*;
import java.io.File;

public class TestLineModel {
    
    private final static Logger LOG = LoggerFactory.getLogger(TestLineModel.class);

    /**
     * Generate lines with different colors.
     * @throws Exception
     */
    @Test
    public void testLineModel() throws Exception {
        GltfWriter _geoWriter = new GltfWriter();
        final MeshBuilder _meshBuilder = new MeshBuilder("test_linemodel");

        final MeshVertex[][] _meshGrid = new MeshVertex[36][2];
        final Point3f[] points = new Point3f[]{
                new Point3f(1000, -1200, 300),
                new Point3f(2200, -1200, 300),
                new Point3f(1000, 1200, 300),
                new Point3f(2200, -1200, 300),
                new Point3f(2200, 1000, 300),
                new Point3f(1000, 1200, 300),
                new Point3f(1000, -1200, 0),
                new Point3f(1000, 1200, 0),
                new Point3f(2200, -1200, 0),
                new Point3f(2200, -1200, 0),
                new Point3f(1000, 1200, 0),
                new Point3f(2200, 1000, 0),
                new Point3f(1000, -1200, 300),
                new Point3f(1000, -1200, 0),
                new Point3f(2200, -1200, 300),
                new Point3f(2200, -1200, 300),
                new Point3f(1000, -1200, 0),
                new Point3f(2200, -1200, 0),
                new Point3f(1000, 1200, 300),
                new Point3f(2200, 1000, 300),
                new Point3f(1000, 1200, 0),
                new Point3f(2200, 1000, 300),
                new Point3f(2200, 1000, 0),
                new Point3f(1000, 1200, 0),
                new Point3f(1000, -1200, 300),
                new Point3f(1000, 1200, 300),
                new Point3f(1000, -1200, 0),
                new Point3f(1000, 1200, 300),
                new Point3f(1000, 1200, 0),
                new Point3f(1000, -1200, 0),
                new Point3f(2200, -1200, 300),
                new Point3f(2200, -1200, 0),
                new Point3f(2200, 1000, 300),
                new Point3f(2200, 1000, 300),
                new Point3f(2200, -1200, 0),
                new Point3f(2200, 1000, 0)
        };

        for(int _xIdx = 0; _xIdx < 36; _xIdx++) {
                Point3f _point = points[_xIdx%36];
                Point3f _point2 = points[(_xIdx+1)%36];
                MeshVertex _vertex = _meshBuilder.newVertex(_point);
                MeshVertex _vertex2 = _meshBuilder.newVertex(_point2);
                _meshGrid[_xIdx][0] = _vertex;
                _meshGrid[_xIdx][1] = _vertex2;
            int colorInt = _xIdx%3;
            Color color=Color.WHITE;
            if(colorInt==1)color=Color.YELLOW;
            else if(colorInt==2)color=Color.BLACK;
                _vertex.setColor(color);
                _vertex2.setColor(color);
        }

        _meshBuilder.addPlane(_meshGrid, true);

        // Set rendering for both sides of the plane
       _geoWriter.setAlphaMode(GltfWriter.AlphaMode.OPAQUE_DS);

        // build the gltf buffers
        final Material _material = _geoWriter.addDefaultMaterial();
        final MeshPrimitive _meshPrimitive = new MeshPrimitive();
        _meshPrimitive.setMode(3);
        _meshBuilder.build(_geoWriter, _material, _meshPrimitive);

        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        _geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);

    }
}
