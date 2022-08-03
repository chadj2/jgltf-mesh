package io.github.chadj2.mesh.demo;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.MeshBuilder;
import io.github.chadj2.mesh.MeshVertex;

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
        addPipe(pointList, colorList, pipeBuilder, radius, sides);
        
        pipeBuilder.build(this._geoWriter);
        
        File _outFile = TestShapeModels.getFile(pipeBuilder.getName());
        this._geoWriter.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }

    private void addPipe(List<Point3f> vecList, List<Color> colorList, 
            MeshBuilder tubeBuilder, float radius, int sides) {
        Point3f origin = new Point3f(0f, 0f, 0f);
        MeshVertex[][] meshGrid = new MeshVertex[vecList.size()][];

        for(int idx = 0; idx < vecList.size(); idx++) {
            Point3f vec1 = null;
            if(idx > 0) {
                vec1 = vecList.get(idx - 1);
            }
            
            Point3f vec2 = vecList.get(idx);
            
            Point3f vec3 = null;
            if(idx < (vecList.size() - 1)) {
                vec3 = vecList.get(idx + 1);
            }
            
            Matrix4f transM4 = transofrmBetween(vec1, vec2, vec3);
            tubeBuilder.setTransform(transM4);

            Color _color = colorList.get(idx);
            meshGrid[idx] = tubeBuilder.addCircleVerticesXZ(origin, radius, sides, _color);
        }
        
        tubeBuilder.addLathe(meshGrid, false);
    }

    /**
     * Create a 4D translation matrix from ux to the vector between points.
     * @param pos1
     * @param pos2
     * @return
     */
    public Matrix4f transofrmBetween(Point3f pos1, Point3f pos2, Point3f pos3) {
        Vector3f toVec = new Vector3f();
        int vecCount = 0;
        
        if(pos1 != null) {
            Vector3f vec = new Vector3f();
            vec.sub(pos2, pos1);
            vec.normalize();
            toVec.add(vec);
            vecCount++;
        }
        
        if(pos3 != null) {
            Vector3f vec = new Vector3f();
            vec.sub(pos3, pos2);
            vec.normalize();
            toVec.add(vec);
            vecCount++;
        }
        
        toVec.scale(1f/(float)vecCount);
        Matrix3f rotM = rotationFromY(toVec);
        
        Matrix4f transM4 = new Matrix4f();
        transM4.set(rotM);
        transM4.setTranslation(new Vector3f(pos2));
        return transM4;
    }
    
    @Test
    public void testTransform() {
        Vector3f yUnit = new Vector3f(0f, 1f, 0f);
        Vector3f toVec = new Vector3f(1f, 0f, 0f);
        
        Matrix3f rotM = rotationFromY(toVec);
        Vector3f transVec = new Vector3f(yUnit);
        rotM.transform(transVec);

        LOG.info("yUnit : {}", yUnit);
        LOG.info("toVec   : {}", toVec);
        LOG.info("transVec: {}", transVec);
    }
    
    /**
     * Create a 3D transform from ux to a vector.
     * @param toVec
     * @return
     */
    public Matrix3f rotationFromY(Vector3f toVec) {
        Vector3f yUnit = new Vector3f(0f, 1f, 0f);
        
        Vector3f zVec = new Vector3f();
        zVec.cross(toVec, yUnit);

        Vector3f xVec = new Vector3f();
        xVec.cross(toVec, zVec);

        Vector3f yVec = new Vector3f(toVec);
        xVec.normalize();
        yVec.normalize();
        zVec.normalize();

        Matrix3f rotM3 = new Matrix3f();
        rotM3.setColumn(0, xVec);
        rotM3.setColumn(1, yVec);
        rotM3.setColumn(2, zVec);
        return rotM3;
    }
}
