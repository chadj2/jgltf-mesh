/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Sampler;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.impl.v2.Texture;
import de.javagl.jgltf.impl.v2.TextureInfo;
import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfReference;
import de.javagl.jgltf.model.io.GltfReferenceResolver;
import de.javagl.jgltf.model.io.GltfWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetsV2;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.GltfModelCreatorV2;

/**
 * Serialize added nodes to glTF format.
 * @author Chad Juliano
 */
public class MeshGltfWriter {
    
    /**
     * The AlphaMode is used when creating a Material.
     */
    public enum AlphaMode { 
        /** Mesh is opaque but invisible from one size. */
        OPAQUE,
        
        /** Mesh is opaque and double sided. */
        OPAQUE_DS,
        
        /** Texture contains a mask that makes certain sections transparent. */
        MASK, 
        
        /** Texture alpha channel is used to blend regions with the background. */
        BLEND,

        /** Double sided version of BLEND. */
        BLEND_DS
    }
    
    /**
     * Indicates if the gltf metadata should be JSON or binary.
     */
    public enum GltfFormat { gltf, glb }

    private final static Logger LOG = LoggerFactory.getLogger(MeshGltfWriter.class);

    /** Largest size of byte buffer we will support. */
    private static final int MAX_BUFFER_SIZE = 50*1024*1024;

    /** Buffer used for primitive serialization. */
    private final ByteBuffer _byteBuffer = Buffers.create(MAX_BUFFER_SIZE);

    private final GlTF _gltf = new GlTF();
    
    /** Contains metadata for the glTF Asset type */
    private final Map<String, Object> _metaParams = new TreeMap<>();
    
    /** The one and only Scene. */
    private final Scene _topScene = new Scene();
    
    /** Alpha mode used for creating materials. */
    private AlphaMode _alphaMode = AlphaMode.OPAQUE;
    
    /** Path for finding texture files. */
    private String _basePath = ".";
    
    /** Copyright for glTF Asset type */
    private String _copyright = "";
    
    /** These nodes will get added to the GlTF object at write time */
    private final List<Node> _nodes = new ArrayList<>();
    
    public MeshGltfWriter() {
        this._gltf.addScenes(this._topScene);
    }
    
    /**
     * Set path for resolving images.
     */
    public void setBasePath(File _path) { this._basePath = _path.getPath(); }
    
    /** 
     * Set the alpha mode to use when creating materials. If your mesh is visible from both sides
     * then you should set this to OPAQUE_DS.
     */
    public void setAlphaMode(AlphaMode _alphaMode) { this._alphaMode = _alphaMode; }
    
    /**
     * Get the buffer used for serializing primitives.
     */
    public ByteBuffer getBuffer() { return this._byteBuffer; }
    
    /**
     * Get the GlTF used for writing metadata.
     */
    public GlTF getGltf() { return this._gltf; }
    
    /**
     * Set extra metadata in the glTF Asset.
     */
    public void setMetaParam(String _key, Object _value) { this._metaParams.put(_key, _value); }
    
    /**
     * Set the copyright in the glTF Asset.
     */
    public void setCopyright(String _value) { this._copyright = _value; }
    
    /**
     * Add a node to the default Scene.
     * @return index of the node
     */
    public int addNode(Node _node) {
        this._nodes.add(_node);
        return this._nodes.indexOf(_node);
    }
    
    private static final float DEFAULT_METALLIC_FACTOR = 0.5f;
    
    private static final float DEFAULT_ROUGHNESS_FACTOR = 0.75f;
    
    /**
     * Create a default material. You would use this if you are not using a texture and you
     * are specifying vertex colors.
     */
    public Material newDefaultMaterial() { 
        return newMaterial("default", DEFAULT_METALLIC_FACTOR, DEFAULT_ROUGHNESS_FACTOR); 
    }

