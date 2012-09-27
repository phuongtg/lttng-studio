package org.lttng.studio.reader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;

public class TraceReader {

	private List<CTFTraceReader> readers;
	private final Map<Class<?>, ITraceEventHandler> handlers;
	private final Map<String, TreeSet<TraceHook>> eventHookMap;
	private final TreeSet<TraceHook> catchAllHook;
	private static Class<?>[] argTypes = new Class<?>[] { TraceReader.class, EventDefinition.class };
	private final TimeKeeper timeKeeper;
	private boolean cancel;
	private int nbCpus;
	private Exception exception;

	public TraceReader() {
		handlers = new HashMap<Class<?>, ITraceEventHandler>();
		eventHookMap = new HashMap<String, TreeSet<TraceHook>>();
		catchAllHook = new TreeSet<TraceHook>();
		readers = new ArrayList<CTFTraceReader>();
		timeKeeper = TimeKeeper.getInstance();
		
	}

	public void loadTrace() throws CTFReaderException {
		checkNumStreams();
	}

	public void registerHook(ITraceEventHandler handler, TraceHook hook) {
		String methodName;
		if (hook.isAllEvent()) {
			methodName = "handle_all_event";
		} else {
			methodName = "handle_" + hook.eventName;
		}
		boolean isHookOk = true;
		TreeSet<TraceHook> eventHooks;
		hook.instance = handler;
		try {
			hook.method = handler.getClass().getMethod(methodName, argTypes);
		} catch (SecurityException e) {
			e.printStackTrace();
			isHookOk = false;
		} catch (NoSuchMethodException e) {
			System.err.println("Error: hook " + handler.getClass() + "." + methodName + " doesn't exist, disabling");
			isHookOk = false;
		}
		if (!isHookOk)
			return;

		if(hook.isAllEvent()) {
			catchAllHook.add(hook);
		} else {
			eventHooks = eventHookMap.get(hook.eventName);
			if (eventHooks == null) {
				eventHooks = new TreeSet<TraceHook>();
				eventHookMap.put(hook.eventName, eventHooks);
			}
			eventHooks.add(hook);
		}
	}

	public void register(ITraceEventHandler handler) {
		if (handler == null)
			return;
		Set<TraceHook> handlerHooks = handler.getHooks();

		/* If handlerHooks is null then add no hooks */
		if (handlerHooks == null || handlerHooks.size() == 0) {
			return;
		}

		/* register individual hooks */
		for (TraceHook hook: handlerHooks) {
			registerHook(handler, hook);
		}

		handlers.put(handler.getClass(), handler);
	}

	public void process() throws Exception {
		loadTrace();
		EventDefinition event;
		CTFTraceReader currentReader;
		String eventName;
		cancel = false;

		for(ITraceEventHandler handler: handlers.values()) {
			if (cancel == true)
				break;
			handler.handleInit(this);
		}
		// Re-throw any handler exception
		if (exception != null)
			throw exception;

		for (CTFTraceReader reader: readers) {
			reader.seek(0);
		}
		
		PriorityQueue<CTFTraceReader> prio = new PriorityQueue<CTFTraceReader>(readers.size(), new CTFTraceReaderComparator());
		prio.addAll(readers);
		//while((event=getReader().getCurrentEventDef()) != null && cancel == false) {
		while((currentReader=prio.poll()) != null) {
			event = currentReader.getCurrentEventDef();
			if (event == null)
				continue;
			if (cancel == true || exception != null)
				break;
			timeKeeper.setCurrentTime(event.getTimestamp());
			eventName = event.getDeclaration().getName();
			TreeSet<TraceHook> treeSet = eventHookMap.get(eventName);
			if (treeSet != null)
				runHookSet(treeSet, event);
			runHookSet(catchAllHook, event);
			currentReader.advance();
			prio.add(currentReader);
		}

		// Re-throw any handler exception
		if (exception != null)
			throw exception;

		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleComplete(this);
		}
	}

	public void runHookSet(TreeSet<TraceHook> hooks, EventDefinition event) {
		for (TraceHook h: hooks){
			try {
				h.method.invoke(h.instance, this, event);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				cancel = true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				cancel = true;
			} catch (InvocationTargetException e) {
				System.err.println("error while executing " + h.method + " on " + h.instance);
				e.printStackTrace();
				cancel = true;
			}
		}
	}

	public ITraceEventHandler getHandler(
			Class<? extends TraceEventHandlerBase> klass) {
		return handlers.get(klass);
	}

	public void cancel() {
		this.cancel = true;
	}
	public void cancel(Exception e) {
		this.cancel = true;
		this.exception = e;
	}

	public Boolean isCancel() {
		return this.cancel;
	}

	public void addReader(CTFTraceReader reader) {
		readers.add(reader);
	}

	public void checkNumStreams() {
		if (readers.isEmpty())
			return;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (CTFTraceReader reader: readers) {
			int num = getNumStreams(reader);
			min = Math.min(num, min);
			max = Math.max(num, max);
		}
		if (min != max) {
			throw new RuntimeException("All traces must have the same number of streams");
		}
		setNbCpus(max);
	}
	
	public static int getNumStreams(CTFTraceReader reader) {
		Field field;
		try {
			field = reader.getClass().getDeclaredField("streamInputReaders");
			field.setAccessible(true);
			Vector v = (Vector) field.get(reader);
			return v.size();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error trying to retreive the number of CPUs of the trace");
		}		
	}
	
	public int getNumCpus() {
		return nbCpus;
	}

	public void setNbCpus(int nbCpus) {
		this.nbCpus = nbCpus;
	}

	public void clearHandlers() {
		this.handlers.clear();
		this.eventHookMap.clear();
	}
	
	public void addTrace(File file) throws CTFReaderException {
		CTFTraceReader reader = new CTFTraceReader(new CTFTrace(file));
		addReader(reader);
	}
}
