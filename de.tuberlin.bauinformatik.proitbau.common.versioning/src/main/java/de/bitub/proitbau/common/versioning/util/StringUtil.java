package de.bitub.proitbau.common.versioning.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	
	private StringUtil() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static StringUtil instance = new StringUtil();
	}
	
	@SuppressWarnings("synthetic-access")
	public static StringUtil getInstance() {
		return Handler.instance;
	}
	
	// TODO TESTS!!!
	public String getNameWithEldestVersion(final String... names) {
		String eldestName = "";
		if (names.length != 0) {
			int eldestVersion = -1;
			for (final String name : names) {
				final int index = name.indexOf('.');
				if (index != -1) {
					final int version = Integer.valueOf(name.substring(index + 1)).intValue();
					if (eldestVersion < version) {
						eldestVersion = version;
						eldestName = name;
					}
				}
			}
		}
		return eldestName;
	}
	
	/*
	 * This method increases the number of the version
	 */
	public String getNameWithNewVersion(final String oldName) {
		String newName = null;
		if ((oldName != null) && (oldName.length() > 2)) {
			try {
				final int pointPosition = oldName.lastIndexOf(".");
				int version = Integer.valueOf(oldName.substring(pointPosition + 1, oldName.length())).intValue();
				newName = oldName.substring(0, pointPosition + 1) + ++version;
			}
			catch (final NumberFormatException e) {
				throw new NumberFormatException("Wrong format of the name");
			}
			catch (final StringIndexOutOfBoundsException e) {
				throw new NumberFormatException("Wrong format of the name");
			}
		}
		return newName;
	}
	
	public String getNewNameWithNewNumber(final String oldName) {
		String newName = null;
		if ((oldName != null) && (oldName.length() > 2)) {
			try {
				final int pointPosition = oldName.lastIndexOf(".");
				final int spacePosition = oldName.lastIndexOf(" ");
				int number = Integer.valueOf(oldName.substring(spacePosition + 1, pointPosition)).intValue();
				final int version = Integer.valueOf(oldName.substring(pointPosition + 1, oldName.length())).intValue();
				newName = oldName.substring(0, spacePosition + 1) + ++number + "." + version;
			}
			catch (final NumberFormatException e) {
				throw new NumberFormatException("Wrong format of the name");
			}
			catch (final StringIndexOutOfBoundsException e) {
				throw new NumberFormatException("Wrong format of the name");
			}
		}
		return newName;
	}
	
	public String getNewNameWithNewNumber(final String oldName, final int number) {
		String newName = null;
		if ((oldName != null) && (oldName.length() > 2)) {
			try {
				final int pointPosition = oldName.lastIndexOf(".");
				final int spacePosition = oldName.lastIndexOf(" ");
				final int version = Integer.valueOf(oldName.substring(pointPosition + 1, oldName.length())).intValue();
				newName = oldName.substring(0, spacePosition + 1) + number + "." + version;
			}
			catch (final NumberFormatException e) {
				throw new NumberFormatException("Wrong format of the name");
			}
			catch (final StringIndexOutOfBoundsException e) {
				throw new NumberFormatException("Wrong format of the name");
			}
		}
		return newName;
	}
	
	/*
	 * This method specifies a new name for the VersionedSet based on class of the
	 * object
	 */
	public String specifyVersionedSetName(final Object object) {
		if (!object.getClass().equals(String.class)) {
			String oldName = object.getClass().getSimpleName();
			Pattern pattern = Pattern.compile("[a-z]{1}[A-Z]{1}");
			Matcher matcher = pattern.matcher(oldName);
			String newName = matcher.replaceAll("---");
			
			int i = 0;
			while (newName.contains("-")) {
				final int index = newName.indexOf("-");
				final String endOfPreviousWord = oldName.substring(index - i, (index - i) + 1);
				final String beginningOfNextWord = oldName.substring((index - i) + 1, (index - i) + 2);
				newName = newName.replaceFirst("---", endOfPreviousWord + " " + beginningOfNextWord);
				i++;
			}
			
			oldName = newName;
			pattern = Pattern.compile("[A-Z]{1}[A-Z]{1}[a-z]");
			matcher = pattern.matcher(newName);
			newName = matcher.replaceAll("----");
			
			i = 0;
			while (newName.contains("-")) {
				final int index = newName.indexOf("-");
				final String endOfPreviousWord = oldName.substring(index - i, (index - i) + 1);
				final String beginningOfNextWord = oldName.substring((index - i) + 1, (index - i) + 3);
				newName = newName.replaceFirst("----", endOfPreviousWord + " " + beginningOfNextWord);
				i++;
			}
			return newName + " 0.0";
		}
		return object + " 0.0";
	}
}
