/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.ext.GlTFMeshGpuInstancing;
import io.github.chadj2.mesh.ext.InstRotation;
import io.github.chadj2.mesh.ext.InstTranslation;

/**
 * 
 * @author Chad Juliano
 */
public class SphereFactoryExt extends SphereFactory {
    
    private final static Logger LOG = LoggerFactory.getLogger(SphereFactoryExt.class);
    
    private final static String EXT_INSTANCING = "EXT_mesh_gpu_instancing";
    
    private static class InstancingNode {
        final InstTranslation _trans;
        final InstRotation _rot;
        final Node _node;
        
        InstancingNode(Node node, String name) {
            this._node = node;
            this._trans = new InstTranslation(name);
            this._rot = new InstRotation(name);
            this._node.setName(name + "_node");
        }
        
        void addPos(Point3f pos) {
            this._trans.add(pos);
        }
        
        void addRotation(Quat4f _quat) {
            this._rot.add(_quat);
        }
        
        void build(GltfWriter writer) {
            GlTFMeshGpuInstancing meshInstancing = new GlTFMeshGpuInstancing();
            this._node.addExtensions(EXT_INSTANCING, meshInstancing);
            this._trans.build(writer, meshInstancing);
            this._rot.build(writer, meshInstancing);
        }
    }
    
    private final Map<Integer, InstancingNode> _meshToNodeIndex = new HashMap<>();
    
    public SphereFactoryExt(GltfWriter writer) {
        super(writer);
    }
    
    @Override
    public Node addSphere(Point3f pos) throws Exception {
        Integer meshIdx = getMeshColorLod();
        getTransform().transform(pos);
        
        InstancingNode iNode = this._meshToNodeIndex.get(meshIdx);
        if(iNode != null) {
            iNode.addPos(pos);
            iNode.addRotation(new Quat4f(0,0,0,1));
            return iNode._node;
        }
        
        Node node = new Node();
        int nodeIdx = this._writer.addNode(node);
        String name = String.format("%s[%d]", getName(), nodeIdx);
        node.setMesh(meshIdx);
        
        iNode = new InstancingNode(node, name);
        iNode.addPos(pos);
        iNode.addRotation(new Quat4f(0,0,0,1));
        
        this._meshToNodeIndex.put(meshIdx, iNode);
        return node;
    }
    
    public void build() {
        GlTF gltf = this._writer.getGltf();
        gltf.addExtensionsUsed(EXT_INSTANCING);
        LOG.info("Adding extension: {}", EXT_INSTANCING);
        
        for(InstancingNode iNode : this._meshToNodeIndex.values()) {
            iNode.build(this._writer);
        }
    }
 
    
}
