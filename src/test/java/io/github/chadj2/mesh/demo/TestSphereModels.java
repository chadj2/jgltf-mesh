/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.demo;

import java.awt.Color;
import java.io.File;

import javax.vecmath.Point3f;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.BaseBuilder;
import io.github.chadj2.mesh.GltfWriter;
import io.github.chadj2.mesh.IcosphereBuilder;
import io.github.chadj2.mesh.SphereFactory;

public class TestSphereModels {

    private final static Logger LOG = LoggerFactory.getLogger(TestSphereModels.class);

    private final GltfWriter _writer = new GltfWriter();

    /**
     * Create a 10x10 grid of spheres with varying color and radius.
     * @throws Exception
     */
    @Test
    public void testSphereFactory() throws Exception {
        SphereFactory factory = new SphereFactory(this._writer);
        factory.setMaxDetail(2);
        
        final int gridSize = 10;
        
        for(int xIdx = 0; xIdx < gridSize; xIdx++) {
            // first loop will vary the color
            float xInterp = (float)xIdx/((float)gridSize);
            Color color = BaseBuilder.createHsbColor(xInterp, 1f, 1f, 0.5f);
            float xPos = xInterp*20f;
            factory.setColor(color);
            
            for(int yIdx = gridSize; yIdx > 0; yIdx--) {
                float yInterp = (float)yIdx/((float)gridSize);
                float yPos = yInterp*20f;
                factory.setRadius(yInterp);
                
                // add the sphere
                factory.addSphere(new Point3f(xPos, yPos, 0f));
            }
        }

        File _outFile = TestShapeModels.getFile("sphere_factory_test");
        this._writer.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    /**
     * Create 2 IcoSpheres with vertex patterns.
     * @throws Exception
     */
    @Test
    public void testIcoBuilder() throws Exception {
        IcosphereBuilder builder = new IcosphereBuilder("icosphere_test");
        
        // add a Cyan sphere
        builder.setColor(Color.CYAN);
        builder.setIsPatterned(true);
        final int lod = 3;
        builder.addIcosphere(lod);
        int meshIdx = builder.buildMesh(this._writer);
        
        Node node1 = new Node();
        int nodeIdx1 = this._writer.addNode(node1);
        node1.setName(String.format("mesh%d-node%d", meshIdx, nodeIdx1));
        node1.setMesh(meshIdx);

        // add a Green sphere offset on the x-axis 
        builder.setColor(Color.GREEN);
        builder.setIsPatterned(true);
        
        builder.addIcosphere(lod);
        int meshIdx2 = builder.buildMesh(this._writer);
        
        Node node2 = new Node();
        int nodeIdx2 = this._writer.addNode(node2);
        node2.setName(String.format("mesh%d-node%d", meshIdx2, nodeIdx2));
        node2.setMesh(meshIdx2);
        
        float[] translation = new float[] { 2f, 0f, 0f};
        node2.setTranslation(translation);
        
        // write the file
        File _outFile = TestShapeModels.getFile(builder.getName());
        this._writer.writeGltf(_outFile);
    }
}
