package com.itlaoqi.bsbdj.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ognl.Ognl;
import ognl.OgnlException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OgnlUtil {

	private static final Logger logger = LoggerFactory.getLogger(OgnlUtil.class);

	public static String getString(String ognl, Map root) {

		String value = null;
		try {
			value = Ognl.getValue(ognl, root).toString();
		} catch (OgnlException e) {
			logger.error("OgnlUtil.getString error. ", e);
		}

		return value;

	}

	public static Number getNumber(String ognl, Map root) {

		Number result = null;

		try {
			Object val = Ognl.getValue(ognl, root);
			if (val != null) {
				if (val instanceof Number) {
					result = (Number) val;
				} else if (val instanceof String) {
					result = new BigDecimal((String) val);
				}
			}
		} catch (OgnlException e) {
			logger.error("OgnlUtil.getNumber error. ", e);
		}

		return result;
	}

	public static Boolean getBoolean(String ognl, Map root) {

		Boolean result = null;

		try {
			Object val = Ognl.getValue(ognl, root);

			if (val != null) {

				if (val instanceof Boolean) {
					result = (Boolean) val;
				} else if (val instanceof String) {
					result = ((String) val).equalsIgnoreCase("true") ? true : false;
				} else if (val instanceof Number) {
					result = ((Number) val).intValue() == 1 ? true : false;
				}

			}
		} catch (OgnlException e) {
			logger.info("OgnlUtil.getBoolean error. ", e);
		}

		return result;
	}

	public static List<Map<String, Object>> getListMap(String ognl, Map root) {

		List<Map<String, Object>> list = null;

		try {
			list = (List) Ognl.getValue(ognl, root);
			if (list == null) {
				list = new ArrayList<>();
			}
		} catch (OgnlException e) {
			logger.error("OgnlUtil.getListMap error. ", e);
		}

		return list;

	}

	public static List<String> getListString(String ognl, Map root) {

		List<String> list = null;

		try {
			list = (List) Ognl.getValue(ognl, root);
			if (list == null) {
				list = new ArrayList<>();
			}
		} catch (OgnlException e) {
			logger.error("OgnlUtil.getListMap error. ", e);
		}

		return list;
	}
}
