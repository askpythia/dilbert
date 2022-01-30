package org.vaadin.addons.metainfo.dilbert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

public class DilbertProperties {

	private static final String FAVORITES_SEPARATOR = " ";

	private static final File PROPERTIES_FILE = new File(System.getProperty("user.home") + File.separator + ".dilbert");

	private static final String FAVORITES_KEY = "favorites";
	private static final String LAST_SHOWN_KEY = "lastshown";
	private static final String LAST_FAVORITE_KEY = "lastfavorite";

	private String username;
	private Properties properties;
	private List<LocalDate> favorites;

	public DilbertProperties(String username) {
		this.username = username;
		this.properties = new Properties();
		if(PROPERTIES_FILE.exists()) {
			try (FileInputStream stream = new FileInputStream(PROPERTIES_FILE)) {
				properties.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected boolean shownToday() {
		LocalDate today = LocalDate.now();
		LocalDate lastShown = getDateForUser(LAST_SHOWN_KEY);
		if(lastShown == null || lastShown.isBefore(today)) {
			setDateForUser(LAST_SHOWN_KEY, today);
			return false;
		}
		return true;
	}

	protected LocalDate nextFavoriteDate() {
		List<LocalDate> favorites = favorites();
		if(favorites.isEmpty()) return null;
		LocalDate last = getDateForUser(LAST_FAVORITE_KEY);
		int idx = last == null ? favorites.size() : favorites.indexOf(last) + 1;
		if(idx >= favorites.size()) idx = 0;
		LocalDate next = favorites.get(idx);
		setDateForUser(LAST_FAVORITE_KEY, next);
		return next;
	}

	protected LocalDate randomFavoriteDate() {
		List<LocalDate> favorites = favorites();
		return favorites.isEmpty() ? null : favorites.get(new Random().nextInt(favorites.size()));
	}

	protected boolean isFavorite(LocalDate date) {
		return favorites().contains(date);
	}

	protected void setFavorite(LocalDate date, boolean favorite) {
		List<LocalDate> favorites = favorites();
		if(favorite && !favorites.contains(date)) {
			favorites.add(date);
			Collections.sort(favorites);
			storeFavorites();
		} else if(!favorite) {
			if(favorites.remove(date)) storeFavorites();
		}
	}

	private List<LocalDate> favorites() {
		if(favorites == null) {
			favorites = new ArrayList<>();
			String fav = properties.getProperty(FAVORITES_KEY);
			if(fav == null) {
				fav = Dilbert.DEFAULT_DILBERT_FAVORITES.stream().map(d -> toString(d)).reduce((a, b) -> a + FAVORITES_SEPARATOR + b).orElse(null);
				if(fav != null && !fav.isEmpty()) {
					properties.setProperty(FAVORITES_KEY,  fav);
					storeProperties();
				}
			}
			favorites = fav == null ? new ArrayList<>() : Arrays.stream(fav.split(FAVORITES_SEPARATOR)).map(f -> toDate(f)).sorted().collect(Collectors.toList());
			
		}
		return favorites; 
	}

	private void storeFavorites() {
		String fav = favorites().stream().sorted().map(f -> toString(f)).collect(Collectors.joining(FAVORITES_SEPARATOR));
		properties.setProperty(FAVORITES_KEY, fav);
		storeProperties();
	}

	private void storeProperties() {
		try (FileOutputStream stream = new FileOutputStream(PROPERTIES_FILE)) {
			properties.store(stream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LocalDate getDateForUser(String key) {
		String value = properties.getProperty(userKey(key));
		return value == null ? null : toDate(value);
	}

	private void setDateForUser(String key, LocalDate date) {
		String userKey = userKey(key);
		if(date == null) {
			properties.remove(userKey);
		} else {
			properties.setProperty(userKey, toString(date));
		}
		storeProperties();
	}

	private String userKey(String key) {
		return username == null ? key : (username + "." + key);
	}
	
	private String toString(LocalDate date) {
		return date.format(DateTimeFormatter.ISO_DATE);
	}
	
	private LocalDate toDate(String isoDate) {
		return LocalDate.parse(isoDate);
	}
}
