package org.lttng.studio.state;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class TestTaskAutomaton {

	public class Transition {
		private String event;
		private State to;
		public Transition(String ev, State to) {
			this.event = ev;
			this.to = to;
		}
		public String getEvent() {
			return event;
		}
		public void setEvent(String event) {
			this.event = event;
		}
		public State getTo() {
			return to;
		}
		public void setTo(State to) {
			this.to = to;
		}
	}

	public class State {
		private int id;
		ArrayList<Transition> t;
		private boolean accepts;
		public State(int id) {
			this.setId(id);
			this.t = new ArrayList<Transition>();
		}
		public void addTransition(Transition tran) {
			t.add(tran);
		}
		public ArrayList<Transition> getTransitions() {
			return t;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public boolean isAccepts() {
			return accepts;
		}
		public void setAccepts(boolean accepts) {
			this.accepts = accepts;
		}
	}

	class Automaton {
		private State state;
		public Automaton(State initial) {
			this.setState(initial);
		}
		public State getState() {
			return state;
		}
		public void setState(State state) {
			this.state = state;
		}
	}

	public class AutomatonRunner {

		public void step(Automaton aut, String event) {
			State curr = aut.getState();
			ArrayList<Transition> transitions = curr.getTransitions();
			for (Transition t: transitions) {
				if (t.getEvent().equals(event)) {
					aut.setState(t.getTo());
				}
			}
		}

	}

	@Test
	public void testAutomaton() {
		State s0 = new State(0);
		State s1 = new State(1);
		State s2 = new State(2);
		State s3 = new State(3);
		State s4 = new State(4);
		State s5 = new State(5);
		s5.setAccepts(true);

		s0.addTransition(new Transition("sys_entry", s1));
		s1.addTransition(new Transition("sys_exit", s0));
		s1.addTransition(new Transition("sched_out", s2));
		s2.addTransition(new Transition("sched_in", s1));
		s2.addTransition(new Transition("wakeup", s3));
		s3.addTransition(new Transition("sched_in", s4));
		s4.addTransition(new Transition("sys_exit", s5));
		Automaton aut = new Automaton(s0);
		AutomatonRunner runner = new AutomatonRunner();
		runner.step(aut, "sys_entry");
		runner.step(aut, "sched_out");
		runner.step(aut, "wakeup");
		runner.step(aut, "sched_in");
		runner.step(aut, "sys_exit");
		assertTrue(aut.getState().isAccepts());
	}

}
