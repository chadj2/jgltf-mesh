/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.util.List;

import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.Node;

/**
 * 
 * @author Chad Juliano
 */
public class SphereFactory {
    
    private final IcosphereBuilder _builder = new IcosphereBuilder("icosphere");
    private final GltfWriter _writer;
    private Color _color = Color.GRAY;
    private float[] _scale = { 1f, 1f, 1f};
    
    public SphereFactory(GltfWriter _writer) {
        this._builder.setIsPatterned(false);
        this._builder.setMaxDetail(3);
        this._writer = _writer;
    }
    
    public void setColor(Color color) { this._color = color; }
    
    public void setScale(float scale) { this._scale = new float[] { scale, scale, scale }; }

    public void addSphere(float xPos, float yPos, float zPos) throws Exception {
        int meshIdx = 0;
        List<Mesh> meshList = this._writer.getGltf().getMeshes();
        
        if(meshList != null) {
            meshIdx = meshList.size();
        }
        
        Node node1 = new Node();
        this._writer.addNode(node1);
        String _name = String.format("sphere[%d]", meshIdx);
        
        this._builder.setColor(this._color);
        this._builder.addIcosphere();
        this._builder.setName(_name);
        meshIdx = this._builder.buildMesh(this._writer);

        node1.setName(String.format("%s-node", _name));
        node1.setMesh(meshIdx);
        node1.setScale(this._scale);
        
        float[] translation = { xPos, yPos, zPos }; 
        node1.setTranslation(translation);
    }

    
}
