package com.apw.oldglobal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public abstract class Log
{
    static
    {
        File folder = new File("logs");
        if(!folder.exists())
        {
            folder.mkdir();
        }
    }
    private static SimpleDateFormat file = new SimpleDateFormat("'logs\\'yyyy-mm-dd_hh-mm-ss'.log'");
    private static SimpleDateFormat line = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private static LogStream los = new LogStream(file.format(new Date()));
    
    public static void d(Object key, Object value)
    {
        debug(key, value);
    }
    
    public static void debug(Object key, Object value)
    {
        log(Level.DEBUG, key, value);
    }
    
    public static void e(Object key, Object value)
    {
        error(key, value);
    }
    
    public static void error(Object key, Object value)
    {
        log(Level.ERROR, key, value);
    }
    
    public static void f(Object key, Object value)
    {
        fatal(key, value);
    }
    
    public static void fatal(Object key, Object value)
    {
        log(Level.FATAL, key, value);
    }
    
    public static void i(Object key, Object value)
    {
        info(key, value);
    }
    
    public static void info(Object key, Object value)
    {
        log(Level.DEBUG, key, value);
    }
    
    public static void t(Object key, Object value)
    {
        trace(key, value);
    }
    
    public static void trace(Object key, Object value)
    {
        log(Level.TRACE, key, value);
    }

    public static void w(Object key, Object value)
    {
        warn(key, value);
    }
    
    public static void warn(Object key, Object value)
    {
        log(Level.DEBUG, key, value);
    }
    
    public static void log(Level level, Object key, Object value)
    {
        los.println(String.format("[%s](%s) %s: %s", line.format(new Date()), level.name(), key, value));
    }

    private static class LogStream extends PrintStream
    {
        public LogStream(String s)
        {
            super(new LogOutputStream(s));
        }
        
        private static class LogOutputStream extends OutputStream
        {
            public FileOutputStream fos = null;

            public LogOutputStream(String s)
            {
                try
                {
                    File file = new File(s);
                    if(!file.exists())
                    {
                        file.createNewFile();
                    }
                    fos = new FileOutputStream(new File(s));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void write(int b) throws IOException
            {
                System.out.print((char) b);
                fos.write(b);
            }
        }
    }

    public enum Level
    {
        INFO,  // Designates informational messages that highlight the progress
               // of the application at coarse-grained level.
        DEBUG, // Designates fine-grained informational events that are most
               // useful to debug an application.
        TRACE, // Designates finer-grained informational events than the DEBUG.
        WARN,  // Designates potentially harmful situations.
        ERROR, // Designates error events that might still allow the application
               // to continue running.
        FATAL, // Designates very severe error events that will presumably lead
               // the application to abort.
    }
}
