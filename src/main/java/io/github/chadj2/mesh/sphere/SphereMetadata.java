/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.sphere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javagl.jgltf.impl.v2.GlTF;
import io.github.chadj2.mesh.MeshGltfWriter;
import io.github.chadj2.mesh.buffer.BufferMetadataString;
import io.github.chadj2.mesh.extsm.ClassProperty;
import io.github.chadj2.mesh.extsm.GlTFStructuralMetadata;
import io.github.chadj2.mesh.extsm.MetadataClass;
import io.github.chadj2.mesh.extsm.PropertyTable;
import io.github.chadj2.mesh.extsm.PropertyTableProperty;
import io.github.chadj2.mesh.extsm.Schema;

/**
 * Add metadata tables for EXT_structural_metadata extension.
 * @author Chad Juliano
 */
public class SphereMetadata {
    
    private static final Logger LOG = LoggerFactory.getLogger(SphereMetadata.class);

    private static final String EXT_STRUCT_META = "EXT_structural_metadata";
    private static final String CLASS_SPHERE = "sphere";
    private static final String PROP_EVENT_ID = "event_id";
    
    private final BufferMetadataString _eventId = new BufferMetadataString(PROP_EVENT_ID);
    
    private int _tableIdx = 0;

    /**
     * Add an eventId to the table.
     * @param eventId
     * @return
     */
    public int addEventId(String eventId) {
        this._eventId.add(eventId);
        int featureId = this._eventId.size() - 1;
        return featureId;
    }
    
    /**
     * Get table size.
     * @return
     */
    public int size() { return this._eventId.size(); }

    /**
     * Get index of table in the propertyTables.
     * @return
     */
    public int getTableIdx() { return this._tableIdx; }

    /**
     * Write schema, table, and data.
     * @param writer
     */
    public void build(MeshGltfWriter writer) {
        GlTF gltf = writer.getGltf();
        
        gltf.addExtensionsUsed(EXT_STRUCT_META);
        LOG.info("Adding extension: {}", EXT_STRUCT_META);

        GlTFStructuralMetadata gltfStuctMeta = new GlTFStructuralMetadata();
        gltf.addExtensions(EXT_STRUCT_META, gltfStuctMeta);
        
        addSchema(gltfStuctMeta);
        
        PropertyTable table = addTable(gltfStuctMeta);
        this._tableIdx = gltfStuctMeta.getPropertyTables().indexOf(table);

        PropertyTableProperty ptProp = this._eventId.createProperty(writer);
        table.addProperties(PROP_EVENT_ID, ptProp);
    }
    
    private static void addSchema(GlTFStructuralMetadata gltfStuctMeta) {
        Schema schema = new Schema();
        gltfStuctMeta.setSchema(schema);
        schema.setName("Sphere Events");
        
        MetadataClass mClass = new MetadataClass();
        schema.addClasses(CLASS_SPHERE, mClass);
        mClass.setName("Sphere");
        
        ClassProperty property = new ClassProperty();
        mClass.addProperties(PROP_EVENT_ID, property);
        property.setName("Event ID");
        property.setType("STRING");
        property.setRequired(true);
    }
    
    private PropertyTable addTable(GlTFStructuralMetadata gltfStuctMeta) {
        PropertyTable propTable = new PropertyTable();
        gltfStuctMeta.addPropertyTables(propTable);
        
        propTable.setClassProperty(CLASS_SPHERE);
        propTable.setName("Sphere");
        propTable.setCount(size());
        
        return propTable;
    }
}
