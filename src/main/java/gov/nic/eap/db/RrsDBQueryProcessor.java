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

	/*
	 * Query Builder with Prepared Statement, Not using 
	 */
	public Config queryBuilder(Config rsConfig, Map<String, String> params, Map<String, String> allRequestParams) throws Exception {
		try (PreparedStatement pStatement = jdbcConnectionUtil.getConnection().prepareStatement(rsConfig.getQuery())) {
			int[] i = { 0 };
			rsConfig.setRequestValidation(true);
			params.forEach((k, v) -> {
				i[0]++;
				try {
					if (v.equals("str"))
						pStatement.setString(i[0], allRequestParams.get(k));
					else if (v.equals("int"))
						pStatement.setInt(i[0], Integer.valueOf(allRequestParams.get(k)));
					else if (v.equals("strArray"))
						pStatement.setArray(i[0], jdbcConnectionUtil.getConnection().createArrayOf(JDBCType.ARRAY.getName(),
								new Object[] { allRequestParams.get(k).split(",") }));
				} catch (Exception e) {
					rsConfig.setResponse(CommonConstant.mismatchList);
					rsConfig.setRequestValidation(false);
					log.error("Data Cast Exception {} ", e.getMessage(), e);
				}
			});
			String qry = pStatement.unwrap(PreparedStatement.class).toString();
			log.error(" ****** Final Binded Query ****** {} ", qry);
			rsConfig.setBindedQuery(qry);
		} catch (Exception e) {
			rsConfig.setRequestValidation(false);
			rsConfig.setResponse(CommonConstant.queryList);
			log.error("Query Builder Exception {} ", e.getMessage(), e);
			return rsConfig;
		}
		return rsConfig;
	}

	/*
	 * Currently, we are using this method for query parameters binding.
	 * This method not supports for array parameters as like IN clause. 
	 * Sub Query Data-sets from another table supported on IN clause.
	 */
	public Config queryFormatterOld(Config rsConfig, Map<String, String> params, Map<String, String> allRequestParams/*, Paseto token*/) throws Exception {
		LinkedHashMap<Integer, Object> queryParams = new LinkedHashMap<>();
		int[] i = { 0 };
		rsConfig.setRequestValidation(true);
		params.forEach((k, v) -> {
			i[0]++;
			try {
				if (v.equals("str"))
					queryParams.put(i[0], allRequestParams.get(k));
				else if (v.equals("int"))
					queryParams.put(i[0], Integer.valueOf(allRequestParams.get(k)));
//					else if (v.equals("token"))
//						queryParams.put(i[0], RrsToken.getTokenParamter(token, k));
				else /*
						* if (v.equals("strArray")) //queryParams.put(i[0],
						 * jdbcConnectionUtil.getConnection().createArrayOf( JDBCType.VARCHAR.getName(),
						 * new Object[] {allRequestParams.get(k).split(",")})); //queryParams.put(i[0],
						 * Stream.of(allRequestParams.get(k).split(",")).collect(Collectors.joining(
						 * "','", "'", "'")) ); queryParams.put(i[0], (List<String>)
						 * Arrays.asList(allRequestParams.get(k).split(","))); else if
						 * (v.equals("intArray"))
						 */
					queryParams.put(i[0], allRequestParams.get(k).split(","));
			} catch (Exception e) {
				rsConfig.setResponse(CommonConstant.mismatchList);
				rsConfig.setRequestValidation(false);
				log.error("Data Cast Exception {} ", e.getMessage(), e);
			}
		});
		rsConfig.setQueryParams(queryParams);
		return rsConfig;
	}

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
