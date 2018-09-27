package org.jupiter.flight.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.jupiter.common.util.Function;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.ThrowUtil;

















public class JavaCompiler
{
  public JavaCompiler() {}
  
  public static byte[] compile(String classPath, String className, List<String> args)
  {
    return compile(classPath, Lists.newArrayList(new String[] { className }), args);
  }
  
  public static byte[] compile(String classPath, List<String> classNames, List<String> args) {
    StandardJavaFileManager javaFileManager = null;
    ClassFileManager classFileManager = null;
    try {
      javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
      javaFileManager = javac.getStandardFileManager(null, null, null);
      classFileManager = new ClassFileManager(javaFileManager);
      
      List<String> classFiles = Lists.transform(classNames, new Function()
      {
        public String apply(String input)
        {
          return val$classPath + input.replace(".", "/") + ".java";
        }
      });
      String[] names = (String[])classFiles.toArray(new String[classFiles.size()]);
      JavaCompiler.CompilationTask javacTask = javac.getTask(null, classFileManager, null, args, null, javaFileManager.getJavaFileObjects(names));
      

      if (javacTask.call().booleanValue()) {
        return classFileManager.getJavaClassObject().classBytes();
      }
      













      return null;
    }
    catch (Throwable t)
    {
      ThrowUtil.throwException(t);
    } finally {
      if (javaFileManager != null) {
        try {
          javaFileManager.close();
        } catch (IOException localIOException6) {}
      }
      if (classFileManager != null) {
        try {
          classFileManager.close();
        }
        catch (IOException localIOException7) {}
      }
    }
  }
  


  static class ClassFileManager
    extends ForwardingJavaFileManager<StandardJavaFileManager>
  {
    private JavaCompiler.JavaClassObject javaclassObject;
    

    protected ClassFileManager(StandardJavaFileManager fileManager)
    {
      super();
    }
    
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
    {
      javaclassObject = new JavaCompiler.JavaClassObject(className, kind);
      return javaclassObject;
    }
    
    public JavaCompiler.JavaClassObject getJavaClassObject() {
      return javaclassObject;
    }
  }
  




  static class JavaClassObject
    extends SimpleJavaFileObject
  {
    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    
    public JavaClassObject(String name, JavaFileObject.Kind kind) {
      super(kind);
    }
    
    public OutputStream openOutputStream() throws IOException
    {
      return bos;
    }
    


    public byte[] classBytes()
    {
      return bos.toByteArray();
    }
  }
}
