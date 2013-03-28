package org.azeckoski.reflectutils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple LifecycleManager for the reflection utilities. This is provided to
 * allow
 * cleanup under certain conditions (ie. the background thread that is run by
 * the ReferenceMap)
 * 
 * Initially, the LifecycleManager is not active, and the reflection utilities
 * will attempt
 * to clean themselves up. To use an explicit lifecycle, first call setActive()
 * to start the manager
 * (before anything uses the reflection utilities - ie. in the context
 * initiliser of a web application!),
 * and then call 'shutdown()' during termination.
 */
public class LifecycleManager {
	
	private static boolean isActive = false;
	private static List<Lifecycle> managedObjects = new ArrayList<Lifecycle>();
	private static Lock moLock = new ReentrantLock();
	
	/**
	 * Test to see if the manager is active
	 * 
	 * @return true if active
	 */
	public static boolean isActive() {
		return LifecycleManager.isActive;
	}
	
	/**
	 * Register an object to receive lifecycle events
	 * 
	 * @param object
	 */
	public static void register(final Lifecycle object) {
		if (LifecycleManager.isActive) {
			LifecycleManager.moLock.lock();
			try {
				if (LifecycleManager.managedObjects == null) {
					throw new RuntimeException("Unable to register - manager already shut down");
				}
				
				LifecycleManager.managedObjects.add(object);
			}
			finally {
				LifecycleManager.moLock.unlock();
			}
		}
	}
	
	/**
	 * Activate the lifecycle manager
	 * 
	 * @param active
	 */
	public static void setActive(final boolean active) {
		LifecycleManager.isActive = active;
	}
	
	/**
	 * Request shutdown for any objects registered with the lifecycle manager
	 */
	public synchronized static void shutdown() {
		LifecycleManager.moLock.lock();
		try {
			if (LifecycleManager.managedObjects != null) {
				while (LifecycleManager.managedObjects.size() > 0) {
					final Lifecycle obj = LifecycleManager.managedObjects.remove(LifecycleManager.managedObjects.size() - 1);
					obj.shutdown();
				}
				
				LifecycleManager.managedObjects = null;
			}
		}
		finally {
			LifecycleManager.moLock.unlock();
		}
	}
}
