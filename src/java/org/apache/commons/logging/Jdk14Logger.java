/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Attic/Jdk14Logger.java,v 1.1 2002/01/05 22:40:40 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/05 22:40:40 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


package org.apache.commons.logging;


import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Implementation of the <code>org.apache.commons.logging.Log</code>
 * interfaces that wraps the standard JDK logging mechanisms that were
 * introduced in the Merlin release (JDK 1.4).</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2002/01/05 22:40:40 $
 */

public class Jdk14Logger implements Log {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk14Logger(String name) {

        logger = Logger.getLogger(name);
        logger.setUseParentHandlers(true);
        logger.setLevel(Level.INFO);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The underlying Logger implementation we are using.
     */
    protected Logger logger = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Log a message with debug log level.
     */
    public void debug(Object message) {

        logger.log(Level.FINEST, message.toString());

    }


    /**
     * Log a message and exception with debug log level.
     */
    public void debug(Object message, Throwable exception) {

        logger.log(Level.FINEST, message.toString(), exception);

    }


    /**
     * Log a message with error log level.
     */
    public void error(Object message) {

        logger.log(Level.SEVERE, message.toString());

    }


    /**
     * Log a message and exception with error log level.
     */
    public void error(Object message, Throwable exception) {

        logger.log(Level.SEVERE, message.toString(), exception);

    }


    /**
     * Log a message with fatal log level.
     */
    public void fatal(Object message) {

        logger.log(Level.SEVERE, message.toString());

    }


    /**
     * Log a message and exception with fatal log level.
     */
    public void fatal(Object message, Throwable exception) {

        logger.log(Level.SEVERE, message.toString(), exception);

    }


    /**
     * Return the current logging level.
     */
    public int getLevel() {

        Level level = logger.getLevel();
        if (level == Level.ALL) {
            return (ALL);
        } else if (level == Level.SEVERE) {
            return (ERROR);
        } else if (level == Level.WARNING) {
            return (WARN);
        } else if (level == Level.INFO) {
            return (INFO);
        } else if (level == Level.CONFIG) {
            return (DEBUG);
        } else if (level == Level.FINE) {
            return (DEBUG);
        } else if (level == Level.FINER) {
            return (DEBUG);
        } else if (level == Level.FINEST) {
            return (DEBUG);
        } else {
            return (OFF);
        }

    }


    /**
     * Return the native Logger instance we are using.
     */
    public Logger getLogger() {

        return (this.logger);

    }


    /**
     * Log a message with info log level.
     */
    public void info(Object message) {

        logger.log(Level.INFO, message.toString());

    }


    /**
     * Log a message and exception with info log level.
     */
    public void info(Object message, Throwable exception) {

        logger.log(Level.INFO, message.toString(), exception);

    }


    /**
     * Is debug logging currently enabled?
     */
    public boolean isDebugEnabled() {

        return (logger.isLoggable(Level.FINEST));

    }


    /**
     * Is info logging currently enabled?
     */
    public boolean isInfoEnabled() {

        return (logger.isLoggable(Level.INFO));

    }


    /**
     * Set the new logging level.
     *
     * @param level New logging level
     */
    public void setLevel(int level) {

        if (level == OFF) {
            logger.setLevel(Level.OFF);
        } else if (level >= FATAL) {
            logger.setLevel(Level.SEVERE);
        } else if (level >= ERROR) {
            logger.setLevel(Level.SEVERE);
        } else if (level >= WARN) {
            logger.setLevel(Level.WARNING);
        } else if (level >= INFO) {
            logger.setLevel(Level.INFO);
        } else if (level >= DEBUG) {
            logger.setLevel(Level.FINEST);
        } else if (level >= ALL) {
            logger.setLevel(Level.ALL);
        }

    }


    /**
     * Log a message with warn log level.
     */
    public void warn(Object message) {

        logger.log(Level.WARNING, message.toString());

    }


    /**
     * Log a message and exception with warn log level.
     */
    public void warn(Object message, Throwable exception) {

        logger.log(Level.WARNING, message.toString(), exception);

    }


}
