/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.demo;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.MeshVertex;
import io.github.chadj2.mesh.TopologyBuilder;
import io.github.chadj2.mesh.TopologyBuilder.TopologyMode;

public class TestLineModels {
    
    private final static Logger LOG = LoggerFactory.getLogger(TestLineModels.class);

    private final GltfWriter _geoWriter = new GltfWriter();
    
    /**
     * Draw a sphere using the line strip topology.
     * @see TopologyMode
     */
    @Test 
    public void testLineStrip() throws Exception {
        List<Point3f> pointList = new ArrayList<>();
        List<Color> colorList = new ArrayList<>();

        createSpiral(pointList, colorList);

        // add the points to he builder
        TopologyBuilder _meshBuilder = new TopologyBuilder("test_line_strip", TopologyMode.LINE_STRIP);
        for(int idx=0; idx<colorList.size(); idx++) {
            Point3f point = pointList.get(idx);
            Color color = colorList.get(idx);
            
            MeshVertex _vertex = _meshBuilder.newVertex(point);
            _vertex.setColor(color);
        }
        
        Node _node = _meshBuilder.build(this._geoWriter);
        
        // example of adding custom data to MeshPrimitive
        int _meshIdx = _node.getMesh();
        Mesh _mesh = this._geoWriter.getGltf().getMeshes().get(_meshIdx);
        MeshPrimitive _primitive = _mesh.getPrimitives().get(0);
        _primitive.setExtras(new String[]{"some","additional","data"});
        
        File _outFile = TestShapeModels.getFile(_meshBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }

    public static void createSpiral(List<Point3f> pointList, List<Color> colorList) {
        final int _rPoints = 1000;
        final int _rotations = 40;
        for(int _rIdx= 0; _rIdx <= _rPoints; _rIdx++) {
            double _part = (double)_rIdx/(double)_rPoints;
            
            // spherical coordinates
            double _anglePhi = _part*_rotations*Math.PI;
            double _angleTheta = _part*Math.PI;
            
            // convert to Cartesian
            double _radius = (float)(Math.sin(_angleTheta));
            float _xPos = (float)(_radius*Math.cos(_anglePhi));
            float _yPos = (float)(_radius*Math.sin(_anglePhi));
            float _zPos = (float)(Math.cos(_angleTheta));
            Point3f point = new Point3f(_xPos, _yPos, _zPos);
            pointList.add(point);
            
            final Color color = Color.getHSBColor((float)_part, 0.6f, 0.5f);
            colorList.add(color);
        }
    }
}
