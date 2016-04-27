package com.confsync.common.config;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import de.tototec.cmdoption.handler.CmdOptionHandler;
import de.tototec.cmdoption.handler.CmdOptionHandlerException;

public class CollectionHandler implements CmdOptionHandler {

	@Override
	public boolean canHandle(AccessibleObject element, int argCount) {
		return argCount == 1 && element instanceof Field
				&& Collection.class.isAssignableFrom(((Field) element).getType());
	}

	@Override
	public void applyParams(Object config, AccessibleObject element, String[] args, String optionName)
			throws CmdOptionHandlerException {
		try {
			final Field field = (Field) element;
			final Collection<String> collection = (Collection<String>) field.get(config);
			collection.addAll(Arrays.asList(args[0].split(",")));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
