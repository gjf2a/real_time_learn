package edu.hendrix.ev3.ai.topological;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.hendrix.ev3.ai.bsoc.BSOCTestee;
import edu.hendrix.ev3.remote.Move;

public class HierarchicalStateMapTest {
	
	HierarchicalStateMap<BSOCTestee> map;

	@Before
	public void setup() {
		map = new HierarchicalStateMap<>(8, BSOCTestee::distance);
		int[] currentState = map.getStartingLabel();
		for (int i = 10; i <= 80; i+=10) {
			currentState = map.addTransition(currentState, Move.FORWARD, new BSOCTestee(i));
		}
		System.out.println(map);
	}
	
	@Test
	public void testString() {
		String target = "{{{{{1{1{10}}}\n{2{1{20}}}\n{3{1{30}}}\n{4{1{40}}}\n{5{1{50}}}\n{6{1{60}}}\n{7{1{70}}}\n{8{1{80}}}\n}\n{{2;1;10}{5;4;10}{3;2;10}{5;3;20}{7;6;10}{4;3;10}{6;5;10}{5;2;30}{4;1;30}{3;1;20}{6;1;50}{6;2;40}{8;6;20}{8;7;10}{6;4;20}{7;1;60}{7;2;50}{7;3;40}{7;4;30}{7;5;20}{4;2;20}{8;1;70}{8;2;60}{8;3;50}{8;4;40}{8;5;30}{6;3;30}{5;1;40}}\n{{1:1;2:2;3:3;4:4;5:5;6:6;7:7;8:8;}{1;2;3;4;5;6;7;8;}}\n{8;9}}\n{{{1{{FORWARD{{1;1;1}{1;2;1}}}}}{2{{FORWARD{{2;3;1}}}}}{3{{FORWARD{{3;4;1}}}}}{4{{FORWARD{{4;5;1}}}}}{5{{FORWARD{{5;6;1}}}}}{6{{FORWARD{{6;7;1}}}}}{7{{FORWARD{{7;8;1}}}}}{8{}}}{{1{1;}}{2{1;}}{3{2;}}{4{3;}}{5{4;}}{6{5;}}{7{6;}}{8{7;}}}}}\n{}}\n{{{{{6{2{15}}}\n{10{2{65}}}\n{11{1{80}}}\n{12{3{40}}}\n}\n{{11;10;15}{12;6;25}{12;10;25}{11;6;65}{10;6;50}{12;11;40}}\n{{1:6;2:6;3:12;4:12;5:12;6:6;7:10;8:12;9:10;10:10;11:11;12:12;}{6:1,2;10:7,9;11;12:3,4,5,8;}}\n{4;13}}\n{{{6{{FORWARD{{6;6;2}{6;12;1}}}}}{10{{FORWARD{{10;10;1}{10;11;1}}}}}{11{}}{12{{FORWARD{{12;10;1}{12;12;2}}}}}}{{6{6;}}{10{10;12;}}{11{10;}}{12{6;12;}}}}}\n{6{1,2,}10{6,7,}11{8,}12{3,4,5,}}}\n{{{{{4{2{15}}}\n{14{6{55}}}\n}\n{{14;4;40}}\n{{1:4;2:4;3:14;4:4;5:14;6:14;7:14;8:14;9:14;10:14;11:14;12:14;13:14;14:14;}{4:1,2;14:3,5,6,7,8,9,10,11,12,13;}}\n{2;15}}\n{{{4{{FORWARD{{4;4;2}{4;14;1}}}}}{14{{FORWARD{{14;14;5}}}}}}{{4{4;}}{14{4;14;}}}}}\n{4{6,}14{10,11,12,}}}\n";
		assertEquals(target, map.toString());
		assertEquals(map, HierarchicalStateMap.fromString(map.toString(), BSOCTestee::new, BSOCTestee::distance));
	}
}
