package nl.hsac.fitnesse.junit.patchFor486;

import fitnesse.junit.FitNesseSuite.ConfigFile;
import fitnesse.junit.FitNesseSuite.DebugMode;
import fitnesse.junit.FitNesseSuite.ExcludeSuiteFilter;
import fitnesse.junit.FitNesseSuite.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseSuite.OutputDir;
import fitnesse.junit.FitNesseSuite.Port;
import fitnesse.junit.FitNesseSuite.SuiteFilter;
import org.junit.runners.model.InitializationError;

import java.io.File;

/**
 * Partial copy of FitNesse's, with changes for issue https://github.com/unclebob/fitnesse/issues/486
 */
public class FitNesseSuite extends FitNesseRunner {

  public FitNesseSuite(Class<?> suiteClass) throws InitializationError {
    super(suiteClass);
  }

  @Override
  protected String getFitNesseDir(Class<?> klass)
          throws InitializationError {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    if (fitnesseDirAnnotation == null) {
      throw new InitializationError("There must be a @FitnesseDir annotation");
    }
    if (!"".equals(fitnesseDirAnnotation.value())) {
      return fitnesseDirAnnotation.value();
    }
    if (!"".equals(fitnesseDirAnnotation.systemProperty())) {
      String baseDir = System.getProperty(fitnesseDirAnnotation.systemProperty());
      File outputDir = new File(baseDir);
      return outputDir.getAbsolutePath();
    }
    throw new InitializationError(
            "In annotation @FitnesseDir you have to specify either 'value' or 'systemProperty'");
  }

  @Override
  protected String getFitNesseRoot(Class<?> klass) {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    return fitnesseDirAnnotation.fitNesseRoot();
  }

  @Override
  protected String getSuiteFilter(Class<?> klass)
          throws Exception {
    SuiteFilter suiteFilterAnnotation = klass.getAnnotation(SuiteFilter.class);
    if (suiteFilterAnnotation == null) {
      return super.getSuiteFilter(klass);
    }
    return suiteFilterAnnotation.value();
  }

  @Override
  protected String getExcludeSuiteFilter(Class<?> klass)
          throws Exception {
    ExcludeSuiteFilter excludeSuiteFilterAnnotation = klass.getAnnotation(ExcludeSuiteFilter.class);
    if (excludeSuiteFilterAnnotation == null) {
      return super.getExcludeSuiteFilter(klass);
    }
    return excludeSuiteFilterAnnotation.value();
  }

  @Override
  protected String getSuiteName(Class<?> klass) throws InitializationError {
    Name nameAnnotation = klass.getAnnotation(Name.class);
    if (nameAnnotation == null) {
      throw new InitializationError("There must be a @Name annotation");
    }
    return nameAnnotation.value();
  }

  @Override
  protected String getOutputDir(Class<?> klass) throws InitializationError {
    OutputDir outputDirAnnotation = klass.getAnnotation(OutputDir.class);
    if (outputDirAnnotation == null) {
      throw new InitializationError("There must be a @OutputDir annotation");
    }
    if (!"".equals(outputDirAnnotation.value())) {
      return outputDirAnnotation.value();
    }
    if (!"".equals(outputDirAnnotation.systemProperty())) {
      String baseDir = System.getProperty(outputDirAnnotation.systemProperty());
      File outputDir = new File(baseDir, outputDirAnnotation.pathExtension());
      return outputDir.getAbsolutePath();
    }
    throw new InitializationError(
            "In annotation @OutputDir you have to specify either 'value' or 'systemProperty'");
  }

  @Override
  protected boolean useDebugMode(Class<?> klass) throws Exception {
    DebugMode debugModeAnnotation = klass.getAnnotation(DebugMode.class);
    if (null == debugModeAnnotation) {
      return super.useDebugMode(klass);
    }
    return debugModeAnnotation.value();
  }

  @Override
  public int getPort(Class<?> klass) throws Exception {
    Port portAnnotation = klass.getAnnotation(Port.class);
    if (null == portAnnotation) {
      return super.getPort(klass);
    }
    int lport = portAnnotation.value();
    if (!"".equals(portAnnotation.systemProperty())) {
      lport = Integer.getInteger(portAnnotation.systemProperty(), lport);
    }
    return lport;
  }

  @Override
  protected File getConfigFile(String rootPath, Class<?> klass) throws Exception {
    ConfigFile configFileAnnotation = klass.getAnnotation(ConfigFile.class);
    if (null == configFileAnnotation) {
      return super.getConfigFile(rootPath, klass);
    }
    return new File(configFileAnnotation.value());
  }
}
