package gov.nic.eap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.service.mTaskDefinition;
import gov.nic.eap.util.RRSMap;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableScheduling
public class DynamicSchedulingManager implements SchedulingConfigurer {
	private final int POOL_SIZE = 10;

	@Autowired
	private TaskDetailsConfiguration taskDetailsConfiguration;

	@Autowired
	private mTaskDefinition mTasks;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
		threadPoolTaskScheduler.setThreadNamePrefix("scheduled_task_thread = ");
		threadPoolTaskScheduler.initialize();
		taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

		RRSMap<String, TaskDetailsConfiguration.Config> jobConfigs = taskDetailsConfiguration.getJobConfigs();
		jobConfigs.forEach((key, value) -> {
			if (value.isAutoStart()) {
				CronTask task = new CronTask(() -> {
					try {
						mTasks.mTaskImplementation(key);
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}, (value.getCron()));
				taskRegistrar.addCronTask(task);
			}
		});
	}
}
