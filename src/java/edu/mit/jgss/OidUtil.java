package edu.mit.jgss;

import java.io.*;
import java.util.regex.*;

/**
 * Contains utility functions for Oid object conversions.
 */
public class OidUtil {

    /**
     * Converts a DER-encoded Oid from a byte array to a dot-separated
     * String representation.
     *
     * @param oid DER-encoded Oid byte array
     * @return Oid in dot-separated String representation
     *
     */
    public static String OidDer2String(byte[] oid) {
        // TODO
        return null;
    }

    /**
     * Converts a DER-encoded Oid from an InputStream to a dot-separated
     * String representation.
     *
     * @param oid DER-encoded Oid InputStream
     * @return Oid in dot-separated String representation
     *
     */
    public static String OidStream2String(InputStream oid) throws IOException {
        
        byte tag;
        byte len;
        byte[] tmpOid;

        try {
            tag = (byte) oid.read();
            len = (byte) oid.read();

            tmpOid = new byte[len+2];
            tmpOid[0] = tag;
            tmpOid[1] = len;

            for (int i = 0; i < len; i++) {
                tmpOid[i+2] = (byte) oid.read();
            }
        } catch (IOException e) {
            throw new IOException("I/O Error occurred when reading InputStream");
        }

        return OidDer2String(tmpOid);
    }

    /**
     * Converts an Oid in dot-separated string representation to a
     * DER-encoded byte array.
     *
     * @param oid Oid in dot-separated String representation
     * @return DER-encoded Oid byte array
     *
     */
    public static byte[] OidString2Der(String oid) {
       
        int octet = 0; 
        int base = 0;
        int times = 0;
        int tbMaj = 0;

        ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
        ByteArrayOutputStream tmpArray = new ByteArrayOutputStream();

        System.out.println("String = " + oid);

        /* Convert String to int[] */
        String[] tmp = oid.split(".");
        for (int j = 0; j < tmp.length; j++) {
            System.out.println(tmp[j]);
        }
        int[] input = new int[tmp.length];
        for (int i = 0; i < tmp.length; i++ ) {
            input[i] = Integer.parseInt(tmp[i]);
        }

        /* Add tag for OID (0x06) */
        bytearray.write(6);

        /* Calculate first byte */
        tmpArray.write((input[0]*40) + input[1]);

        /* Encode the rest of the OID nodes in DER format */
        for (int j = 2; j < input.length; j++) {

            if (input[j] < 127) {
                /* Encode directly */
                tmpArray.write(input[j]);
            } else if (input[j] > 127) {

                /* Reset variables */
                octet = input[j];
                base = 128;
                times = 0;
                tbMaj = 0;

                /* If bigger than 16383 */
                if (octet > 16383) {

                    base = 262144;
                    times = (int) Math.floor(octet / base);
                    tbMaj = 8 + times;
                    octet = octet - (times * base);

                    base = 16384;
                    times = (int) Math.floor(octet / base);
                    tmpArray.write((16*tbMaj) + times);

                    /* Reset tbMaj in case we're skipping next if */
                }

                /* 2047 < octet <= 16383 */
                if (octet > 2047 && octet <= 16383) {

                    base = 2048;
                    times = (int) Math.floor(octet / base);
                    tbMaj = 8 + times;
                    octet = octet - (times * base);
                }

                /* 127 < octet < 2047 */
                base = 128;
                times = (int) Math.floor(octet / base);
                tmpArray.write((16 * tbMaj) + times);
                tmpArray.write(octet - (times * 128));
            }
        }

        byte[] convArray = tmpArray.toByteArray();
        bytearray.write(convArray.length);
        for (int k = 0; k < convArray.length; k++) {
            bytearray.write(convArray[k]);
        }

        return bytearray.toByteArray();

    } /* end OidString2Der */

    /**
     * Verifies that an OID is in dot-separated string format.
     * TODO: Make this more robust.
     *
     * @param oid OID to verify
     * @return true if OID is valid, false otherwise
     */
    public static boolean verifyOid(String oid) {
        
        // Pattern to match '.' separated Oid string format
        Pattern oidPattern = Pattern.compile("^([0-9]+.{1})+[0-9]+$");
        Matcher oidIN = oidPattern.matcher(oid);
        
        if (oidIN.matches()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Verifies DER-encoded byte array has a valid tag and length
     *
     * @param oid OID to verify, DER-encoded byte array
     * @return true if OID is valid, false otherwise
     */
    public static boolean verifyOid(byte[] oid) {
       
        /* verify tag is set to an OID (0x06) */
        if (oid[0] != 6)
            return false;

        /* verify length is correct */
        if (oid.length-2 != oid[1]) {
            return false;
        }
        
        return true;
    }

}
