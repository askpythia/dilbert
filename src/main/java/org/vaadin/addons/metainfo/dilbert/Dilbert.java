package org.vaadin.addons.metainfo.dilbert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;

public class Dilbert {

	// The default list of Dilbert strip favorites (can be updated if wished)
	public static List<LocalDate> DEFAULT_DILBERT_FAVORITES = Arrays.stream(new String[] {
		"2004-01-25",
		"2022-01-02",
		"2022-01-05",
		"2022-01-06",
		"2022-01-09"
	}).map(f -> LocalDate.parse(f)).sorted().collect(Collectors.toList());

	// The day when the first Dilbert strip was published
	protected static final LocalDate FIRST_DILBERT_OF_THE_DAY_DATE = LocalDate.parse("1989-04-16");

	// The default type
	private static final DilbertType DEFAULT_DILBERT_TYPE = DilbertType.ofTheDay;

	// Constants to determine correct image url
	private static final String DILBERT_URL = "https://dilbert.com";
	private static final String DILBERT_STRIP_URL = DILBERT_URL + "/strip/";
	private static final String IMAGE_ATTRIBUTE = "data-image";
	private static final String IMAGE_URL = "https://assets.amuniversal.com/";

	/**
	 * In multiuser server environments it is possible to distinguish different users for Dilbert state info. Info will be stored in .dilbert properties
	 */
	private DilbertProperties properties;

	public Dilbert() {
		this(null);
	}

	public Dilbert(String username) {
		this.properties = new DilbertProperties(username);
	}

	public void show() {
		show(null);
	}

	public void show(DilbertType type) {
		show(type, false);
	}

	public void show(DilbertType type, boolean force) {
		if(force || !properties.shownToday()) {
			if(type == null) type = DEFAULT_DILBERT_TYPE;
			LocalDate date = null;
			switch(type) {
			case ofTheDay:
				date = LocalDate.now();
				break;
			case random:
				date = LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(FIRST_DILBERT_OF_THE_DAY_DATE.toEpochDay(), LocalDate.now().toEpochDay()+1));
				break;
			case nextFavorite:
				date= properties.nextFavoriteDate();
				break;
			case randomFavorite:
				date = properties.randomFavoriteDate();
				break;
			}
			openDialog(type, date);
		}
	}

	private void openDialog(DilbertType type_, LocalDate date_) {
		// Fallback to Dilbert of the day if no type given
		if(type_ == null) type_ = DilbertType.ofTheDay;

		// Fallback to today if no date given, date is before the first Dilbert or date in the future
		if(date_ == null || date_.isAfter(LocalDate.now()) || date_.isBefore(FIRST_DILBERT_OF_THE_DAY_DATE)) {
			date_ = LocalDate.now();
			type_ = DilbertType.ofTheDay; 
		}

		// Capture image url from dilbert.com for given date
		String imageUrl = dilbertImageUrl(date_);

		// If not available yet fall back to the strip of last day
		if(imageUrl == null && date_.equals(LocalDate.now())) {
			date_ = date_.minusDays(1);
			imageUrl = dilbertImageUrl(date_);
		}

		// Show dialog if image url exists
		if(imageUrl == null) {
			// There seems to be a problem, notify
			Notification.show("Can't find " + type_.getText() + " for " + date_, 3000, Position.MIDDLE);
		} else {
			// Make final for lambdas
			LocalDate date = date_;
			DilbertType type = type_;

			// Build main layout 
			VerticalLayout layout = new VerticalLayout();
			layout.setMargin(false);
			layout.setPadding(false);

			// Build dialog
			Dialog dialog = new Dialog(layout);
			dialog.setModal(true);
			dialog.setCloseOnEsc(true);
			dialog.setCloseOnOutsideClick(true);

			// Get Dilbert strip image
			Image image = new Image(imageUrl, type.getText() + " " + String.valueOf(date));

			// Allow selection of a different date
			DatePicker datePicker = new DatePicker(date);
			datePicker.addValueChangeListener(event -> {
				LocalDate value = event.getValue();
				if(value.compareTo(FIRST_DILBERT_OF_THE_DAY_DATE) >= 0 && value.compareTo(LocalDate.now()) <= 0) {
					dialog.close();
					openDialog(null, value);
				} else {
					Notification.show("Date out of range, select date between " + FIRST_DILBERT_OF_THE_DAY_DATE + " and " + LocalDate.now() + "!", 5000, Position.MIDDLE);
				}
			});
			
			// Handle Favorites
			Checkbox fav = new Checkbox("Favorite", properties.isFavorite(date));
			fav.addValueChangeListener(event -> properties.setFavorite(date, event.getValue().booleanValue()));

			// Prepare action footer
			HorizontalLayout actions = new HorizontalLayout();
			actions.setWidthFull();
			actions.setMargin(false);
			actions.setPadding(false);
			actions.setAlignItems(Alignment.BASELINE);
			actions.add(new Label(type.getText()), datePicker, fav);

			// Add possible follow up action
			if(type.getAction() != null) {
				actions.add(new Button(type.getAction(), event -> {
					dialog.close();
					show(type, true);
				}));
			}

			// Add copyright and link to dilbert.com
			Image dilbert = new Image("dilbert/dilbert-logo.png", DILBERT_URL);
			//dilbert.setHeight("48px");
			dilbert.setHeight("3rem");
			Style style = dilbert.getElement().getStyle();
			style.set("align-self", "center");
			style.set("margin-left", "auto");
			style.set("cursor", "pointer");
			dilbert.getElement().setAttribute("title", "Dilbert © 2022, Andrews McMeel Syndication");
			dilbert.addClickListener(event -> UI.getCurrent().getPage().open(DILBERT_URL, "_blank"));
			actions.add(dilbert);

			// Assemble and open dialog
			layout.add(image, actions);
			dialog.open();
		}
	}

	/**
	 * Capture correct Image URL to the comic strip from dilbert.com of given date
	 * look for an element with attribute named
	 * "data-image" (IMAGE_ATTRIBUTE)
	 * with value starting with base image url "https://assets.amuniversal.com/" (IMAGE_URL)
	 * @return URL String to Dilbert comic strip of given date, null if not exists   
	 */
	private String dilbertImageUrl(LocalDate date) {
		String isoDate = date.format(DateTimeFormatter.ISO_DATE);
		try {
			URL url = new URL(DILBERT_STRIP_URL + isoDate);
			URLConnection conn = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
            while ((line = br.readLine()) != null) {
            	int ai = line.indexOf(IMAGE_ATTRIBUTE);
            	if( ai >= 0) {
            		int ui = line.indexOf(IMAGE_URL);
            		if(ui >= 0) {
                    	line = line.substring(ui + IMAGE_URL.length());
                    	int end = line.indexOf("\"");
                    	if(end >= 0) {
                    		return IMAGE_URL + line.substring(0, end);
                    	}
            		}
            	}
            }
            br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
        return null;
	}
}
