package be.nabu.libs.channels.resources;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelRewriter;
import be.nabu.libs.channels.util.RewrittenChannel;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.evaluator.EvaluationException;
import be.nabu.libs.evaluator.PathAnalyzer;
import be.nabu.libs.evaluator.QueryParser;
import be.nabu.libs.evaluator.api.Operation;
import be.nabu.libs.evaluator.impl.PlainOperationProvider;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.java.BeanInstance;

/**
 * If you need non-string objects like integer etc, it is still advisable to define them as string (easier to inject variables)
 * If you want to replace non-string objects that can not be easily cast, you will have to define them as Object to prevent class cast exceptions
 */
public class ChannelPropertyRewriter implements ChannelRewriter {

	private Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
	private PathAnalyzer<Object> analyzer = new PathAnalyzer<Object>(new PlainOperationProvider());
	private Converter converter = ConverterFactory.getInstance().getConverter();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Channel<?>> rewrite(Channel<?> channel) {
		List<Channel<?>> result = new ArrayList<Channel<?>>();
		for (Object rewritten : rewrite(channel.getProperties())) {
			result.add(new RewrittenChannel(channel, rewritten));
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> rewrite(T properties) {
		ComplexContent content = properties instanceof ComplexContent 
			? (ComplexContent) properties
			: new BeanInstance(properties);
		List<T> result = new ArrayList<T>();
		for (ComplexContent rewritten : rewrite(content)) {
			if (rewritten instanceof BeanInstance) {
				result.add((T) TypeUtils.getAsBean(rewritten, ((BeanInstance) rewritten).getType().getBeanClass()));
			}
			else {
				result.add((T) rewritten);
			}
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	private List<ComplexContent> rewrite(ComplexContent original) {
		List<ComplexContent> list = new ArrayList<ComplexContent>();
		list.add(original.getType().newInstance());
		for (Element<?> element : TypeUtils.getAllChildren(original.getType())) {
			Object value = original.get(element.getName());
			if (value == null) {
				continue;
			}
			if (element.getType() instanceof ComplexType) {
				ComplexContent child = value instanceof ComplexContent
					? (ComplexContent) value
					: new BeanInstance(value);
				List<ComplexContent> rewrittenList = rewrite(child);
				// if there is more than one result, duplicate the properties, one for each result
				if (rewrittenList.size() > 1) {
					List<ComplexContent> newList = new ArrayList<ComplexContent>();
					for (ComplexContent current : list) {
						newList.addAll(clone(current, element.getName(), rewrittenList));
					}
					list = newList;
				}
				else {
					for (ComplexContent cloned : list) {
						cloned.set(element.getName(), rewrittenList.get(0));
					}
				}
			}
			else if (value instanceof String) {
				List<String> replacement = replace((String) value);
				// if there is more than one result, duplicate the properties, one for each result
				if (replacement.size() > 1) {
					List<ComplexContent> newList = new ArrayList<ComplexContent>();
					for (ComplexContent current : list) {
						newList.addAll(clone(current, element.getName(), replacement));
					}
					list = newList;
				}
				else {
					for (ComplexContent cloned : list) {
						cloned.set(element.getName(), replacement.get(0));
					}
				}
			}
			else {
				for (ComplexContent cloned : list) {
					cloned.set(element.getName(), value);
				}
			}
		}
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	private List<ComplexContent> clone(ComplexContent original, String elementThatTriggeredClone, List valueList) {
		List<ComplexContent> result = new ArrayList<ComplexContent>();
		int amount = valueList == null ? 1 : valueList.size();
		for (int i = 0; i < amount; i++) {
			ComplexContent cloned = original.getType().newInstance();
			for (Element<?> element : TypeUtils.getAllChildren(original.getType())) {
				if (elementThatTriggeredClone != null && element.getName().equals(elementThatTriggeredClone)) {
					cloned.set(element.getName(), valueList.get(i));
				}
				else {
					Object value = original.get(element.getName());
					if (element.getType() instanceof ComplexType) {
						ComplexContent child = value instanceof ComplexContent
							? (ComplexContent) value
							: new BeanInstance(value);
						cloned.set(element.getName(), clone(child, null, null));
					}
					else {
						cloned.set(element.getName(), value);
					}
				}
			}
			result.add(cloned);
		}
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<String> replace(String value) {
		if (value == null) {
			return null;
		}
		Matcher matcher = pattern.matcher(value);
		List<String> values = new ArrayList<String>(Arrays.asList(value));
		try {
			while (matcher.find()) {
				String query = matcher.group().replaceAll(pattern.pattern(), "$1");
				Operation<Object> operation = analyzer.analyze(QueryParser.getInstance().parse(query));
				Object result = operation.evaluate(null);
				if (result != null) {
					CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(result.getClass());
					// it's a list
					if (handler != null) {
						List<String> newValues = new ArrayList<String>();
						for (int i = 0; i < values.size(); i++) {
							for (Object single : handler.getAsCollection(result)) {
								String stringified = converter.convert(single, String.class);
								newValues.add(values.get(i).replaceAll(Pattern.quote(matcher.group()), stringified == null ? "" : Matcher.quoteReplacement(stringified)));
							}
						}
						values = newValues;
					}
					else {
						String stringified = converter.convert(result, String.class);
						for (int i = 0; i < values.size(); i++) {
							values.set(i, values.get(i).replaceAll(Pattern.quote(matcher.group()), result == null ? "" : Matcher.quoteReplacement(stringified)));
						}
					}
				}
			}
			return values;
		}
		catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
		catch (EvaluationException e) {
			throw new RuntimeException(e);
		}
	}
}
