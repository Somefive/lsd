package edu.cmu.mat.scores;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import edu.cmu.mat.geom.Point;
import edu.cmu.mat.parsers.JsonParser;
import edu.cmu.mat.parsers.exceptions.CompilerException;
import edu.cmu.mat.scores.events.Event;
import edu.cmu.mat.scores.events.EventTypeAdapter;

public class Score implements ScoreObject {
	private static JsonParser PARSER = new JsonParser();
	public static Gson GSON = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
			.registerTypeAdapter(Event.class, new EventTypeAdapter()).create();

	private File _root;

	@Expose
	private String _name;

	@Expose
	private Set<Section> _sections;
	@Expose 
	private Set<Repeat> _repeats;
	@Expose
	private List<Page> _pages;
	@Expose
	private Arrangement _arrangement = new Arrangement(this);

	public Score(File root, String name, Set<Section> sections, Set<Repeat> repeats, List<Page> pages) {
		_root = root;
		_name = name;
		_sections = sections;
		_repeats = repeats;
		_pages = pages;
	}

	public Score(File root, String name) {
		this(root, name, new HashSet<Section>(), new HashSet<Repeat>(), new ArrayList<Page>());
	}

	public Score(File root, String name, List<Image> images) {
		this(root, name);
		for (Image image : images) {
			_pages.add(new Page(this, image));
		}
	}

	public Score(File root, Score other, List<Image> images) {
		this(root, other.getName());
		for (int i = 0; i < other._pages.size(); i++) {
			_pages.add(new Page(this, other._pages.get(i), images.get(i)));
		}

		for (Section section : other._sections) {
			_sections.add(new Section(this, section));
		}
		
		for (Repeat repeat : other._repeats) {
			_repeats.add(new Repeat(this, repeat));
		}

		// Initializing arrangements has to come after initializing the pages
		// and sections since arrangements relies on them to already exist.
		_arrangement = new Arrangement(this, other._arrangement);
	}

