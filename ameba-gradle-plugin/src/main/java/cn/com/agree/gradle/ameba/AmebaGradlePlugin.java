/**
 * Copyright 1993-2019 Agree Tech.
 * All rights reserved.
 */
package cn.com.agree.gradle.ameba;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

import cn.com.agree.gradle.ameba.tasks.bundling.AmebaJar;

/**
 *
 * @author PuYun pu.yun@agree.com.cn
 */
public class AmebaGradlePlugin implements Plugin<Project> {
	
	private static final String AMEBA_JAR_TASK_NAME = "amebaJar";
	
	private AmebaModel ameba;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gradle.api.Plugin#apply(java.lang.Object)
	 */
	@Override
	public void apply(Project project) {

		// auto apply plugins
		applyPlugins(project);

		// ameba model
		ameba = project.getExtensions().create("ameba", AmebaModel.class);

		// java compatibility
		configJavaCompatibility(project);

		// encoding
		configEncoding(project);

		// eclipse
		configEclipse(project);

		// mkdir for sources
		configSourceFolders(project);

		// create config files
		configBuildFiles(project);
		
		// create tasks
		configTasks(project);
	}

	/**
	 * @param project
	 */
	private void configTasks(Project project) {
		AmebaJar amebaJar = project.getTasks().create(AMEBA_JAR_TASK_NAME,
				AmebaJar.class);
		amebaJar.setDescription("Assembles an ameba osgi jar archive.");
		amebaJar.setGroup(BasePlugin.BUILD_GROUP);
		amebaJar.classpath((Callable<FileCollection>) () -> {
			JavaPluginConvention convention = project.getConvention()
					.getPlugin(JavaPluginConvention.class);
			SourceSet mainSourceSet = convention.getSourceSets()
					.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
			return mainSourceSet.getRuntimeClasspath();
		});
	}

	/**
	 * @param project
	 * @param ameba
	 */
	private void configBuildFiles(Project project) {
		project.getTasks().getByName("eclipse").doLast((task) -> {
			File file = project.file("build.gradle");
			if (!file.exists() && ameba.getGradleTxt() != null) {
				try (OutputStream out = new FileOutputStream(file)) {
					out.write(ameba.getGradleTxt().getBytes("utf8"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			file = project.file(".settings/org.eclipse.buildship.core.prefs");
			if (!file.exists() && ameba.getBuildshipTxt() != null) {
				file.getParentFile().mkdirs();
				try (OutputStream out = new FileOutputStream(file)) {
					out.write(ameba.getBuildshipTxt().getBytes("utf8"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * @param project
	 */
	private void configSourceFolders(Project project) {
		project.getTasks().getByName("eclipseClasspath").doFirst((task) -> {
			project.mkdir("src/main/java");
			project.mkdir("src/main/resources");
			project.mkdir("src/test/java");
			project.mkdir("src/test/resources");
		});
	}

	/**
	 * @param project
	 */
	private void configEclipse(Project project) {
		EclipseModel eclipse = project.getExtensions().getByType(EclipseModel.class);
		eclipse.getProject().natures("org.eclipse.jdt.core.javanature",
				"org.eclipse.buildship.core.gradleprojectnature");
		eclipse.getProject().buildCommand("org.eclipse.buildship.core.gradleprojectbuilder");
		eclipse.getClasspath().containers("org.eclipse.buildship.core.gradleclasspathcontainer");
	}

	/**
	 * @param project
	 */
	private void configEncoding(Project project) {
		project.afterEvaluate((p) -> {
			p.getTasks().withType(JavaCompile.class, (task) -> task.getOptions().setEncoding("UTF-8"));
			p.getTasks().withType(Test.class, (task) -> task.systemProperty("file.encoding", "UTF-8"));
		});
	}

	/**
	 * @param project
	 */
	private void configJavaCompatibility(Project project) {
		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		javaConvention.setSourceCompatibility("1.8");
		javaConvention.setTargetCompatibility("1.8");
	}

	/**
	 * @param project
	 */
	private void applyPlugins(Project project) {
		project.getPlugins().apply("java");
		project.getPlugins().apply("eclipse");
		project.getPlugins().apply("org.springframework.boot");
	}

}
