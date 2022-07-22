package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.HashMap;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class IcoSphereBuilder extends TriangleBuilder {

    private static final int idxTop = 10;
    private static final int idxBottom = 11;
    private static final int icoSides = 5;
    private static final double icoRadius = 1d;
    
    private final HashMap<String, MeshVertex> _midpointMap = new HashMap<>();
    
    public IcoSphereBuilder(String _name) {
        super(_name);
        // don't invert X axis
        this.getTransform().m00 = 1;
    }

    public MeshVertex[] getIcosphereVertices() {
        
        final double vAngle = Math.atan(1.0d/2);  // 26.565 degrees
        final double zPos = icoRadius * Math.sin(vAngle);
        final double xyPos = icoRadius * Math.cos(vAngle);
        final double hAngle = (2d*Math.PI)/icoSides; // 72 degrees
        
        final MeshVertex[] vertices = new MeshVertex[12];
        
        // compute 10 vertices at 1st and 2nd rows
        for(int idx = 0; idx < icoSides; ++idx)
        {
            // add vertex in first row
            int row1Idx = idx;
            double row1hAngle = idx*hAngle;
            MeshVertex row1 = newVertex(new Point3f(
                    (float)(xyPos * Math.cos(row1hAngle)), 
                    (float)(xyPos * Math.sin(row1hAngle)), 
                    (float)zPos));
            row1.setColor(Color.GRAY);
            vertices[row1Idx] = row1;

            // add vertex in second row
            int row2Idx = row1Idx + icoSides;
            double row2hAngle = row1hAngle + hAngle/2;
            MeshVertex row2 = newVertex(new Point3f(
                    (float)(xyPos * Math.cos(row2hAngle)), 
                    (float)(xyPos * Math.sin(row2hAngle)), 
                    (float)-zPos));
            row2.setColor(Color.GRAY);
            vertices[row2Idx] = row2;
        }
        
        // top vertex
        vertices[idxTop] = newVertex(new Point3f(0f, 0f, (float)icoRadius));
        vertices[idxTop].setColor(Color.BLUE);
        
        // bottom vertex
        vertices[idxBottom] = newVertex(new Point3f(0f, 0f, (float)-icoRadius));
        vertices[idxBottom].setColor(Color.RED);
        
        return vertices;
    }
    
    public void addIcosphereTriangles() {
        MeshVertex[] vertices = getIcosphereVertices();
        
        for(int idx = 0; idx < icoSides; idx++) {
            // row 1 indexes. the next index needs to be wrapped around
            int row1Idx = idx;
            int row1Next = (row1Idx + 1) % icoSides;
            
            // row 2 indexes
            int row2Idx = row1Idx + icoSides;
            int row2Next = row1Next + icoSides;
            
            int detail = 3;
            
            // top triangle
            addIcosphereTriangle(detail,
                    vertices[idxTop], 
                    vertices[row1Next], 
                    vertices[row1Idx]);
            
            // downward middle triangle
            addIcosphereTriangle(detail,
                    vertices[row2Idx], 
                    vertices[row1Idx], 
                    vertices[row1Next]);
            
            // upward middle triangle
            addIcosphereTriangle(detail,
                    vertices[row1Next], 
                    vertices[row2Next], 
                    vertices[row2Idx]);
            
            // bottom triangle
            addIcosphereTriangle(detail,
                    vertices[idxBottom], 
                    vertices[row2Idx], 
                    vertices[row2Next]);
        }
    }
    
    public void addIcosphereTriangle(int level, 
            MeshVertex v1, MeshVertex v2, MeshVertex v3) {
        
        if(level == 0) {
            // this is the bottom of the recursion tree so add the triangle.
            addTriangle(v1, v2, v3);
            return;
        }

        // compute 3 new vertices by splitting half on each edge
        //         v1       
        //        / \       
        // newV1 *---* newV3
        //      / \ / \     
        //    v2---*---v3   
        //       newV2 
        MeshVertex newV1 = icoMidPoint(v1, v2);
        MeshVertex newV2 = icoMidPoint(v2, v3);
        MeshVertex newV3 = icoMidPoint(v1, v3);
        
        // recurse
        level = level-1;
        addIcosphereTriangle(level, v1, newV3, newV1);
        addIcosphereTriangle(level, v2, newV1, newV2);
        addIcosphereTriangle(level, v3, newV2, newV3);
        addIcosphereTriangle(level, newV2, newV1, newV3);
    }
    
    /**
     * find middle point of 2 vertices.  
     * @param tBuilder
     * @param v1
     * @param v2
     * @return
     */
    public MeshVertex icoMidPoint(MeshVertex v1, MeshVertex v2) {
        // check the index to see if the midpoint already exists.
        String key = String.format("%d_%d", 
                Math.min(v1.getIndex(), v2.getIndex()),
                Math.max(v1.getIndex(), v2.getIndex()));
        MeshVertex newMv = _midpointMap.get(key);
        
        if(newMv != null) {
            // midpoint already exists.
            return newMv;
        }
        
        Vector3f p3 = new Vector3f();
        p3.add(v1.getVertex(), v2.getVertex());

        // new vertex must be resized, so the length is equal to the radius
        p3.normalize();
        p3.scale((float)icoRadius);
        
        Point3f newPoint = new Point3f(p3);
        newMv = newVertex(newPoint);
        newMv.setColor(Color.GRAY);
        
        // add new midpoint to the index
        _midpointMap.put(key, newMv);
        return newMv;
    }
}
