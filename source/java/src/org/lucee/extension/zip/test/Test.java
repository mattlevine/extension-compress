package org.lucee.extension.zip.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Test {
	public static void maine(String[] args) throws ZipException {
		File file = new File("/Users/mic/Tmp3/testcase.zip");
		ZipFile zf = new ZipFile(file);
		zf.setRunInThread(false);
		zf.setPassword("Susi");
		zf.isRunInThread();
	}

	public static void main(String[] args) throws FileNotFoundException {

		try {
			// This is name and path of zip file to be created
			File file = new File("/Users/mic/Tmp3/test/teset.zip");
			if (file.exists()) file.delete();
			ZipFile zipFile = new ZipFile(file);

			// Add files to be archived into zip file
			ArrayList<File> filesToAdda = new ArrayList<File>();
			filesToAdda.add(new File("/Users/mic/Tmp3/act.log"));
			filesToAdda.add(new File("/Users/mic/Tmp3/susi.log"));

			// Initiate Zip Parameters which define various properties
			ZipParameters p = new ZipParameters();

			// Initiate Zip Parameters which define various properties
			ZipParameters parameters = new ZipParameters();
			p.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			p.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			// Set the encryption flag to true
			// p.setEncryptFiles(true);

			// Set the encryption method to AES Zip Encryption
			p.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

			// AES_STRENGTH_128 - For both encryption and decryption
			// AES_STRENGTH_192 - For decryption only
			// AES_STRENGTH_256 - For both encryption and decryption
			// Key strength 192 cannot be used for encryption. But if a zip file already has a
			// file encrypted with key strength of 192, then Zip4j can decrypt this file
			p.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

			// Set password
			p.setPassword("Susi");

			p.setFileNameInZip("ss/susi.log");
			p.setSourceExternalStream(true);
			p.setPassword("Susi");

			zipFile.addStream(new FileInputStream(new File("/Users/mic/Tmp3/act.log")), p);
			// zipFile.addFile(new File("/Users/mic/Tmp3/susi.log"), p);

			System.out.println("!!!!");

		}
		catch (ZipException e) {
			e.printStackTrace();
		}
	}
}