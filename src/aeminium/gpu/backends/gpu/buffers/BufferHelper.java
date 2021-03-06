package aeminium.gpu.backends.gpu.buffers;

import java.util.HashMap;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.lists.AbstractList;
import aeminium.gpu.collections.lists.BooleanList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.AbstractMatrix;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.collections.properties.evaluation.LazyCollection;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.LocalSize;

public class BufferHelper {

	
	private static HashMap<String, IBufferFactory> factories = new HashMap<String, IBufferFactory>();

	private static HashMap<String, String> clTypes = new HashMap<String, String>();
	private static HashMap<String, Class<?>> clClasses = new HashMap<String, Class<?>>();

	static {
		factories.put("Integer", new IntBufferFactory());
		factories.put("Float", new FloatBufferFactory());
		factories.put("Double", new DoubleBufferFactory());
		factories.put("Character", new CharBufferFactory());
		factories.put("Long", new LongBufferFactory());
		factories.put("Boolean", new CharBufferFactory());

		clTypes.put("Integer", "int");
		clTypes.put("Float", "float");
		clTypes.put("Double", "double");
		clTypes.put("Character", "char");
		clTypes.put("Long", "long");
		clTypes.put("Boolean", "char");

		clClasses.put("Integer", new Integer(1).getClass());
		clClasses.put("Float", new Float(1).getClass());
		clClasses.put("Double", new Double(1).getClass());
		clClasses.put("Character", new Character('a').getClass());
		clClasses.put("Long", new Long(1).getClass());
		clClasses.put("Boolean", new Boolean(true).getClass());

		for (Object t : clTypes.keySet().toArray()) {
			String v = clTypes.get(t);
			clTypes.put("java.lang." + t, v);
		}

	}

	private static <T> IBufferFactory getFactory(PList<T> list) {
		return getFactory(list.getContainingType().getSimpleName());
	}

	private static <T> IBufferFactory getFactory(String type) {
		if (type.startsWith("java.lang.")) {
			type = type.substring(10);
		}
		IBufferFactory f = factories.get(type);
		if (f == null) {
			System.out.println("No buffer for type " + type + ".");
			System.exit(1);
		}
		return f;
	}

	public static <T> CLBuffer<?> createInputBufferFor(CLContext context,
			PList<T> list, int size) {
		if (list instanceof LazyCollection) {
			LazyCollection linput = (LazyCollection) list;
			return linput.getGPUHelper().getInputBuffer(context);
		} else {
			IBufferFactory f = getFactory(list);
			return f.createInputBufferFor(context, list);
		}

	}

	public static <T> CLBuffer<?> createInputOutputBufferFor(CLContext context,
			PList<T> list) {
		if (list instanceof LazyCollection) {
			LazyCollection linput = (LazyCollection) list;
			return linput.getGPUHelper().getInputBuffer(context);
		} else {
			IBufferFactory f = getFactory(list);
			return f.createInputBufferFor(context, list);
		}
	}
	
	public static <T> CLBuffer<?> createInputOutputBufferFor(CLContext context,
			PList<T> list, long size) {
		if (list instanceof LazyCollection) {
			LazyCollection linput = (LazyCollection) list;
			return linput.getGPUHelper().getInputBuffer(context);
		} else {
			IBufferFactory f = getFactory(list);
			return f.createInputOutputBufferFor(context, list);
		}
	}

	public static <T> CLBuffer<?> createInputOutputBufferFor(CLContext context,
			String outputType, int size) {
		IBufferFactory f = getFactory(outputType);
		return f.createInputOutputBufferFor(context, outputType, size);
	}

	public static <T> CLBuffer<?> createOutputBufferFor(CLContext context,
			PList<T> list, int size) {
		return createOutputBufferFor(context, list.getClass().getSimpleName(),
				size);
	}

	public static LocalSize createSharedBufferFor(CLContext context,
			String outputType, int size) {
		IBufferFactory f = getFactory(outputType);
		return f.createSharedBufferFor(context, outputType, size);
	}

	public static <T> CLBuffer<?> createOutputBufferFor(CLContext context,
			String type, int size) {
		IBufferFactory f = getFactory(type);
		return f.createOutputBufferFor(context, size);
	}

	public static PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev, int size, PList<?> list) {
		return extractFromBuffer(outbuffer, q, ev, list.getContainingType()
				.getSimpleName(), size);
	}

	public static PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev, String type, int size) {
		IBufferFactory f = getFactory(type);
		return f.extractFromBuffer(outbuffer, q, ev, size);
	}

	public static Object extractElementFromBuffer(CLBuffer<?> outbuffer,
			CLQueue q, CLEvent ev, String type) {
		IBufferFactory f = getFactory(type);
		return f.extractElementFromBuffer(outbuffer, q, ev);
	}
	
	public static CLBuffer<?> createInputOutputBufferFor(CLContext ctx,
			PObject o) {
		if (o instanceof AbstractList) {
			PList<?> l = (PList<?>) o;
			return createInputOutputBufferFor(ctx, l);
		}
		if (o instanceof AbstractMatrix) {
			PMatrix<?> l = (PMatrix<?>) o;
			return createInputOutputBufferFor(ctx, l.elements());
		}
		return null;
	}


	public static String getCLTypeOf(String type) {
		return clTypes.get(type);
	}

	public static Class<?> getClassOf(String type) {
		return clClasses.get(type);
	}

	public static Object decode(Object in, String outputType) {
		if (outputType.equals("Boolean") && in instanceof Character) {
			return BooleanList.decode((Character) in);
		}
		return in;
	}

	public static String getCLTypeOfObject(PObject o) {
		if (o instanceof PList) {
			PList<?> l = (PList<?>) o;
			return getCLTypeOf(l.getContainingType().toString()) + "[]";
		} else {
			System.out.println("o not a PLIST");
			return null; // TODO
		}
	}

	public static String getCLTypeOf(Class<?> containingType) {
		String k = containingType.getSimpleName();
		return clTypes.get(k);
	}
	
	@SuppressWarnings("unchecked")
	public static <O> void debugBuffers(CLContext ctx, CLQueue q, String bufname, CLBuffer<?> buf, int size, CLEvent comp, String outputType) {
		PList<O> li;
		System.out.println("buf:" + bufname);
		li = (PList<O>) BufferHelper.extractFromBuffer(buf, q,
				comp, outputType, size);
		for (int i = 0; i < size; i++) {
			System.out.print(li.get(i) + ",");
		}
		System.out.println("___");
	}

	public static Object getSize(int i, String string) {
		if (string.equals("Integer")) return i*4;
		if (string.equals("Character")) return i;
		if (string.equals("Long")) return i*8;
		if (string.equals("Float")) return i*4;
		if (string.equals("Double")) return i*8;
		return i*4;
	}

}
