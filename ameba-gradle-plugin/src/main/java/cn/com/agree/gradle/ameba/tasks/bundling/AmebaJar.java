/**
 * Copyright 1993-2019 Agree Tech.
 * All rights reserved.
 */
package cn.com.agree.gradle.ameba.tasks.bundling;

import java.util.Collections;

import org.gradle.api.file.FileCollection;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.bundling.Jar;

/**
 *
 * @author PuYun pu.yun@agree.com.cn
 */
public class AmebaJar extends Jar {

	private FileCollection classpath;

	public void classpath(Object... classpath) {
		FileCollection existingClasspath = this.classpath;
		this.classpath = getProject().files((existingClasspath != null) ? existingClasspath : Collections.emptyList(),
				classpath);
	}

	/**
	 * 
	 */
	private void configMenifest() {
		Attributes attributes = getManifest().getAttributes();
		attributes.putIfAbsent("Hello-World", true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gradle.api.tasks.AbstractCopyTask#copy()
	 */
	@Override
	protected void copy() {
		configMenifest();
		super.copy();
	}

	@InputFiles
	public FileCollection getClasspath() {
		return this.classpath;
	}

	public void setClasspath(FileCollection classpath) {
		this.classpath = getProject().files(classpath);
	}

	public void setClasspath(Object classpath) {
		this.classpath = getProject().files(classpath);
	}
}
