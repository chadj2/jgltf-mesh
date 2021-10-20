/* 
 * Copyright (c) 2019, Chad Juliano, Kinetica DB Inc.
 * 
 * SPDX-License-Identifier: MIT
 */

package io.github.chadj2.mesh.test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.Test;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfModelReader;

public class TestGltfWriter {
    
    @Test 
    public void testReadGLtf() throws IOException {
        GltfModelReader gltfModelReader = new GltfModelReader();

        File _outputFile = Paths.get("triangle_copy.gltf").toFile();
        @SuppressWarnings("unused")
        GltfModel gltfModel = gltfModelReader.read(_outputFile.toURI());
    }
    
    @Test
    public void testDecodeBase64() {
        String base64 = "AAABAAIAAQADAAIAAAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAAAACAPwAAgD8AAAAAAAAAAAAAgD8AAAAAAACAPwAAgD8AAAAAAAAAAAAAAAAAAAAAAACAPwAAAAAAAAAA";
        byte data[] = Base64.getDecoder().decode(base64);
        ByteBuffer byteBuffer = Buffers.create(data);
        
        System.out.println("Size: " + data.length);
        
        for(int i = 0; i < 6; i++) {
            System.out.print(byteBuffer.getShort());
            System.out.print(", ");
        }

        System.out.println();
        System.out.println("Buffer pos: " + byteBuffer.position());
        
        for(int i = 0; i < 4; i++) {
            float _x = byteBuffer.getFloat();
            float _y = byteBuffer.getFloat();
            float _z = byteBuffer.getFloat();
            System.out.println(String.format("(%f, %f, %f)", _x, _y, _z));
        }

        System.out.println("Buffer pos: " + byteBuffer.position());

        for(int i = 0; i < 4; i++) {
            float _x = byteBuffer.getFloat();
            float _y = byteBuffer.getFloat();
            float _z = byteBuffer.getFloat();
            System.out.println(String.format("(%f, %f, %f)", _x, _y, _z));
        }

        System.out.println("Buffer pos: " + byteBuffer.position());
    }

//    @Test 
//    public void testLaunchBrowser() throws InterruptedException {
//        // this is set to exit in close so this thread will be terminated.
//        String[] _args = {  };
//        GltfBrowser.main(_args);
//        Thread.currentThread().join();
//    }

}
