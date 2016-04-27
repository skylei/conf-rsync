package com.confsync.common.utils;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;

public class ZipUtil {

	public static void zip(String src, String dest) {
		Zip zip = new Zip();
		zip.setBasedir(new File(src));
		zip.setDestFile(new File(dest));
		Project p = new Project();
		p.setBaseDir(new File(src));
		zip.setProject(p);
		zip.execute();
	}

	public void testZip() {
		zip("c:/test.zip", "e:/test");
	}
}
