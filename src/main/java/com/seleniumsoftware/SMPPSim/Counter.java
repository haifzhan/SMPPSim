/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seleniumsoftware.SMPPSim;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hzhang
 */
class Counter extends TimerTask
{
    public static long counter = 0;
    private Logger logger =  LoggerFactory.getLogger(this.getClass().getSimpleName());
    
    public void run()
    {
       logger.info("COUNTER: {}", counter);
    }
}
