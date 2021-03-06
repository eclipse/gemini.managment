/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 *     Christopher Frost - Updates for RFC 169
 ******************************************************************************/

package org.eclipse.gemini.management.framework;

import static org.osgi.framework.Constants.OBJECTCLASS;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.eclipse.gemini.management.Monitor;
import org.eclipse.gemini.management.framework.internal.OSGiService;
import org.eclipse.gemini.management.framework.internal.OSGiServiceEvent;
import org.eclipse.gemini.management.internal.OSGiProperties;
import org.osgi.framework.AllServiceListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.util.tracker.ServiceTracker;

/** 
 * 
 */
public final class ServiceState extends Monitor implements CustomServiceStateMBean {

	private ServiceListener serviceListener;
	
	private BundleContext bundleContext;
	
	/**
	 * Constructor
	 * 
	 * @param bundleContext
	 */
	public ServiceState(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getBundleIdentifier(long serviceId) throws IOException {
		return getServiceReference(serviceId).getBundle().getBundleId();
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getProperties(long serviceId) throws IOException {
		return OSGiProperties.tableFrom(getServiceReference(serviceId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getObjectClass(long serviceId) throws IOException {
		return (String[]) getServiceReference(serviceId).getProperty(OBJECTCLASS);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listServices() {
		ArrayList<OSGiService> services = new ArrayList<OSGiService>();
		for (Bundle bundle : bundleContext.getBundles()) {
			ServiceReference<?>[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference<?> ref : refs) {
					services.add(new OSGiService(ref));
				}
			}
		}
		return OSGiService.tableFrom(services);
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getUsingBundles(long serviceId) throws IOException {
		return OSGiService.getBundlesUsing(getServiceReference(serviceId));
	}

	
	/**
	 * {@inheritDoc}
	 */
	public CompositeData getService(long serviceId) throws IOException {
		for (Bundle bundle : bundleContext.getBundles()) {
			ServiceReference<?>[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference<?> ref : refs) {
					if(serviceId == (Long) ref.getProperty(Constants.SERVICE_ID)){
						return new OSGiService(ref).asCompositeData();
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getProperty(long serviceId, String key) throws IOException {
		for (Bundle bundle : bundleContext.getBundles()) {
			ServiceReference<?>[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference<?> ref : refs) {
					if(serviceId == (Long) ref.getProperty(Constants.SERVICE_ID)){
						return OSGiProperties.encode(key, ref.getProperty(key));
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listServices(String clazz, String filter) throws IOException {
		try {
			ArrayList<OSGiService> services = new ArrayList<OSGiService>();
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(clazz, filter);
			for (ServiceReference<?> reference : allServiceReferences) {
				services.add(new OSGiService(reference));
			}
			return OSGiService.tableFrom(services);
		} catch (InvalidSyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listServices(String clazz, String filter, String... serviceTypeItems) throws IOException {
		try {
			ArrayList<OSGiService> services = new ArrayList<OSGiService>();
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(clazz, filter);
			for (ServiceReference<?> reference : allServiceReferences) {
				services.add(new OSGiService(reference));
			}
			TabularData table = OSGiService.tableFrom(services, serviceTypeItems);
			return table;
		} catch (InvalidSyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getServiceIds() throws IOException {
		ServiceReference<?>[] allServiceReferences;
		try {
			allServiceReferences = bundleContext.getAllServiceReferences(null, null);
			long[] serviceIds = new long[allServiceReferences.length];
			for (int i = 0; i < allServiceReferences.length; i++) {
				serviceIds[i] = (Long) allServiceReferences[i].getProperty(Constants.SERVICE_ID);
			}
			return serviceIds;
		} catch (InvalidSyntaxException e) {
			throw new IOException(e);
		}
	}

	//Local extensions to the API

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getRegisteredServices(long bundleId) throws IOException {
		Bundle bundle = this.getBundle(bundleId);
		ServiceReference<?>[] registeredServices = bundle.getRegisteredServices();
		return this.getServicesAsCompositeDatas(registeredServices);
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getServicesInUse(long bundleId) throws IOException {
		Bundle bundle = this.getBundle(bundleId);
		ServiceReference<?>[] servicesInUse = bundle.getServicesInUse();
		return this.getServicesAsCompositeDatas(servicesInUse);
	}
	
	private Bundle getBundle(long bundleId){
		Bundle bundle = bundleContext.getBundle(bundleId);
		if(bundle == null) {
			throw new IllegalArgumentException("No such bundle '" + bundleId + "'");
		}
		return bundle;
	}
	
	private CompositeData[] getServicesAsCompositeDatas(ServiceReference<?>[] services){
		CompositeData[] servicesInUseCompositeData;
		if (services != null) {
			servicesInUseCompositeData = new CompositeData[services.length];
			for (int i = 0; i < services.length; i++) {
				servicesInUseCompositeData[i] = new OSGiService(services[i]).asCompositeData();
			}
		} else {
			servicesInUseCompositeData = new CompositeData[0]; 
		}
		return servicesInUseCompositeData;
	}
	
	private ServiceReference<?> getServiceReference(long serviceId) throws IOException {
		Filter filter;
		try {
			filter = bundleContext.createFilter("(" + Constants.SERVICE_ID + "=" + serviceId + ")");
		} catch (InvalidSyntaxException e) {
			throw new IOException("Invalid filter syntax: " + e);
		}
		ServiceTracker<?, ?> tracker = new ServiceTracker<Object, Object>(bundleContext, filter, null);
		tracker.open();
		ServiceReference<?> serviceReference = tracker.getServiceReference();
		if (serviceReference == null) {
			throw new IOException("Service <" + serviceId + "> does not exist");
		}
		tracker.close();
		return serviceReference;
	}
	
	//End methods for the MBean
	
	/**
	 * {@inheritDoc}
	 */
	protected void addListener() {
		serviceListener = this.getServiceListener();
		bundleContext.addServiceListener(serviceListener);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void removeListener() {
		if (serviceListener != null) {
			bundleContext.removeServiceListener(serviceListener);
		}
	}

	private ServiceListener getServiceListener() {
		return new AllServiceListener() {
			public void serviceChanged(ServiceEvent serviceEvent) {
				Notification notification = new Notification(ServiceStateMBean.EVENT, objectName, sequenceNumber++);
				notification.setUserData(new OSGiServiceEvent(serviceEvent).asCompositeData());
				sendNotification(notification);
			}
		};
	}

}
