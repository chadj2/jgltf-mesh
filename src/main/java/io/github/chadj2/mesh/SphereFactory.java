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

import javax.vecmath.Point3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import io.github.chadj2.mesh.GltfWriter.AlphaMode;

/**
 * Generate a large set of spheres of various sizes, colors, and transparencies. 
 * Spheres of the same color will get reused to reduce the file size. 
 * @author Chad Juliano
 */
public class SphereFactory extends BaseBuilder {
    
    private final static Logger LOG = LoggerFactory.getLogger(SphereFactory.class);
    
    private final IcosphereBuilder _builder = new IcosphereBuilder("icosphere");
    private final GltfWriter _writer;
    
    /**
     * Map of color/lod to mesh indices.
     */
    private final Map<String, Integer> _colorLodToMeshIdx = new HashMap<>();
    
    /**
     * Map of lod to mesh indices.
     */
    private final Map<Integer, Integer> _lodToMeshIdx = new HashMap<>();
    
    private float[] _radius = { 1f, 1f, 1f};
    
    private int _lod = 2;
    
    private Color _color = Color.WHITE;
    
    public SphereFactory(GltfWriter _writer) {
        super("sphere");
        this._builder.setIsPatterned(false);
        this._builder.setColor(Color.WHITE);
        
        // need to set BLEND mode or transparency does not work.
        _writer.setAlphaMode(AlphaMode.BLEND);
        
        this._writer = _writer;
    }
    
    public void setMaxDetail(int val) { this._lod = val; }
    
    public void setColor(Color color) { this._color = color; }
    
    /**
     * Set the radius of the sphere. Note that if you translate points with setTransform() then the radius is in
     * the units of the transformed frame. 
     * @param radius
     */
    public void setRadius(float radius) { this._radius = new float[] { radius, radius, radius }; }
    
    /**
     * Add a sphere at the given position.
     * @param xPos
     * @param yPos
     * @param zPos
     * @throws Exception
     */
    public Node addSphere(Point3f pos) throws Exception {
        Integer meshIdx = getMeshColorLod();
        
        Node node = new Node();
        int nodeIdx = this._writer.addNode(node);
        node.setMesh(meshIdx);
        node.setName(String.format("%s[%d]-node", getName(), nodeIdx));
        node.setScale(this._radius);

        getTransform().transform(pos);
        
        if(LOG.isDebugEnabled()) {
            String colorStr = String.format("r=%d,g=%d,b=%d,a=%d", 
                    this._color.getRed(), this._color.getGreen(), this._color.getBlue(), this._color.getAlpha());
            LOG.debug("Add Sphere: pos=<{}> radius=<{}> color=<{}> ", pos, this._radius[0], colorStr);
        }
        
        float[] translation = {pos.x, pos.y, pos.z}; 
        node.setTranslation(translation);
        
        return node;
    }
    
    /**
     * Create a new mesh for the color/LOD or return a cached version.
     * @return
     * @throws Exception
     */
    private int getMeshColorLod() throws Exception {
        String key = String.format("%X-%d", this._color.getRGB(), this._lod);
        Integer meshIdx = this._colorLodToMeshIdx.get(key);
        if(meshIdx != null) {
            // found a cached mesh for this color/lod combo
            return meshIdx;
        }
        
        meshIdx = getMeshLod();
        
        // add this sphere to the cache.
        this._colorLodToMeshIdx.put(key, meshIdx);
        return meshIdx;
    }
    
    /**
     * Create a new mesh for the LOD or return a cached version.
     * @return
     * @throws Exception
     */
    private int getMeshLod() throws Exception {
        Integer meshIdx = this._lodToMeshIdx.get(this._lod);
        if(meshIdx != null) {
            // found a mesh for the LOD. 
            // create a copy of this mesh with the new color
            int newMeshIdx = copyMesh(meshIdx, this._color);
            return newMeshIdx;
        }
        
        // create a new mesh for this LOD
        meshIdx = createMesh(this._color, this._lod);

        // add this sphere to the cache.
        this._lodToMeshIdx.put(this._lod, meshIdx);
        return meshIdx;
    }
    
    /**
     * Copy a mesh with a material having a new color.
     * @param origMeshIdx
     * @return
     */
    private int copyMesh(int origMeshIdx, Color color) {
        List<Mesh> meshList = this._writer.getGltf().getMeshes();
        Mesh origMesh = meshList.get(origMeshIdx);
        
        Mesh newMesh = new Mesh();
        meshList.add(newMesh);
        int newMeshIdx = meshList.indexOf(newMesh);

        LOG.debug("Copy Mesh: <{}> {} -> {}", origMesh.getName(), origMeshIdx, newMeshIdx);
        String name = String.format("%s[%d]", origMesh.getName(), newMeshIdx);
        newMesh.setName(name);
        
        MeshPrimitive newMeshPr = new MeshPrimitive();
        newMesh.addPrimitives(newMeshPr);
        MeshPrimitive orighMeshPr = origMesh.getPrimitives().get(0);
        
        newMeshPr.setIndices(orighMeshPr.getIndices());
        newMeshPr.setMaterial(orighMeshPr.getMaterial());
        newMeshPr.setMode(orighMeshPr.getMode());
        newMeshPr.setAttributes(orighMeshPr.getAttributes());
        
        // create the new material
        Material material = newMaterial(color);
        int materialIdx = this._writer.getGltf().getMaterials().indexOf(material);
        newMeshPr.setMaterial(materialIdx);
        
        return newMeshIdx;
    }
    
    /**
     * Create a new mesh for the given color and LOD.
     * @param color
     * @param lod
     * @return
     * @throws Exception
     */
    private int createMesh(Color color, int lod) throws Exception {
        // Get the next mesh ID.
        int meshIdx = 0;
        List<Mesh> meshList = this._writer.getGltf().getMeshes();
        if(meshList != null) {
            meshIdx = meshList.size();
        }

        this._builder.addIcosphere(lod);
        
        // set the name of the builder so that all objects in the JSON can be
        // identified with this sphere
        String name = String.format("%s(%d)[%d]", getName(), lod, meshIdx);
        this._builder.setName(name);
        LOG.info("Create Sphere for LOD: <{}> {}", lod, name);
        
        Material material = newMaterial(color);
        this._builder.setMaterial(material);
        meshIdx = this._builder.buildMesh(this._writer);
        
        return meshIdx;
    }
    
    /**
     * Get a new material with an appropriate color.
     * @return
     */
    private Material newMaterial(Color color) {
        Material material = this._writer.newMaterial("sphere", 0.7f, 0.5f);
        MaterialPbrMetallicRoughness pbr = material.getPbrMetallicRoughness();
        float[] components = color.getRGBComponents(null);
        pbr.setBaseColorFactor(components);
        return material;
    }
    
}
