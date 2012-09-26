package org.lttng.studio.model;

import java.util.HashMap;

public class ModelRegistry {

	private static ModelRegistry self = null;
	HashMap<Object, HashMap<Class<?>, Object>> reg;

	private ModelRegistry() {
		reg = new HashMap<Object, HashMap<Class<?>, Object>>();
	}

	public static ModelRegistry getInstance() {
		if (self == null)
			self = new ModelRegistry();
		return self;
	}

	public HashMap<Class<?>, Object> getOrCreateContext(Object context) {
		if (!reg.containsKey(context)) {
			reg.put(context, new HashMap<Class<?>, Object>());
		}
		return reg.get(context);
	}

	public Object getOrCreateModel(Object context, Class<?> klass) throws InstantiationException, IllegalAccessException {
		HashMap<Class<?>, Object> map = getOrCreateContext(context);
		if (!map.containsKey(klass)) {
			Object inst = klass.newInstance();
			map.put(klass, inst);
		}
		return map.get(klass);
	}

	public Object getModel(Object context, Class<?> klass) {
		if (!reg.containsKey(context))
			return null;
		return reg.get(context).get(klass);
	}

}
