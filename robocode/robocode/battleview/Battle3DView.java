/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - Rewritten
 *******************************************************************************/
package robocode.battleview;


import robocode.battle.events.BattleEventDispatcher;
import robocode.battle.events.BattleAdaptor;
import robocode.battle.snapshot.TurnSnapshot;
import robocode.battle.snapshot.BulletSnapshot;
import robocode.battle.snapshot.RobotSnapshot;
import robocode.battlefield.BattleField;
import robocode.battlefield.DefaultBattleField;
import robocode.dialog.RobocodeFrame;
import robocode.gfx.RobocodeLogo;
import robocode.manager.RobocodeManager;
import robocode.pimods.XMLMessageMaker;
import robocode.control.BattleSpecification;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.*;

import pimods.Animator;
import pimods.DataStore;
import pimods.robocode.GraphicListener4Robocode;
import pimods.robocode.MVCManager4Robocode;
import pimods.robocode.OptionFrame4Robocode;
import pimods.robocode.animators.Animator4Robocode;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Pavel Savara (contributor)
 * @author Marco Della Vedova
 */
@SuppressWarnings("serial")
public class Battle3DView extends GLCanvas {
	private final static int TIMER_TICKS_PER_SECOND = 50;

	private final static String ROBOCODE_SLOGAN = "Build the best, destroy the rest";

	private RobocodeFrame robocodeFrame;

	// The battle and battlefield,
	private BattleField battleField;
	private BattleObserver observer;

	private boolean initialized;

	// Draw option related things

	// FPS (frames per second) calculation
	private int fps;

	private GeneralPath robocodeTextPath = new RobocodeLogo().getRobocodeText();
	private Animator animator;
	private XMLMessageMaker xmlMaker;
	private DataStore dataStore;
    private MVCManager4Robocode mvcManager; //TODO HINT

    /**
	 * BattleView constructor.
	 */
	public Battle3DView(RobocodeManager manager, RobocodeFrame robocodeFrame) {
		super( new GLCapabilities() );
		mvcManager = new MVCManager4Robocode( this ); //TODO HINT
		OptionFrame4Robocode oFrame = new OptionFrame4Robocode( mvcManager );
		mvcManager.setOptionFrame( oFrame );
		oFrame.setVisible( true );
		dataStore = new DataStore();
		animator = new Animator4Robocode( mvcManager, dataStore );
		mvcManager.setup( new GraphicListener4Robocode(), animator );
		
		this.robocodeFrame = robocodeFrame;

		battleField = new DefaultBattleField(800, 600);
		
		xmlMaker = new XMLMessageMaker();
	}

	public int getFPS() {
		return fps;
	}
	
	/**
	 * Shows the next frame. The game calls this every frame.
	 */
	private void update(TurnSnapshot snapshot) {
		if (!initialized) {
			initialize(snapshot);
		}

		if (robocodeFrame.isIconified() /*|| offscreenImage == null */ || !isDisplayable() || (getWidth() <= 0)
				|| (getHeight() <= 0)) {
			return;
		}
		if(snapshot!=null){
//			System.out.println( lastSnapshot);
			
			xmlMaker.addTurn(snapshot.getTurn());
			for( RobotSnapshot r : snapshot.getRobots()){
				if( r.getState().isAlive()){
					xmlMaker.addTankPosition(r.getVeryShortName(), r.getX(), r.getY(), r.getBodyHeading(), 
							r.getEnergy(), r.getGunHeading(), r.getRadarHeading());
				}
			}
			for( BulletSnapshot b : snapshot.getBullets()){
				if( b.getState().getValue()<2){
					xmlMaker.addBullet( b.getId(), b.getX(), b.getY(), b.getPower());
				}
			}
			String message=xmlMaker.getCurrentTurn();
//			System.out.println(message);
			dataStore.setData(message);
            mvcManager.update(); //TODO HINT:
        }

	}

	@Override
	public void paint(Graphics g) {
		if (observer != null && observer.isRunning()) {
			update(observer.getLastSnapshot());
		} else {
			paintRobocodeLogo((Graphics2D) g);
		}
	}

	public void setDisplayOptions() {

	}

	private void initialize(TurnSnapshot snapshot) {
		setDisplayOptions();

		if(snapshot!=null){
			xmlMaker.clear();
			xmlMaker.setupField( battleField.getWidth(), battleField.getHeight() );
			for(RobotSnapshot r : snapshot.getRobots()){
				xmlMaker.setupTank(r.getVeryShortName(), r.getBodyColor(), r.getGunColor(), r.getRadarColor(), r.getScanColor()); //the last will be bulletcolor
			}
			String message=xmlMaker.getSettings();			
			dataStore.setData( message );
			
			System.out.println("Initilize: "+message);	
			initialized = true;
		
		}
	}

	public void setup(BattleField battleField, BattleEventDispatcher battleEventDispatcher) {
		this.battleField = battleField;
		if (observer != null) {
			observer.dispose();
		}
		observer = new BattleObserver(battleEventDispatcher);
	}

	/**
	 * Draws the Robocode logo
	 */
	private void paintRobocodeLogo(Graphics2D g) {
		setBackground(Color.BLACK);
		g.clearRect(0, 0, getWidth(), getHeight());

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.transform(AffineTransform.getTranslateInstance((getWidth() - 320) / 2.0, (getHeight() - 46) / 2.0));
		g.setColor(new Color(0, 0x40, 0));
		g.fill(robocodeTextPath);

		Font font = new Font("Dialog", Font.BOLD, 14);
		int width = g.getFontMetrics(font).stringWidth(ROBOCODE_SLOGAN);

		g.setTransform(new AffineTransform());
		g.setFont(font);
		g.setColor(new Color(0, 0x50, 0));
		g.drawString(ROBOCODE_SLOGAN, (float) ((getWidth() - width) / 2.0), (float) (getHeight() / 2.0 + 50));
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

    private class BattleObserver extends AwtBattleAdaptor {
        public BattleObserver(BattleEventDispatcher dispatcher) {
            super(dispatcher, TIMER_TICKS_PER_SECOND, false);
        }

        protected void updateView(TurnSnapshot snapshot) {
            update(snapshot);
        }
    }

}