/**
 * Copyright 1993-2019 Agree Tech.
 * All rights reserved.
 */
package cn.com.agree.gradle.ameba.tasks.bundling;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.bundling.Jar;

/**
 *
 * @author PuYun pu.yun@agree.com.cn
 */
public class AmebaJar extends Jar {

	private FileCollection classpath;

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

	/**
	 * 
	 */
	private void configMenifest() {
		Attributes attributes = getManifest().getAttributes();
		attributes.putIfAbsent("Hello-World", true);
	}

	public void classpath(Object... classpath) {
		FileCollection existingClasspath = this.classpath;
		this.classpath = getProject().files((existingClasspath != null) ? existingClasspath : Collections.emptyList(),
				classpath);
	}

	public void setClasspath(Object classpath) {
		this.classpath = getProject().files(classpath);
	}

	public void setClasspath(FileCollection classpath) {
		this.classpath = getProject().files(classpath);
	}

	private Action<CopySpec> classpathFiles(Spec<File> filter) {
		return (copySpec) -> copySpec
				.from((Callable<Iterable<File>>) () -> (this.classpath != null) ? this.classpath.filter(filter)
						: Collections.emptyList());
	}

	@InputFiles
	public FileCollection getClasspath() {
		return this.classpath;
	}
}
