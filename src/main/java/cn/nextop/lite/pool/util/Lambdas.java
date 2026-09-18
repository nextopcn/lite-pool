package cn.nextop.lite.pool.util;

import static cn.nextop.lite.pool.util.Objects.cast;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.nextop.lite.pool.glossary.Required;

/**
 * @author Baoyi Chen
 */
public final class Lambdas {
	//
	static final ClassLoader LOADER = Lambdas.class.getClassLoader ();
	
	static final org.slf4j.Logger LOGGER = getLogger( Lambdas.class );
	
	static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	
	/**
	 * 
	 */
	private static final boolean isWrappable ( final Method method ) {
		
		return LOADER == method.getDeclaringClass().getClassLoader ();
	}
	
	private static <T> T invoke (Method m, Object o, Object... args) {
		
		try { return cast(m.invoke(o, args)); } catch (Throwable tx) {
			if (tx instanceof RuntimeException e) /* ! */ { throw e; }
			else { final var x = new RuntimeException (tx); throw x; }
		}
	}
	
	/**
	 * 
	 */
	public static VarHandle findVarHandle(final @Required Field field) {
		
		return Lambdas.findVarHandle( field.getDeclaringClass(), field.getName(), field.getType(), true );
	}
	
	public static MethodHandle findGetter(final @Required Field field) {
		
		return Lambdas.findGetter( field.getDeclaringClass() , field.getName() , field.getType() , true );
	}
	
	public static MethodHandle findSetter(final @Required Field field) {
		
		return Lambdas.findSetter( field.getDeclaringClass() , field.getName() , field.getType() , true );
	}
	
	public static VarHandle findVarHandle (final Class<?> clazz, final String name, final Class<?> type) {
		
		final VarHandle r = Lambdas.findVarHandle(clazz, name, type, true); /* private lookup */ return r;
	}
	
	public static <T> boolean cas(VarHandle h , Object o , T p , T n) { return h.compareAndSet(o, p, n); }
	
	public static <T> T cae(VarHandle h, Object o, T p, T n) { return (T) h.compareAndExchange(o, p, n); }
	
	/**
	 * 
	 */
	public static MethodHandle findGetter(Class<?> clazz , String name , Class<?> type , boolean strict) {
		try {
			if (!strict) /** non-private **/ { return LOOKUP.in(clazz).findGetter (clazz , name , type); }
			else { return MethodHandles.privateLookupIn(clazz, LOOKUP).findGetter (clazz , name , type); }
		} catch(Throwable cause) {
			LOGGER.error("failed to find handle: {}, {}, {}, {}", clazz, name, type, strict); return null;
		}
	}
	
	public static MethodHandle findSetter(Class<?> clazz , String name , Class<?> type , boolean strict) {
		try {
			if (!strict) /** non-private **/ { return LOOKUP.in(clazz).findSetter (clazz , name , type); }
			else { return MethodHandles.privateLookupIn(clazz, LOOKUP).findSetter (clazz , name , type); }
		} catch(Throwable cause) {
			LOGGER.error("failed to find handle: {}, {}, {}, {}", clazz, name, type, strict); return null;
		}
	}
	
	public static VarHandle findVarHandle(Class<?> clazz , String name , Class<?> type , boolean strict) {
		try {
			if (!strict) /** non-private **/ { return LOOKUP.in(clazz).findVarHandle(clazz, name, type); }
			else { return MethodHandles.privateLookupIn(clazz, LOOKUP).findVarHandle(clazz, name, type); }
		} catch(Throwable cause) {
			LOGGER.error("failed to find handle: {}, {}, {}, {}", clazz, name, type, strict); return null;
		}
	}
}
