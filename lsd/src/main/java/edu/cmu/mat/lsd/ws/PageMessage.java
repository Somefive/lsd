package edu.cmu.mat.lsd.ws;

import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.System;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class PageMessage extends Message {
	
	public class SystemPackage {
		public int pageNumber;
		public double begin, end;
		public double[] barlines;
		SystemPackage(System system, int _pageNumber) {
			pageNumber = _pageNumber;
			begin = system.getTop();
			end = system.getBottom();
			barlines = system.getBarlines().stream().mapToDouble(barline -> barline.getOffset()).toArray();
		}
	}
	
	public int pageNumber;
	public String image;
	public List<SystemPackage> systems;
	PageMessage(Page page) {
		super("page");
		pageNumber = page.getParent().getPages().indexOf(page);
		try {
			BufferedImage bi = page.getImage().getImage();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "jpg", baos);
			image = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (Exception e) {
			image = "";
		}
		systems = page.getSystems().stream().map(system -> new SystemPackage(system, pageNumber)).collect(Collectors.toList());
	}
}
