/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.yangtools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangToolsCloner2 {

	private static final Logger LOG = LoggerFactory.getLogger(YangToolsCloner2.class);

    public enum Accessor {
    	ACCESSOR_FIELD,
    	ACCESSOR_METHOD;
    }

	private Accessor accessor;

	public YangToolsCloner2() {
		LOG.info("Provide new {}",this.getClass().getName());
		this.accessor = Accessor.ACCESSOR_METHOD;
	}

	YangToolsCloner2 setAcessor(Accessor accessor) {
		this.accessor = accessor;
		return this;
	}

	Accessor getAccessor() {
		return accessor;
	}

	public interface Builder<T> {
		T build();
	}

	/**
	 *
	 * @param source source object
	 * @param clazz Class of return object
	 * @attrList filter for attribute Names to clone
	 * @return list of cloned object
	 * @throws Exception
	 */
	public <S, T> List<T> cloneList(List<S> source, Builder<T> builder, String ... attrList) throws Exception {
		if (source == null) {
			return null;
		}
		List<T> list = new ArrayList<T>();
		for (S s : source) {
			list.add(copyAttributes(s, builder.build(), attrList));
		}
		return list;
	}

	/**
	 * Copy attributes from source to destination object.
	 * Copy the references.
	 * @param source source object
	 * @param clazz Class of return object
	 * @attrList attribute Names NOT to clone.
	 * @return cloned object
	 * @throws Exception
	 */
	@SuppressWarnings("null")
	public @Nullable <S, T> T copyAttributes(S source, T destination, String ... attributeArray) throws Exception {

		LOG.debug("copyAttributes source.class {} destination.class {} attributes {}", source, destination, attributeArray.length);

		if (destination == null || source == null)
			return null;

		List<String> attributeList = Arrays.asList(attributeArray);
		LOG.debug("copyAttributes 2 attributes {}", attributeList);

		Field[] destinationAttributeFields = source.getClass().getDeclaredFields();
		String destinationName;
		Class<?> destinationType;
		for (Field destinationAttributeField : destinationAttributeFields) {
			destinationName = destinationAttributeField.getName();
			destinationType = destinationAttributeField.getType();
			LOG.debug("Field: {}", destinationName);
			// check if attr is in exclusion list
			if (attributeList.contains(destinationName)) {
				continue;
			}
			// ignore QNAME
			if (destinationName.equals("QNAME")) {
				continue;
			}

			destinationAttributeField.setAccessible(true);
			Object sourceData = null;
			Class<?> sourceType = null;
			Class<?> sourceListType = null;
			try {
				if (accessor == Accessor.ACCESSOR_FIELD) {
					Field sourceField;
					sourceField = source.getClass().getDeclaredField(destinationName);
					sourceField.setAccessible(true);
					sourceType = sourceField.getType();
					sourceData = sourceField.get(source);
					sourceListType = getListClass(sourceType, sourceData);

				} else if (accessor == Accessor.ACCESSOR_METHOD) {
					Method sourceMethod;
					sourceMethod = source.getClass().getDeclaredMethod(getter(destinationName));
					sourceMethod.setAccessible(true);
					sourceType = sourceMethod.getReturnType();
					sourceData = sourceMethod.invoke(source);
					sourceListType = getListClass(sourceType, sourceData);
				}
				LOG.info("Handle {} {} {}", destinationName, destinationType, sourceType);
				if (destinationType == sourceType) {
					destinationAttributeField.set(destination, sourceData);
				} else {
					throw new Exception(
							"Problem to copy attribute " + destinationName
							+" Sourceclass:" +sourceType
							+" Destinationclass:" + destinationType
							+" Method:"+accessor.name());
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
					| NoSuchMethodException | InvocationTargetException e) {
				throw e;
			}
		}
		return destination;

	}

	private static String getter(String name) {
		if (name == null || name.length() == 0) {
			return null;
		} else if (name.length() == 1) {
			return String.format("%s%s", "get", name.substring(1, 2).toUpperCase());
		} else { // >= 2
			return String.format("%s%s%s", "get", name.substring(1, 2).toUpperCase(), name.substring(2));
		}
	}

	private static Class<?> getListClass(Class<?> sourceType, Object sourceData) {
		if (sourceData != null && sourceType.equals(List.class)) {
			List<Object> sourceDataList = (List<Object>)sourceData;
			if (sourceDataList.size() > 0) {
				LOG.info("Is list with type"+sourceDataList.get(0).getClass().getName());
			} else {
				LOG.info("Is empty list");
			}
		}
	    return(sourceType);
	}

}
