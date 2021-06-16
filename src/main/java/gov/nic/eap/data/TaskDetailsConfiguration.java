package gov.nic.eap.data;

import java.util.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import gov.nic.eap.util.RRSMap;
import lombok.Data;
import lombok.ToString;

@Configuration
@ConfigurationProperties("schedule")
@Data
@Component
public class TaskDetailsConfiguration { // TaskDetailsConfiguration

	RRSMap<String, Config> jobConfigs;

	public Optional<TaskDetailsConfiguration.Config> getJobConfigs(String key) {
		return Optional.ofNullable(this.getJobConfigs().get(key));
	}

	@Data
	@Component
	public static class Config {
		private String method;
		private String cron;
		private String query;
		private Map<String, String> inputs;
		private String updatequery;
		private Map<String, String> updateinputs;
		private boolean autoStart;
		private String targetType;
		private Map<String, String> targetInputs;
		private String bindedQuery;
		private boolean requestValidation;
		private List<Map<String, Object>> response;
		private LinkedHashMap<Integer, Object> queryParams = new LinkedHashMap<>();
		private List<LinkedHashMap<Integer, Object>> listOfQueryParams = new ArrayList<>();

	}
}
