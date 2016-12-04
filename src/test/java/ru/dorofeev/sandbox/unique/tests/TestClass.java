package ru.dorofeev.sandbox.unique.tests;

import org.junit.Test;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class TestClass {

	@Test
	public void test() {

		getTextFileReader("temp/data.txt")
			.compose(chars -> wordExtractor(chars, " ,;\n\r\t"))
			.compose(words -> wordCounter(words, TreeMap::new))
			.compose(wordCount -> filterByCount(wordCount, c -> c == 1))
			.subscribe(System.out::println);
	}

	private Observable<Character> getTextFileReader(String fileName) {
		return Observable.create(subscriber -> {

			try (FileInputStream fis = new FileInputStream(fileName)) {

				byte[] buffer = new byte[16384];

				int bytesRead;
				while ( (bytesRead = fis.read(buffer)) != -1 ) {

					for (int i = 0; i < bytesRead; i++)
						subscriber.onNext((char)buffer[i]);
				}

				subscriber.onCompleted();
			} catch (IOException e) {
				subscriber.onError(e);
			}
		});
	}

	private static Observable<String> wordExtractor(Observable<Character> characters, String delimiters) {

		return Observable.create(subscriber -> {

			StringBuilderRef sb = new StringBuilderRef();

			characters.subscribe(character -> {

				boolean isDelimiter = delimiters.indexOf(character) != -1;
				if (isDelimiter && sb.isNotEmpty()) {
					subscriber.onNext(sb.build());
				} else if (!isDelimiter) {
					sb.append(character);
				}
			},
				subscriber::onError,
				() -> {
					if (sb.isNotEmpty())
						subscriber.onNext(sb.build());

					subscriber.onCompleted();
				});
		});
	}

	private static Observable<Map<String, Integer>> wordCounter(Observable<String> words, Func0<Map<String, Integer>> mapFactory) {

		Map<String, Integer> map = mapFactory.call();

		return Observable.create(subscriber ->
			words.subscribe(
				word -> map.compute(word, (s, count) -> count != null ? count + 1 : 1),
				subscriber::onError,
				() -> {
					subscriber.onNext(map);
					subscriber.onCompleted();
				}));
	}

	private static Observable<String> filterByCount(Observable<Map<String, Integer>> words, Func1<Integer, Boolean> countPredicate) {
		return Observable.create(subscriber ->
			words.subscribe(map -> {
				map.forEach((word, count) -> {
					if (countPredicate.call(count))
						subscriber.onNext(word);
				});

				subscriber.onCompleted();
			}));
	}

	private static class StringBuilderRef {

		private StringBuilder sb = new StringBuilder();

		void append(char character) {
			sb.append(character);
		}

		boolean isNotEmpty() {
			return sb.length() != 0;
		}

		public String build() {
			String s = sb.toString();
			sb = new StringBuilder();
			return s;
		}
	}
}
