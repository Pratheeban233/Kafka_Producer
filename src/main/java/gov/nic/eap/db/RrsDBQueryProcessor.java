package gov.nic.eap.db;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration.Config;

@Service
public class RrsDBQueryProcessor {

	private final Logger log = LoggerFactory.getLogger(RrsDBQueryProcessor.class);
	@Autowired
	JdbcConnectionUtil jdbcConnectionUtil;

	public Config queryFormatter(Config rsConfig, Map<String, String> params, Map<String, String> allRequestParams/*, Paseto token*/) throws Exception {
		LinkedHashMap<Integer, Object> queryParams = new LinkedHashMap<>();
		int[] i = { 0 };
		rsConfig.setRequestValidation(true);
		for (Map.Entry<String, String> param : params.entrySet()) {
			// params.forEach((k, v) -> {
			i[0]++;
			try {
				/*if (v.equals("TOKEN"))
						queryParams.put(i[0], new SqlParameterValue(Types.INTEGER, RrsToken.getTokenParamter(token, k)));
				else */
				if (param.getValue().contains("ARRAY"))
					queryParams.put(i[0],
							new SqlParameterValue(JDBCType.valueOf(param.getValue()).getVendorTypeNumber(), allRequestParams.get(param.getKey()).split(",")));
				else
					queryParams.put(i[0],
							new SqlParameterValue(JDBCType.valueOf(param.getValue()).getVendorTypeNumber(), allRequestParams.get(param.getKey())));
			} catch (Exception e) {
				rsConfig.setResponse(CommonConstant.mismatchList);
				rsConfig.setRequestValidation(false);
				log.error("Data Cast Exception {} ", e.getMessage(), e);
			}
		}
//		});
		rsConfig.setQueryParams(queryParams);
		return rsConfig;
	}

	public Config queryBatchFormatter(Config rsConfig, Map<String, String> params, List<Map<String, String>> listOfAllRequestParams/*, Paseto token*/)
			throws Exception {
		LinkedHashMap<Integer, Object> queryParams = new LinkedHashMap<>();
		List<LinkedHashMap<Integer, Object>> listOfQueryParams = new ArrayList<>();
		int[] i = { 0 };
		rsConfig.setRequestValidation(true);
		for (Map<String, String> listOfRequestParam : listOfAllRequestParams) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				i[0]++;
				try {
					if (param.getValue().contains("ARRAY")) {
						queryParams.put(i[0], new SqlParameterValue(JDBCType.valueOf(param.getValue()).getVendorTypeNumber(),
								listOfRequestParam.get(param.getKey()).split(",")));
						listOfQueryParams.add(queryParams);
					} else {
						queryParams.put(i[0],
								new SqlParameterValue(JDBCType.valueOf(param.getValue()).getVendorTypeNumber(), listOfRequestParam.get(param.getKey())));
						listOfQueryParams.add(queryParams);
					}
				} catch (Exception e) {
					rsConfig.setResponse(CommonConstant.mismatchList);
					rsConfig.setRequestValidation(false);
					log.error("Data Cast Exception {} ", e.getMessage(), e);
				}
			}
		}
		rsConfig.setQueryParams(queryParams);
		rsConfig.setListOfQueryParams(listOfQueryParams);
		return rsConfig;
	}
}
