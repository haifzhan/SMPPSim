package com.seleniumsoftware.SMPPSim;

import java.util.TimerTask;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
/**
 *
 * @author hzhang
 */
class Counter extends TimerTask
{
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    public static int counter = 0;
    
    @Override
    public void run()
    {
        logger.info("COUNTER: {}", counter);
    }
}

