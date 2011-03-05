package com.tag.upnp;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyStroke {

	private Robot robot;

	private Map<Character, Integer> keyCodeMap;
	private Map<Character, Character> charMap;

	public KeyStroke(Robot robot) {
		if (robot == null)
			throw new IllegalArgumentException("robot must not be null");
		this.robot = robot;

		keyCodeMap = new CharacterMap();
		keyCodeMap.put('0', KeyEvent.VK_0);
		keyCodeMap.put('1', KeyEvent.VK_1);
		keyCodeMap.put('2', KeyEvent.VK_2);
		keyCodeMap.put('3', KeyEvent.VK_3);
		keyCodeMap.put('4', KeyEvent.VK_4);
		keyCodeMap.put('5', KeyEvent.VK_5);
		keyCodeMap.put('6', KeyEvent.VK_6);
		keyCodeMap.put('7', KeyEvent.VK_7);
		keyCodeMap.put('8', KeyEvent.VK_8);
		keyCodeMap.put('9', KeyEvent.VK_9);

		keyCodeMap.put('A', KeyEvent.VK_A);
		keyCodeMap.put('B', KeyEvent.VK_B);
		keyCodeMap.put('C', KeyEvent.VK_C);
		keyCodeMap.put('D', KeyEvent.VK_D);
		keyCodeMap.put('E', KeyEvent.VK_E);
		keyCodeMap.put('F', KeyEvent.VK_F);
		keyCodeMap.put('G', KeyEvent.VK_G);
		keyCodeMap.put('H', KeyEvent.VK_H);
		keyCodeMap.put('I', KeyEvent.VK_I);
		keyCodeMap.put('J', KeyEvent.VK_J);
		keyCodeMap.put('K', KeyEvent.VK_K);
		keyCodeMap.put('L', KeyEvent.VK_L);
		keyCodeMap.put('M', KeyEvent.VK_M);
		keyCodeMap.put('N', KeyEvent.VK_N);
		keyCodeMap.put('O', KeyEvent.VK_O);
		keyCodeMap.put('P', KeyEvent.VK_P);
		keyCodeMap.put('Q', KeyEvent.VK_Q);
		keyCodeMap.put('R', KeyEvent.VK_R);
		keyCodeMap.put('S', KeyEvent.VK_S);
		keyCodeMap.put('T', KeyEvent.VK_T);
		keyCodeMap.put('U', KeyEvent.VK_U);
		keyCodeMap.put('V', KeyEvent.VK_V);
		keyCodeMap.put('W', KeyEvent.VK_W);
		keyCodeMap.put('X', KeyEvent.VK_X);
		keyCodeMap.put('Y', KeyEvent.VK_Y);
		keyCodeMap.put('Z', KeyEvent.VK_Z);

		keyCodeMap.put('~', KeyEvent.VK_DEAD_TILDE);
		keyCodeMap.put('!', KeyEvent.VK_EXCLAMATION_MARK);
		keyCodeMap.put('¡', KeyEvent.VK_INVERTED_EXCLAMATION_MARK);
		keyCodeMap.put('@', KeyEvent.VK_AT);
		keyCodeMap.put('#', KeyEvent.VK_NUMBER_SIGN);
		keyCodeMap.put('$', KeyEvent.VK_DOLLAR);
		keyCodeMap.put('€', KeyEvent.VK_EURO_SIGN);
		keyCodeMap.put('^', KeyEvent.VK_DEAD_CIRCUMFLEX);
		keyCodeMap.put('&', KeyEvent.VK_AMPERSAND);
		keyCodeMap.put('*', KeyEvent.VK_ASTERISK);
		keyCodeMap.put('(', KeyEvent.VK_LEFT_PARENTHESIS);
		keyCodeMap.put(')', KeyEvent.VK_RIGHT_PARENTHESIS);
		keyCodeMap.put('_', KeyEvent.VK_UNDERSCORE);
		keyCodeMap.put('+', KeyEvent.VK_PLUS);
		keyCodeMap.put('{', KeyEvent.VK_BRACELEFT);
		keyCodeMap.put('}', KeyEvent.VK_BRACERIGHT);
		keyCodeMap.put(':', KeyEvent.VK_COLON);
		keyCodeMap.put('"', KeyEvent.VK_QUOTEDBL);
		keyCodeMap.put('<', KeyEvent.VK_LESS);
		keyCodeMap.put('>', KeyEvent.VK_GREATER);
		keyCodeMap.put('`', KeyEvent.VK_DEAD_GRAVE);
		keyCodeMap.put('-', KeyEvent.VK_MINUS);
		keyCodeMap.put('=', KeyEvent.VK_EQUALS);
		keyCodeMap.put('[', KeyEvent.VK_OPEN_BRACKET);
		keyCodeMap.put(']', KeyEvent.VK_CLOSE_BRACKET);
		keyCodeMap.put('\\', KeyEvent.VK_BACK_SLASH);
		keyCodeMap.put(';', KeyEvent.VK_SEMICOLON);
		keyCodeMap.put('\'', KeyEvent.VK_QUOTE);
		keyCodeMap.put(',', KeyEvent.VK_COMMA);
		keyCodeMap.put('.', KeyEvent.VK_PERIOD);
		keyCodeMap.put('/', KeyEvent.VK_SLASH);

		keyCodeMap.put(' ', KeyEvent.VK_SPACE);
		keyCodeMap.put('\b', KeyEvent.VK_BACK_SPACE);
		keyCodeMap.put('\r', KeyEvent.VK_ENTER);
		keyCodeMap.put('\n', KeyEvent.VK_ENTER);
		keyCodeMap.put('\t', KeyEvent.VK_TAB);

		charMap = new HashMap<Character, Character>();
		charMap.put('~', '`');
		charMap.put('!', '1');
		charMap.put('@', '2');
		charMap.put('#', '3');
		charMap.put('$', '4');
		charMap.put('%', '5');
		charMap.put('^', '6');
		charMap.put('&', '7');
		charMap.put('*', '8');
		charMap.put('(', '9');
		charMap.put(')', '0');
		charMap.put('_', '-');
		charMap.put('+', '=');
		charMap.put('{', '[');
		charMap.put('}', ']');
		charMap.put('|', '\\');
		charMap.put(':', ';');
		charMap.put('"', '\'');
		charMap.put('<', ',');
		charMap.put('>', '.');
		charMap.put('?', '/');
	}

	public void typeString(char... value) {
		typeString(new String(value));
	}

	public void typeString(String s) {
		for (char ch : s.toCharArray())
			typeCharacter(ch);
	}

	public void typeCharacter(char ch) {
		boolean isShiftKey = charMap.containsKey(ch);
		if (isShiftKey) {
			ch = charMap.get(ch);
			robot.keyPress(KeyEvent.VK_SHIFT);
		}
		
		if (keyCodeMap.containsKey(ch)) {
			try {
				doType(ch);
				return;
			} catch (IllegalArgumentException e) {
				handleException(e, ch);
			}
		}
		
		if (isShiftKey)
			robot.keyRelease(KeyEvent.VK_SHIFT);
	}

	private void doType(char ch) {
		if (!keyCodeMap.containsKey(ch))
			return;

		if (Character.isUpperCase(ch))
			robot.keyPress(KeyEvent.VK_SHIFT);
		int keyCode = keyCodeMap.get(ch);
		try {
			robot.keyPress(keyCode);
			robot.keyRelease(keyCode);
		} finally {
			if (Character.isUpperCase(ch))
				robot.keyRelease(KeyEvent.VK_SHIFT);
		}
	}

	private static void handleException(IllegalArgumentException e, char ch) {
		String message = e.getMessage();
		if ("Invalid key code".equals(message))
			System.err.printf("invalid key code '%s'\n", Integer.toString(ch));
	}

}

@SuppressWarnings("serial")
class CharacterMap extends HashMap<Character, Integer> {

	@Override
	public Integer get(Object key) {
		key = prepareKey(key);
		return super.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		key = prepareKey(key);
		return super.containsKey(key);
	}

	@Override
	public Integer put(Character key, Integer value) {
		key = (Character) prepareKey(key);
		return super.put(key, value);
	}

	private Object prepareKey(Object key) {
		if (key instanceof Character)
			key = Character.toUpperCase((Character) key);
		return key;
	}
}