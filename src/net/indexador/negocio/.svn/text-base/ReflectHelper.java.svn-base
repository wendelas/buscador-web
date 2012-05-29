package net.indexador.negocio;

import java.lang.reflect.*;

import org.apache.commons.beanutils.*;
import org.apache.log4j.*;

/**
 * Utilitário para operações de reflection
 * 
 * @version 1.0
 * @author Celso Antônio
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class ReflectHelper {
  /**
   * Objeto logger do log4j
   */
  private static final Logger log = Logger.getLogger(ReflectHelper.class);
  private static final Class[] NO_CLASSES = new Class[0];
  private static final Class[] OBJECT = new Class[] {Object.class};
  public static final Object[] PARAMETROS_VAZIOS = new Object[0];
  private static final Method OBJECT_EQUALS;
  private static final Class[] NO_PARAM = new Class[] {};
  private static final Method OBJECT_HASHCODE;
  /**
   * PRIMITIVE_WRAPPER_MAP
   */
  private static final Class[] PRIMITIVE_WRAPPER_MAP = {Boolean.TYPE,
      Boolean.class, Byte.TYPE, Byte.class, Character.TYPE, Character.class,
      Double.TYPE, Double.class, Float.TYPE, Float.class, Integer.TYPE,
      Integer.class, Long.TYPE, Long.class, Short.TYPE, Short.class,};
  static {
    Method eq = null;
    Method hash = null;
    try {
      eq = Object.class.getMethod("equals", OBJECT);
      hash = Object.class.getMethod("hashCode", NO_PARAM);
    } catch (Exception e) {
      log.error("Erro ao inicializar a classe ReflectHelper", e);
    }
    OBJECT_EQUALS = eq;
    OBJECT_HASHCODE = hash;
  }

  /**
   * 
   * @param classe
   * @return
   */
  public static boolean implementaEquals(Class classe) {
    Method equals;
    try {
      equals = classe.getMethod("equals", OBJECT);
    } catch (NoSuchMethodException nsme) {
      return false; // its an interface so we can't really tell
      // anything...
    }
    return !OBJECT_EQUALS.equals(equals);
  }

  /**
   * 
   * @param classe
   * @return
   */
  public static boolean implementaHashCode(Class classe) {
    Method hashCode;
    try {
      hashCode = classe.getMethod("hashCode", NO_PARAM);
    } catch (NoSuchMethodException nsme) {
      return false; // its an interface so we can't really tell
      // anything...
    }
    return !OBJECT_HASHCODE.equals(hashCode);
  }

  /**
   * 
   * @param name
   * @param caller
   * @return
   * @throws ClassNotFoundException
   */
  public static Class classForName(String name, Class caller)
      throws ClassNotFoundException {
    try {
      ClassLoader contextClassLoader = Thread.currentThread()
          .getContextClassLoader();
      if (contextClassLoader != null) {
        return contextClassLoader.loadClass(name);
      } else {
        return Class.forName(name, true, caller.getClassLoader());
      }
    } catch (Exception e) {
      return Class.forName(name, true, caller.getClassLoader());
    }
  }

  /**
   * 
   * @param name
   * @return
   * @throws ClassNotFoundException
   */
  public static Class classForName(String name) throws ClassNotFoundException {
    try {
      ClassLoader contextClassLoader = Thread.currentThread()
          .getContextClassLoader();
      if (contextClassLoader != null) {
        return contextClassLoader.loadClass(name);
      } else {
        return Class.forName(name);
      }
    } catch (Exception e) {
      return Class.forName(name);
    }
  }

  /**
   * Cria uma nova instância de uma classe
   * 
   * @param classeNegocioImpl
   * @param construtor
   * @param arr
   *            parâmetros do contrutor
   * @return
   */
  public static Object novaInstancia(Class classeNegocioImpl,
      Constructor construtor, Object[] arr) {
    Object objImpl = null;
    try {
      // Se conseguir recuperar um array de parâmetros
      if (arr != null) {
        objImpl = construtor.newInstance(arr);
      } else {
        // Caso o objeto não possua um contrutor com argumentos
        objImpl = classeNegocioImpl.newInstance(); // Cria a instância
      }
    } catch (Exception e) {
      String erro = "Erro ao instanciar um novo objeto. ";
      log.error(erro, e);
      throw new IllegalArgumentException(erro, e);
    }
    return objImpl;
  }

  /**
   * Cria uma nova instância de uma classe
   * 
   * @param nomeClasse
   * @return
   */
  public static Object novaInstancia(String nomeClasse) {
    try {
      // Carrega a classe
      Class classe = classForName(nomeClasse);
      return classe.newInstance(); // Cria a instância
    } catch (Exception e) {
      String erro = "Erro ao instanciar um novo objeto. ";
      log.error(erro, e);
      throw new IllegalArgumentException(erro, e);
    }
  }

  /**
   * 
   * @param classe
   * @param member
   * @return
   */
  public static boolean isPublico(Class classe, Member member) {
    return Modifier.isPublic(member.getModifiers())
        && Modifier.isPublic(classe.getModifiers());
  }

  /**
   * 
   * @param classe
   * @return
   */
  public static boolean isClassePublica(Class classe) {
    return Modifier.isPublic(classe.getModifiers());
  }

  /**
   * getConstrutor
   * 
   * @param classe
   * @return
   */
  public static Constructor getConstrutor(Class classe, Object... parametros) {
    Constructor[] construtores = classe.getDeclaredConstructors();
    if (construtores.length == 1) {
      Constructor constructor = construtores[0];
      if (!isPublico(classe, constructor)) {
        constructor.setAccessible(true);
      }
      return constructor;
    }
    Class[] classes = null;
    if (parametros == null || parametros.length == 0) {
      classes = NO_CLASSES;
    } else {
      classes = new Class[parametros.length];
      for (int i = 0; i < parametros.length; i++) {
        Class param = parametros[i].getClass();
        Class[] interfaces = param.getInterfaces();
        if (interfaces != null) {
          param = interfaces[0];
        }
        classes[i] = param;
      }
    }
    if (isClasseAbstrata(classe)) {
      return null;
    }
    try {
      Constructor constructor = classe.getDeclaredConstructor(classes);
      if (!isPublico(classe, constructor)) {
        constructor.setAccessible(true);
      }
      return constructor;
    } catch (NoSuchMethodException nme) {
      return null;
    }
  }

  /**
   * 
   * @param classe
   * @return
   */
  public static boolean isClasseAbstrata(Class classe) {
    int modifier = classe.getModifiers();
    return Modifier.isAbstract(modifier) || Modifier.isInterface(modifier);
  }

  /**
   * 
   * @param classe
   * @return
   */
  public static boolean isClasseFinal(Class classe) {
    return Modifier.isFinal(classe.getModifiers());
  }

  /**
   * 
   * @param classe
   * @param method
   * @return
   */
  public static Method getMetodo(Class classe, String name,
      Class<?>... parameterTypes) {
    try {
      return classe.getMethod(name, parameterTypes);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 
   * @param classe
   * @param method
   * @return
   */
  public static Method getMetodo(Class classe, Method method) {
    try {
      return getMetodo(classe, method.getName(), method.getParameterTypes());
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Invoca um método
   * 
   * @param obj
   * @param nome
   * @param parametros
   * @param paramMetodo
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static Object invocaMetodo(Object obj, String nome,
      Class[] parametros, Object[] paramMetodo) {
    try {
      Method m = obj.getClass().getMethod(nome, parametros);
      return invocaMetodo(m, obj, paramMetodo);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Erro (NoSuchMethodException)", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Erro (IllegalAccessException)", e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        //        if (cause instanceof OperacaoRealizadaException) {
        //          throw (OperacaoRealizadaException) cause;
        //        }
        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        }
      }
      throw new IllegalStateException("Erro (InvocationTargetException)", e);
    }
  }

  /**
   * Invoca um método
   * 
   * @param obj
   * @param nome
   * @param parametros
   * @param paramMetodo
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static Object invocaMetodo(Object obj, String nome) {
    return invocaMetodo(obj, nome, NO_CLASSES, NO_PARAM);
  }

  /**
   * Invoca um método
   * 
   * @param obj
   * @param paramMetodo
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static Object invocaMetodo(Method m, Object obj, Object[] paramMetodo)
      throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    return m.invoke(obj, paramMetodo);
  }

  /**
   * isPrimitiveWrapper
   * 
   * @param type
   * @return
   */
  public static boolean isPrimitiveWrapper(final Class type) {
    for (int i = 0; i < PRIMITIVE_WRAPPER_MAP.length; i += 2) {
      if (type.equals(PRIMITIVE_WRAPPER_MAP[i + 1])) {
        return true;
      }
    }
    return false;
  }

  private ReflectHelper() {
  }

  public static void copyProperties(Object destino, Object origem) {
    try {
      BeanUtils.copyProperties(destino, origem);
    } catch (IllegalAccessException ex) {
      log.error("IllegalAccessException ao copiar propriedades ", ex);
      throw new IllegalArgumentException(
          "IllegalAccessException ao copiar propriedades ", ex);
    } catch (InvocationTargetException ex) {
      log.error("InvocationTargetException ao copiar propriedades ", ex);
      throw new IllegalArgumentException(
          "InvocationTargetException ao copiar propriedades ", ex);
    }
  }
}
