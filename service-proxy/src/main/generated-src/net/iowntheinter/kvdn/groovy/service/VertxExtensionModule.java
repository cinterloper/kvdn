package net.iowntheinter.kvdn.groovy.service;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
public class VertxExtensionModule extends ExtensionModule {
  private static final String extensionClasses = "net.iowntheinter.kvdn.groovy.service.KvdnService_GroovyExtension";
  private static final String staticExtensionClasses = "";
  private final ExtensionModule delegate;  public VertxExtensionModule() {
    super("net.iowntheinter.kvdn.service", "3.5.0");
    Properties props = new Properties();
    props.put("moduleName", "net.iowntheinter.kvdn.service");
    props.put("moduleVersion", "3.5.0");
    props.put("extensionClasses", extensionClasses);
    props.put("staticExtensionClasses", staticExtensionClasses);
    delegate = MetaInfExtensionModule.newModule(props, getClass().getClassLoader());
  }
  public List<MetaMethod> getMetaMethods() {
    return delegate.getMetaMethods();
  }
}