package org.lttng.studio.latency.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
	Table<Long, String, SummaryStatistics> statsTable;
	//Table<Long, String, MaxHeap> heapTable;
	private SystemModel system;
	private HashSet<String> monitor; 
	
	public LatencyEventHandler() {
		super();
		this.hooks.add(new TraceHook());
		monitor = new HashSet<String>();
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
		statsTable = HashBasedTable.create();
	} 

	@Override
	public void handleComplete(TraceReader reader) {

	}

	public boolean monitorEvent(String name) {
		return monitor.add(name);
	}
	
	public boolean monitorAllEvents(Collection<String> eventNames) {
		return monitor.addAll(eventNames);
	}
	
	public void handle_all_event(TraceReader reader, EventDefinition event) {
		int cpu = event.getCPU();
		long tid = system.getCurrentTid(cpu);
		if (tid < 0)
			return;
		String evName = event.getDeclaration().getName();
		if (!monitor.contains(evName))
			return;
		Long prevts = event.getTimestamp();
		if (historyTable.contains(tid, evName)) {
			prevts = historyTable.get(tid, evName);
		}
		if (prevts < event.getTimestamp()) {
			Long delta = event.getTimestamp() - prevts;
			
			// latency
			List<Long> list = latencyTable.get(tid, evName);
			if (list == null) {
				list = new ArrayList<Long>();
				latencyTable.put(tid, evName, list);
			}
			list.add(delta);

			// stats
			SummaryStatistics stats = statsTable.get(tid, evName);
			if (stats == null) {
				stats = new SummaryStatistics();
				statsTable.put(tid, evName, stats);
			}
			stats.addValue((double) delta);
		}
		historyTable.put(tid, evName, event.getTimestamp());
	}

	public Table<Long, String, SummaryStatistics> getStatsTable() {
		return statsTable;
	}

	public Table<Long, String, List<Long>> getLatencyTable() {
		return latencyTable;
	}
	
	public void printLatencyTable() {
		for (Long row: latencyTable.rowKeySet()) {
			Map<String, List<Long>> rowData = latencyTable.row(row);
			for (String evName: rowData.keySet()) {
				System.out.println(String.format("%d %s %s", row, evName, rowData.get(evName)));
			}
		}
	}
	
	public void printStatsTable() {
		StringBuilder str = new StringBuilder();
		String format = "%8d %20s %8d %10.0f %10.0f %10.0f %10.0f\n";
		String header = "%8s %20s %8s %10s %10s %10s %10s\n";
		str.append(String.format(header, "tid", "ev", "N", "min", "max", "avg", "stddev"));
		for (Long row: statsTable.rowKeySet()) {
			Map<String, SummaryStatistics> rowData = statsTable.row(row);
			for (String evName: rowData.keySet()) {
				SummaryStatistics stats = rowData.get(evName);
				str.append(String.format(format, row, evName,
						stats.getN(), stats.getMin(), stats.getMax(),
						stats.getMean(), stats.getStandardDeviation()));
			}
		}
		System.out.println(str.toString());
	}
}
