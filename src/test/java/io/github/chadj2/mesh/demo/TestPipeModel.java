package io.github.chadj2.mesh.demo;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.MeshBuilder;

public class TestPipeModel {
    
    private final static Logger LOG = LoggerFactory.getLogger(TestPipeModel.class);

    private final GltfWriter _geoWriter = new GltfWriter();

    @Test
    public void testTube() throws Exception {
        final int pieSections = 50;
        List<Point3f> pointList = new ArrayList<>();
        List<Color> colorList = new ArrayList<>();
        
        for(int idx = 0; idx < pieSections; idx++) {
            double partIdx = (double)idx/(double)pieSections;
            double angleIdx = 2*Math.PI*partIdx;
            
            float xPos = (float)Math.cos(angleIdx);
            float yPos = (float)partIdx;
            float zPos = (float)Math.sin(angleIdx);

            Point3f point = new Point3f(xPos, yPos, zPos);
            pointList.add(point);
            
            Color color = Color.getHSBColor((float)partIdx, 0.6f, 0.5f);
            colorList.add(color);
        }
        
        MeshBuilder pipeBuilder = new MeshBuilder("test_pipe");
        float radius = 0.05f;
        int sides = 10;
        pipeBuilder.addPipe(pointList, colorList, radius, sides);
        
        pipeBuilder.build(this._geoWriter);
        
        File _outFile = TestShapeModels.getFile(pipeBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    @Test
    public void testTransform() {
        Vector3f yUnit = new Vector3f(0f, 1f, 0f);
        Vector3f toVec = new Vector3f(1f, 0f, 0f);
        
        Matrix3f rotM = MeshBuilder.rotationFromY(toVec);
        Vector3f transVec = new Vector3f(yUnit);
        rotM.transform(transVec);

        LOG.info("yUnit : {}", yUnit);
        LOG.info("toVec   : {}", toVec);
        LOG.info("transVec: {}", transVec);
    }
}
