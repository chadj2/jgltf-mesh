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
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.sphere.IcosphereBuilder;
import io.github.chadj2.mesh.sphere.SphereFactory;
import io.github.chadj2.mesh.sphere.SphereFactoryInst;

public class TestSphereModels {

    private final static Logger LOG = LoggerFactory.getLogger(TestSphereModels.class);

    private final MeshGltfWriter _writer = new MeshGltfWriter();

    /**
     * Create a 10x10 grid of spheres with varying color and radius.
     * @throws Exception
     */
    @Test
    public void testSphereFactory() throws Exception {
        SphereFactory factory = new SphereFactory(this._writer);
        factory.setMaxDetail(2);
        
        createSpheres(factory);

        File _outFile = TestShapeModels.getFile("test_sphere_factory");
        this._writer.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    /**
     * Create a 10x10 grid of spheres using the EXT_mesh_gpu_instancing extension.
     * @throws Exception
     */
    @Test
    public void testSphereFactoryExt() throws Exception {
        SphereFactoryInst factory = new SphereFactoryInst(this._writer);
        factory.setMaxDetail(2);
        
        createSpheres(factory);
        factory.build();

        File _outFile = TestShapeModels.getFile("test_sphere_factory_ext");
        this._writer.writeGltf(_outFile);
        LOG.info("Finished generating: {}", _outFile);
    }
    
    public void createSpheres(SphereFactory factory) throws Exception {
        final int xGridSize = 10;
        final int yGridSize = 10;
        
        for(int xIdx = 0; xIdx < xGridSize; xIdx++) {
            // first loop will vary the color
            float xInterp = (float)xIdx/((float)xGridSize);
            Color color = BaseBuilder.colorCreateHsba(xInterp, 1f, 1f, 0.6f);
            float xPos = xInterp*20f;
            factory.setColor(color);
            
            for(int yIdx = yGridSize; yIdx > 0; yIdx--) {
                float yInterp = (float)yIdx/((float)yGridSize);
                float yPos = yInterp*20f;
                factory.setRadius(yInterp);
                
                // add the sphere
                factory.addSphere(new Point3f(xPos, yPos, 0f));
            }
        }
    }
    
    /**
     * Create 2 IcoSpheres with vertex patterns.
     * @throws Exception
     */
    @Test
    public void testIcoBuilder() throws Exception {
        IcosphereBuilder builder = new IcosphereBuilder("test_icosphere");
        
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
