package org.vaadin.addons.metainfo.dilbert;

public enum DilbertType {

	ofTheDay("Dilbert of the day", null),
	random("Random Dilbert of the day","Shuffle again"),
	nextFavorite("Favorite Dilbert of day", "Next Favorite"),
	randomFavorite("Random favorite Dilbert of day", "Shuffle Favorite"),
	;

	private final String text;
	private final String action;

	private DilbertType(String text, String action) {
		this.text = text;
		this.action = action;
	}

	public String getText() {
		return text;
	}
	
	public String getAction() {
		return action;
	}
}
