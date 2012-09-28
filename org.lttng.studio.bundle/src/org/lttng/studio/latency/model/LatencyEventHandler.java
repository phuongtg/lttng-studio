package org.lttng.studio.latency.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramUtils;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.MinMaxPriorityQueue.Builder;
import com.google.common.collect.Table;

public class LatencyEventHandler extends TraceEventHandlerBase {

	public class HighLatency {
		public long ts;
		public long delta;
	}

	// (tid, evname, prevts)
	Table<Long, String, Long> historyTable;
	Table<Long, String, List<Long>> latencyTable;
	Table<Long, String, SummaryStatistics> statsTable;
	Table<Long, String, MinMaxPriorityQueue<HighLatency>> heapTable;
	private SystemModel system;
	private final HashSet<String> monitor;
	Builder<HighLatency> heapBuilder;

	public LatencyEventHandler() {
		super();
		this.hooks.add(new TraceHook());
		monitor = new HashSet<String>();
		heapBuilder = MinMaxPriorityQueue.orderedBy(new Comparator<HighLatency>() {
			@Override
			public int compare(HighLatency self, HighLatency other) {
				// MaxPriorityQueue comparator returns inverted value
				if (self.delta < other.delta)
					return 1;
				if (self.delta > other.delta)
					return -1;
				return 0;
			}
		});
		heapBuilder.maximumSize(10);
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
		heapTable = HashBasedTable.create();
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
		Long delta = event.getTimestamp() - prevts;
		if (delta > 0) {
			// latency
			/*
			List<Long> list = latencyTable.get(tid, evName);
			if (list == null) {
				list = new ArrayList<Long>();
				latencyTable.put(tid, evName, list);
			}
			list.add(delta);
			*/

			// stats
			SummaryStatistics stats = statsTable.get(tid, evName);
			if (stats == null) {
				stats = new SummaryStatistics();
				statsTable.put(tid, evName, stats);
			}
			stats.addValue(delta);

			// highLatency
			MinMaxPriorityQueue<HighLatency> heap = heapTable.get(tid, evName);
			if (heap == null) {
				heap = heapBuilder.create();
				heapTable.put(tid, evName, heap);
			}
			HighLatency lat = new HighLatency();
			Long offset = (Long) reader.getCurrentCtfReader().getTrace().getClock().getProperty("offset");
			lat.ts = event.getTimestamp() + offset;
			lat.delta = delta;
			heap.add(lat);
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

	public void printHighLatency() {
		StringBuilder str = new StringBuilder();
		for (Long row: heapTable.rowKeySet()) {
			Map<String, MinMaxPriorityQueue<HighLatency>> rowData = heapTable.row(row);
			for (String evName: rowData.keySet()) {
				MinMaxPriorityQueue<HighLatency> latencies = rowData.get(evName);
				str.append(String.format("%d %s :", row, evName));
				for (HighLatency lat: latencies) {
					/*
					CtfTmfTimestamp ts = new CtfTmfTimestamp(lat.ts);
					ts.setType(TimestampType.NANOS);
					long val = ts.normalize(0, -9).getValue();
					*/
					String nano = HistogramUtils.nanosecondsToString(lat.ts);
					str.append(String.format("[%s %d %d] ", nano, lat.ts, lat.delta));
				}
				str.append("\n");
			}
		}
		System.out.println(str.toString());
	}
}
