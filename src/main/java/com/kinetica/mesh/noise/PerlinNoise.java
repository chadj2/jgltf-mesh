
package com.kinetica.mesh.noise;

/**
 * Improved Noise reference implementation 
 * COPYRIGHT 2002 KEN PERLIN.
 * 
 * Originally from: https://mrl.nyu.edu/~perlin/noise/
 * Also see SIGGRAPH 2002 paper: http://mrl.nyu.edu/~perlin/paper445.pdf
 * 
 * Code modified for readability by Chad Juliano.
 * @author Ken Perlin
 */
public final class PerlinNoise {

    // Doubled permutation to avoid overflow
    private static final int PERM[] = new int[512];
    
    static {
        // Hash lookup table as defined by Ken Perlin.  This is a randomly
        // arranged array of all numbers from 0-255 inclusive.
        final int permutation[] = { 151, 160, 137, 91, 90, 15, 131, 13, 201, 95,
            96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21,
            10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219,
            203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125,
            136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146,
            158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55,
            46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209,
            76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86,
            164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5,
            202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58,
            17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154,
            163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98,
            108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251,
            34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235,
            249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204,
            176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114,
            67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180 };
        
        for (int i = 0; i < 256; i++) {
            PERM[256 + i] = PERM[i] = permutation[i];
        }
    }
    
    /**
     * Generate perlin noise for multiple octaves.
     * @param x 
     * @param y
     * @param z
     * @param octaves Octaves to include
     * @param persistence Values < 1.0 will smooth out the result
     * @return
     */
    public static double noise(double x, double y, double z, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0; // Used for normalizing result to 0.0 - 1.0
        
        for(int i=0;i<octaves;i++) {
            total += noise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return total/maxValue;
    }
    
    /**
     * Generate perlin nose for a single octave.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double noise(double x, double y, double z) {
        
        // Calculate the "unit cube" that the point asked will be located in
        // The left bound is ( |_x_|,|_y_|,|_z_| ) and the right bound is that
        // plus 1.  Next we calculate the location (from 0.0 to 1.0) in that cube.
        // We also fade the location to smooth the result.
        
        // FIND UNIT CUBE THAT CONTAINS POINT.
        final int hashX = (int) Math.floor(x) & 255;
        final int hashY = (int) Math.floor(y) & 255;
        final int hashZ = (int) Math.floor(z) & 255;
        
        // FIND RELATIVE X,Y,Z OF POINT IN CUBE.
        final double relX = x - Math.floor(x);
        final double relY = y - Math.floor(y);
        final double relZ = z - Math.floor(z);
        
        // COMPUTE FADE CURVES FOR EACH OF X,Y,Z.
        final  double fadeX = fade(relX);
        final double fadeY = fade(relY);
        final double fadeZ = fade(relZ);
        
        // HASH COORDINATES OF THE 8 CUBE CORNERS
        final int hashA  = PERM[hashX]     + hashY;
        final int hashB  = PERM[hashX + 1] + hashY;
        final int hashAA = PERM[hashA]      + hashZ;
        final int hashAB = PERM[hashA + 1]  + hashZ; 
        final int hashBA = PERM[hashB]      + hashZ;
        final int hashBB = PERM[hashB + 1]  + hashZ;

        // The gradient function calculates the dot product between a pseudorandom
        // gradient vector and the vector from the input coordinate to the 8
        // surrounding points in its unit cube.
        // This is all then lerped together as a sort of weighted average based on the faded (u,v,w)
        // values we made earlier.
        final double c000 = grad(PERM[hashAA], relX,     relY,     relZ);
        final double c100 = grad(PERM[hashBA], relX - 1, relY,     relZ);
        final double c010 = grad(PERM[hashAB], relX,     relY - 1, relZ);
        final double c110 = grad(PERM[hashBB], relX - 1, relY - 1, relZ);
        final double c001 = grad(PERM[hashAA + 1], relX,     relY,     relZ - 1);
        final double c101 = grad(PERM[hashBA + 1], relX - 1, relY,     relZ - 1);
        final double c011 = grad(PERM[hashAB + 1], relX,     relY - 1, relZ - 1);
        final double c111 = grad(PERM[hashBB + 1], relX - 1, relY - 1, relZ - 1);

        // AND ADD BLENDED RESULTS FROM 8 CORNERS OF CUBE
        final double blend1 = lerp(fadeY, lerp(fadeX, c000, c100), lerp(fadeX, c010, c110));
        final double blend2 = lerp(fadeY,  lerp(fadeX, c001, c101), lerp(fadeX, c011, c111));
        return lerp(fadeZ, blend1, blend2);
    }

    /**
     * Fade function as defined by Ken Perlin.  This eases coordinate values
     * so that they will "ease" towards integral values.  This ends up smoothing
     * the final output.
     * 6t^5 - 15t^4 + 10t^3
     * @param t
     * @return
     */
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Linear interpolation
     * @param t
     * @param start
     * @param end
     * @return
     */
    private static double lerp(double t, double start, double end) {
        return start + t * (end - start);
    }

    /**
     * The gradient function calculates the dot product between a pseudorandom
     * gradient vector and the vector from the input coordinate 
     * @param hash
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static double grad(int hash, double x, double y, double z) {
        final int b1111 = 017; // 15
        final int b1000 = 010; // 8
        final int b0100 = 004; // 4
        final int b1100 = 014; // 12
        final int b1110 = 016; // 14
        
        // Take the hashed value and take the first 4 bits of it (15 == 0b1111)
        final int hash2 = hash & b1111; 
        
        // CONVERT LO 4 BITS OF HASH CODE INTO 12 GRADIENT DIRECTIONS.
        // If the most significant bit (MSB) of the hash is 0 then set u = x.  Otherwise y.
        final double u1;
        if(hash2 < b1000) { 
            u1 = x; 
        } 
        else { 
            u1 = y; 
        }
      
        final double v1;
        if(hash2 < b0100) {
            // If the first and second significant bits are 0 set v = y
            v1 = y;
        }
        else if(hash2 == b1100 || hash2 == b1110) {
            // If the first or second significant bits are 1 set v = x
            v1 = x;
        }
        else {
            // If the first and second significant bits are not equal (0/1, 1/0) set v = z
            v1 = z;
        }
        
        // Use the last 2 bits to decide if u and v are positive or negative.
        final double u2;
        if((hash2 & 1) == 0) {
            u2 = u1;
        }
        else {
            u2 = -u1;
        }
        
        final double v2;
        if((hash2 & 2) == 0) {
            v2 = v1;
        }
        else {
            v2 = -v1;
        }
        
        return u2 + v2;
    }
}
