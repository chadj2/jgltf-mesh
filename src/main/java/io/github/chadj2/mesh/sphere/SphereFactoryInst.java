/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.sphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.buffer.BufferShort;
import io.github.chadj2.mesh.buffer.BufferVecFloat3;
import io.github.chadj2.mesh.buffer.BufferVecFloat4;
import io.github.chadj2.mesh.ext.FeatureId;
import io.github.chadj2.mesh.ext.GlTFMeshGpuInstancing;
import io.github.chadj2.mesh.ext.NodeInstanceFeatures;

/**
 * Create a set of spheres using the EXT_mesh_gpu_instancing extension. This is necessary
 * for visualizations requiring a large number of spheres.
 * @author Chad Juliano
 */
public class SphereFactoryInst extends SphereFactory {
    
    private final static Logger LOG = LoggerFactory.getLogger(SphereFactoryInst.class);
    
    private final static String EXT_INSTANCING = "EXT_mesh_gpu_instancing";
    private final static String EXT_INST_FEATURES = "EXT_instance_features";
    
    private static class InstancingNode {
        final BufferVecFloat3 _scale;
        final BufferVecFloat4 _rotation;
        final BufferVecFloat3 _trans;
        final BufferShort _featureId;
        final Node _node;
        
        InstancingNode(Node node, String name) {
            this._node = node;
            this._node.setName(name + "_node");
            
            this._scale = new BufferVecFloat3(name + "-scale");
            //this._rotation = new BufferByte4(name, "ROTATION");
            this._rotation = new BufferVecFloat4(name + "-rotation");
            this._trans = new BufferVecFloat3(name + "-translation");
            this._featureId = new BufferShort(name + "-featureId");
            
        }
        
        void add(Vector3f scale, Quat4f rot, Point3f trans, int featureId) {
            this._scale.add(scale);
            this._rotation.add(rot);
            this._trans.add(trans);
            this._featureId.add((short)featureId);
        }
        
        void build(MeshGltfWriter writer) {
            GlTFMeshGpuInstancing meshInstancing = new GlTFMeshGpuInstancing();
            this._node.addExtensions(EXT_INSTANCING, meshInstancing);
            
            this._scale.buildAttrib(writer, meshInstancing, "SCALE");
            this._rotation.buildAttrib(writer, meshInstancing, "ROTATION");
            this._trans.buildAttrib(writer, meshInstancing, "TRANSLATION");
            this._featureId.buildAttrib(writer, meshInstancing, "_FEATURE_ID_0");
        }
        
        void addFeatures(MeshGltfWriter writer) {
            NodeInstanceFeatures instFeatures = new NodeInstanceFeatures();
            this._node.addExtensions(EXT_INST_FEATURES, instFeatures);
            
            FeatureId featureId = new FeatureId();
            instFeatures.addFeatureIds(featureId);
            featureId.setLabel("eventId");
            featureId.setFeatureCount(this._scale.size());
            featureId.setPropertyTable(0);
        }
    }
    
    private final Map<Integer, InstancingNode> _meshToNodeIndex = new HashMap<>();
    
    public SphereFactoryInst(MeshGltfWriter writer) {
        super(writer);
    }
    
    private ArrayList<String> eventIdList = new ArrayList<>();
    
    @Override
    public Node addSphere(Point3f pos, String eventId) throws Exception {
        Integer meshIdx = getMeshColorLod();
        
        InstancingNode iNode = this._meshToNodeIndex.get(meshIdx);
        if(iNode == null) {
            Node node = new Node();
            int nodeIdx = this._writer.addNode(node);
            String name = String.format("%s[%d]", getName(), nodeIdx);
            node.setMesh(meshIdx);
            
            iNode = new InstancingNode(node, name);
            this._meshToNodeIndex.put(meshIdx, iNode);
        }

        getTransform().transform(pos);
        Quat4f rotation = new Quat4f(0,0,0,1);
        Vector3f scale = new Vector3f(this._radius);
        
        this.eventIdList.add(eventId);
        int featureId = this.eventIdList.size() - 1;
        
        iNode.add(scale, rotation, pos, featureId);
        
        return iNode._node;
    }
    
    @Override
    public void build() {
        GlTF gltf = this._writer.getGltf();
        gltf.addExtensionsUsed(EXT_INSTANCING);
        gltf.addExtensionsRequired(EXT_INSTANCING);
        LOG.info("Adding extension: {}", EXT_INSTANCING);
        

        gltf.addExtensionsUsed(EXT_INST_FEATURES);
        LOG.info("Adding extension: {}", EXT_INST_FEATURES);
        
        for(InstancingNode iNode : this._meshToNodeIndex.values()) {
            iNode.build(this._writer);
            iNode.addFeatures(this._writer);
        }
    }
}
