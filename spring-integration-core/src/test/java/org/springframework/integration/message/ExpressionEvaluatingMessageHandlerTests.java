/* * Copyright 2002-2011 the original author or authors. * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * *      http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package org.springframework.integration.message;import org.junit.Before;import org.junit.Ignore;import org.junit.Test;import org.springframework.expression.Expression;import org.springframework.expression.ExpressionParser;import org.springframework.expression.spel.standard.SpelExpressionParser;import org.springframework.integration.Message;import org.springframework.integration.MessagingException;import org.springframework.integration.core.MessageHandler;import org.springframework.integration.handler.ExpressionEvaluatingMessageHandler;import java.util.HashMap;import static org.junit.Assert.assertEquals;/** * @author Artem Bilan * @since 2.1 */public class ExpressionEvaluatingMessageHandlerTests {	private ExpressionParser parser;	@Before	public void setup() {		parser = new SpelExpressionParser();	}	@Test	public void validExpression() {		Expression expression = parser.parseExpression("T(System).out.println(payload)");		MessageHandler handler = new ExpressionEvaluatingMessageHandler(expression);		handler.handleMessage(new GenericMessage<String>("test"));	}	@Test	public void validExpressionWithNoArgs() {		Expression expression = parser.parseExpression("T(System).out.println()");		MessageHandler handler = new ExpressionEvaluatingMessageHandler(expression);		handler.handleMessage(new GenericMessage<String>("test"));	}	@Test	public void validExpressionWithSomeArgs() {		Expression expression = parser.parseExpression("T(System).out.write(payload.bytes, 0, headers.offset)");		MessageHandler handler = new ExpressionEvaluatingMessageHandler(expression);		HashMap<String, Object> headers = new HashMap<String, Object>();		headers.put("offset", 4);		handler.handleMessage(new GenericMessage<String>("testtest", headers));	}	@Test(expected = MessagingException.class)	public void expressionWithReturnValue() {		Message<?> message = new GenericMessage<Float>(.1f);		try {			Expression expression = parser.parseExpression("T(System).out.printf('$%4.2f', payload)");			MessageHandler handler = new ExpressionEvaluatingMessageHandler(expression);			handler.handleMessage(message);		}		catch (MessagingException e) {			assertEquals(e.getFailedMessage(), message);			throw e;		}	}}