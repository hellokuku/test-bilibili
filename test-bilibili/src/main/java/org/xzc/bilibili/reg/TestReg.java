package org.xzc.bilibili.reg;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestReg {

	@Test
	public void test1() throws Exception {
		List<String[]> list = new ArrayList<String[]>();
		list.add( new String[] { "19997766", "bc2f0737%2C1482199747%2C17f88d16" } );
		list.add( new String[] { "19997827", "e4357dd0%2C1482199857%2Cae6f4a86" } );
		list.add( new String[] { "19997736", "9f8ee641%2C1482199682%2Cb62922e8" } );
		for (String[] ss : list) {
			RegService rs = new RegService( ss[0], ss[1] );
			boolean ok = rs.isOK();
			while (!ok) {
				System.out.println( ss[0] + " 开始答题" );
				rs.answer1();
				System.out.println( "通过阶段1" );
				if (rs.answer2()) {
					System.out.println( "通过阶段2" );
					break;
				} else {
					System.out.println( "阶段2失败" );
				}
			}
			System.out.println( ss[0] + " ok!" );
		}
	}
}
