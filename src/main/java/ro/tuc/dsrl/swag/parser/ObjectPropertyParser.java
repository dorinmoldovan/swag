package ro.tuc.dsrl.swag.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import ro.tuc.dsrl.swag.datamodel.ObjectPropertyData;
import ro.tuc.dsrl.swag.datamodel.ObjectPropertyValue;
import ro.tuc.dsrl.swag.datamodel.RangeData;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class ObjectPropertyParser {

	private static final Logger LOGGER = Logger.getLogger(ObjectPropertyParser.class);

	private static final String ID = "id";

	private ObjectPropertyParser() {
	}

	private static ObjectPropertyValue parseForeignKey(Object value, ObjectPropertyData objectPropertyData) {

		String objectProperty = objectPropertyData.getObjectProperty();

		if (value == null) {
			Class<?> clazz = objectPropertyData.getRange();
			String name = clazz.getSimpleName();
			return new ObjectPropertyValue(objectProperty, new RangeData(name, null));
		}

		Class<?> clazz = value.getClass();
		String name = clazz.getSimpleName();

		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				field.setAccessible(true);
				Object fkFieldValue;
				try {
					fkFieldValue = field.get(value);
					if (fkFieldValue != null && ID.equals(field.getName())) {
						return new ObjectPropertyValue(objectProperty,
								new RangeData(name, (Long) fkFieldValue));

					}
				} catch (IllegalArgumentException e) {
					LOGGER.error("", e);
				} catch (IllegalAccessException e) {
					LOGGER.error("", e);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	public static List<ObjectPropertyValue> parseCollectionForeignKey(Object value, ObjectPropertyData objectProperty) {
		List<ObjectPropertyValue> toReturn = new ArrayList<ObjectPropertyValue>();

		Object[] containedValues;
		if (value instanceof Collection) {
			containedValues = ((Collection<?>) value).toArray();
			for (Object obj : containedValues) {
				toReturn.add(parseForeignKey(obj, objectProperty));
			}

			return toReturn;
		}

		toReturn.add(parseForeignKey(value, objectProperty));
		return toReturn;
	}

}