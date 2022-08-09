/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh;

import java.io.File;
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
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.io.GltfReference;
import de.javagl.jgltf.model.io.GltfReferenceResolver;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.v2.GltfModelV2;

/**
 * Serialize added nodes to glTF format.
 * @author Chad Juliano
 */
public class GltfWriter {
    
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

    private final static Logger LOG = LoggerFactory.getLogger(GltfWriter.class);

    private static final int FILTER_LINEAR = 9729;
    //private static final int FILTER_LINEAR_MIPMAP_LINEAR = 9987;
    private static final int WRAP_CLAMP_TO_EDGE = 33071;
    //private static final int WRAP_MIRRORED_REPEAT = 33648;
    
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
    
    public GltfWriter() {
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
     * @returns index of the node
     */
    public int addNode(Node _node) {
        this._nodes.add(_node);
        return this._nodes.indexOf(_node);
    }
    
    /**
     * Create a default material. You would use this if you are not using a texture and you
     * are specifying vertex colors.
     */
    public Material newDefaultMaterial() {
        Material _material = newMaterial();

        int _idx = this._gltf.getMaterials().indexOf(_material);
        LOG.debug("Default Material[{}]: idx=<{}> alpha=<{}>", _material.getName(), _idx, _material.getAlphaMode());

        String name = String.format("default[%d]", _idx);
        _material.setName(name);
        return _material;
    }

    /**
     * Add a material with optional texture. 
     * @param _imageFile The image to use for the texture or null if none.
     */
    public Material newTextureMaterial(String _imageFile) {
        Material _material = newMaterial();

        Sampler _sampler = new Sampler();
        this._gltf.addSamplers(_sampler);
        _sampler.setMagFilter(FILTER_LINEAR);
        _sampler.setMinFilter(FILTER_LINEAR);
        _sampler.setWrapS(WRAP_CLAMP_TO_EDGE);
        _sampler.setWrapT(WRAP_CLAMP_TO_EDGE);
        
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

        _material.setName(_imageFile);

        int _idx = this._gltf.getMaterials().indexOf(_material);
        LOG.debug("Texture Material[{}]: idx=<{}> alpha=<{}>", _material.getName(), _idx, _material.getAlphaMode());
        
        return _material;
    }
    
    private Material newMaterial() {
        Material _material = new Material();
        this._gltf.addMaterials(_material);
        
        MaterialPbrMetallicRoughness _roughness = new MaterialPbrMetallicRoughness();
        _material.setPbrMetallicRoughness(_roughness);
        _roughness.setMetallicFactor(0.05f);
        _roughness.setRoughnessFactor(0.5f);

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
     * Called before a write operation.
     */
    private void prepareWrite() {
        this._gltf.setNodes(this._nodes);
        
        List<Integer> rangeList = IntStream.range(0, this._nodes.size())
                .boxed()
                .collect(Collectors.toList());
        
        this._topScene.setNodes(rangeList);
    }
    
    /**
     * Write gltf to an OutputStream. Specify gltf or glb format.
     * @param _format Indicates if this is JSON or binary format.
     */
    public void writeGltf(OutputStream _os, GltfFormat _format) throws Exception {
        prepareWrite();
        
        GltfModelV2 _gltfModel = getGltfModel();
        GltfModelWriter _gltfModelWriter = new GltfModelWriter();
        
        if(_format == GltfFormat.gltf) {
            _gltfModelWriter.writeEmbedded(_gltfModel, _os);
        }
        else if(_format == GltfFormat.glb) {
            _gltfModelWriter.writeBinary(_gltfModel, _os);
        }
        else {
            throw new IOException("File extension not recognized: " + _format);
        }
    }

    /**
     * Write a gltf to a file. The filename should have a gltf or glb extension to indicate 
     * the type.
     */
    public void writeGltf(File _outFile) throws Exception {
        prepareWrite();
        
        GltfModelV2 _gltfModel = getGltfModel();
        GltfModelWriter _gltfModelWriter = new GltfModelWriter();

        String _ext = FilenameUtils.getExtension(_outFile.getName());
        GltfFormat _format = GltfFormat.valueOf(_ext);
        
        if(_format == GltfFormat.gltf) {
            _gltfModelWriter.writeEmbedded(_gltfModel, _outFile);
            GltfWriter.LOG.info("Wrote glTF: {}", _outFile.getAbsolutePath());
        }
        else if(_format == GltfFormat.glb) {
            _gltfModelWriter.writeBinary(_gltfModel, _outFile);
            GltfWriter.LOG.info("Wrote glb: {}", _outFile.getAbsolutePath());
        }
        else {
            throw new IOException("File extension not recognized: " + _ext);
        }
    }
    
    private GltfModelV2 getGltfModel() throws Exception {
        GltfAssetV2 _gltfAsset = newGltfAsset();
        GltfModelV2 _gltfModel = new GltfModelV2(_gltfAsset);
        return _gltfModel;
    }
    
    private GltfAssetV2 newGltfAsset() throws Exception {
        Asset _asset = new Asset();
        this._gltf.setAsset(_asset);
        _asset.setVersion("2.0");
        _asset.setGenerator("jglTF-mesh");
        _asset.setCopyright(this._copyright);
        _asset.setExtras(this._metaParams);
        
        this._metaParams.forEach((_k, _v) -> LOG.debug("attribute[{}] = {}", _k, _v));

        // flip the buffer for read
        this._byteBuffer.flip();
        int _totalSize = this._byteBuffer.remaining();
        if(_totalSize <= 0) {
            throw new Exception("glTF buffer has no data to write.");
        }
        
        LOG.debug("Created glTF buffer: size=<{} bytes>", _totalSize);
        
        // add buffer to glTF
        Buffer _gltfBuffer = new Buffer();
        this._gltf.addBuffers(_gltfBuffer);
        _gltfBuffer.setByteLength(_totalSize);
        
        GltfAssetV2 _gltfAsset = new GltfAssetV2(this._gltf, this._byteBuffer);
        resolveImages(_gltfAsset);
        
        return _gltfAsset;
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