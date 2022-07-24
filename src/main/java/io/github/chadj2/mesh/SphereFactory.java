/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.GltfWriter.AlphaMode;

/**
 * Generate a large set of spheres of various sizes, colors, and transparencies. 
 * Spheres of the same color will get reused to reduce the file size. 
 * @author Chad Juliano
 */
public class SphereFactory {
    
    private final static Logger LOG = LoggerFactory.getLogger(SphereFactory.class);
    
    private final IcosphereBuilder _builder = new IcosphereBuilder("icosphere");
    private final GltfWriter _writer;
    
    /**
     * Map of colors to mesh indicies.
     */
    private final Map<Color, Integer> _meshMap = new HashMap<>();
    
    private float[] _scale = { 1f, 1f, 1f};
    
    public SphereFactory(GltfWriter _writer) {
        this._builder.setIsPatterned(false);
        this._builder.setMaxDetail(2);
        
        // need to set BLEND mode or transparency does not work.
        _writer.setAlphaMode(AlphaMode.BLEND);
        Material material = _writer.newDefaultMaterial();
        this._builder.setMaterial(material);
        
        this._writer = _writer;
    }
    
    public void setMaxDetail(int val) { this._builder.setMaxDetail(val); }
    
    public void setColor(Color color) { this._builder.setColor(color); }
    
    public void setScale(float scale) { this._scale = new float[] { scale, scale, scale }; }

    /**
     * Add a sphere at the given position.
     * @param xPos
     * @param yPos
     * @param zPos
     * @throws Exception
     */
    public void addSphere(float xPos, float yPos, float zPos) throws Exception {
        Integer meshIdx = getMesh();
        
        Node node = new Node();
        this._writer.addNode(node);
        node.setMesh(meshIdx);
        
        node.setName(String.format("sphere[%d]-node", meshIdx));
        node.setScale(this._scale);
        
        float[] translation = { xPos, yPos, zPos }; 
        node.setTranslation(translation);
    }
    
    /**
     * Create a new mesh or returned a cached version.
     * @return
     * @throws Exception
     */
    private int getMesh() throws Exception {
        Integer meshIdx = this._meshMap.get(this._builder.getColor());
        if(meshIdx != null) {
            // found a cached sphere
            return meshIdx;
        }
        
        // Get the next mesh ID.
        meshIdx = 0;
        List<Mesh> meshList = this._writer.getGltf().getMeshes();
        if(meshList != null) {
            meshIdx = meshList.size();
        }
        
        LOG.debug("Building new sphere: {}", meshIdx);
        this._builder.addIcosphere();
        
        // set the name of the builder so that all objects in the JSON can be
        // identified with this sphere
        String name = String.format("sphere[%d]", meshIdx);
        this._builder.setName(name);
        meshIdx = this._builder.buildMesh(this._writer);
        
        // add this sphere to the cache.
        this._meshMap.put(this._builder.getColor(), meshIdx);
        return meshIdx;
    }
}