    /**
     * Add a material with optional texture. 
     * @param _imageFile The image to use for the texture or null if none.
     */
    public Material newTextureMaterial(String _imageFile) {
        Material _material = newMaterial(_imageFile, DEFAULT_METALLIC_FACTOR, DEFAULT_ROUGHNESS_FACTOR);

        Sampler _sampler = new Sampler();
        this._gltf.addSamplers(_sampler);
        _sampler.setMagFilter(GltfConstants.GL_LINEAR);
        _sampler.setMinFilter(GltfConstants.GL_LINEAR);
        _sampler.setWrapS(GltfConstants.GL_CLAMP_TO_EDGE);
        _sampler.setWrapT(GltfConstants.GL_CLAMP_TO_EDGE);
        
        Image _image = new Image();
        this._gltf.addImages(_image);
        _image.setName(_imageFile);
        _image.setUri(_imageFile);

        Texture _texture = new Texture();
        this._gltf.addTextures(_texture);
        _texture.setSampler(this._gltf.getSamplers().indexOf(_sampler));
        _texture.setSource(this._gltf.getImages().indexOf(_image));
        
        TextureInfo _texInfo = new TextureInfo();
        _texInfo.setIndex(this._gltf.getTextures().indexOf(_texture));
        MaterialPbrMetallicRoughness _roughness = _material.getPbrMetallicRoughness();
        _roughness.setBaseColorTexture(_texInfo);

        return _material;
    }
    
    public Material newBlendMaterial(String name, 
            float metallicFactor, float roughnesFactor, Color color) {
        Material material = this.newMaterial(name, metallicFactor, roughnesFactor);
        
        MaterialPbrMetallicRoughness pbr = material.getPbrMetallicRoughness();
        float[] components = color.getRGBComponents(null);
        pbr.setBaseColorFactor(components);
        return material;
    }
    
    public Material newMaterial(String name, float metallicFactor, float roughnesFactor) {
        Material _material = new Material();
        this._gltf.addMaterials(_material);
        
        int _idx = this._gltf.getMaterials().indexOf(_material);
        _material.setName(String.format("%s[%d]", name, _idx));
        LOG.debug("New Material:  alpha=<{}>", _material.getName(), _material.getAlphaMode());
        
        MaterialPbrMetallicRoughness _roughness = new MaterialPbrMetallicRoughness();
        _material.setPbrMetallicRoughness(_roughness);
        _roughness.setMetallicFactor(metallicFactor);
        _roughness.setRoughnessFactor(roughnesFactor);
        _material.setDoubleSided(false);

        switch(this._alphaMode) {
            case OPAQUE_DS:
                _material.setDoubleSided(true);
            case OPAQUE:
                _material.setAlphaMode(AlphaMode.OPAQUE.name());
                break;
            case MASK:
                _material.setAlphaMode(AlphaMode.MASK.name());
                _material.setAlphaCutoff(0.5f);
                _material.setDoubleSided(true);
                break;
            case BLEND_DS:
                _material.setDoubleSided(true);
            case BLEND:
                _material.setAlphaMode(AlphaMode.BLEND.name());
                _roughness.setBaseColorFactor(new float[] {1f, 1f, 1f, 1f} );
                break;
        }
        
        return _material;
    }

    /**
     * Write a gltf to a file. The filename should have a gltf or glb extension to indicate 
     * the type.
     * @param outFile 
     */
    public void writeGltf(File outFile) throws Exception {
        String ext = FilenameUtils.getExtension(outFile.getName());
        GltfFormat format = GltfFormat.valueOf(ext);
        MeshGltfWriter.LOG.info("Writing glTF: {}", outFile.getAbsolutePath());
        
        try (OutputStream os = new FileOutputStream(outFile))
        {
            writeGltf(os, format);
        }
    }
    
