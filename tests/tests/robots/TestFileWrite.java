/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Pavel Savara
 *     - Initial implementation
 *******************************************************************************/
package robots;


import helpers.RobotTestBed;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;


/**
 * @author Pavel Savara (original)
 */
public class TestFileWrite extends RobotTestBed {

	@Test
	public void run() {
		super.run();
	}

	public String getRobotNames() {
		return "sample.Walls,sample.SittingDuck";
	}

	File file = new File("robots/sample/SittingDuck.data/count.dat");

	@Override
	protected void runSetup() {
		if (file.exists()) {
			file.delete();
		}
	}

	@Override
	protected void runTeardown() {
		Assert.assertTrue(file.exists());
		file.delete();
	}
}