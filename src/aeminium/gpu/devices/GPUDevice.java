package aeminium.gpu.devices;

import aeminium.gpu.backends.gpu.GPUKernel;
import aeminium.gpu.operations.contracts.ProgramLogger;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

public class GPUDevice {
	private CLContext context;
	private CLQueue queue;
	private ProgramLogger logger;

	public GPUDevice(CLContext ctx) {
		this.context = ctx;
		queue = context.createDefaultQueue();
	}

	// Execute Programs

	public void compile(String kernel) {
		CLProgram p;
		try {
			p = context.createProgram(kernel).build();
			p.release();
		} catch (CLBuildException e) {
			// GPU not available during offline compilation.
		}

	}

	public void startExecution(GPUKernel p) {
		if (this.getLogger() != null) {
			startExecutionWithLogger(p);
			return;
		}
		p.prepareSource(context);
		p.prepareBuffers(context);
		p.execute(context, queue);
	}
	
	public void awaitExecution(GPUKernel p) {
		if (this.getLogger() != null) {
			return;
		}
		p.retrieveResults(context, queue);
		p.release();
	}

	private void startExecutionWithLogger(GPUKernel p) {
		ProgramLogger logger = this.getLogger();
		long startTime;

		startTime = System.nanoTime();
		p.prepareSource(context);
		logger.saveTime("kernel.compilation", System.nanoTime() - startTime);

		startTime = System.nanoTime();
		p.prepareBuffers(context);
		logger.saveTime("buffer.to", System.nanoTime() - startTime);

		startTime = System.nanoTime();
		p.execute(context, queue);
		p.waitExecution(context, queue);
		logger.saveTime("kernel.execution", System.nanoTime() - startTime);

		startTime = System.nanoTime();
		p.retrieveResults(context, queue);
		logger.saveTime("buffer.from", System.nanoTime() - startTime);

		p.release();

	}

	public void release() {
		if (queue != null) {
			queue.finish();
			queue.release();
		}
		if (context != null) {
			context.release();
		}
	}

	// OpenCL data

	public CLDevice getDevice() {
		return context.getDevices()[0];
	}

	public CLDevice[] getAllDevices() {
		return context.getDevices();
	}

	// Getters/Setters

	public CLContext getContext() {
		return context;
	}

	public void setContext(CLContext context) {
		this.context = context;
	}

	public CLQueue getQueue() {
		return queue;
	}

	public void setQueue(CLQueue queue) {
		this.queue = queue;
	}

	public void refreshQueue() {
		queue = context.createDefaultProfilingQueue();
	}
	
	public ProgramLogger getLogger() {
		return logger;
	}

	public void setLogger(ProgramLogger logger) {
		this.logger = logger;
	}

}