    /**
     * Write gltf to an OutputStream. Specify gltf or glb format.
     * @param os 
     * @param format Indicates if this is JSON or binary format.
     */
    public void writeGltf(OutputStream os, GltfFormat format) throws Exception {
        this._gltf.setNodes(this._nodes);
        
        List<Integer> rangeList = IntStream
                .range(0, this._nodes.size())
                .boxed()
                .collect(Collectors.toList());
        this._topScene.setNodes(rangeList);
        
        GltfAssetV2 gltfAsset = newGltfAsset();
        DefaultGltfModel gltfModel =  GltfModelCreatorV2.create(gltfAsset);
        GltfModelWriterV2 gltfModelWriter = new GltfModelWriterV2();
        
        if(format == GltfFormat.gltf) {
            // With the introduction of DefaultGltfModel in JglTF version 2.0.3 
            // there was a change that broke the way the asset and usedExtensions 
            // sections are written. The new workflow is:
            // 1. Create GltfAsset
            // 2. Create DefaultGltfModel with GltfAsset.
            // 3. Create new embedded asset with the DefaultGltfModel.
            // 4. New embedded asset has new GlTF object which is passed to GltfWriter.
            //
            // There are currently some bugs in this approach where some contents
            // of the GLTF file are not passed from the old asset to the new embedded asset.
            // Once this is resolved the GltfModelWriter can be used again.
            writeEmbedded(gltfModel, os);
            //_gltfModelWriter.writeEmbedded(_gltfModel, _os);
        }
        else if(format == GltfFormat.glb) {
            gltfModelWriter.writeBinary(gltfModel, os);
        }
    }
    
    private void writeEmbedded(DefaultGltfModel gltfModel, OutputStream os) throws IOException {
        GltfAssetV2 embeddedAsset = GltfAssetsV2.createEmbedded(gltfModel);
        GlTF embeddedGltf = embeddedAsset.getGltf();
        
        // workaround to copy extensions and asset from old gltf object.
        embeddedGltf.setExtensionsUsed(this._gltf.getExtensionsUsed());
        embeddedGltf.setExtensionsRequired(this._gltf.getExtensionsRequired());
        embeddedGltf.setAsset(this._gltf.getAsset());

        GltfWriter gltfWriter = new GltfWriter();
        gltfWriter.write(embeddedGltf, os);
    }
    
    private GltfAssetV2 newGltfAsset() throws Exception {
        Asset _asset = new Asset();
        this._gltf.setAsset(_asset);
        _asset.setVersion("2.0");
        _asset.setGenerator("jglTF-mesh");
        _asset.setCopyright(this._copyright);
        _asset.setExtras(this._metaParams);
        
        this._metaParams.forEach((_k, _v) -> LOG.debug("attribute[{}] = {}", _k, _v));
        
        // add buffer to glTF
        Buffer _gltfBuffer = getGltfBuffer();
        this._gltf.addBuffers(_gltfBuffer);
        
        GltfAssetV2 _gltfAsset = new GltfAssetV2(this._gltf, this._byteBuffer);
        resolveImages(_gltfAsset);
        
        return _gltfAsset;
    }
    
    private Buffer getGltfBuffer() throws Exception {
        // flip the buffer for read
        this._byteBuffer.flip();
        int _totalSize = this._byteBuffer.remaining();
        if(_totalSize <= 0) {
            throw new Exception("glTF buffer has no data to write.");
        }
        
        // add buffer to glTF
        Buffer _gltfBuffer = new Buffer();
        _gltfBuffer.setByteLength(_totalSize);
        LOG.debug("Created glTF buffer: size=<{} bytes>", _totalSize);
        
        return _gltfBuffer;
    }

    private void resolveImages(GltfAssetV2 _gltfAsset) {
        List<GltfReference> _refList =  _gltfAsset.getImageReferences();
        URI _baseUri = Paths.get(this._basePath).toAbsolutePath().toUri();
        LOG.debug("Resolving images with base: {}", _baseUri.getPath());
        GltfReferenceResolver.resolveAll(_refList, _baseUri);
        
        Map<String,ByteBuffer> _refDatas = _gltfAsset.getReferenceDatas();
        for(Entry<String,ByteBuffer> _entry : _refDatas.entrySet()) {
            LOG.debug("Image[{}]: <{} bytes>", _entry.getKey(), _entry.getValue().remaining());
        }
    }
    
}