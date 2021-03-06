package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.RecursiveCallCodeGen;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPURecursiveCall<R, A> extends GPUGenericKernel {

	public PList<A> args;
	public PList<A> argsNext;
	public static int DEFAULT_SPAWN = 8;
	public static int MAX_ITEMS = 512;
	public R output;
	public BinaryRecursiveStrategy<R, A> strategy;
	boolean isDone;
	
	static {
		if (System.getenv("WORKERS") != null) MAX_ITEMS = Integer.parseInt(System.getenv("WORKERS"));
		if (System.getenv("SPAWNS") != null) DEFAULT_SPAWN = Integer.parseInt(System.getenv("SPAWNS"));
	}

	protected CLBuffer<?> argbuffer;
	protected CLBuffer<?> accbuffer;
	protected CLBuffer<Integer> rbuffer;

	private RecursiveCallCodeGen gen;

	public GPURecursiveCall(BinaryRecursiveStrategy<R, A> recursiveStrategy) {
		strategy = recursiveStrategy;
		gen = new RecursiveCallCodeGen(this);
		output = strategy.getSeed();

		otherData = OtherData.extractOtherData(recursiveStrategy);
		gen.setOtherData(otherData);
	}

	public void setArgs(PList<A> args) {
		this.args = args;
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		super.prepareBuffers(ctx);
		argsNext = args.subList(0, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {

		CLEvent[] eventsArr = new CLEvent[1];

		args = args.subList(start, end);
		
		if (args.size() > MAX_ITEMS) {
			argsNext = args.subList(MAX_ITEMS, args.size());
			args = args.subList(0, MAX_ITEMS);
		}
		int workUnits = args.length();
		int bufferSize = workUnits * DEFAULT_SPAWN;

		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx,
				"Integer", MAX_ITEMS * DEFAULT_SPAWN);
		accbuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed()
				.getClass().getSimpleName(), MAX_ITEMS);

		int counter = 0;
		while (!isDone) {

			workUnits = args.length(); // end - start; // Using Limits from
										// Decider
			bufferSize = workUnits * DEFAULT_SPAWN;

			args.set(bufferSize - 1, args.get(0));
			argbuffer = BufferHelper.createInputOutputBufferFor(ctx, args);

			synchronized (kernel) {
				kernel.setArgs(counter, workUnits, rbuffer, accbuffer,
						argbuffer);
				setExtraDataArgs(5, kernel);

				eventsArr[0] = kernel.enqueueNDRange(q,
						new int[] { MAX_ITEMS }, eventsArr);
			}
			counter++;

			PList<Integer> rs = (PList<Integer>) BufferHelper
					.extractFromBuffer(rbuffer, q, eventsArr[0], "Integer",
							bufferSize);
			PList<A> argsBack = (PList<A>) BufferHelper.extractFromBuffer(
					argbuffer, q, eventsArr[0], bufferSize, args);

			for (int i = 0; i < bufferSize; i++) {
				if (rs.get(i) == 0) {
					argsNext.add(argsBack.get(i));
				}
				if (rs.get(i) == -2) {
					System.out.println("There was an error");
				}
			}

			if (System.getenv("DEBUG") != null) {
				System.out.println("Left: " + argsNext.size() + ", WorkUnits:" + workUnits);
				if (System.getenv("DEBUG").equals("2")) {
					PList<R> accs = (PList<R>) BufferHelper.extractFromBuffer(
							accbuffer, q, eventsArr[0], strategy.getSeed()
									.getClass().getSimpleName(), MAX_ITEMS);
					int i = 0;
					for (R a : accs) {
						System.out.print(a + "/");
						for (int j = 0; j < DEFAULT_SPAWN; j++)
							System.out.print(args.get(i + j * workUnits) + "´"
									+ rs.get(i + j * workUnits) + "|");
						System.out.println(", ");
						i++;
					}
					System.out.println("_______");
				}
				if (System.getenv("DEBUG").equals("3")) {
					PList<R> accs = (PList<R>) BufferHelper.extractFromBuffer(
							accbuffer, q, eventsArr[0], strategy.getSeed()
									.getClass().getSimpleName(), MAX_ITEMS);
					R acc = strategy.getSeed();
					for (R a : accs) {
						acc = strategy.combine(acc, a);
					}
					
					
					int k = 0;
					for (int i=0;i<bufferSize;i++) {
						if (rs.get(i) == 0) {
							k += fib((Integer) argsBack.get(i));
						}
					}
					int k2 = 0;
					for (A a: argsNext) {
						k2 += fib((Integer) a);
					}
					System.out.println("Acc: " + acc + ", l1: " + k + ", l2: " + k2);
				}
			}

			if (argsNext.isEmpty()) {
				isDone = true;
			} else {				
				if (argsNext.size() <= MAX_ITEMS) {
					args = argsNext;
					argsNext = args.subList(0, 0);
				} else {
					if (System.getenv("FIFO") != null) {
						args = argsNext.subList(0, MAX_ITEMS);
						argsNext = argsNext.subList(MAX_ITEMS, argsNext.size());
					} else {
						args = argsNext.subList(argsNext.size()-MAX_ITEMS, argsNext.size());
						argsNext = argsNext.subList(0, argsNext.size()-MAX_ITEMS);
					}
				}
			}
		}

		PList<R> accs = (PList<R>) BufferHelper.extractFromBuffer(accbuffer, q,
				eventsArr[0], strategy.getSeed().getClass().getSimpleName(),
				MAX_ITEMS);
		for (int i = 0; i < MAX_ITEMS; i++) {
			output = strategy.combine(output, accs.get(i));
		}
	}

	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
	}

	@Override
	public String getSource() {
		return gen.getRecursiveKernelSource();
	}

	@Override
	public String getKernelName() {
		return gen.getRecursiveKernelName();
	}

	public Integer getStackSize() {
		return DEFAULT_SPAWN;
	}

	public R getOutput() {
		return output;
	}

	public String getRType() {
		return BufferHelper.getCLTypeOf(strategy.getSeed().getClass()
				.getSimpleName());
	}

	public String getAType() {
		return BufferHelper.getCLTypeOf(strategy.getArgument().getClass()
				.getSimpleName());
	}

	public BinaryRecursiveStrategy<R, A> getRecursiveStrategy() {
		return strategy;
	}
	
	public static int fib(int n) {
		if (n <= 2) return 1;
		return fib(n-1) + fib(n-2);
	}

	public int getNumWorkers() {
		return MAX_ITEMS;
	}
	
}
