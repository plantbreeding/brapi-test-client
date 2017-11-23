package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.MonthlyJob;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.SchedulerManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.WeeklyJob;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import io.restassured.RestAssured;

/**
 * Initializes tables, Daos and others.
 */
@WebListener
public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            DataSourceManager.closeConnectionSource();
            SchedulerManager.getScheduler().shutdown();

        } catch (IOException | SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent e) {
        Config.init();
        createDatabaseConnection(e.getServletContext());
        createTables();
        buildDaos();
        setupScheduler(e.getServletContext());
        if (Config.get("proxy") != null) {
            RestAssured.proxy(Config.get("proxy"), Integer.parseInt(Config.get("proxyport")));
        }


    }

    private static void createDatabaseConnection(ServletContext servletContext) {
        String path = Config.get("dbpath");
        String databaseUrl = "jdbc:h2:" + path + "brapivalDB";
        ConnectionSource connectionSource;
        try {
            connectionSource = new JdbcPooledConnectionSource(databaseUrl);
            ((JdbcPooledConnectionSource) connectionSource).setUsername(Config.get("dbuser"));
            ((JdbcPooledConnectionSource) connectionSource).setPassword(Config.get("dbpass"));
            DataSourceManager.setConnectionSource(connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() {
        try {
            DataSourceManager.createTable(Endpoint.class);
            DataSourceManager.createTable(TestReport.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void buildDaos() {
        try {
            DataSourceManager.addDao(Endpoint.class);
            DataSourceManager.addDao(TestReport.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void setupScheduler(ServletContext ctx) {
    	String key = "org.quartz.impl.StdSchedulerFactory.KEY";
    	StdSchedulerFactory factory = (StdSchedulerFactory) ctx
    		       .getAttribute(key);
        try {
			Scheduler quartzScheduler = factory.getScheduler("QuartzSchedulerInstance");
			
	    	JobDetail weeklyJob = newJob(WeeklyJob.class)
	    		      .withIdentity("weeklyJob", "group1")
	    		      .build();
	    
	    	JobDetail monthlyJob = newJob(MonthlyJob.class)
	  		      .withIdentity("monthlyJob", "group1")
	  		      .build();
	    	
	    	Trigger weeklyTrigger = newTrigger()
	    		    .withIdentity("weekly", "group1")
	    		    .startNow()
	    		    //.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(15, 20)) // for testing
	    		    .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(DateBuilder.THURSDAY, 15, 43)) // fire every Monday at 08:00
	    		    .build();
	    	Trigger monthlyTrigger = newTrigger()
	    		    .withIdentity("monthly", "group1")
	    		    .startNow()
	    		    .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 8, 0)) // fire every 1st at 08:00
	    		    .build();
	    	quartzScheduler.scheduleJob(weeklyJob, weeklyTrigger);
	    	quartzScheduler.scheduleJob(monthlyJob, monthlyTrigger);
	    	
	    	quartzScheduler.start();
	    	
	    	SchedulerManager.setScheduler(quartzScheduler);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

    	
    }

}
