package org.lttng.studio.reader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;

public class TraceReader {

	protected String tracePath;
	private CTFTraceReader reader;
	private final Map<Class<?>, ITraceEventHandler> handlers;
	private final Map<String, TreeSet<TraceHook>> eventHookMap;
	private final Map<Long, TreeSet<TraceHook>> eventHookMapCache;
	private final TreeSet<TraceHook> catchAllHook;
	private static Class<?>[] argTypes = new Class<?>[] { TraceReader.class, EventDefinition.class };
	private final TimeKeeper timeKeeper;
	private boolean cancel;
	private int nbCpus;
	private Exception exception;

	public TraceReader(String trace_path) {
		this.tracePath = trace_path;
		handlers = new HashMap<Class<?>, ITraceEventHandler>();
		eventHookMap = new HashMap<String, TreeSet<TraceHook>>();
		eventHookMapCache = new HashMap<Long, TreeSet<TraceHook>>();
		catchAllHook = new TreeSet<TraceHook>();
		timeKeeper = TimeKeeper.getInstance();
	}

	public TraceReader(File file) throws IOException {
		this(file.getCanonicalPath());
	}

	public void loadTrace() throws CTFReaderException {
		setReader(new CTFTraceReader(new CTFTrace(tracePath)));
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
		Long eventId;
		cancel = false;

		for(ITraceEventHandler handler: handlers.values()) {
			if (cancel == true)
				break;
			handler.handleInit(this, getReader().getTrace());
		}
		// Re-throw any handler exception
		if (exception != null)
			throw exception;

		buildHookCache();
		getReader().seek(0);
		while((event=getReader().getCurrentEventDef()) != null && cancel == false) {
			timeKeeper.setCurrentTime(event.getTimestamp());
			eventId = event.getDeclaration().getId();
			TreeSet<TraceHook> treeSet = eventHookMapCache.get(eventId);
			if (treeSet != null)
				runHookSet(treeSet, event);
			runHookSet(catchAllHook, event);
			getReader().advance();
		}

		// Re-throw any handler exception
		if (exception != null)
			throw exception;

		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleComplete(this);
		}
	}

	public void buildHookCache() {
		CTFTrace trace = getReader().getTrace();
		Set<Long> streamIds = trace.getStreams().keySet();
		for (Long id: streamIds) {
			HashMap<Long, EventDeclaration> decl = trace.getEvents(id);
			for (Long evId: decl.keySet()) {
				String eventName = decl.get(evId).getName();
				Set<TraceHook> hooks = eventHookMap.get(eventName);
				if (hooks == null)
						continue;
				TreeSet<TraceHook> set = eventHookMapCache.get(evId);
				if (set == null) {
					set = new TreeSet<TraceHook>();
					eventHookMapCache.put(evId, set);
				}
				set.addAll(hooks);
			}
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

	public Long getStartTime() {
		return getReader().getStartTime();
	}

	public Long getEndTime() {
		return getReader().getEndTime();
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

	public CTFTraceReader getReader() {
		return reader;
	}

	public void setReader(CTFTraceReader reader) {
		this.reader = reader;
		Field field;
		try {
			field = reader.getClass().getDeclaredField("streamInputReaders");
			field.setAccessible(true);
			Vector v = (Vector) field.get(reader);
			setNbCpus(v.size());
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
		this.eventHookMapCache.clear();
	}
}
