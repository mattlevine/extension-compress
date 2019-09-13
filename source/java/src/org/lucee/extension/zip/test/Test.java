package org.lucee.extension.zip.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class Test {
	public static void maine(String[] args) throws ZipException {
		File file = new File("/Users/mic/Tmp3/testcase.zip");
		ZipFile zf = new ZipFile(file);
		zf.setRunInThread(false);
		zf.setPassword("Susi".toCharArray());
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
			p.setCompressionMethod(CompressionMethod.DEFLATE); // set compression method to deflate compression

			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			p.setCompressionLevel(CompressionLevel.NORMAL);

			// Set the encryption flag to true
			// p.setEncryptFiles(true);

			// Set the encryption method to AES Zip Encryption
			p.setEncryptionMethod(EncryptionMethod.AES);

			// AES_STRENGTH_128 - For both encryption and decryption
			// AES_STRENGTH_192 - For decryption only
			// AES_STRENGTH_256 - For both encryption and decryption
			// Key strength 192 cannot be used for encryption. But if a zip file already has a
			// file encrypted with key strength of 192, then Zip4j can decrypt this file
			p.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

			// Set password
			// p.setPassword("Susi".toCharArray());

			p.setFileNameInZip("ss/susi.log");

			zipFile.addStream(new FileInputStream(new File("/Users/mic/Tmp3/act.log")), p);
			// zipFile.addFile(new File("/Users/mic/Tmp3/susi.log"), p);

			System.out.println("!!!!");

		}
		catch (ZipException e) {
			e.printStackTrace();
		}
	}
}