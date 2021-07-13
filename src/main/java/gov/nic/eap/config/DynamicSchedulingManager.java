package gov.nic.eap.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.service.mTaskDefinition;
import gov.nic.eap.util.mTaskMap;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableScheduling
public class DynamicSchedulingManager implements SchedulingConfigurer {

	@Value("${thread.poolsize}")
	private int POOL_SIZE;

	@Autowired
	private TaskDetailsConfiguration taskDetailsConfiguration;

	@Autowired
	private mTaskDefinition mTasks;

	@Override
	public void configureTasks (ScheduledTaskRegistrar taskRegistrar) {

		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
		threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
		threadPoolTaskScheduler.setThreadNamePrefix("scheduled_task_thread = ");
		threadPoolTaskScheduler.initialize();
		taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

		mTaskMap<String, TaskDetailsConfiguration.Config> jobConfigs = taskDetailsConfiguration.getJobConfigs();
		jobConfigs.forEach((key, value) -> {
			if ( value.isAutoStart() ) {
				CronTask task = new CronTask(() -> {
					try {
						log.info("mTask process begins for the key = {}", key);
						// test
						Set<Thread> threads = Thread.getAllStackTraces().keySet();
						threads.stream().filter(thread -> thread.getName().startsWith("scheduled_task_thread = ")).findAny().ifPresent(Thread::interrupt);

						mTasks.mTaskDescription(key, value);
						log.info("mTask process completed for the key = {}", key);
					} catch (Exception ex) {
						log.error("mTask process failed due to -> {} ", ex.getMessage(), ex);
					}
				}, (value.getCron()));
				taskRegistrar.addCronTask(task);
			}
		});
	}
}
