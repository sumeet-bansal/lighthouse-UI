package controller;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainController extends MongoConnector {

	private DirTree tree = new DirTree();
	private String[] dropL = { "", "", "", "" };
	private String[] dropR = { "", "", "", "" };

	@ResponseBody
	@RequestMapping(value = "/fetchList", method = RequestMethod.POST)
	public String fetchList(HttpServletRequest req, HttpServletResponse res) throws IOException {
		MongoConnector.connectToDatabase();
		System.out.println("Attempt to get list of data.");

		// TODO wildcard support
		
		ObjectMapper mapper = new ObjectMapper();
		Set<String> list = new LinkedHashSet<>();
		String selectedKey = "";
		String selectedVal = "";
		String path = "";

		int level = -1;
		if (req.getParameterMap().size() == 1) {
			tree = MongoConnector.populate();
		} else {
			Map.Entry<String, String[]> selectedDrop = null;
			for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
				selectedDrop = entry;
			}
			selectedKey = selectedDrop.getKey();
			selectedVal = selectedDrop.getValue()[0];
			String type = selectedKey.substring(selectedKey.lastIndexOf('[')+1, selectedKey.length()-1);
			
			switch (type) {
			case "env":
				level = 0;
				break;
			case "fabric":
				level = 1;
				break;
			case "node":
				level = 2;
				break;
			case "file":
				level = 3;
				break;
			default:
				level = -1;
			}
			
			String[] selectedSide;
			if (selectedKey.contains("left")) {
				System.out.println("left server");
				selectedSide = dropL;
			} else {
				System.out.println("right server");
				selectedSide = dropR;
			}

			selectedSide[level] = selectedVal;
			for (int i = 0; i <= level; i++) {
				path += selectedSide[i] + "/";
			}
			for (int i = level+1; i < selectedSide.length; i++) {
				selectedSide[i] = "";
			}
		}
		
		System.out.println("path: " + path);
		list.add("*");
		list.addAll(tree.getChildren(path));
		
		try {
			String ret = "{\"result\": \"SUCCESS\", \"list\":" + mapper.writeValueAsString(list)
					+ "}";
			System.out.println(ret);
			System.out.println("\n\n");
			return ret;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "{\"result\":\"FAILED\"}";
		}
	}

	@ResponseBody
	@RequestMapping(value = "/fetchData", method = RequestMethod.POST)
	public String fetchData(HttpServletRequest req, HttpServletResponse res) {
		System.out.println("User data complete");
		// System.out.println(req.getParameterMap().keySet().toString());
		System.out.println(req.getParameter("index"));
		String data = req.getParameter("env") + "/" + req.getParameter("fabric") + "/"
				+ req.getParameter("node") + "/" + req.getParameter("file") + "/";

		return "{\"result\":\"SUCCESS\", \"data\":\"" + data + "\"}";
	}

	@ResponseBody
	@RequestMapping(value = "/CSV", method = RequestMethod.POST)
	public String csv() {
		ArrayList<ArrayList<String>> report = new ArrayList<ArrayList<String>>();
		String path1 = dropL[0] + "/" + dropL[1] + "/" + dropL[2] + "/" + dropL[3];
		String path2 = dropR[0] + "/" + dropR[1] + "/" + dropR[2] + "/" + dropR[3];
		;
		int l = 0;
		int r = 0;
		for (int i = 0; i < dropL.length; i++) {
			System.out.println("left");
			if (i < 3) {
				path1 += dropL[i] + "/";
			} else {
				path1 += dropL[i];
			}

		}
		for (int i = 0; i < dropR.length; i++) {
			System.out.println("right");
			if (i < 3) {
				path2 += dropR[i] + "/";
			} else {
				path2 += dropR[i];
			}

		}
		while (!dropL[l].equals("") && l < 4) {
			if (l < 3) {
				path1 += dropL[l] + "/";
			} else {
				path1 += dropL[l];
			}
			l++;
		}
		while (!dropR[r].equals("") && r < 4) {
			System.out.println("right");
			if (r < 3) {
				path1 += dropR[r] + "/";
			} else {
				path1 += dropR[r];
			}
			r++;
		}
		System.out.println(path1 + " " + path2);
		if (dropL[0].equals("")) {
			cmd("C:/Users/GGupta/Desktop", "java -jar ADS_1.0.jar query compare " + path2);
		} else if (dropR[0].equals("")) {
			cmd("C:/Users/GGupta/Desktop", "java -jar ADS_1.0.jar query compare " + path1);
		} else {
			cmd("C:/Users/GGupta/Desktop",
					"java -jar ADS_1.0.jar query compare " + path1 + " " + path2);
		}
		String loc = System.getProperty("user.home") + "\\Desktop\\ADS Reports";
		File csvReport = lastFileModified(loc);
		ArrayList<String> data = readFile(csvReport);
		String line = "";
		String file = "";
		for (int i = 0; i < data.size(); i++) {
			line = data.get(i) + ",";
			file += data.get(i);
			while (line.indexOf(',') > -1) {
				report.get(report.size() - 1).add(line.substring(0, line.indexOf(',')));
				line = line.substring(line.indexOf(',') + 1);
			}
		}
		return file;
	}

	public void cmd(String jarLoc, String command) {
		try {
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
					"cd \"" + jarLoc + "\" && " + command);
			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println("cmd jar command invalid");
		}
	}

	@ResponseBody
	@RequestMapping(value = "/readFile", method = RequestMethod.POST)
	public ArrayList<String> readFile(File file) {
		ArrayList<String> csv = new ArrayList<String>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String str;
			while ((str = br.readLine()) != null) {
				csv.add(str);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("ERROR: File does not exist");
		}
		return csv;
	}

	public File lastFileModified(String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		try {
			for (File file : files) {
				if (file.lastModified() > lastMod) {
					choice = file;
					lastMod = file.lastModified();
				}
			}
		} catch (NullPointerException e) {
			System.out.println("Error: " + dir + " is empty");
		}
		return choice;
	}
}
