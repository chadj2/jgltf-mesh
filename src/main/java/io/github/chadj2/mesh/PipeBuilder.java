/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate 3D pipes for glTF based on 2D grid arrays
 * @author Chad Juliano
 */
public class PipeBuilder extends MeshBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(PipeBuilder.class);
    
    public PipeBuilder(String _name) {
        super(_name);
    }

    private final static Point3f ORIGIN_POINT = new Point3f(0f, 0f, 0f);
    
    /**
     * Add a pipe with given points and colors. Radius is in transformed coordinate system.
     * @param pointList
     * @param colorList
     * @param radius
     * @param sides
     * @throws Exception 
     */
    public void addPipe(List<Point3f> pointList, List<Color> colorList, float radius, int sides) throws Exception {
        List<Point3f> tPointList = new ArrayList<>();

        // save the original transform
        Matrix4f origM4 = new Matrix4f(this.getTransform());
        
        // transform each of the input points.
        for(Point3f point : pointList) {
            Point3f tPoint = new Point3f(point);
            origM4.transform(tPoint);
            tPointList.add(tPoint);
        }
        
        // Create the mesh that will contain the ring segments this is done in the destination coordinate 
        // system since input points are already transformed. 
        final MeshVertex[][] meshGrid = new MeshVertex[pointList.size()][];
        
        for(int idx = 0; idx < pointList.size(); idx++) {
            Color color = colorList.get(idx);
            
            try {
                // We set the transformation so the ring segment will be in the correct location
                Matrix4f rtsMatrix = calcTransform(tPointList, idx);
                setTransform(rtsMatrix);
                
            }
            catch(Exception ex) {
                String msg = String.format("Failure to calculate transform at index: %d", idx);
                LOG.warn(msg, ex);
                //throw new Exception(msg, ex);
            }
            
            // add the ring segment
            meshGrid[idx] = addCircleVerticesXZ(ORIGIN_POINT, radius, sides, color);
            
            // add caps to start and end
            if(idx == 0) {
                // cap the start
                addDiscXZ(ORIGIN_POINT, -radius, sides, color);
            }
            else if(idx == (tPointList.size() - 1)) {
                // cap the end
                addDiscXZ(ORIGIN_POINT, radius, sides, color);
            }
        }
        
        // restore original transform
        setTransform(origM4);
        
        // build the mesh
        addLathe(meshGrid, false);
    }
    
    private static Matrix4f calcTransform(List<Point3f> tPointList, int idx) throws Exception {
        // get previous, current, and next points 
        Point3f prevPoint = null;
        Point3f currentPoint = tPointList.get(idx);
        Point3f nextPoint = null;
        
        if(idx > 0) {
            prevPoint = tPointList.get(idx - 1);
        }
        
        if(idx < (tPointList.size() - 1)) {
            nextPoint = tPointList.get(idx + 1);
        }

        // create a rotation matrix based on the axis
        Vector3f axisVec = getPipeAxis(prevPoint, currentPoint, nextPoint);
        //LOG.info("axisVec: {}", axisVec);
        Matrix3f rotation3f = rotationFromY(axisVec);
        
        // create a matrix for the rotation and translation.
        Matrix4f rtsMatrix = new Matrix4f(rotation3f, new Vector3f(currentPoint), 1f);
        
        return rtsMatrix;
    }
    
    /**
     * Get the axis or direction of the pipe.
     * @param prevPoint
     * @param curPoint
     * @param nextPoint
     * @return
     * @throws Exception
     */
    private static Vector3f getPipeAxis(Point3f prevPoint, Point3f curPoint, Point3f nextPoint) throws Exception {
        // average available segments
        Vector3f axisVec = new Vector3f();
        int segCount = 0;
        
        // prev-current segment
        if(prevPoint != null) {
            Vector3f seg = new Vector3f();
            seg.sub(curPoint, prevPoint);
            seg.normalize();
            axisVec.add(seg);
            segCount++;
        }
        
        // current-next segment
        if(nextPoint != null) {
            Vector3f seg = new Vector3f();
            seg.sub(nextPoint, curPoint);
            seg.normalize();
            axisVec.add(seg);
            segCount++;
        }
        
        // divide by number of segments
        axisVec.scale(1f/(float)segCount);
        
        if(Float.isNaN(axisVec.x) || Float.isNaN(axisVec.y) || Float.isNaN(axisVec.z)) {
            throw new Exception("Unable to calculate transform");
        }
        
        return axisVec;
    }
    
    //private final static Vector3f Y_UNIT_VECTOR = new Vector3f(0f, 1f, 0f);
    
    //private final static Vector3f X_UNIT_VECTOR = new Vector3f(1f, 0f, 0f);
    
    private final static Vector3f Z_UNIT_VECTOR = new Vector3f(0f, 0f, 1f);
    
    /**
     * Create a 3D transform in the direction of the axis.
     * @param axisVec
     * @return
     * @throws Exception 
     */
    public static Matrix3f rotationFromY(Vector3f axisVec) throws Exception {
        Vector3f zVec = new Vector3f();
        Vector3f yVec = new Vector3f(axisVec);
        Vector3f xVec = new Vector3f();
        
        // create zVec perpendicular to axisVec
        zVec.cross(axisVec, Z_UNIT_VECTOR);
        
        if(zVec.length() == 0f) {
            throw new Exception("Unable to calculate rotation transform for axis: " + axisVec.toString());
        }
        
        // create xVec perpendicular to zVec
        xVec.cross(axisVec, zVec);
        
        xVec.normalize();
        yVec.normalize();
        zVec.normalize();

        Matrix3f rotation = new Matrix3f();
        rotation.setColumn(0, xVec);
        rotation.setColumn(1, yVec);
        rotation.setColumn(2, zVec);
        
        return rotation;
    }
}
