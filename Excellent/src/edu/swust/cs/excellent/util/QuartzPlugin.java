package edu.swust.cs.excellent.util;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





import com.jfinal.plugin.IPlugin;

import edu.swust.cs.excellent.config.Constant;

public class QuartzPlugin implements IPlugin {

	private Logger logger = (Logger) LoggerFactory.getLogger(getClass());
	private SchedulerFactory sf = null;
	private Scheduler sched = null;
	private String config = Constant.JOB_PROPERTY;
	private Properties properties;

	public QuartzPlugin(String config) {
		this.config = config;
	}

	public QuartzPlugin() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean start() {
		sf = new StdSchedulerFactory();
		try {
			sched = sf.getScheduler();
		} catch (SchedulerException e) {
			new RuntimeException(e);
		}
		loadProperties();
		Enumeration enums = properties.keys();
		while (enums.hasMoreElements()) {
			String key = enums.nextElement() + "";
			if (!key.endsWith("job")) {
				continue;
			}
			String cronKey = key.substring(0, key.indexOf("job")) + "cron";
			String enable = key.substring(0, key.indexOf("job")) + "enable";
			if (isDisableJob(enable)) {
				continue;
			}
			String jobClassName = properties.get(key) + "";
			String jobCronExp = properties.getProperty(cronKey) + "";
			Class clazz;
			try {
				clazz = Class.forName(jobClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			JobDetail job = newJob(clazz).withIdentity(jobClassName,
					jobClassName).build();
			CronTrigger trigger = newTrigger()
					.withIdentity(jobClassName, jobClassName)
					.withSchedule(cronSchedule(jobCronExp)).build();
			Date ft = null;
			try {
				ft = sched.scheduleJob(job, trigger);
				sched.start();
			} catch (SchedulerException ee) {
				new RuntimeException(ee);
			}
			logger.info(job.getKey() + " has been scheduled to run at: " + ft
					+ " and repeat based on expression: "
					+ trigger.getCronExpression());
		}
		return true ;
	}

	private boolean isDisableJob(String enable) {
		return Boolean.valueOf(properties.get(enable) + "") == false;
	}

	private void loadProperties() {
		properties = new Properties();
		InputStream is = QuartzPlugin.class.getClassLoader()
				.getResourceAsStream(config);
		try {
			properties.load(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean stop() {
		try {
			sched.shutdown();
		} catch (SchedulerException e) {
			logger.error("shutdown error", e);
			return false;
		}
		return true;
	}

	public static void main(String []args)  {
		QuartzPlugin plugin = new QuartzPlugin();
		plugin.start();
		System.out.println("执行成功！！！");

	}

}
