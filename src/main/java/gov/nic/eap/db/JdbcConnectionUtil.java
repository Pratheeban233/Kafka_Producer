package gov.nic.eap.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import gov.nic.eap.constant.CommonConstant;

@Component
@Repository
public class JdbcConnectionUtil {

	private final Logger log = LoggerFactory.getLogger(JdbcConnectionUtil.class);
	@Autowired
	JdbcTemplate jdbcTemplate;

	public Connection getConnection() throws SQLException {
		return jdbcTemplate.getDataSource().getConnection();
	}

	public List<Map<String, Object>> getResultSet(String sqlQuery) {
		try {
			return jdbcTemplate.queryForList(sqlQuery);
		} catch (Exception e) {
			log.error("Database Connection Exception {} ", e.getMessage(), e);
			return CommonConstant.queryList;
		}
	}

	public List<Map<String, Object>> getResultSets(String sqlQuery, LinkedHashMap<Integer, Object> queryParams) throws Exception {
		try {
			if (sqlQuery.toLowerCase().startsWith("select")) {
				return jdbcTemplate.queryForList(sqlQuery, queryParams.values().toArray());
			} else {
				List<Map<String, Object>> recordList = new ArrayList<>();
				Map<String, Object> recordMap = new HashMap<>();
				int noOfRecords = jdbcTemplate.update(sqlQuery, queryParams.values().toArray());
				recordMap.put("No. of Records Affected", noOfRecords);
				recordList.add(recordMap);
				return recordList;
			}

		} catch (Exception e) {
			log.error("Database related Exception {} ", e.getMessage(), e);
			return CommonConstant.queryList;
		}
	}

// trying batch update here
	public List<Map<String, Object>> getBatchResultSets(String sqlQuery, List<LinkedHashMap<Integer, Object>> listOfQueryParams) throws Exception {
		try {
			if (sqlQuery.toLowerCase().startsWith("select")) {
				return jdbcTemplate.queryForList(sqlQuery, listOfQueryParams.get(0).values().toArray());
			} else {
				List<Map<String, Object>> recordList = new ArrayList<>();
				List<Object[]> listObjArgs = new ArrayList<>();
				Map<String, Object> recordMap = new HashMap<>();

				for (Map<Integer, Object> queryParam : listOfQueryParams) {
					Object[] args = { queryParam.values() };
					listObjArgs.add(args);
				}
				int[] batchUpdate = jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
					int[] k = { 0 };

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						k[0]++;
						for (Object[] params : listObjArgs) {
							ps.setObject(k[0], params[k[0]]);
							ps.setObject(k[0]++, params[k[0]++]);
							ps.setObject(k[0] + 2, params[k[0] + 2]);
						}
					}

					@Override
					public int getBatchSize() {
						return listOfQueryParams.size();
					}
				});
				int noOfRecords = 0;
				for (int i = 0; i < batchUpdate.length; i++) {
					if (batchUpdate[i] == 1) {
						noOfRecords++;
					}
				}
				recordMap.put("No. of Records Affected", noOfRecords);
				recordList.add(recordMap);
				return recordList;
			}
		} catch (Exception e) {
			log.error("Database related Exception {} ", e.getMessage(), e);
			return CommonConstant.queryList;
		}
	}

}
