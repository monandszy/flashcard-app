plugins {
  application
  pmd
  jacoco
  checkstyle
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.management)
}

group = "code"
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
application.mainClass = "code.FlashcardApp"

apply(from = rootProject.file("gradle/util/misc.gradle.kts"))
apply(from = rootProject.file("gradle/util/git.gradle.kts"))
apply(from = rootProject.file("gradle/util/docker.gradle.kts"))

java {
  @Suppress("UnstableApiUsage")
  consistentResolution {
    useCompileClasspathVersions()
  }
}

repositories {
  mavenCentral()
  maven {
    url = uri("https://repo.spring.io/release")
  }
}

dependencies {
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform)
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.modulith:spring-modulith-bom:1.2.1")
  }
}

tasks {

  bootJar {
    archiveFileName = "${project.name}-${version}.${archiveExtension.get()}"
  }
  jar {
    enabled = false
  }

  test {
    useJUnitPlatform()
    testLogging {
      events("passed", "skipped", "failed")
    }
  }

  pmd {
    isConsoleOutput = false
    isIgnoreFailures = true
    rulesMinimumPriority = 5
    ruleSets = listOf("category/java/errorprone.xml", "category/java/bestpractices.xml")
    pmdMain {
      exclude(
      )
    }
    pmdTest {
      exclude(
      )
    }
  }
  check {
    dependsOn(pmdMain)
    dependsOn(pmdTest)
    dependsOn(jacocoTestReport)
    dependsOn(checkstyleMain)
    dependsOn(checkstyleTest)
  }

  checkstyle {
    isIgnoreFailures = true
    isShowViolations = false
    configFile = file("config/checkstyle.xml")
    checkstyleMain {
      source("src/main/java")
      classpath = project.files()
    }
    checkstyleTest {
      source("src/test/java")
      classpath = project.files()
    }
  }

  jacoco {
    jacocoTestReport {
      reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco")
      }
      doLast {
        val reportPath = layout.buildDirectory.file("reports/jacoco/index.html").get().asFile
        println("Jacoco report: file://${reportPath.toURI().path}")
      }
      classDirectories.setFrom(
        files(classDirectories.files.map {
          fileTree(it) {
            exclude(
            )
          }
        })
      )
      dependsOn(test)
    }
  }

  javadoc {
    setDestinationDir(file(layout.buildDirectory.dir("docs")))
    options.encoding = "UTF-8"
  }
  compileJava {
    options.encoding = "UTF-8"
  }
  compileTestJava {
    options.encoding = "UTF-8"
  }
}