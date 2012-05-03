package util;

import java.util.Queue;

public abstract class ComputingScore implements Runnable {
	private Mode mMode;
	private Queue<Object> mQueue;	
	public ComputingScore() {
		this.mMode = null;
		this.mQueue = null;		
	}
	public ComputingScore(Mode m, Queue<Object> q) {
		this.mMode = m;
		this.mQueue = q;		
	}
	public Mode getMode() { return this.mMode; }
	public Queue<Object> getQueue() { return this.mQueue; }	
	public abstract void run();
}
