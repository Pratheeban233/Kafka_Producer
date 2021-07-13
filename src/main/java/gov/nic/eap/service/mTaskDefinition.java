package gov.nic.eap.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

	public List<Map<String, Object>> mTaskDescription (String key, TaskDetailsConfiguration.Config value) throws Exception {
		List<Map<String, Object>> result = null;
		log.info("{} job trying to acquiring the shed lock.", key);
		boolean execute = checkShedLock(key);
		if ( execute ) {
			log.info("{} job acquired the shed lock at {}.", key, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			if ( value.getQuery() != null ) {
				result = fetchDataForMQueue(value.getQuery());
			}
			Ingester ingester = (Ingester) Class.forName("gov.nic.eap.service.implementation." + value.getTargetType()).newInstance();
			result = ingester.mTaskImplementation(key, value, result, applicationContext);
		} else {
			log.debug("{} job doesn't acquire the shed lock. another job is already running.", key);
			return CommonConstant.threadLockList;
		}
		return result;
	}

	private boolean checkShedLock (String key) {
		Optional<Shedlock> shedLock = shedlockRepository.findById(key);
		if ( shedLock.isPresent() ) {
			Duration duration = Duration.between(shedLock.get().getLockUntil(), LocalDateTime.now());
			log.debug("ShedLock duration time : {}", duration.toMinutes());
			if ( !duration.isNegative() ) {
				shedLock.get().setLockUntil(LocalDateTime.now().plusMinutes(2));
				shedLock.get().setLockedAt(LocalDateTime.now());
				shedLock.get().setLockedBy("Cron");
				shedlockRepository.save(shedLock.get());
				return true;
			}
		}
		return false;
	}

	private List<Map<String, Object>> fetchDataForMQueue (String query) {
		return jdbcConnectionUtil.getResultSet(query);
	}
}

