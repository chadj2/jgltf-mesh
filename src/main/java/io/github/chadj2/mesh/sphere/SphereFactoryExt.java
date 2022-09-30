/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.sphere;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.buffer.BufferFloat3;
import io.github.chadj2.mesh.buffer.GlTFMeshGpuInstancing;
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.buffer.BufferByte4;

/**
 * 
 * @author Chad Juliano
 */
public class SphereFactoryExt extends SphereFactory {
    
    private final static Logger LOG = LoggerFactory.getLogger(SphereFactoryExt.class);
    
    private final static String EXT_INSTANCING = "EXT_mesh_gpu_instancing";
    
    private static class InstancingNode {
        final BufferFloat3 _trans;
        final BufferByte4 _rotation;
        final BufferFloat3 _scale;
        final Node _node;
        
        InstancingNode(Node node, String name) {
            this._node = node;
            this._trans = new BufferFloat3(name,"TRANSLATION");
            this._rotation = new BufferByte4(name, "ROTATION");
            this._scale = new BufferFloat3(name,"SCALE");
            this._node.setName(name + "_node");
        }
        
        void addPos(Point3f pos) {
            this._trans.add(pos);
        }
        
        void addRotation(Quat4f _quat) {
            this._rotation.add(_quat);
        }
        
        void addScale(Vector3f scale) {
            this._scale.add(scale);
        }
        
        void build(MeshGltfWriter writer) {
            GlTFMeshGpuInstancing meshInstancing = new GlTFMeshGpuInstancing();
            this._node.addExtensions(EXT_INSTANCING, meshInstancing);
            this._trans.build(writer, meshInstancing);
            this._rotation.build(writer, meshInstancing);
            this._scale.build(writer, meshInstancing);
        }
    }
    
    private final Map<Integer, InstancingNode> _meshToNodeIndex = new HashMap<>();
    
    public SphereFactoryExt(MeshGltfWriter writer) {
        super(writer);
    }
    
    @Override
    public Node addSphere(Point3f pos) throws Exception {
        Integer meshIdx = getMeshColorLod();
        getTransform().transform(pos);
        Quat4f rotation = new Quat4f(0,0,0,1);
        Vector3f scale = new Vector3f(this._radius);
        
        InstancingNode iNode = this._meshToNodeIndex.get(meshIdx);
        if(iNode == null) {
            Node node = new Node();
            int nodeIdx = this._writer.addNode(node);
            String name = String.format("%s[%d]", getName(), nodeIdx);
            node.setMesh(meshIdx);
            
            iNode = new InstancingNode(node, name);
            this._meshToNodeIndex.put(meshIdx, iNode);
        }
        
        iNode.addPos(pos);
        iNode.addRotation(rotation);
        iNode.addScale(scale);
        return iNode._node;
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
