/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.gemini.management.integration.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.gemini.management.framework.Framework;
import org.junit.Test;
import org.osgi.jmx.framework.FrameworkMBean;

/**
 * Integration tests for the {@link Framework} implementation of {@link FrameworkMBean}
 *
 */
public final class FrameworkTest extends AbstractOSGiMBeanTest {
	
	public FrameworkTest() {
		super.mBeanObjectName = FrameworkMBean.OBJECTNAME;
		super.addFrameworkAndUUID = true;
	}
	
	@Test
	public void frameworkStartLevelTest() throws Exception {
		Integer result = jmxFetchAttribute("FrameworkStartLevel", Integer.class);
		assertEquals(6, result.intValue());
	}
	
}
