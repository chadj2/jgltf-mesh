/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package com.kinetica.mesh;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
 * Write content to a gltf file.
 * @author chadjuliano
 *
 */
public class GltfWriter {
    
    private final static Logger LOG = LoggerFactory.getLogger(GltfWriter.class);
    
    public enum AlphaMode { OPAQUE, MASK, BLEND, OPAQUE_DS }

    // sampler
    private static final int FILTER_LINEAR = 9729;
    //private static final int FILTER_LINEAR_MIPMAP_LINEAR = 9987;
    private static final int WRAP_CLAMP_TO_EDGE = 33071;
    //private static final int WRAP_MIRRORED_REPEAT = 33648;
    
    private AlphaMode _alphaMode = AlphaMode.OPAQUE;
    private static final int MAX_BUFFER_SIZE = 50*1024*1024;

    public static enum GltfFormat { gltf, glb }
    
    public final GlTF _gltf = new GlTF();
    public final ByteBuffer _byteBuffer = Buffers.create(MAX_BUFFER_SIZE);
    private final Map<String, Object> _metaParams = new TreeMap<>();
    private String _basePath = ".";
    
    private final Scene _topScene = new Scene();
    
    public GltfWriter() {
        this._gltf.addScenes(this._topScene);
    }
    
    public void setBasePath(File _path) {
        this._basePath = _path.getPath();
    }
    
    public void setAlphaMode(AlphaMode _alphaMode) {
        this._alphaMode = _alphaMode;
    }
    
    public void addNode(Node _node) {
        this._gltf.addNodes(_node);
        List<Node> _gltfList = this._gltf.getNodes();
        this._topScene.addNodes(_gltfList.indexOf(_node));
    }
    
    public void writeGltf(OutputStream _os, GltfFormat _format) throws Exception {
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
    
    public void writeGltf(File _outFile) throws Exception {
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
    
    public void setMetaParam(String _key, Object _value) {
        this._metaParams.put(_key, _value);
    }

    public Material addMaterial(String _imageFile) {

        Material _material = new Material();
        this._gltf.addMaterials(_material);
        
        MaterialPbrMetallicRoughness _roughness = new MaterialPbrMetallicRoughness();
        _material.setPbrMetallicRoughness(_roughness);
        _roughness.setMetallicFactor(0.05f);
        _roughness.setRoughnessFactor(0.5f);

        switch(this._alphaMode) {
            case OPAQUE:
                _material.setAlphaMode("OPAQUE");
                break;
            case MASK:
                _material.setAlphaMode("MASK");
                _material.setAlphaCutoff(0.5f);
                _material.setDoubleSided(true);
                break;
            case BLEND:
                _material.setAlphaMode("BLEND");
                _material.setDoubleSided(true);
                _roughness.setBaseColorFactor(new float[] {1f, 1f, 1f, 1f} );
                break;
            case OPAQUE_DS: 
                _material.setDoubleSided(true);
                _material.setAlphaMode("OPAQUE");
                _roughness.setBaseColorFactor(new float[] {1f, 1f, 1f, 1f} );
                break;
        }

        if(_imageFile != null) {
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
            _roughness.setBaseColorTexture(_texInfo);
    
            _material.setName(_imageFile);
        }
        else {
            _material.setName("default");
        }

        int _idx = this._gltf.getMaterials().indexOf(_material);
        LOG.debug("New Material[{}]: idx=<{}> alpha=<{}>", _material.getName(), _idx, _material.getAlphaMode());
        
        return _material;
    }
    
    private GltfModelV2 getGltfModel() throws Exception {
        GltfAssetV2 _gltfAsset = createGltfAsset();
        GltfModelV2 _gltfModel = new GltfModelV2(_gltfAsset);
        return _gltfModel;
    }
    
    private GltfAssetV2 createGltfAsset() throws Exception {
        Asset _asset = new Asset();
        this._gltf.setAsset(_asset);
        _asset.setVersion("2.0");
        _asset.setGenerator(this.getClass().getSimpleName());
        _asset.setCopyright("2018 Kinetica DB");
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