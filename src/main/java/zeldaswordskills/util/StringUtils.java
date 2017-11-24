/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.util;

import net.minecraft.util.StatCollector;

public class StringUtils {

	/**
	 * Breaks up the given string into whole words in lines of the given length;
	 * Words too long for a single line will be hyphenated automatically.
	 * @return Array in which each element is a single line of no more than the given length
	 */
	public static String[] wrapString(String string, int length) {
		return StringUtils.wrapString(string, length, -1);
	}

	/**
	 * Breaks up the given string into whole words in lines of the given length;
	 * Words too long for a single line will be hyphenated automatically.
	 * @param hyphenate Threshold length for words to hyphenate at the end of a line,
	 * 					or -1 to always move the entire word to a new line (if it fits)
	 * @return Array in which each element is a single line of no more than the given length
	 */
	public static String[] wrapString(String string, int length, int hyphenate) {
		StringBuilder wrapped = new StringBuilder(string.length());
		int i = 0;
		for (String word : string.split(" ", -1)) {
			// Word is too long for remaining line length
			if (length < i + word.length()) {
				// Allow manual hyphenation
				int hyphen = word.indexOf("-") + 1;
				if (hyphen > 0 && i + hyphen < length) {
					wrapped.append(word.substring(0, hyphen));
					word = word.substring(hyphen);
				}
				// Otherwise check for automatic hyphenation
				else if (hyphenate > 0 && length - i > hyphenate) {
					hyphen = length - i - 1; // subtract one to allow for hyphen
					wrapped.append(word.substring(0, hyphen));
					wrapped.append("-");
					word = word.substring(hyphen);
				}
				wrapped.append("\n");
				i = 0;
			}
			// TODO bug if hyphen manually added (by user) at end of line, results in 2 hyphens
			// TODO newlines are not added separately from actual words and are thus counted in the word length
			// Word is too long for a single line: break it up using hyphens
			if (length < word.length()) {
				int j = 0;
				int len = length - 1;
				int sub = 0;
				while (sub < word.length()) {
					sub = Math.min(sub + len, word.length());
					wrapped.append(word.substring(j, sub));
					j += len;
					if (sub == word.length() && sub - j < len) {
						wrapped.append(" ");
						i = (sub - j);
					} else {
						wrapped.append("-\n");
					}
				}
			} else {
				wrapped.append(word);
				wrapped.append(" ");
				i += word.length() + 1;
			}
		}
		string = wrapped.toString();
		return string.split("\n", -1);
	}
	
	/**
	 * Translates a provided key to the local language using {@code StatCollector}. If no key is found
	 * for the locale, the default English version is returned instead, as any reference to a language
	 * key in ZSS should have an English return
	 * @param key the language key to be translated
	 * 
	 * @return the translation for the provided key in the current language, or the English translation if the key
	 *         does not exist in the current language
	 */
	public static String translateKey(String key) {
		return StatCollector.canTranslate(key) ? StatCollector.translateToLocal(key) : StatCollector.translateToFallback(key);
	}
}