	public void setRoot(File root) {
		_root = root;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getName() {
		return _name;
	}

	public Section addSection(Barline start, Barline end) {
		if (start == null || end == null) {
			return null;
		}

		Section newSection = new Section(this, start, end);
		_sections.add(newSection);
		return newSection;
	}
	
	public Repeat addRepeat(Barline start, Barline end) {
		
		if (start == null || end == null) {
			return null;
		}
		
		Repeat newRepeat = new Repeat(this, start, end);
		_repeats.add(newRepeat);
		return newRepeat;
	}

	public Set<Section> getSections() {
		return _sections;
	}
	
	public Set<Repeat> getRepeats() {
		return _repeats;
	}

	public Section getSectionByName(String name) {
		for (Section section : _sections) {
			if (section.getName().equals(name)) {
				return section;
			}
		}
		return null;
	}

	public Repeat getRepeatByName(String name) {
		for (Repeat repeat : _repeats) {
			java.lang.System.out.println(name+" "+repeat.getName()+" "+ repeat.getName().equals(name));
			if (repeat.getName().equals(name)) {
				return repeat;
			}
		}
		return null;
	}
	
	public void removeSection(Section section) {
		if (section == null) {
			return;
		}
		_sections.remove(section);
		Event startEvent = section.getStartEvent();
		Event endEvent = section.getEndEvent();

		if (startEvent != null) {
			startEvent.getParent().deleteChild(startEvent);
		}

		if (endEvent != null) {
			endEvent.getParent().deleteChild(endEvent);
		}
	}

	public void removeRepeat(Repeat repeat) {
		if (repeat == null) {
			return;
		}
		
		_repeats.remove(repeat);
		Event startEvent = repeat.getStartEvent();
		Event endEvent = repeat.getEndEvent();
		
		if (startEvent != null) {
			startEvent.getParent().deleteChild(startEvent);
		}
		
		if (endEvent != null) {
			endEvent.getParent().deleteChild(endEvent);
		}
	}
	
	public void addPage(Page page) {
		_pages.add(page);
	}

	public void addPages(File[] images) throws IOException {
		File imagesDir = new File(_root, "images");
		int num = _pages.size();

		for (int i = 0; i < images.length; i++) {
			String fromString = images[i].toString();
			String ext = fromString.substring(fromString.lastIndexOf('.'));
			File to = new File(imagesDir, String.valueOf(num + i + 1) + ext);
			Files.copy(images[i].toPath(), to.toPath());
			_pages.add(new Page(this, new Image(ImageIO.read(to))));
		}
	}

	public int getNumberPages() {
		return _pages.size();
	}

	public Page getPage(int index) {
		return _pages.get(index);
	}

	public List<Page> getPages() {
		return _pages;
	}

	public void saveTo(File score_directory) {
		String json = GSON.toJson(this);
		File init_file = new File(score_directory, "init.json");
		try {
			FileWriter writer = new FileWriter(init_file);
			writer.write(json);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Score fromDirectory(File score_directory) throws IOException,
			CompilerException {
		File imagesDir = new File(score_directory.getAbsolutePath()
				+ File.separator + "images");
		File[] imageFiles = imagesDir.listFiles();

		List<Image> images = new ArrayList<Image>();
		if (imageFiles != null) {
			int number = 0;
			for (File image : imageFiles) {
				String name = image.getName();
				try {
					Integer.parseInt(name.substring(0, name.indexOf('.')));
					number++;
				} catch (Exception e) {
				}
			}

			images = Arrays.asList(new Image[number]);
			for (File image : imageFiles) {
				String name = image.getName();
				try {
					int index = Integer.parseInt(name.substring(0,
							name.indexOf('.'))) - 1;
					images.set(index, new Image(ImageIO.read(image)));
				} catch (Exception e) {
					java.lang.System.err.println(e);
				}
			}
		}

		File init_file = new File(score_directory, "init.json");
		return PARSER.parse(score_directory.getName(), init_file, images);
	}

	public static Score createNew(File score_path, File[] images)
			throws IOException, CompilerException {
		score_path.mkdir();
		File images_path = new File(score_path, "images");
		images_path.mkdir();

		for (int i = 0; i < images.length; i++) {
			String fromString = images[i].toString();
			String ext = fromString.substring(fromString.lastIndexOf('.'));
			File image_path = new File(images_path, String.valueOf(i + 1) + ext);
			Files.copy(images[i].toPath(), image_path.toPath());
		}

		return fromDirectory(score_path);
	}

	public ScoreObject getParent() {
		return null;
	}

	public Arrangement getArrangement() {
		return _arrangement;
	}

	public List<Section> getArrangementList() {
		return _arrangement.getList();
	}

	public void saveArrangement(String string) {
		_arrangement.save(string);
	}

	public ScoreObject move(Point distance, ScoreObject intersect) {
		// Does nothing.
		return null;
	}

	public void setActive(Point location) {
		// Does nothing.
	}

	public void setInactive() {
		// Does nothing.
	}

	public void normalize() {
		for (Page page : _pages) {
			page.normalize();
		}

		for (Section section : _sections) {
			section.normalize();
		}
		
		for (Repeat repeat : _repeats) {
			repeat.normalize();
		}
	}

	public void delete() {
		for (Page page : _pages) {
			page.delete();
		}
	}

	public void deleteChild(ScoreObject child) {
		if (_pages.remove(child)) {
			child.delete();
		}
	}

	public List<Barline> getStartBarlines() {
		List<Barline> barlines = new LinkedList<Barline>();

		List<Page> pages = getPages();
		for (int page = 0; page < pages.size(); page++) {
			List<System> systems = pages.get(page).getSystems();
			for (int system = 0; system < systems.size(); system++) {
				List<Barline> system_barlines = systems.get(system)
						.getBarlines();
				for (int i = 0; i < system_barlines.size() - 1; i++) {
					barlines.add(system_barlines.get(i));
				}
			}
		}
		return barlines;
	}

	public List<Barline> getEndBarlines() {
		List<Barline> barlines = new LinkedList<Barline>();
		barlines.add(null);

		List<Page> pages = getPages();
		for (int page = 0; page < pages.size(); page++) {
			List<System> systems = pages.get(page).getSystems();
			for (int system = 0; system < systems.size(); system++) {
				List<Barline> system_barlines = systems.get(system)
						.getBarlines();
				for (int i = 1; i < system_barlines.size(); i++) {
					barlines.add(system_barlines.get(i));
				}
			}
		}
		return barlines;
	}

	private List<PlaybackEvent> add(Section _section, List<Barline> start_barlines, List<Barline> end_barlines) {
		Barline start_barline = _section.getStart();
		Barline end_barline = _section.getEnd();
		int start = start_barlines.indexOf(start_barline);
		int end = end_barlines.indexOf(end_barline);
		
		List<PlaybackEvent> section_events = new ArrayList<PlaybackEvent>();
		int duration = 4;
		boolean is_first = true;
		for (int i = start; i < end; i++) {
			section_events.add(new PlaybackEvent(_section,
					start_barlines.get(i), end_barlines.get(i),
					duration, is_first));
			is_first = false;
		}
		return section_events;
	}
	
	public List<PlaybackEvent> createPlaybackEvents(String[] arrangement_string) {
		try {
			List<PlaybackEvent> events = new LinkedList<PlaybackEvent>();

			List<Barline> start_barlines = getStartBarlines();
			List<Barline> end_barlines = getEndBarlines();

			java.lang.System.out.println("Playback start");

			if (arrangement_string.length == 0) {
				for (Section section : _sections) {
					
					List<PlaybackEvent> section_events = add(section, start_barlines, end_barlines);

					events.addAll(section_events);
				}
			}
			else 
			for (String section_string : arrangement_string) {
				String[] parts = section_string.split(",");
				String name = parts[0];

				// TODO: This currently works with Section names. Instead, this
				// should be using a proper flattened score mapping.

				Section section = getSectionByName(name);
				if (section == null) {
					java.lang.System.err
							.println("Could not find section with name: "
									+ name);
					continue;
				}
				
				List<PlaybackEvent> section_events = add(section, start_barlines, end_barlines);

				events.addAll(section_events);
			}

			return events;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private System getNextSystemInScore(System current) {
		Page current_page = current.getParent();
		List<System> systems = current_page.getSystems();
		if (systems.indexOf(current) < systems.size() - 1) {
			return systems.get(systems.indexOf(current) + 1);
		}
		if (_pages.indexOf(current_page) < _pages.size() - 1) {
			List<System> next_page_systems = _pages.get(
					_pages.indexOf(current_page) + 1).getSystems();
			if (next_page_systems.size() == 0)
				return null;
			return next_page_systems.get(0);
		}
		return null;
	}

	private Block createCurrentBlock(System first, double block_height) {
		List<System> systems = new ArrayList<System>();
		System previous = null;
		System current = first;
		double current_height = first.getBottom() - first.getTop();

		while (current != null && current_height <= block_height) {

			previous = current;
			current = getNextSystemInScore(previous);

			if (current != null) {
				current_height += current.getBottom() - current.getTop();
			}

			systems.add(previous);

		}
		return new Block(systems);

	}

	public boolean outOfBlock(Block block, Barline start) {
		System block_start = block.getStartSystem();
		System block_end = block.getEndSystem();
		return start.getParent().compareTo(block_start) < 0 || start.getParent().compareTo(block_end) > 0;
	}

	private boolean isJump(Barline curr, Barline next) {
		if (curr != next) {
			System curr_system = curr.getParent();
			List<Barline> curr_barlines = curr_system.getBarlines();
			System next_system = next.getParent();
			List<Barline> next_barlines = next_system.getBarlines();

			if (next_system != getNextSystemInScore(curr_system)
					|| curr != curr_barlines.get(curr_barlines.size() - 1)
					|| next != next_barlines.get(0)) {
				return true;
			}
		}
		return false;
	}

	private PlaybackEvent propagateEvent(Block block,
			List<PlaybackEvent> events, PlaybackEvent event) {
		int index = events.indexOf(event);
		int size = events.size();

		PlaybackEvent curr = event;
		PlaybackEvent next = event;
		while (index < size - 1) {
			curr = next;
			next = events.get(index + 1);
			index++;

			if (outOfBlock(block, next.getStart())) {
				block.addJump(curr.getEnd(), next.getStart());
				break;
			} else if (isJump(curr.getEnd(), next.getStart())) {
				block.addJump(curr.getEnd(), next.getStart());
			}
		}
		// if the last event is still inside current block,
		// this is the last block, and nothing to propagate.
		if (index == size - 1 && !outOfBlock(block, next.getStart())) {
			return null;
		}
		return next;
	}

	public List<Block> createBlockList(List<PlaybackEvent> events,
			double block_height) {
		List<Block> blocks = new ArrayList<Block>();
		int event_start_index = 0;
		PlaybackEvent current_event = events.get(event_start_index);
		System next_block_start = current_event.getStart().getParent();
		while (next_block_start != null) {
			Block block = createCurrentBlock(next_block_start,
					block_height);
			blocks.add(block);

			// propagate event forward until out of block;
			current_event = propagateEvent(block, events, current_event);

			if (current_event == null) {
				next_block_start = null;
			} else {
				System event_start = current_event.getStart().getParent();
				next_block_start = event_start;
			}
		}
		return blocks;
	}
}
