package aeminium.gpu.buffers;

import java.nio.IntBuffer;

import aeminium.gpu.lists.IntList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class IntBufferFactory implements IBufferFactory{

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		int[] ar = ((IntList) list).getArray();
		IntBuffer ibuffer = IntBuffer.wrap(ar);
		return context.createIntBuffer(CLMem.Usage.Input, ibuffer, true);
	}

	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		return context.createIntBuffer(CLMem.Usage.Output, size);
	}
	
	@Override
	public CLBuffer<?> createSharedBufferFor(CLContext context, String type,
			int size) {
		return context.createIntBuffer(CLMem.Usage.InputOutput, size);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		int[] content = new int[size];
		outbuffer.asCLIntBuffer().read(q, ev).get(content);
		return new IntList(content, size);	
	}



}