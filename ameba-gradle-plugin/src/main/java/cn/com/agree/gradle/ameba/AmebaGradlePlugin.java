/**
 * Copyright 1993-2019 Agree Tech.
 * All rights reserved.
 */
package cn.com.agree.gradle.ameba;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

/**
 *
 * @author PuYun pu.yun@agree.com.cn
 */
public class AmebaGradlePlugin implements Plugin<Project>
{

    private AmebaModel ameba;

    /*
     * (non-Javadoc)
     * 
     * @see org.gradle.api.Plugin#apply(java.lang.Object)
     */
    @Override
    public void apply(Project project)
    {

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

        // config manifest
        configTasks(project);

        // config pde
        configPde(project);
    }

    /**
     * @param project
     */
    private void configPde(Project project)
    {
        project.getTasks().getByName("eclipseClasspath").doFirst((task) ->
        {
            if (!ameba.isWithPde())
            {
                return;
            }
            // write build.properties
            File file = project.file("build.properties");
            if (!file.exists())
            {
                try (OutputStream out = new FileOutputStream(file))
                {
                    String content = ""
                    //@formatter:off
                            + "source.. = src/main/java,src/main/resources\n"
                            + "src.excludes = src/test/java/,src/test/resources/\n"
                            + "output.. = bin/main\n"
                            + "bin.includes = .,META-INF/"
                    //@formatter:on
                    ;
                    out.write(content.getBytes("utf8"));
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            // write MANIFEST.MF
            file = project.file("META-INF/MANIFEST.MF");
            if (!file.exists())
            {
                if (!file.getParentFile().exists())
                {
                    file.getParentFile().mkdirs();
                }
                try (OutputStream out = new FileOutputStream(file))
                {
                    String content = "" +
                    //@formatter:off
                    "Manifest-Version: 1.0\r\n" + 
                    "Bundle-ManifestVersion: 2\r\n" + 
                    "Bundle-Name: "+project.getDisplayName()+"\r\n" + 
                    "Bundle-SymbolicName: "+project.getName()+";singleton:=true\r\n" + 
                    "Bundle-Version: "+project.getVersion()+"\r\n" + 
                    "Automatic-Module-Name: "+project.getName()+"\r\n" + 
                    "Bundle-RequiredExecutionEnvironment: JavaSE-1.8"
                    //@formatter:on
                    ;
                    out.write(content.getBytes("utf8"));
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            // modify .project and .classpath model
            EclipseModel eclipse = project.getExtensions().getByType(EclipseModel.class);
            eclipse.getProject().natures("org.eclipse.pde.PluginNature");
            eclipse.getProject().buildCommand("org.eclipse.pde.ManifestBuilder");
            eclipse.getProject().buildCommand("org.eclipse.pde.SchemaBuilder");
            eclipse.getClasspath().containers("org.eclipse.pde.core.requiredPlugins");
        });
    }

    /**
     * @param project
     */
    private void configTasks(Project project)
    {
        project.afterEvaluate((p) ->
        {
            p.getTasks().withType(Jar.class, (task) ->{
                task.getInputs().files("plugin.xml","META-INF/MANIFEST.MF");
            });
            p.getTasks().withType(Jar.class, (task) -> task.doFirst((t) ->
            {
                if (task.getName().equals("bootJar"))// not for bootJar
                {
                    return;
                }
                if (project.file("plugin.xml").exists())
                {
                    task.from("plugin.xml");
                }
                Attributes attributes = task.getManifest().getAttributes();
                attributes.putIfAbsent("Bundle-ManifestVersion", 2);
                attributes.putIfAbsent("Bundle-Name", project.getName());
                attributes.putIfAbsent("Bundle-SymbolicName", project.getName());
                attributes.putIfAbsent("Bundle-Version", project.getVersion());
                attributes.putIfAbsent("Automatic-Module-Name", project.getName());
                attributes.putIfAbsent("Bundle-RequiredExecutionEnvironment", "JavaSE-1.8");
                // require bundle
                if (!attributes.containsKey("Require-Bundle"))
                {
                    StringBuilder sb = new StringBuilder();
                    // add from gradle dependency
                    Set<String> addedBundleName = new HashSet<String>();
                    ResolvedConfiguration rc = project.getConfigurations().getByName("runtimeClasspath")
                            .getResolvedConfiguration();
                    rc.getFirstLevelModuleDependencies().forEach((rd) -> rd.getModuleArtifacts().forEach((a) ->
                    {
                        String bundleInfo = findBundleInfo(project, a.getFile());
                        if (bundleInfo != null)
                        {
                            sb.append(bundleInfo).append(',');
                            addedBundleName.add(bundleInfo.split(";")[0]);
                        }
                    }));
                    // add from pde denpendency
                    File file = project.file("META-INF/MANIFEST.MF");
                    if (file.exists())
                    {
                        try (InputStream in = new FileInputStream(file))
                        {
                            Manifest mf = new Manifest(in);
                            java.util.jar.Attributes attrs = mf.getMainAttributes();
                            String requireBundle = attrs.getValue("Require-Bundle");
                            if (requireBundle != null)
                            {
                                for (String seg : requireBundle.split(","))
                                {
                                    String name = seg.split(";")[0];
                                    if (!addedBundleName.contains(name))
                                    {
                                        sb.append(seg).append(',');
                                        addedBundleName.add(name);
                                    }
                                }
                            }
                        } catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    // set
                    if (sb.length() > 0)
                    {
                        sb.setLength(sb.length() - 1);
                        attributes.putIfAbsent("Require-Bundle", sb.toString());
                    }
                }
                // export package
                if (!attributes.containsKey("Export-Package"))
                {
                    StringBuilder sb = new StringBuilder();
                    JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
                    for (File dir : java.getSourceSets().getByName("main").getJava().getSrcDirs())
                    {
                        for (String pack : searchJavaPackages(dir, null))
                        {
                            sb.append(pack).append(',');
                        }
                    }
                    if (sb.length() > 0)
                    {
                        sb.setLength(sb.length() - 1);
                        attributes.putIfAbsent("Export-Package", sb.toString());
                    }
                }
            }));

        });
    }

    /**
     * @param dir
     * @param object
     * @return
     */
    private List<String> searchJavaPackages(File dir, String prefix)
    {
        List<String> result = new ArrayList<String>();
        if (prefix != null)
        {
            if (dir.listFiles((d, name) -> name.endsWith(".java")).length > 0)
            {
                result.add(prefix);
            }
        }
        String subPrefix = prefix == null ? "" : (prefix + ".");
        for (File subDir : dir.listFiles((file) -> file.isDirectory()))
        {
            result.addAll(searchJavaPackages(subDir, subPrefix + subDir.getName()));
        }
        return result;
    }

    private String findBundleInfo(Project project, File file)
    {
        if (file.isDirectory() && file.getAbsolutePath().startsWith(project.getProjectDir().getAbsolutePath()))
        {
            return null;
        }
        if (file.isFile() && file.getName().endsWith(".jar"))
        {
            try (JarFile jarFile = new JarFile(file))
            {
                java.util.jar.Attributes attr = jarFile.getManifest().getMainAttributes();
                if (attr.getValue("Bundle-SymbolicName") != null && attr.getValue("Bundle-Version") != null)
                {
                    return attr.getValue("Bundle-SymbolicName") + ";bundle-version=\"" + attr.getValue("Bundle-Version")
                            + '"';
                }
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * @param project
     * @param ameba
     */
    private void configBuildFiles(Project project)
    {
        project.getTasks().getByName("eclipse").doLast((task) ->
        {
            File file = project.file("build.gradle");
            if (!file.exists() && ameba.getGradleTxt() != null)
            {
                try (OutputStream out = new FileOutputStream(file))
                {
                    out.write(ameba.getGradleTxt().getBytes("utf8"));
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            file = project.file(".settings/org.eclipse.buildship.core.prefs");
            if (!file.exists() && ameba.getBuildshipTxt() != null)
            {
                file.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(file))
                {
                    out.write(ameba.getBuildshipTxt().getBytes("utf8"));
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * @param project
     */
    private void configSourceFolders(Project project)
    {
        project.getTasks().getByName("eclipseClasspath").doFirst((task) ->
        {
            project.mkdir("src/main/java");
            project.mkdir("src/main/resources");
            project.mkdir("src/test/java");
            project.mkdir("src/test/resources");
        });
    }

    /**
     * @param project
     */
    private void configEclipse(Project project)
    {
        EclipseModel eclipse = project.getExtensions().getByType(EclipseModel.class);
        eclipse.getProject().natures("org.eclipse.jdt.core.javanature",
                "org.eclipse.buildship.core.gradleprojectnature");
        eclipse.getProject().buildCommand("org.eclipse.buildship.core.gradleprojectbuilder");
        eclipse.getClasspath().containers("org.eclipse.buildship.core.gradleclasspathcontainer");
    }

    /**
     * @param project
     */
    private void configEncoding(Project project)
    {
        project.afterEvaluate((p) ->
        {
            p.getTasks().withType(JavaCompile.class, (task) -> task.getOptions().setEncoding("UTF-8"));
            p.getTasks().withType(Test.class, (task) -> task.systemProperty("file.encoding", "UTF-8"));
        });
    }

    /**
     * @param project
     */
    private void configJavaCompatibility(Project project)
    {
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        javaConvention.setSourceCompatibility("1.8");
        javaConvention.setTargetCompatibility("1.8");
    }

    /**
     * @param project
     */
    private void applyPlugins(Project project)
    {
        project.getPlugins().apply("java");
        project.getPlugins().apply("eclipse");
        // project.getPlugins().apply("org.springframework.boot");
    }

}
