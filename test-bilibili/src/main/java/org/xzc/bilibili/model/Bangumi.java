package org.xzc.bilibili.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Bangumi {
	private String bid;
	private String name;
	private List<String> aids = new ArrayList<String>();
	private String content;
	private Document document;

	public String getContent() {
		return content;
	}

	public Bangumi(String bid, String content) {
		this.bid = bid;
		this.content = content;
		this.document = Jsoup.parse( content );
		this.name = document.select( ".info-title" ).text();
		Iterator<Element> iter = document.select( "#episode_list > ul > li" ).iterator();
		while (iter.hasNext()) {
			Element t = iter.next();
			String attr = t.select( "a:first-child" ).attr( "href" );
			aids.add( StringUtils.substringBetween( attr, "av", "/" ) );
		}
		Collections.reverse( aids );
	}

	public Document getDocument() {
		return document;
	}

	public String getBid() {
		return bid;
	}

	public String getName() {
		return name;
	}

	public List<String> getAids() {
		return aids;
	}

	@Override
	public String toString() {
		return "Bangumi [bid=" + bid + ", name=" + name + ", aids=" + aids + "]";
	}

}
