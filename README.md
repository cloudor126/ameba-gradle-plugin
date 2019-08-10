# ameba-gradle-plugin
A gradle plugin for Agree Ameba projects

# Example
```
plugins{
    id 'cn.com.agree.gradle.ameba-gradle-plugin' version '1.0.6' apply false
    id 'org.springframework.boot' version '2.1.7.RELEASE' apply false
}

allprojects {
    group = 'cn.com.agree'
    version = '1.0.0'
    ext.VERSION_SPRING_BOOT = '2.1.7.RELEASE'
}

subprojects{
	apply plugin: 'cn.com.agree.gradle.ameba-gradle-plugin'
	
	// version = rootProject.version + new Date().format('.yyyyMMdd_HHmm')
	
	// 仓库设置
	repositories {
        mavenCentral()
    }

    // 不使用alpha和beta版本，例如grovvy-3.0.0-alpha、junit-4.13-beta-2
    configurations.all{
    	resolutionStrategy.componentSelection{ 
    		all{ComponentSelection selection ->
				if (selection.candidate.version ==~ /.*(alpha|beta).*/ ) {
                    selection.reject("we don't use alpha or beta version")
                }
    		}
    	}
    }
    
    // 子工程的gradle模板
    ameba{
    	gradleTxt = """\
				plugins{
					//id 'org.springframework.boot' // 提供bootJar任务
					//id 'distribution' // 提供distXXX任务
				}
				dependencies{
					//implementation project(':other-project') // 依赖其他工程
					//implementation project(':other-project-without-dep'), {transitive=false} // 屏蔽间接依赖
					//implementation 'com.google.guava:guava:+'
					//implementation "org.springframework.boot:spring-boot-starter:\${VERSION_SPRING_BOOT}"
					testImplementation 'junit:junit:+'
				}
				
				/*ameba{
					withPde = true // 支持Eclipse插件开发
				}*/
				
				/*distributions{ // 设置distXXX任务的打包内容
					main{
						contents{
							from bootJar
							from "src/dist"
							from ("configs") {into "configs"}
						}
					}
				}*/
				/*bootJar { // 设置bootJar的扩展加载功能
					manifest {
						attributes 'Main-Class': 'org.springframework.boot.loader.PropertiesLauncher'
						attributes 'Loader-Path':'file://./addons/,file://./dropins/'
					}
				}*/

				""".stripIndent()
    }
}
```
