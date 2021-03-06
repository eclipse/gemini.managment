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
 ******************************************************************************/

package org.eclipse.gemini.management.permissionadmin;

import java.io.IOException;

import org.osgi.jmx.service.permissionadmin.PermissionAdminMBean;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.permissionadmin.PermissionInfo;

/** 
 * 
 */
public final class PermissionManager implements PermissionAdminMBean {

	private PermissionAdmin admin;

	public PermissionManager(PermissionAdmin admin) {
		this.admin = admin;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] listLocations() throws IOException {
		return admin.getLocations();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getPermissions(String location) throws IOException {
		if (location == null) {
			throw new IOException("Locatin must not be null");
		}
		PermissionInfo[] permissions = admin.getPermissions(location);
		if (permissions == null) {
			return null;
		}
		String[] encodedPermissions = new String[permissions.length];
		int i = 0;
		for (PermissionInfo permission : permissions) {
			encodedPermissions[i++] = permission.getEncoded();
		}
		return encodedPermissions;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefaultPermissions(String[] encodedPermissions) throws IOException {
		if (encodedPermissions == null) {
			throw new IOException("Encoded permissions must not be null");
		}
		PermissionInfo[] permissions = new PermissionInfo[encodedPermissions.length];
		int i = 0;
		for (String encodedPermission : encodedPermissions) {
			try {
				permissions[i] = new PermissionInfo(encodedPermission);
			} catch (Throwable e) {
				IOException iox = new IOException("Invalid encoded permission: " + encodedPermission);
				iox.initCause(e);
				throw iox;
			}
		}
		admin.setDefaultPermissions(permissions);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] listDefaultPermissions() throws IOException {
		PermissionInfo[] permissions = admin.getDefaultPermissions();
		if (permissions == null) {
			return null;
		}
		String[] encodedPermissions = new String[permissions.length];
		int i = 0;
		for (PermissionInfo permission : permissions) {
			encodedPermissions[i++] = permission.getEncoded();
		}
		return encodedPermissions;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPermissions(String location, String[] encodedPermissions) throws IOException {
		if (location == null) {
			throw new IOException("Location must not be null");
		}
		if (encodedPermissions == null) {
			throw new IOException("Encoded permissions must not be null");
		}
		PermissionInfo[] permissions = new PermissionInfo[encodedPermissions.length];
		int i = 0;
		for (String encodedPermission : encodedPermissions) {
			try {
				permissions[i] = new PermissionInfo(encodedPermission);
			} catch (Throwable e) {
				IOException iox = new IOException("Invalid encoded permission: " + encodedPermission);
				iox.initCause(e);
				throw iox;
			}
		}
		admin.setPermissions(location, permissions);
	}

}
