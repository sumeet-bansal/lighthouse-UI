package controller;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainController extends MongoConnector {

	private ArrayList<Map<String, Set<String>>> tree = new ArrayList<Map<String, Set<String>>>();
	private String selR[] = { "", "", "", "" };
	private String selL[] = { "", "", "", "" };

	@ResponseBody
	@RequestMapping(value = "/fetchList", method = RequestMethod.POST)
	public String fetchList(HttpServletRequest req, HttpServletResponse res) throws IOException {
		System.out.println("Attempt to get list of Data");

		MongoConnector m = new MongoConnector();
		ObjectMapper mapper = new ObjectMapper();
		Set<String> list = new TreeSet<String>();
		String type = req.getParameter("listType");
		Map<String, String[]> reqParams = req.getParameterMap();
		String key = reqParams.keySet().toString();
		String server = "";
		String realType = "";
		String context = "";

		if (key.indexOf(',') > -1) {
			String location = key.substring(key.lastIndexOf(',') + 2, key.lastIndexOf(']'));
			context = req.getParameter(location);
			realType = location.substring(location.lastIndexOf('[') + 1, location.lastIndexOf(']'));
			if (location.substring(location.indexOf('[') + 1, location.indexOf(']'))
					.equals("left")) {
				server = "left";
				if (realType.equals("env")) {
					selL[0] = context;
					selL[1] = "";
					selL[2] = "";
					selL[3] = "";
				} else if (realType.equals("fabric")) {
					selL[1] = context;
					selL[2] = "";
					selL[3] = "";
				} else if (realType.equals("node")) {
					selL[2] = context;
					selL[3] = "";
				} else if (realType.equals("file")) {
					selL[3] = context;
				}
			} else {
				server = "right";
				if (realType.equals("env")) {
					selR[0] = context;
					selR[1] = "";
					selR[2] = "";
					selR[3] = "";
				} else if (realType.equals("fabric")) {
					selR[1] = context;
					selR[2] = "";
					selR[3] = "";
				} else if (realType.equals("node")) {
					selR[2] = context;
					selR[3] = "";
				} else if (realType.equals("file")) {
					selR[3] = context;
				}
			}
		} else {
			m.connectToDatabase();
			m.populate();
			tree.add(m.fabrics);
			tree.add(m.nodes);
			tree.add(m.files);
		}

		if (type == null || type.equals("env")) {
			list.addAll(tree.get(0).keySet());
			list.add("*");
		} else if (type.equals("fabric")) {
			if (server.equals("left")) {
				list = tree.get(0).get(context);
				list.add("*");
			} else {
				list = tree.get(0).get(context);
				list.add("*");
			}
		} else if (type.equals("node")) {
			if (server.equals("left")) {
				if (tree.get(1).containsKey(selL[0] + "." + context)) {
					list = tree.get(1).get(selL[0] + "." + context);
					list.add("*");
				}
			} else {
				if (tree.get(1).containsKey(selR[0] + "." + context)) {
					list = tree.get(1).get(selR[0] + "." + context);
					list.add("*");
				}
			}
		} else if (type.equals("file")) {
			if (server.equals("left")) {
				if (tree.get(2).containsKey(selL[0] + "." + selL[1] + "." + context)) {
					list = tree.get(2).get(selL[0] + "." + selL[1] + "." + context);
					list.add("*");
				}
			} else {
				if (tree.get(2).containsKey(selR[0] + "." + selR[1] + "." + context)) {
					list = tree.get(2).get(selR[0] + "." + selR[1] + "." + context);
					list.add("*");
				}
			}
		}

		try {
			String ret = "{\"result\": \"SUCCESS\", \"list\":" + mapper.writeValueAsString(list)
					+ "}";
			System.out.println(ret);
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
		String path1 = selL[0] + "/" + selL[1] + "/" + selL[2] + "/" + selL[3];
		String path2 = selR[0] + "/" + selR[1] + "/" + selR[2] + "/" + selR[3];
		;
		int l = 0;
		int r = 0;
		for (int i = 0; i < selL.length; i++) {
			System.out.println("left");
			if (i < 3) {
				path1 += selL[i] + "/";
			} else {
				path1 += selL[i];
			}

		}
		for (int i = 0; i < selR.length; i++) {
			System.out.println("right");
			if (i < 3) {
				path2 += selR[i] + "/";
			} else {
				path2 += selR[i];
			}

		}
		while (!selL[l].equals("") && l < 4) {
			if (l < 3) {
				path1 += selL[l] + "/";
			} else {
				path1 += selL[l];
			}
			l++;
		}
		while (!selR[r].equals("") && r < 4) {
			System.out.println("right");
			if (r < 3) {
				path1 += selR[r] + "/";
			} else {
				path1 += selR[r];
			}
			r++;
		}
		System.out.println(path1 + " " + path2);
		if (selL[0].equals("")) {
			cmd("C:/Users/GGupta/Desktop", "java -jar ADS_1.0.jar query compare " + path2);
		} else if (selR[0].equals("")) {
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
