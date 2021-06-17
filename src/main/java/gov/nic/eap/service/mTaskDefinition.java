package gov.nic.eap.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.db.JdbcConnectionUtil;
import gov.nic.eap.db.ShedlockRepository;
import gov.nic.eap.model.Shedlock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class mTaskDefinition {

	@Autowired
	JdbcConnectionUtil jdbcConnectionUtil;

	@Autowired
	ShedlockRepository shedlockRepository;

	@Autowired
	ApplicationContext applicationContext;

	public List<Map<String, Object>> mTaskDescription(String key, TaskDetailsConfiguration.Config value) throws Exception {
		List<Map<String, Object>> result = null;
		boolean execute = checkShedLock(key);
		if (execute) {
			if (value.getQuery() != null) {
				result = fetchDataForMQueue(value.getQuery());
				if (result.isEmpty())
					return CommonConstant.norecordsList;
			}
			Class cls = Class.forName("gov.nic.eap.service.implementation." + value.getTargetType());
			Ingester ingester = (Ingester) cls.newInstance();
			result = ingester.mTaskImplementation(key, value, result, applicationContext);
		}
		return result;
	}

	private boolean checkShedLock(String key) {
		Optional<Shedlock> shedLock = shedlockRepository.findById(key);
		if (shedLock.isPresent()) {
			Duration duration = Duration.between(shedLock.get().getLockUntil(), LocalDateTime.now());
			log.debug("ShedLock duration time : {}", duration);
			if (!duration.isNegative()) {
				shedLock.get().setLockUntil(LocalDateTime.now().plusMinutes(2));
				shedLock.get().setLockedAt(LocalDateTime.now());
				shedLock.get().setLockedBy("Cron");
				shedlockRepository.save(shedLock.get());
				return true;
			}
		}
		return false;
	}

	private List<Map<String, Object>> fetchDataForMQueue(String query) {
		return jdbcConnectionUtil.getResultSet(query);
	}
}
