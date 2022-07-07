package com.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class LocationTrackUtil {

	private LocationTrackUtil() {
	}

	static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	static double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 1852;
		return (dist);
	}

	public static CSVParser getCsvFile() throws IOException {

		File rFile = new File(LocationTrackUtil.class.getClassLoader().getResource("USZIPCodes202201.csv").getFile());
		return CSVParser.parse(rFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader());
	}

	public static Pair getApiDtl(String postalCode) throws IOException {
		Pair p1 = new Pair();

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpget = new HttpGet(String.format("https://geocoder.ca/%s?json=1", postalCode));

			try (CloseableHttpResponse response = httpclient.execute(httpget)) {
				HttpEntity entity = response.getEntity();

				if (response.getStatusLine().getStatusCode() != 200) {
					System.out.println("Connection is bad, status code: " + response.getStatusLine().getStatusCode());
				} else {
					System.out.println("Connection is ok");
					String output = EntityUtils.toString(entity);
					StringTokenizer st = new StringTokenizer(output.replaceAll("[\"{}]", ""), ",");
					while (st.hasMoreTokens()) {
						String tkn = st.nextToken();
						if (tkn.contains("longt : ")) {
							tkn = tkn.replace("longt : ", "");
							if (StringUtils.isNotEmpty(tkn)) {
								p1.setLongitude(Double.parseDouble(tkn));
							}
						}
						if (tkn.contains("latt : ")) {
							tkn = tkn.replace("latt : ", "");
							if (StringUtils.isNotEmpty(tkn)) {
								p1.setLatitude(Double.parseDouble(tkn));
							}
						}
					}

					/*
					 * System.out.println(output); System.out.println("final details: " + p1);
					 */
				}
			}

		}
		return p1;
	}

	public static Pair getLocationFromPostalCode(String postalCode) {

		Pair p = new Pair();
		String latVal = "";
		String longVal = "";
		boolean isRecFoundFlg = false;

		if (postalCode == null)
			return p;

		postalCode = postalCode.trim().replaceFirst("^0+(?!$)", "").replaceAll("\\s", "");
		if ("".equals(postalCode))
			return p;

		try {

			CSVParser csvParser = getCsvFile();

			for (CSVRecord csvRecord : csvParser) {

				if (postalCode
						.equalsIgnoreCase(csvRecord.get(0).trim().replaceFirst("^0+(?!$)", "").replaceAll("\\s", ""))) {

					latVal = csvRecord.get(8);
					longVal = csvRecord.get(9);
					if (StringUtils.isNotEmpty(latVal) && StringUtils.isNotEmpty(longVal)) {
						p.setLatitude(Double.parseDouble(latVal));
						p.setLongitude(Double.parseDouble(longVal));
						isRecFoundFlg = true;
					}
					break;
				}

			}

			if (!isRecFoundFlg) {
				p = getApiDtl(postalCode);
			}

		} catch (IOException | NumberFormatException e) {
			System.out.println("Error-" + e.getMessage()); // don't do any restricted activities, only log/show
															// messaging

		} finally {
			// System.out.println("final: " + p);
			return p;
		}
	}

	@SuppressWarnings("finally")
	public static List<Pair> getNearestLocations(Pair sourceLocation, List<Pair> listOfTargetLocations) {

		List<PairDistance> tempList = new ArrayList<>();
		List<Pair> finalList = new ArrayList<>();
		try {

			for (Pair p : listOfTargetLocations) {
				PairDistance pd = new PairDistance();
				pd.setLatitude(p.getLatitude());
				pd.setLongitude(p.getLongitude());
				pd.setDistance(distance(sourceLocation.getLatitude(), sourceLocation.getLongitude(), p.getLatitude(),
						p.getLongitude()));
				tempList.add(pd);
			}

			TreeSet<PairDistance> sortedList = tempList.stream().collect(Collectors
					.toCollection(() -> new TreeSet<>(Comparator.comparingDouble(PairDistance::getDistance))));

			for (PairDistance p : sortedList) {
				Pair pr = new Pair();
				pr.setLatitude(p.getLatitude());
				pr.setLongitude(p.getLongitude());
				finalList.add(pr);
			}

		} catch (NumberFormatException e) {
			System.out.println(e.getMessage());
		} finally {
			return finalList;
		}

	}

}
