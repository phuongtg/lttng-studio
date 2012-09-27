package org.lttng.studio.latency.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class LatencyEventHandler extends TraceEventHandlerBase {
	
	// (tid, evname, prevts)
	Table<Long, String, Long> historyTable;
	Table<Long, String, List<Long>> latencyTable;
	private SystemModel system; 
	
	public LatencyEventHandler() {
		super();
		this.hooks.add(new TraceHook());
	}
	
	@Override
	public void handleInit(TraceReader reader) {
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		system.init(reader);
		historyTable = HashBasedTable.create();
		latencyTable = HashBasedTable.create();
	} 

	@Override
	public void handleComplete(TraceReader reader) {
		for (Long row: latencyTable.rowKeySet()) {
			Map<String, List<Long>> rowData = latencyTable.row(row);
			for (String evName: rowData.keySet()) {
				System.out.println(String.format("%d %s %s", row, evName, rowData.get(evName)));
			}
		}
	}
	
	public void handle_all_event(TraceReader reader, EventDefinition event) {
		int cpu = event.getCPU();
		long tid = system.getCurrentTid(cpu);
		if (tid < 0)
			return;
		String evName = event.getDeclaration().getName();
		Long prevts = event.getTimestamp();
		if (historyTable.contains(tid, evName)) {
			prevts = historyTable.get(tid, evName);
		}
		if (prevts < event.getTimestamp()) {
			Long delta = event.getTimestamp() - prevts;
			List<Long> list = latencyTable.get(tid, evName);
			if (list == null) {
				list = new ArrayList<Long>();
				latencyTable.put(tid, evName, list);
			}
			list.add(delta);
		}
		historyTable.put(tid, evName, event.getTimestamp());
	}
}
